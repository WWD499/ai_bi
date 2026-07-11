package com.ruoyi.bi.mapper;

import com.ruoyi.bi.domain.BiDashboard;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * BI大屏 Mapper
 *
 * @author ruoyi-bi
 */
@Mapper
public interface BiDashboardMapper {

    List<BiDashboard> selectBiDashboardList(BiDashboard dashboard);

    BiDashboard selectBiDashboardById(Long id);

    int insertBiDashboard(BiDashboard dashboard);

    int updateBiDashboard(BiDashboard dashboard);

    int deleteBiDashboardById(Long id);

    int deleteBiDashboardByIds(Long[] ids);
}
