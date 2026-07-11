package com.ruoyi.bi.service;

import com.ruoyi.bi.domain.BiAlertRecord;
import com.ruoyi.bi.domain.BiAlertRule;

/**
 * BI预警通知服务
 * 预警触发后负责将异常"通知到人"：站内信（sys_notice）+ 可选的邮件通道
 *
 * @author ruoyi-bi
 */
public interface IBiAlertNotifyService {

    /**
     * 触发预警后的通知处理
     *
     * @param rule   命中的预警规则
     * @param record 已生成的预警记录
     */
    void notify(BiAlertRule rule, BiAlertRecord record);
}
