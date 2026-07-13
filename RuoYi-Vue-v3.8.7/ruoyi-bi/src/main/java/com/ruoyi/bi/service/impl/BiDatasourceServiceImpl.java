package com.ruoyi.bi.service.impl;

import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.mapper.BiDatasourceMapper;
import com.ruoyi.bi.service.IBiDatasourceService;
import com.ruoyi.bi.util.BiDataSourceFactory;
import com.ruoyi.bi.util.JdbcUrlBuilder;
import com.ruoyi.bi.vo.DbColumnVo;
import com.ruoyi.bi.vo.DbTableVo;
import com.ruoyi.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BI数据源配置表 Service实现类
 * 
 * @author ruoyi-bi
 */
@Service
public class BiDatasourceServiceImpl implements IBiDatasourceService {

    private static final Logger log = LoggerFactory.getLogger(BiDatasourceServiceImpl.class);

    @Autowired
    private BiDatasourceMapper datasourceMapper;

    @Autowired
    private BiDataSourceFactory dataSourceFactory;

    /**
     * 查询数据源列表
     */
    @Override
    public List<BiDatasource> selectBiDatasourceList(BiDatasource datasource) {
        return datasourceMapper.selectBiDatasourceList(datasource);
    }

    /**
     * 查询数据源 by ID
     */
    @Override
    public BiDatasource selectBiDatasourceById(Long id) {
        return datasourceMapper.selectBiDatasourceById(id);
    }

    /**
     * 新增数据源
     */
    @Override
    public int insertBiDatasource(BiDatasource datasource) {
        // TODO: 加密密码
        // TODO: 生成JDBC URL
        return datasourceMapper.insertBiDatasource(datasource);
    }

    /**
     * 修改数据源
     */
    @Override
    public int updateBiDatasource(BiDatasource datasource) {
        // TODO: 如果密码有变化，重新加密
        return datasourceMapper.updateBiDatasource(datasource);
    }

    /**
     * 删除数据源 by ID
     */
    @Override
    public int deleteBiDatasourceById(Long id) {
        return datasourceMapper.deleteBiDatasourceById(id);
    }

    @Override
    public int deleteBiDatasourceByIds(Long[] ids) {
        return datasourceMapper.deleteBiDatasourceByIds(ids);
    }

    /**
     * 测试数据源连接
     */
    @Override
    public boolean testConnection(BiDatasource datasource) {
        // TODO: 实现连接测试逻辑
        // 1. 根据type构建JDBC URL
        // 2. 尝试建立连接
        // 3. 返回结果
        log.info("测试数据源连接：{}", datasource.getName());
        return true; // 暂时返回true
    }

    /**
     * 列出数据源当前库所有表（供预警规则表单级联选择）
     * 复用 BiDataSourceFactory 连接池 + JdbcUrlBuilder.catalog（PG catalog 必须为 ""）
     */
    @Override
    public List<DbTableVo> listTables(Long datasourceId) {
        BiDatasource ds = selectBiDatasourceById(datasourceId);
        if (ds == null) {
            return Collections.emptyList();
        }
        String catalog = JdbcUrlBuilder.catalog(ds);
        List<DbTableVo> result = new ArrayList<>();
        try (Connection conn = dataSourceFactory.getDataSource(ds).getConnection();
             ResultSet tables = conn.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                DbTableVo vo = new DbTableVo();
                vo.setTableName(tables.getString("TABLE_NAME"));
                vo.setRemarks(tables.getString("REMARKS"));
                result.add(vo);
            }
        } catch (SQLException e) {
            // P1-4：异常必须带堆栈
            log.error("获取表列表失败", e);
            throw new ServiceException("获取表列表失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 列出指定表所有字段（供预警规则表单级联选择）
     * 表名做白名单校验，防止非法标识符注入元数据库查询
     */
    @Override
    public List<DbColumnVo> listColumns(Long datasourceId, String tableName) {
        // P1-6：表名白名单校验，防止非法标识符注入
        if (tableName == null || !tableName.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            log.warn("非法表名，拒绝获取字段：{}", tableName);
            return Collections.emptyList();
        }
        BiDatasource ds = selectBiDatasourceById(datasourceId);
        if (ds == null) {
            return Collections.emptyList();
        }
        String catalog = JdbcUrlBuilder.catalog(ds);
        List<DbColumnVo> result = new ArrayList<>();
        try (Connection conn = dataSourceFactory.getDataSource(ds).getConnection();
             ResultSet cols = conn.getMetaData().getColumns(catalog, null, tableName, null)) {
            while (cols.next()) {
                DbColumnVo vo = new DbColumnVo();
                vo.setColumnName(cols.getString("COLUMN_NAME"));
                vo.setDataType(cols.getString("TYPE_NAME"));
                vo.setRemarks(cols.getString("REMARKS"));
                result.add(vo);
            }
        } catch (SQLException e) {
            log.error("获取字段列表失败", e);
            throw new ServiceException("获取字段列表失败：" + e.getMessage());
        }
        return result;
    }
}
