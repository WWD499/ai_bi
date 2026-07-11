package com.ruoyi.bi.vo;

import com.alibaba.fastjson2.JSONObject;
import java.util.List;

/**
 * 查询结果 VO
 * <p>原作为 {@code BiQueryService} 的静态内部类，按代码规范 P2-2 抽到独立 vo 包，
 * 与项目其余实体保持一致的手写 getter/setter 风格（项目未引入 Lombok）。
 */
public class QueryResultVo {
    private String sql;
    private List<String> columns;
    private List<JSONObject> data;
    private String chartType;
    private String chartName;
    private JSONObject echartsOption;
    private String interpretation;
    private int rowCount;

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<JSONObject> getData() { return data; }
    public void setData(List<JSONObject> data) { this.data = data; }

    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }

    public String getChartName() { return chartName; }
    public void setChartName(String chartName) { this.chartName = chartName; }

    public JSONObject getEchartsOption() { return echartsOption; }
    public void setEchartsOption(JSONObject echartsOption) { this.echartsOption = echartsOption; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }
}
