package com.ruoyi.bi.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bi.domain.BiDashboard;
import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.mapper.BiDashboardMapper;
import com.ruoyi.bi.service.IBiDashboardService;
import com.ruoyi.bi.service.IBiDatasourceService;
import com.ruoyi.bi.util.BiDataSourceFactory;
import com.ruoyi.bi.util.JdbcUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BI大屏 Service 实现
 *
 * @author ruoyi-bi
 */
@Service
public class BiDashboardServiceImpl implements IBiDashboardService {

    private static final Logger log = LoggerFactory.getLogger(BiDashboardServiceImpl.class);

    @Autowired
    private BiDashboardMapper dashboardMapper;

    @Autowired
    private IBiDatasourceService datasourceService;

    @Autowired
    private BiDataSourceFactory dataSourceFactory;

    @Override
    public List<BiDashboard> selectBiDashboardList(BiDashboard dashboard) {
        return dashboardMapper.selectBiDashboardList(dashboard);
    }

    @Override
    public BiDashboard selectBiDashboardById(Long id) {
        return dashboardMapper.selectBiDashboardById(id);
    }

    @Override
    public int insertBiDashboard(BiDashboard dashboard) {
        if (dashboard.getStatus() == null) dashboard.setStatus(1);
        if (dashboard.getIsPublic() == null) dashboard.setIsPublic(0);
        return dashboardMapper.insertBiDashboard(dashboard);
    }

    @Override
    public int updateBiDashboard(BiDashboard dashboard) {
        return dashboardMapper.updateBiDashboard(dashboard);
    }

    @Override
    public int deleteBiDashboardByIds(Long[] ids) {
        return dashboardMapper.deleteBiDashboardByIds(ids);
    }

    /**
     * 根据 Widget 配置查询数据
     * widgetConfig 格式：{ datasourceId, sql, chartType, title }
     */
    @Override
    public JSONObject queryWidgetData(JSONObject widgetConfig) {
        JSONObject result = new JSONObject();

        try {
            Long datasourceId = widgetConfig.getLong("datasourceId");
            String sql = widgetConfig.getString("sql");

            if (datasourceId == null || sql == null || sql.isEmpty()) {
                result.put("error", "缺少 datasourceId 或 sql 参数");
                return result;
            }

            BiDatasource datasource = datasourceService.selectBiDatasourceById(datasourceId);
            if (datasource == null) {
                result.put("error", "数据源不存在");
                return result;
            }

            // 构建 JDBC 连接（走连接池）
            try (Connection conn = dataSourceFactory.getDataSource(datasource).getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.setQueryTimeout(30);
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    // 列名
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        columns.add(meta.getColumnName(i));
                    }
                    result.put("columns", columns);

                    // 数据行
                    List<JSONObject> rows = new ArrayList<>();
                    while (rs.next()) {
                        JSONObject row = new JSONObject();
                        for (int i = 1; i <= colCount; i++) {
                            row.put(meta.getColumnName(i), rs.getString(i));
                        }
                        rows.add(row);
                    }
                    result.put("rows", rows);
                    result.put("total", rows.size());
                }
            }

        } catch (Exception e) {
            log.error("大屏Widget数据查询失败", e);
            result.put("error", "查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取数据源的所有表结构
     */
    @Override
    public List<Map<String, Object>> getTableSchemas(Long datasourceId) {
        List<Map<String, Object>> tables = new ArrayList<>();

        try {
            BiDatasource datasource = datasourceService.selectBiDatasourceById(datasourceId);
            if (datasource == null) return tables;

            try (Connection conn = dataSourceFactory.getDataSource(datasource).getConnection()) {
                // P0-2 修复：PostgreSQL 的 catalog 必须为 ""，否则 getTables 返回空
                String catalog = JdbcUrlBuilder.catalog(datasource);
                try (ResultSet tableRs = conn.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"})) {
                    while (tableRs.next()) {
                        Map<String, Object> table = new HashMap<>();
                        table.put("tableName", tableRs.getString("TABLE_NAME"));
                        table.put("remark", tableRs.getString("REMARKS"));

                        List<Map<String, String>> columns = new ArrayList<>();
                        try (ResultSet colRs = conn.getMetaData().getColumns(catalog, null,
                                tableRs.getString("TABLE_NAME"), null)) {
                            while (colRs.next()) {
                                Map<String, String> col = new HashMap<>();
                                col.put("name", colRs.getString("COLUMN_NAME"));
                                col.put("type", colRs.getString("TYPE_NAME"));
                                col.put("remark", colRs.getString("REMARKS"));
                                columns.add(col);
                            }
                        }
                        table.put("columns", columns);
                        tables.add(table);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取表结构失败", e);
        }

        return tables;
    }
}
