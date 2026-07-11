package com.ruoyi.bi.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlertStatus 枚举测试：消灭 "pending"/"confirmed"/"resolved" 魔法字符串的回归保护
 */
class AlertStatusTest {

    @Test
    void codes_and_descs() {
        assertEquals("pending", AlertStatus.PENDING.getCode());
        assertEquals("待处理", AlertStatus.PENDING.getDesc());
        assertEquals("confirmed", AlertStatus.CONFIRMED.getCode());
        assertEquals("resolved", AlertStatus.RESOLVED.getCode());
    }

    @Test
    void fromCode_roundTrip() {
        assertSame(AlertStatus.PENDING, AlertStatus.fromCode("pending"));
        assertSame(AlertStatus.CONFIRMED, AlertStatus.fromCode("confirmed"));
        assertSame(AlertStatus.RESOLVED, AlertStatus.fromCode("resolved"));
    }

    @Test
    void fromCode_unknownOrNull_returnsNull() {
        assertNull(AlertStatus.fromCode(null));
        assertNull(AlertStatus.fromCode("nope"));
    }
}
