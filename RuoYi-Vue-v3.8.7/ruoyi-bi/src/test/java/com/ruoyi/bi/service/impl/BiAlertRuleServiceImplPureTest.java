package com.ruoyi.bi.service.impl;

import com.ruoyi.bi.domain.BiAlertRule;
import com.ruoyi.bi.enums.AlertLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BiAlertRuleServiceImpl 纯函数单测（包级可见方法，无 Spring 容器、无 Mock）
 * 覆盖：compare / determineAlertLevel / shouldCheck
 */
class BiAlertRuleServiceImplPureTest {

    private final BiAlertRuleServiceImpl svc = new BiAlertRuleServiceImpl();

    // ===== compare =====
    @Test
    void compare_gt_lt() {
        assertTrue(svc.compare(10.0, 5.0, ">"));
        assertFalse(svc.compare(10.0, 5.0, "<"));
        assertTrue(svc.compare(3.0, 5.0, "<"));
    }

    @Test
    void compare_ge_le() {
        assertTrue(svc.compare(5.0, 5.0, ">="));
        assertTrue(svc.compare(5.0, 5.0, "<="));
        assertFalse(svc.compare(4.0, 5.0, ">="));
    }

    @Test
    void compare_eq_ne_withTolerance() {
        assertTrue(svc.compare(5.0, 5.0, "="));
        assertTrue(svc.compare(5.0, 5.0, "=="));
        assertFalse(svc.compare(5.0, 5.0, "!="));
        assertTrue(svc.compare(5.1, 5.0, "!="));
        // 容差 0.0001
        assertFalse(svc.compare(5.00005, 5.0, "!="));
        assertTrue(svc.compare(5.0002, 5.0, "!="));
    }

    @Test
    void compare_nullOrUnknownOperator_returnsFalse() {
        assertFalse(svc.compare(null, 5.0, ">"));
        assertFalse(svc.compare(5.0, null, ">"));
        assertFalse(svc.compare(5.0, 5.0, null));
        assertFalse(svc.compare(5.0, 5.0, "???"));
    }

    // ===== determineAlertLevel =====
    @Test
    void determineAlertLevel_nullOrZeroThreshold_isWarning() {
        // P0-3 修复点：threshold 为 null 或 0.0 都不能拆箱 NPE
        assertEquals(AlertLevel.WARNING.getCode(), svc.determineAlertLevel(100.0, null));
        assertEquals(AlertLevel.WARNING.getCode(), svc.determineAlertLevel(100.0, 0.0));
    }

    @Test
    void determineAlertLevel_byDeviation() {
        // 偏差 0.5 -> warning
        assertEquals(AlertLevel.WARNING.getCode(), svc.determineAlertLevel(150.0, 100.0));
        // 偏差 1.0 -> critical
        assertEquals(AlertLevel.CRITICAL.getCode(), svc.determineAlertLevel(200.0, 100.0));
        // 偏差 0.2 -> info
        assertEquals(AlertLevel.INFO.getCode(), svc.determineAlertLevel(120.0, 100.0));
    }

    // ===== shouldCheck =====
    @Test
    void shouldCheck_nullLastCheckTime_alwaysTrue() {
        BiAlertRule r = new BiAlertRule();
        r.setCheckInterval(60);
        assertTrue(svc.shouldCheck(r, LocalDateTime.now()));
    }

    @Test
    void shouldCheck_intervalNotElapsed_false() {
        BiAlertRule r = new BiAlertRule();
        r.setCheckInterval(60);
        r.setLastCheckTime(LocalDateTime.now().minusMinutes(59));
        assertFalse(svc.shouldCheck(r, LocalDateTime.now()));
    }

    @Test
    void shouldCheck_intervalElapsed_true() {
        BiAlertRule r = new BiAlertRule();
        r.setCheckInterval(60);
        r.setLastCheckTime(LocalDateTime.now().minusMinutes(61));
        assertTrue(svc.shouldCheck(r, LocalDateTime.now()));
    }

    @Test
    void shouldCheck_exactlyAtInterval_true() {
        BiAlertRule r = new BiAlertRule();
        r.setCheckInterval(60);
        r.setLastCheckTime(LocalDateTime.now().minusMinutes(60));
        assertTrue(svc.shouldCheck(r, LocalDateTime.now()));
    }

    @Test
    void shouldCheck_nullInterval_exceptionFallsBackToTrue() {
        BiAlertRule r = new BiAlertRule();
        r.setCheckInterval(null);
        r.setLastCheckTime(LocalDateTime.now());
        assertTrue(svc.shouldCheck(r, LocalDateTime.now()));
    }
}
