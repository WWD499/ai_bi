package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiKnowledge;

import java.util.List;

/**
 * RAG知识库服务接口
 *
 * @author ruoyi-bi
 */
public interface IBiKnowledgeService {

    /**
     * 新增知识条目（自动向量化）
     */
    int insertBiKnowledge(BiKnowledge knowledge);

    /**
     * 批量新增知识条目（用于文档切分后批量插入）
     */
    int batchInsertBiKnowledge(List<BiKnowledge> list);

    /**
     * 根据ID查询知识条目
     */
    BiKnowledge selectBiKnowledgeById(Long id);

    /**
     * 查询知识条目列表
     */
    List<BiKnowledge> selectBiKnowledgeList(BiKnowledge knowledge);

    /**
     * 更新知识条目（更新内容时会重新向量化）
     */
    int updateBiKnowledge(BiKnowledge knowledge);

    /**
     * 根据ID删除知识条目
     */
    int deleteBiKnowledgeById(Long id);

    /**
     * 批量删除知识条目
     */
    int deleteBiKnowledgeByIds(Long[] ids);

    /**
     * 向量相似度检索（RAG核心）
     * 将查询文本向量化后，检索最相似的知识条目
     *
     * @param query  查询文本
     * @param topK   返回最相似的K条
     * @param domain 业务领域过滤（可选，传null不过滤）
     * @return 相似度排序的知识条目列表
     */
    List<BiKnowledge> searchSimilar(String query, int topK, String domain);

    /**
     * 构建RAG上下文（用于注入到NL2SQL Prompt）
     * 检索相关知识，拼接成结构化文本
     *
     * @param query  用户自然语言查询
     * @param domain 业务领域（可选）
     * @return RAG上下文文本（直接注入Prompt）
     */
    String buildRagContext(String query, String domain);

    /**
     * 重新向量化指定条目（内容更新后调用）
     */
    void reEmbed(Long id);

    /**
     * 批量重新向量化（模型切换后调用）
     */
    void batchReEmbed();
}
