package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.vo.DbTableVo;
import com.ruoyi.bi.vo.DbColumnVo;
import java.util.List;

/**
 * BI数据源配置表 Service接口
 * 
 * @author ruoyi-bi
 */
public interface IBiDatasourceService {

    /**
     * 查询数据源列表
     * 
     * @param datasource 数据源条件
     * @return 数据源列表
     */
    List<BiDatasource> selectBiDatasourceList(BiDatasource datasource);

    /**
     * 查询数据源 by ID
     * 
     * @param id 数据源ID
     * @return 数据源
     */
    BiDatasource selectBiDatasourceById(Long id);

    /**
     * 新增数据源
     * 
     * @param datasource 数据源
     * @return 结果
     */
    int insertBiDatasource(BiDatasource datasource);

    /**
     * 修改数据源
     * 
     * @param datasource 数据源
     * @return 结果
     */
    int updateBiDatasource(BiDatasource datasource);

    /**
     * 删除数据源 by ID
     * 
     * @param id 数据源ID
     * @return 结果
     */
    int deleteBiDatasourceById(Long id);

    /**
     * 批量删除数据源
     *
     * @param ids 数据源ID数组
     * @return 结果
     */
    int deleteBiDatasourceByIds(Long[] ids);

    /**
     * 测试数据源连接
     * 
     * @param datasource 数据源配置
     * @return 是否连接成功
     */
    boolean testConnection(BiDatasource datasource);

    /**
     * 列出数据源当前库中的所有表（供预警规则表单级联选择）
     *
     * @param datasourceId 数据源ID
     * @return 表信息列表
     */
    List<DbTableVo> listTables(Long datasourceId);

    /**
     * 列出指定表的所有字段（供预警规则表单级联选择）
     *
     * @param datasourceId 数据源ID
     * @param tableName     表名（做白名单校验，非法标识符直接返回空）
     * @return 字段信息列表
     */
    List<DbColumnVo> listColumns(Long datasourceId, String tableName);
}
