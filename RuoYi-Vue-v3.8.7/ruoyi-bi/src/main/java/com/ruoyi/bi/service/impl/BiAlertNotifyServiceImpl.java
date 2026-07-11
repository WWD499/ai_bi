package com.ruoyi.bi.service.impl;

import com.ruoyi.bi.domain.BiAlertRecord;
import com.ruoyi.bi.domain.BiAlertRule;
import com.ruoyi.bi.service.IBiAlertNotifyService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BI预警通知服务实现
 * 站内信通道（sys_notice）立即可用，前台右上角铃铛会自动提示未读；
 * 邮件通道为可选扩展位，当前项目未集成 spring-boot-starter-mail 与 SMTP 配置，自动降级跳过。
 *
 * @author ruoyi-bi
 */
@Service
public class BiAlertNotifyServiceImpl implements IBiAlertNotifyService {

    private static final Logger log = LoggerFactory.getLogger(BiAlertNotifyServiceImpl.class);

    /** 站内信标题最大长度（对应 sys_notice.notice_title 字段长度限制） */
    private static final int MAX_TITLE_LEN = 50;
    /** sys_notice.notice_type：1=通知 */
    private static final String NOTICE_TYPE_NOTIFY = "1";
    /** sys_notice.status：0=正常 */
    private static final String NOTICE_STATUS_NORMAL = "0";
    /** 站内信创建人标记 */
    private static final String NOTICE_CREATE_BY = "SYSTEM";

    @Autowired
    private ISysNoticeService noticeService;

    @Override
    public void notify(BiAlertRule rule, BiAlertRecord record) {
        sendSiteMessage(rule, record);
        sendEmailOptional(rule, record);
    }

    /**
     * 站内信通知：写入 sys_notice，前台右上角铃铛自动提示未读
     */
    private void sendSiteMessage(BiAlertRule rule, BiAlertRecord record) {
        try {
            SysNotice notice = new SysNotice();
            String title = "【数据预警】" + (rule != null ? rule.getName() : "未知规则");
            if (title.length() > MAX_TITLE_LEN) {
                title = title.substring(0, MAX_TITLE_LEN);
            }
            notice.setNoticeTitle(title);
            notice.setNoticeType(NOTICE_TYPE_NOTIFY);
            notice.setStatus(NOTICE_STATUS_NORMAL);
            notice.setNoticeContent(buildContent(rule, record));
            notice.setCreateBy(NOTICE_CREATE_BY);
            noticeService.insertNotice(notice);
            log.info("已发送站内信预警通知：{}", title);
        } catch (Exception e) {
            log.error("发送站内信预警通知失败：{}", e.getMessage());
        }
    }

    private String buildContent(BiAlertRule rule, BiAlertRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("预警规则：").append(record.getRuleName()).append("\n");
        sb.append("监控表：").append(record.getTableName()).append("\n");
        sb.append("触发消息：").append(record.getAlertMessage()).append("\n");
        sb.append("实际值：").append(record.getActualValue())
                .append("，阈值：").append(record.getThresholdValue())
                .append(" ").append(record.getComparisonOperator()).append("\n");
        if (StringUtils.isNotBlank(record.getAnalysisResult())) {
            sb.append("\n【AI分析】\n").append(record.getAnalysisResult());
        }
        sb.append("\n\n（请前往「BI数据分析 → 数据预警 → 预警记录」处理）");
        return sb.toString();
    }

    /**
     * 邮件通知（可选通道）
     * <p>
     * 说明：当前项目未集成 spring-boot-starter-mail 与 SMTP 配置，暂未启用。
     * 启用步骤：
     *   1) ruoyi-bi/pom.xml 增加 spring-boot-starter-mail 依赖；
     *   2) application.yml 增加 spring.mail.host / port / username / password 配置；
     *   3) 在此方法内注入 JavaMailSender 并发送 MimeMessage。
     * 因目前无 JavaMailSender bean，本方法仅留扩展位，不执行任何操作。
     */
    private void sendEmailOptional(BiAlertRule rule, BiAlertRecord record) {
        // TODO: 引入 spring-boot-starter-mail 并配置 SMTP 后实现邮件推送
        log.info("邮件通知通道未启用（缺少 spring-boot-starter-mail 与 SMTP 配置），已跳过");
    }
}
