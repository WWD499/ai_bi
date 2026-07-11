package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiAlertRule;
import java.util.List;

/**
 * 预警规则 Service 接口
 *
 * @author ruoyi-bi
 */
public interface IBiAlertRuleService {

    List<BiAlertRule> selectBiAlertRuleList(BiAlertRule rule);

    BiAlertRule selectBiAlertRuleById(Long id);

    int insertBiAlertRule(BiAlertRule rule);

    int updateBiAlertRule(BiAlertRule rule);

    int deleteBiAlertRuleByIds(Long[] ids);

    /** 扫描所有启用的规则，执行异常检测 */
    int scanAndCheckAlerts();
}
