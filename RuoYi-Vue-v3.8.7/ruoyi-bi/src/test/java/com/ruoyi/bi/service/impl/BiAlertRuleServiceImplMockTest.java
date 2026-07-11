package com.ruoyi.bi.service.impl;

import com.ruoyi.bi.domain.BiAlertRecord;
import com.ruoyi.bi.domain.BiAlertRule;
import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.enums.AlertStatus;
import com.ruoyi.bi.mapper.BiAlertRecordMapper;
import com.ruoyi.bi.mapper.BiAlertRuleMapper;
import com.ruoyi.bi.service.IBiAlertNotifyService;
import com.ruoyi.bi.service.IBiDatasourceService;
import com.ruoyi.bi.service.llm.LlmService;
import com.ruoyi.bi.service.llm.PromptBuilder;
import com.ruoyi.bi.util.BiDataSourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BiAlertRuleServiceImpl 引擎编排单测（Mockito 模拟 DB / 连接池 / LLM）
 * 验证 scanAndCheckAlerts 的核心业务路径：
 *  - 无数据源 → 跳过且只更新检查时间
 *  - 检查间隔未到 → 完全跳过
 *  - 触发但已有 pending → 跳过入库与通知（去重）
 *  - 触发且无 pending → 入库 + 通知 + 更新告警时间
 */
@ExtendWith(MockitoExtension.class)
class BiAlertRuleServiceImplMockTest {

    @Mock private BiAlertRuleMapper alertRuleMapper;
    @Mock private BiAlertRecordMapper alertRecordMapper;
    @Mock private IBiDatasourceService datasourceService;
    @Mock private LlmService llmService;
    @Mock private PromptBuilder promptBuilder;
    @Mock private IBiAlertNotifyService notifyService;
    @Mock private BiDataSourceFactory dataSourceFactory;

    @InjectMocks
    private BiAlertRuleServiceImpl service;

    private BiAlertRule baseRule(Long id, Long dsId, String sql, Double threshold, String op) {
        BiAlertRule r = new BiAlertRule();
        r.setId(id);
        r.setName("rule-" + id);
        r.setStatus(1);
        r.setCheckInterval(60);
        r.setAnalysisEnabled(0); // 关闭 AI 分析分支，聚焦引擎编排
        r.setDatasourceId(dsId);
        r.setLastCheckTime(null); // 强制 shouldCheck=true
        r.setConditionSql(sql);
        r.setThresholdValue(threshold);
        r.setComparisonOperator(op);
        return r;
    }

    private void mockJdbcChain(BiDatasource ds, double value) throws Exception {
        DataSource mockDs = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSourceFactory.getDataSource(ds)).thenReturn(mockDs);
        when(mockDs.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDouble(1)).thenReturn(value);
        when(rs.wasNull()).thenReturn(false);
    }

    @Test
    void scan_noDatasource_skipAndUpdateCheckTime() {
        BiAlertRule r = baseRule(1L, 99L, "SELECT 1", 100.0, ">");
        when(alertRuleMapper.selectEnabledRules()).thenReturn(Collections.singletonList(r));
        when(datasourceService.selectBiDatasourceById(99L)).thenReturn(null);

        int cnt = service.scanAndCheckAlerts();

        assertEquals(0, cnt);
        verify(alertRuleMapper).updateLastCheckTime(eq(1L), any(LocalDateTime.class));
        verify(alertRecordMapper, never()).insertBiAlertRecord(any());
        verify(notifyService, never()).notify(any(), any());
    }

    @Test
    void scan_intervalNotElapsed_completelySkipped() {
        BiAlertRule r = baseRule(1L, 99L, "SELECT 1", 100.0, ">");
        r.setLastCheckTime(LocalDateTime.now()); // 刚检查过
        when(alertRuleMapper.selectEnabledRules()).thenReturn(Collections.singletonList(r));

        int cnt = service.scanAndCheckAlerts();

        assertEquals(0, cnt);
        verify(alertRuleMapper, never()).updateLastCheckTime(anyLong(), any());
    }

    @Test
    void scan_triggeredButPendingExists_skipInsertAndNotify() throws Exception {
        BiAlertRule r = baseRule(2L, 7L, "SELECT val FROM t", 100.0, ">");
        BiDatasource ds = new BiDatasource();
        ds.setType("postgresql");
        ds.setHost("h");
        ds.setPort(5432);
        ds.setDatabaseName("db");
        when(alertRuleMapper.selectEnabledRules()).thenReturn(Collections.singletonList(r));
        when(datasourceService.selectBiDatasourceById(7L)).thenReturn(ds);
        mockJdbcChain(ds, 150.0); // 150 > 100 -> 触发
        when(alertRecordMapper.countPendingByRuleId(2L)).thenReturn(1); // 已有未处理

        int cnt = service.scanAndCheckAlerts();

        assertEquals(0, cnt);
        verify(alertRuleMapper).updateLastCheckTime(eq(2L), any(LocalDateTime.class));
        verify(alertRecordMapper, never()).insertBiAlertRecord(any());
        verify(notifyService, never()).notify(any(), any());
        verify(alertRuleMapper, never()).updateLastAlertTime(anyLong(), any());
    }

    @Test
    void scan_triggeredNoPending_insertAndNotify() throws Exception {
        BiAlertRule r = baseRule(2L, 7L, "SELECT val FROM t", 100.0, ">");
        BiDatasource ds = new BiDatasource();
        ds.setType("postgresql");
        when(alertRuleMapper.selectEnabledRules()).thenReturn(Collections.singletonList(r));
        when(datasourceService.selectBiDatasourceById(7L)).thenReturn(ds);
        mockJdbcChain(ds, 150.0);
        when(alertRecordMapper.countPendingByRuleId(2L)).thenReturn(0);

        int cnt = service.scanAndCheckAlerts();

        assertEquals(1, cnt);
        ArgumentCaptor<BiAlertRecord> cap = ArgumentCaptor.forClass(BiAlertRecord.class);
        verify(alertRecordMapper).insertBiAlertRecord(cap.capture());
        assertEquals(AlertStatus.PENDING.getCode(), cap.getValue().getStatus());
        verify(notifyService).notify(eq(r), any(BiAlertRecord.class));
        verify(alertRuleMapper).updateLastAlertTime(eq(2L), any(LocalDateTime.class));
    }
}
