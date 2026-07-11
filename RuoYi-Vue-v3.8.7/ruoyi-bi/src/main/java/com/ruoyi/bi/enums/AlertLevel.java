package com.ruoyi.bi.enums;

/**
 * 预警级别枚举（消灭 "info" / "warning" / "critical" 魔法字符串）
 *
 * @author ruoyi-bi
 */
public enum AlertLevel {
    INFO("info", "提示"),
    WARNING("warning", "警告"),
    CRITICAL("critical", "严重");

    private final String code;
    private final String desc;

    AlertLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AlertLevel fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AlertLevel l : values()) {
            if (l.code.equals(code)) {
                return l;
            }
        }
        return null;
    }
}
