package com.ruoyi.bi.task;

import com.ruoyi.bi.service.IBiAlertRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BI数据预警定时检查任务
 * 通过若依 Quartz（sys_job表）调度执行
 * invoke_target: biAlertCheckTask.scanAndCheckAlerts()
 *
 * @author ruoyi-bi
 */
@Component("biAlertCheckTask")
public class BiAlertCheckTask {

    private static final Logger log = LoggerFactory.getLogger(BiAlertCheckTask.class);

    @Autowired
    private IBiAlertRuleService alertRuleService;

    /**
     * 扫描并执行所有启用的预警规则
     * 由 Quartz 定时调用
     */
    public void scanAndCheckAlerts() {
        log.info("===== BI数据预警定时检查开始 =====");
        long startTime = System.currentTimeMillis();

        try {
            int alertCount = alertRuleService.scanAndCheckAlerts();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("===== BI数据预警检查完成，触发 {} 条预警，耗时 {} ms =====", alertCount, elapsed);
        } catch (Exception e) {
            log.error("===== BI数据预警检查异常：{} =====", e.getMessage(), e);
        }
    }

    /**
     * 无参方法（Quartz 兼容）
     */
    public void scanAndCheckAlerts(String params) {
        scanAndCheckAlerts();
    }
}
