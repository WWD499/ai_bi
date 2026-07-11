package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiDatasource;
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
}
