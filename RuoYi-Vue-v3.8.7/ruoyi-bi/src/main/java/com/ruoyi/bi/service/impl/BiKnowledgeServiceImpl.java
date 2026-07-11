package com.ruoyi.bi.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.bi.domain.BiKnowledge;
import com.ruoyi.bi.mapper.BiKnowledgeMapper;
import com.ruoyi.bi.service.IBiKnowledgeService;
import com.ruoyi.bi.service.llm.LlmService;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RAG知识库服务实现
 *
 * 核心功能：
 * 1. 文本切分（按段落，每段≤500字，重叠50字）
 * 2. 向量化（调用LlmService.embed()，BGE-M3模型）
 * 3. 向量存储（pgvector）
 * 4. 相似度检索（RAG上下文注入）
 *
 * @author ruoyi-bi
 */
@Service
public class BiKnowledgeServiceImpl implements IBiKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(BiKnowledgeServiceImpl.class);

    /**
     * 文本切分配置
     */
    private static final int CHUNK_SIZE = 500;      // 每段最大字数
    private static final int CHUNK_OVERLAP = 50;   // 重叠字数
    private static final int EMBED_BATCH_SIZE = 16; // 每次向量化批量大小

    @Autowired
    private BiKnowledgeMapper knowledgeMapper;

    @Autowired(required = false)
    private LlmService llmService;

    /**
     * 是否启用向量化（可在 application.yml 中配置，默认 true）
     * 设为 false 时，跳过向量化，仅存储文本内容
     */
    @Value("${ai.ark.embedding-enabled:true}")
    private boolean embeddingEnabled;

    // =====================================================
    // 新增 / 更新
    // =====================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBiKnowledge(BiKnowledge knowledge) {
        // 文本切分
        List<String> chunks = splitText(knowledge.getContent());

        if (chunks.isEmpty()) {
            throw new ServiceException("知识内容不能为空");
        }

        // 批量向量化（如果启用且可用）
        List<float[]> embeddings = null;
        if (embeddingEnabled && llmService != null) {
            try {
                embeddings = embedTexts(chunks);
            } catch (Exception e) {
                log.warn("向量化失败，将跳过向量存储：{}", e.getMessage());
            }
        }

        // 批量插入
        List<BiKnowledge> entities = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            BiKnowledge entity = new BiKnowledge();
            entity.setTitle(knowledge.getTitle());
            entity.setContent(chunks.get(i));
            if (embeddings != null && i < embeddings.size()) {
                entity.setContentVector(floatArrayToPgVector(embeddings.get(i)));
            }
            entity.setSourceType(knowledge.getSourceType());
            entity.setSourceUrl(knowledge.getSourceUrl());
            entity.setBusinessDomain(knowledge.getBusinessDomain());
            entity.setTags(knowledge.getTags());
            entity.setChunkIndex(i);
            entity.setTotalChunks(chunks.size());
            entity.setStatus(knowledge.getStatus() != null ? knowledge.getStatus() : 1);
            entity.setCreateBy(knowledge.getCreateBy());
            entity.setRemark(knowledge.getRemark());
            entities.add(entity);
        }

        int rows = knowledgeMapper.batchInsertBiKnowledge(entities);
        log.info("插入知识条目：{}，切片数：{}，向量化：{}", knowledge.getTitle(), chunks.size(), embeddings != null);
        return rows;
    }

    @Override
    public int batchInsertBiKnowledge(List<BiKnowledge> list) {
        // 批量向量化
        List<String> allTexts = new ArrayList<>();
        for (BiKnowledge k : list) {
            allTexts.add(k.getContent());
        }
        List<float[]> embeddings = embedTexts(allTexts);

        // 设置向量
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setContentVector(floatArrayToPgVector(embeddings.get(i)));
        }

        return knowledgeMapper.batchInsertBiKnowledge(list);
    }

    @Override
    public BiKnowledge selectBiKnowledgeById(Long id) {
        return knowledgeMapper.selectBiKnowledgeById(id);
    }

    @Override
    public List<BiKnowledge> selectBiKnowledgeList(BiKnowledge knowledge) {
        return knowledgeMapper.selectBiKnowledgeList(knowledge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBiKnowledge(BiKnowledge knowledge) {
        // 如果内容有更新，重新向量化（如果启用）
        if (knowledge.getContent() != null && !knowledge.getContent().trim().isEmpty()
                && embeddingEnabled && llmService != null) {
            try {
                List<float[]> embeddings = embedTexts(Collections.singletonList(knowledge.getContent()));
                knowledge.setContentVector(floatArrayToPgVector(embeddings.get(0)));
            } catch (Exception e) {
                log.warn("向量化失败，将保持原向量：{}", e.getMessage());
            }
        }
        return knowledgeMapper.updateBiKnowledge(knowledge);
    }

    @Override
    public int deleteBiKnowledgeById(Long id) {
        return knowledgeMapper.deleteBiKnowledgeById(id);
    }

    @Override
    public int deleteBiKnowledgeByIds(Long[] ids) {
        return knowledgeMapper.deleteBiKnowledgeByIds(ids);
    }

    // =====================================================
    // RAG 检索
    // =====================================================

    @Override
    public List<BiKnowledge> searchSimilar(String query, int topK, String domain) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 如果向量化未启用或不可用，返回空列表（调用方会跳过 RAG 上下文）
        if (!embeddingEnabled || llmService == null) {
            log.debug("向量化未启用，跳过相似度检索");
            return new ArrayList<>();
        }

        try {
            // 1. 将查询文本向量化
            List<float[]> queryEmbedding = embedTexts(Collections.singletonList(query.trim()));
            String vectorStr = floatArrayToPgVector(queryEmbedding.get(0));

            // 2. 向量相似度检索（pgvector <=> 余弦距离）
            return knowledgeMapper.searchByVector(vectorStr, topK, domain);
        } catch (Exception e) {
            log.warn("向量相似度检索失败：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String buildRagContext(String query, String domain) {
        // 检索最相似的5条知识
        List<BiKnowledge> relevant = searchSimilar(query, 5, domain);

        if (relevant == null || relevant.isEmpty()) {
            return "";
        }

        // 拼接成结构化上下文
        StringBuilder context = new StringBuilder();
        context.append("【业务知识库参考】\n");
        for (int i = 0; i < relevant.size(); i++) {
            BiKnowledge k = relevant.get(i);
            context.append("\n").append(i + 1).append(". ");
            context.append(k.getTitle() != null ? "【" + k.getTitle() + "】" : "");
            context.append(k.getContent());
        }
        context.append("\n");

        return context.toString();
    }

    // =====================================================
    // 向量化重新生成
    // =====================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reEmbed(Long id) {
        if (!embeddingEnabled || llmService == null) {
            throw new ServiceException("向量化未启用，无法重新向量化");
        }

        BiKnowledge knowledge = knowledgeMapper.selectBiKnowledgeById(id);
        if (knowledge == null) {
            throw new ServiceException("知识条目不存在");
        }

        List<float[]> embeddings = embedTexts(Collections.singletonList(knowledge.getContent()));
        knowledge.setContentVector(floatArrayToPgVector(embeddings.get(0)));
        knowledgeMapper.updateBiKnowledge(knowledge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReEmbed() {
        if (!embeddingEnabled || llmService == null) {
            throw new ServiceException("向量化未启用，无法重新向量化");
        }

        List<BiKnowledge> all = knowledgeMapper.selectBiKnowledgeList(new BiKnowledge());
        if (all.isEmpty()) {
            return;
        }

        // 分批向量化（避免单次请求过大）
        for (int i = 0; i < all.size(); i += EMBED_BATCH_SIZE) {
            int end = Math.min(i + EMBED_BATCH_SIZE, all.size());
            List<BiKnowledge> batch = all.subList(i, end);

            List<String> texts = new ArrayList<>();
            for (BiKnowledge k : batch) {
                texts.add(k.getContent());
            }

            List<float[]> embeddings = embedTexts(texts);

            for (int j = 0; j < batch.size(); j++) {
                batch.get(j).setContentVector(floatArrayToPgVector(embeddings.get(j)));
                knowledgeMapper.updateBiKnowledge(batch.get(j));
            }

            log.info("批量重新向量化进度：{}/{}", end, all.size());
        }
    }

    // =====================================================
    // 私有方法：文本切分 & 向量化
    // =====================================================

    /**
     * 文本切分（按段落，重叠切分）
     * 策略：
     * 1. 先按 \n\n 分段
     * 2. 每段超过 CHUNK_SIZE 时，按句子切分
     * 3. 最终每段不超过 CHUNK_SIZE，相邻段重叠 CHUNK_OVERLAP 字
     */
    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        text = text.trim();

        // 1. 按段落切分
        String[] paragraphs = text.split("\\n\\s*\\n");

        for (String para : paragraphs) {
            para = para.trim();
            if (para.isEmpty()) continue;

            if (para.length() <= CHUNK_SIZE) {
                chunks.add(para);
            } else {
                // 2. 长段落按句子切分
                List<String> sentences = splitBySentence(para);
                StringBuilder current = new StringBuilder();

                for (String sent : sentences) {
                    if (current.length() + sent.length() > CHUNK_SIZE) {
                        if (current.length() > 0) {
                            chunks.add(current.toString().trim());
                        }
                        // 重叠：保留最后 CHUNK_OVERLAP 字
                        String overlap = current.length() > CHUNK_OVERLAP
                                ? current.substring(current.length() - CHUNK_OVERLAP)
                                : current.toString();
                        current = new StringBuilder(overlap).append(sent);
                    } else {
                        current.append(sent);
                    }
                }

                if (current.length() > 0) {
                    chunks.add(current.toString().trim());
                }
            }
        }

        return chunks;
    }

    /**
     * 按句子切分（。！？\n）
     */
    private List<String> splitBySentence(String text) {
        List<String> sentences = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            current.append(c);

            if (c == '。' || c == '！' || c == '？' || c == '\n') {
                sentences.add(current.toString());
                current = new StringBuilder();
            }
        }

        if (current.length() > 0) {
            sentences.add(current.toString());
        }

        return sentences;
    }

    /**
     * 批量文本向量化
     * 调用 LlmService.embed()，自动处理分批
     */
    private List<float[]> embedTexts(List<String> texts) {
        if (llmService == null) {
            throw new ServiceException("AI服务未启用，无法向量化");
        }

        List<float[]> result = new ArrayList<>();

        // 分批调用（避免单次请求过大）
        for (int i = 0; i < texts.size(); i += EMBED_BATCH_SIZE) {
            int end = Math.min(i + EMBED_BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);

            try {
                List<float[]> batchResult = llmService.embed(batch);
                result.addAll(batchResult);
            } catch (Exception e) {
                log.error("向量化失败，批次：{}-{}", i, end, e);
                throw new ServiceException("向量化失败：" + e.getMessage());
            }
        }

        return result;
    }

    /**
     * float[] 转 pgvector 字符串格式
     * 输入：[0.1f, 0.2f, ..., 0.3f]
     * 输出："[0.1,0.2,...,0.3]"
     */
    private String floatArrayToPgVector(float[] vec) {
        if (vec == null || vec.length == 0) {
            return "[0.0]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vec[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
