package com.ruoyi.web.controller.bi;

import com.ruoyi.bi.domain.BiKnowledge;
import com.ruoyi.bi.service.IBiKnowledgeService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * RAG知识库 Controller
 *
 * @author ruoyi-bi
 */
@Api("BI-RAG知识库")
@RestController
@RequestMapping("/bi/knowledge")
public class BiKnowledgeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BiKnowledgeController.class);

    @Autowired
    private IBiKnowledgeService knowledgeService;

    /**
     * 新增知识条目
     * 自动文本切分 + 向量化
     */
    @ApiOperation("新增知识条目（自动切分+向量化）")
    @PostMapping
    public AjaxResult add(@RequestBody BiKnowledge knowledge) {
        int rows = knowledgeService.insertBiKnowledge(knowledge);
        return toAjax(rows);
    }

    /**
     * 更新知识条目
     * 内容更新时会自动重新向量化
     */
    @ApiOperation("更新知识条目（自动重新向量化）")
    @PutMapping
    public AjaxResult edit(@RequestBody BiKnowledge knowledge) {
        int rows = knowledgeService.updateBiKnowledge(knowledge);
        return toAjax(rows);
    }

    /**
     * 删除知识条目
     */
    @ApiOperation("删除知识条目")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        int rows = knowledgeService.deleteBiKnowledgeByIds(ids);
        return toAjax(rows);
    }

    /**
     * 查询知识条目详情
     */
    @ApiOperation("获取知识条目详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(knowledgeService.selectBiKnowledgeById(id));
    }

    /**
     * 查询知识条目列表
     */
    @ApiOperation("查询知识条目列表")
    @GetMapping("/list")
    public TableDataInfo list(BiKnowledge knowledge) {
        startPage();
        List<BiKnowledge> list = knowledgeService.selectBiKnowledgeList(knowledge);
        return getDataTable(list);
    }

    /**
     * 向量相似度检索（RAG核心接口）
     * 用于：根据自然语言查询，检索最相关的知识条目
     *
     * 请求体：{
     *   "query": "如何统计月度销售额",
     *   "topK": 5,
     *   "domain": "销售"
     * }
     */
    @ApiOperation("向量相似度检索（RAG核心接口）")
    @PostMapping("/search")
    public AjaxResult searchSimilar(@RequestBody SearchRequest request) {
        List<BiKnowledge> results = knowledgeService.searchSimilar(
                request.getQuery(),
                request.getTopK() != null ? request.getTopK() : 5,
                request.getDomain()
        );
        return success(results);
    }

    /**
     * 构建RAG上下文（内部接口，供BiQueryService调用）
     * 返回可直接注入NL2SQL Prompt的结构化文本
     */
    @ApiOperation("构建RAG上下文（内部接口）")
    @PostMapping("/rag-context")
    public AjaxResult buildRagContext(@RequestBody RagContextRequest request) {
        String context = knowledgeService.buildRagContext(
                request.getQuery(),
                request.getDomain()
        );
        return success(context);
    }

    /**
     * 重新向量化指定条目
     */
    @ApiOperation("重新向量化指定条目")
    @PostMapping("/reembed/{id}")
    public AjaxResult reEmbed(@PathVariable Long id) {
        knowledgeService.reEmbed(id);
        return success();
    }

    /**
     * 批量重新向量化（模型切换后调用）
     */
    @ApiOperation("批量重新向量化")
    @PostMapping("/reembed/batch")
    public AjaxResult batchReEmbed() {
        knowledgeService.batchReEmbed();
        return success();
    }

    // =====================================================
    // 内部类：请求体
    // =====================================================

    /**
     * 搜索请求体
     */
    public static class SearchRequest {
        private String query;
        private Integer topK;
        private String domain;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }

        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }

    /**
     * RAG上下文请求体
     */
    public static class RagContextRequest {
        private String query;
        private String domain;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }
}
