package com.ruoyi.bi.service.sql;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ChartSelector 智能选图单元测试
 * 重点锁定：用户表达「占比」意图时，时间维度（每月/每天）的
 * 占比查询必须返回饼图，而非被 rowCount 上限误杀后降级成折线图。
 */
public class ChartSelectorTest {

    private final ChartSelector selector = new ChartSelector();

    /** 构造 时间列 + 数值列 的模拟数据（month/day 维度） */
    private List<JSONObject> buildTimeSeries(int rows) {
        List<JSONObject> data = new ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            JSONObject row = new JSONObject();
            row.put("dim", String.format("2026-%02d", i));
            row.put("sales", String.valueOf(i * 100));
            data.add(row);
        }
        return data;
    }

    private List<String> cols() {
        List<String> columns = new ArrayList<>();
        columns.add("dim");
        columns.add("sales");
        return columns;
    }

    @Test
    void proportionIntent_monthly_returnsPie() {
        // 「每月销售额占比」典型返回 12 行，原 rowCount<=10 会误杀 → 折线图（BUG）
        ChartSelector.ChartType type = selector.selectChart(
                cols(), buildTimeSeries(12), "查看每月销售额占比");
        assertEquals(ChartSelector.ChartType.PIE, type, "每月销售额占比(12行) 应命中饼图");
    }

    @Test
    void proportionIntent_daily_returnsPie() {
        // 「每天销售额占比」返回 31 行，验证 31 上限覆盖日维度
        ChartSelector.ChartType type = selector.selectChart(
                cols(), buildTimeSeries(31), "查看每天销售额占比");
        assertEquals(ChartSelector.ChartType.PIE, type, "每天销售额占比(31行) 应命中饼图");
    }

    @Test
    void trendIntent_withoutProportion_returnsLine() {
        // 回归保护：无占比词、仅趋势意图时，仍应返回折线图
        ChartSelector.ChartType type = selector.selectChart(
                cols(), buildTimeSeries(12), "查看近12个月销售额趋势");
        assertEquals(ChartSelector.ChartType.LINE, type, "无占比词、仅趋势时应折线图");
    }
}
