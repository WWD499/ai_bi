package com.ruoyi.web.controller.bi;

import com.ruoyi.bi.service.BiQueryService;
import com.ruoyi.bi.service.llm.LlmService;
import com.ruoyi.bi.vo.QueryResultVo;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * BI自然语言查询 Controller
 *
 * @author ruoyi-bi
 */
@Api("BI-自然语言查询")
@RestController
@RequestMapping("/bi/query")
public class BiQueryController extends BaseController {

    @Autowired
    private BiQueryService biQueryService;

    @Autowired(required = false)
    private LlmService llmService;

    /**
     * 自然语言查询
     *
     * POST /bi/query
     *
     * 请求体：
     * {
     *   "query": "查询每个部门的员工数量",
     *   "datasourceId": 1,
     *   "tableName": "employees"   // 可选
     * }
     */
    @PreAuthorize("@ss.hasPermi('bi:query:list')")
    @ApiOperation("自然语言查询（NL2SQL）")
    @RateLimiter(key = "bi-query", time = 60, count = 10, limitType = LimitType.DEFAULT)
    @PostMapping
    public AjaxResult query(@RequestBody QueryRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return error("查询内容不能为空");
        }
        if (request.getDatasourceId() == null) {
            return error("数据源ID不能为空");
        }

        QueryResultVo result = biQueryService.naturalLanguageQuery(
                request.getQuery(),
                request.getDatasourceId(),
                request.getTableName()
        );

        return success(result);
    }

    /**
     * 测试LLM连接
     */
    @PreAuthorize("@ss.hasPermi('bi:query:list')")
    @ApiOperation("测试LLM连接")
    @RateLimiter(key = "bi-test-llm", time = 60, count = 5, limitType = LimitType.DEFAULT)
    @GetMapping("/test-llm")
    public AjaxResult testLlm() {
        if (llmService == null) {
            return error("LLM服务未启用，请检查 ai.ark.api-key 配置");
        }
        boolean ok = llmService.testConnection();
        return ok ? success("LLM连接正常") : error("LLM连接失败");
    }

    /**
     * 请求参数
     */
    public static class QueryRequest {
        private String query;
        private Long datasourceId;
        private String tableName;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }

        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
    }
}
