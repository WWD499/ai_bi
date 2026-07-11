package com.ruoyi.bi.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.time.LocalDateTime;

/**
 * BI预警规则实体
 * 映射 bi_alert_config 表
 *
 * @author ruoyi-bi
 */
public class BiAlertRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "预警名称")
    private String name;

    @Excel(name = "数据源ID")
    private Long datasourceId;

    @Excel(name = "监控表名")
    private String tableName;

    @Excel(name = "监控字段")
    private String metricField;

    /** 检查SQL：用于查出当前值的SQL语句 */
    private String conditionSql;

    /** 阈值 */
    private Double thresholdValue;

    /** 比较运算符：>、<、>=、<=、=、!= */
    private String comparisonOperator;

    /** 检查间隔（分钟） */
    private Integer checkInterval;

    /** 通知方式：email、sms、wechat */
    private String notifyType;

    /** 通知目标（邮箱、手机号等） */
    private String notifyTarget;

    /** 状态：0-停用，1-启用 */
    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    private Integer status;

    /** 是否启用AI分析 */
    private Integer analysisEnabled;

    /** 上次检查时间（P1-3：改用 LocalDateTime，禁止字符串存时间） */
    private LocalDateTime lastCheckTime;

    /** 上次预警时间 */
    private LocalDateTime lastAlertTime;

    // ==================== Getter/Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDatasourceId() { return datasourceId; }
    public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getMetricField() { return metricField; }
    public void setMetricField(String metricField) { this.metricField = metricField; }

    public String getConditionSql() { return conditionSql; }
    public void setConditionSql(String conditionSql) { this.conditionSql = conditionSql; }

    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }

    public String getComparisonOperator() { return comparisonOperator; }
    public void setComparisonOperator(String comparisonOperator) { this.comparisonOperator = comparisonOperator; }

    public Integer getCheckInterval() { return checkInterval; }
    public void setCheckInterval(Integer checkInterval) { this.checkInterval = checkInterval; }

    public String getNotifyType() { return notifyType; }
    public void setNotifyType(String notifyType) { this.notifyType = notifyType; }

    public String getNotifyTarget() { return notifyTarget; }
    public void setNotifyTarget(String notifyTarget) { this.notifyTarget = notifyTarget; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getAnalysisEnabled() { return analysisEnabled; }
    public void setAnalysisEnabled(Integer analysisEnabled) { this.analysisEnabled = analysisEnabled; }

    public LocalDateTime getLastCheckTime() { return lastCheckTime; }
    public void setLastCheckTime(LocalDateTime lastCheckTime) { this.lastCheckTime = lastCheckTime; }

    public LocalDateTime getLastAlertTime() { return lastAlertTime; }
    public void setLastAlertTime(LocalDateTime lastAlertTime) { this.lastAlertTime = lastAlertTime; }
}
