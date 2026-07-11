package com.ruoyi.bi.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 查询历史表 bi_query_history
 * 
 * @author ruoyi-bi
 */
public class BiQueryHistory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 查询ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 数据源ID */
    private Long datasourceId;

    /** 自然语言查询 */
    private String naturalQuery;

    /** 生成的SQL */
    private String generatedSql;

    /** SQL是否合法：0-否，1-是 */
    private Integer sqlValid;

    /** 执行耗时（毫秒） */
    private Integer executionTime;

    /** 结果行数 */
    private Integer resultCount;

    /** 图表类型：bar、line、pie、scatter等 */
    private String chartType;

    /** 图表配置（JSON格式） */
    private String chartConfig;

    /** 状态：0-失败，1-成功 */
    private Integer status;

    /** 错误信息 */
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public String getNaturalQuery() {
        return naturalQuery;
    }

    public void setNaturalQuery(String naturalQuery) {
        this.naturalQuery = naturalQuery;
    }

    public String getGeneratedSql() {
        return generatedSql;
    }

    public void setGeneratedSql(String generatedSql) {
        this.generatedSql = generatedSql;
    }

    public Integer getSqlValid() {
        return sqlValid;
    }

    public void setSqlValid(Integer sqlValid) {
        this.sqlValid = sqlValid;
    }

    public Integer getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Integer executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getChartConfig() {
        return chartConfig;
    }

    public void setChartConfig(String chartConfig) {
        this.chartConfig = chartConfig;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("naturalQuery", getNaturalQuery())
                .append("status", getStatus())
                .toString();
    }
}
