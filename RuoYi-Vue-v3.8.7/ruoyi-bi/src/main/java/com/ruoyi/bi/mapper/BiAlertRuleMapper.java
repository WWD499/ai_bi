package com.ruoyi.bi.mapper;

import com.ruoyi.bi.domain.BiAlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警规则 Mapper
 *
 * @author ruoyi-bi
 */
@Mapper
public interface BiAlertRuleMapper {

    List<BiAlertRule> selectBiAlertRuleList(BiAlertRule rule);

    BiAlertRule selectBiAlertRuleById(Long id);

    int insertBiAlertRule(BiAlertRule rule);

    int updateBiAlertRule(BiAlertRule rule);

    int deleteBiAlertRuleById(Long id);

    int deleteBiAlertRuleByIds(Long[] ids);

    /** 查询所有启用的、需要检查的预警规则 */
    List<BiAlertRule> selectEnabledRules();

    /** 更新最近检查时间（P1-3：LocalDateTime） */
    int updateLastCheckTime(@Param("id") Long id, @Param("lastCheckTime") LocalDateTime lastCheckTime);

    /** 更新最近预警时间（P1-3：LocalDateTime） */
    int updateLastAlertTime(@Param("id") Long id, @Param("lastAlertTime") LocalDateTime lastAlertTime);
}
