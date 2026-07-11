package com.ruoyi.bi.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlertLevel 枚举测试：消灭 "info"/"warning"/"critical" 魔法字符串的回归保护
 */
class AlertLevelTest {

    @Test
    void codes_and_descs() {
        assertEquals("info", AlertLevel.INFO.getCode());
        assertEquals("warning", AlertLevel.WARNING.getCode());
        assertEquals("critical", AlertLevel.CRITICAL.getCode());
    }

    @Test
    void fromCode_roundTrip() {
        assertSame(AlertLevel.INFO, AlertLevel.fromCode("info"));
        assertSame(AlertLevel.WARNING, AlertLevel.fromCode("warning"));
        assertSame(AlertLevel.CRITICAL, AlertLevel.fromCode("critical"));
    }

    @Test
    void fromCode_unknownOrNull_returnsNull() {
        assertNull(AlertLevel.fromCode(null));
        assertNull(AlertLevel.fromCode("boom"));
    }
}
