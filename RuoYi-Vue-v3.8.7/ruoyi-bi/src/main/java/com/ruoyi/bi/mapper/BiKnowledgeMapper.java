package com.ruoyi.bi.mapper;

import com.ruoyi.bi.domain.BiKnowledge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG知识库 Mapper 接口
 *
 * @author ruoyi-bi
 */
public interface BiKnowledgeMapper {

    /**
     * 新增知识条目
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
     * 查询知识条目列表（带条件）
     */
    List<BiKnowledge> selectBiKnowledgeList(BiKnowledge knowledge);

    /**
     * 更新知识条目
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
     * 向量相似度检索（pgvector）
     * 使用余弦距离 <=> 运算符，距离越小越相似
     *
     * @param vectorStr 查询向量，格式：'[0.1,0.2,...,0.3]'
     * @param topK      返回最相似的K条
     * @param domain    业务领域过滤（可选，传null不过滤）
     * @return 相似度排序的知识条目列表
     */
    List<BiKnowledge> searchByVector(@Param("vectorStr") String vectorStr,
                                     @Param("topK") int topK,
                                     @Param("domain") String domain);

    /**
     * 根据业务领域查询启用的知识条目（用于RAG上下文注入）
     */
    List<BiKnowledge> selectByDomain(@Param("domain") String domain);
}
