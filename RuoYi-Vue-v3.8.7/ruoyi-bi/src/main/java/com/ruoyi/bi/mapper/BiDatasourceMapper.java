package com.ruoyi.bi.mapper;

import com.ruoyi.bi.domain.BiDatasource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BI数据源配置表 Mapper
 *
 * @author ruoyi-bi
 */
@Mapper
public interface BiDatasourceMapper {

    List<BiDatasource> selectBiDatasourceList(BiDatasource datasource);

    BiDatasource selectBiDatasourceById(Long id);

    int insertBiDatasource(BiDatasource datasource);

    int updateBiDatasource(BiDatasource datasource);

    int deleteBiDatasourceById(Long id);

    /**
     * 批量删除数据源
     *
     * @param ids 数据源ID数组
     * @return 结果
     */
    int deleteBiDatasourceByIds(Long[] ids);

    /**
     * 根据状态查询数据源
     *
     * @param status 状态：0-停用，1-启用
     * @return 数据源列表
     */
    List<BiDatasource> selectByStatus(Integer status);

    /**
     * 根据类型查询数据源
     *
     * @param type 类型：mysql、postgresql等
     * @return 数据源列表
     */
    List<BiDatasource> selectByType(String type);
}
