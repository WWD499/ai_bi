package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiDashboard;
import com.alibaba.fastjson2.JSONObject;
import java.util.List;
import java.util.Map;

/**
 * BI大屏 Service 接口
 *
 * @author ruoyi-bi
 */
public interface IBiDashboardService {

    List<BiDashboard> selectBiDashboardList(BiDashboard dashboard);

    BiDashboard selectBiDashboardById(Long id);

    int insertBiDashboard(BiDashboard dashboard);

    int updateBiDashboard(BiDashboard dashboard);

    int deleteBiDashboardByIds(Long[] ids);

    /**
     * 根据 Widget 配置查询数据
     * @param widgetConfig Widget配置JSON（含 datasourceId、sql、chartType等）
     * @return 查询结果：{ columns: [], rows: [], chartOption: {} }
     */
    JSONObject queryWidgetData(JSONObject widgetConfig);

    /**
     * 获取数据源的所有表结构（供大屏配置时选择数据源和表）
     * @param datasourceId 数据源ID
     * @return 表结构信息
     */
    List<Map<String, Object>> getTableSchemas(Long datasourceId);
}
