package com.ruoyi.bi.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 关键词兜底检索 —— 中文停用词提取逻辑单测
 * 验证：多字停用词优先匹配、业务实体词保留、纯停用词返回空
 */
public class BiKnowledgeServiceImplKeywordTest {

    private final BiKnowledgeServiceImpl service = new BiKnowledgeServiceImpl();

    @Test
    void extractKeywords_removesStopwords_keepsEntity() {
        // 中文长串按 2-gram 切分：销售额 → 销售 / 售额
        List<String> terms = service.extractKeywords("上个月的销售额是多少");
        assertTrue(terms.contains("销售"), "2-gram 应保留业务实体 销售，实际：" + terms);
        assertFalse(terms.contains("上个月"), "多字停用词 上个月 应被移除");
        assertFalse(terms.contains("月"), "单字噪声 月 不应残留");
        assertFalse(terms.contains("多少"), "疑问词 多少 应被移除");
    }

    @Test
    void extractKeywords_keepsMultipleEntities() {
        // 产品的销量库存 → 2-gram 含 产品 / 销量 / 库存
        List<String> terms = service.extractKeywords("帮我统计各产品的销量和库存");
        assertTrue(terms.contains("产品"), "产品 应保留，实际：" + terms);
        assertTrue(terms.contains("销量"), "销量 应保留");
        assertTrue(terms.contains("库存"), "库存 应保留");
        assertFalse(terms.contains("帮我"), "帮我 应被移除");
    }

    @Test
    void extractKeywords_emptyWhenAllStopwords() {
        assertTrue(service.extractKeywords("是多少").isEmpty(), "纯停用词应返回空");
        assertTrue(service.extractKeywords("   ").isEmpty(), "空白应返回空");
        assertTrue(service.extractKeywords(null).isEmpty(), "null 应返回空");
    }
}
