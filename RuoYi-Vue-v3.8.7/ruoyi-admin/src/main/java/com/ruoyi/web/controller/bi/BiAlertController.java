package com.ruoyi.web.controller.bi;

import com.ruoyi.bi.domain.BiAlertRecord;
import com.ruoyi.bi.domain.BiAlertRule;
import com.ruoyi.bi.service.IBiAlertRuleService;
import com.ruoyi.bi.mapper.BiAlertRecordMapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import com.ruoyi.bi.enums.AlertLevel;
import com.ruoyi.bi.enums.AlertStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BI数据预警 Controller
 *
 * @author ruoyi-bi
 */
@Api("BI-数据预警")
@RestController
@RequestMapping("/bi/alert")
public class BiAlertController extends BaseController {

    @Autowired
    private IBiAlertRuleService alertRuleService;

    @Autowired
    private BiAlertRecordMapper alertRecordMapper;

    // ==================== 预警规则 CRUD ====================

    @PreAuthorize("@ss.hasPermi('bi:alert:list')")
    @ApiOperation("查询预警规则列表")
    @GetMapping("/rule/list")
    public TableDataInfo ruleList(BiAlertRule rule) {
        startPage();
        List<BiAlertRule> list = alertRuleService.selectBiAlertRuleList(rule);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bi:alert:query')")
    @ApiOperation("获取预警规则详情")
    @GetMapping("/rule/{id}")
    public AjaxResult getRuleInfo(@PathVariable("id") Long id) {
        return success(alertRuleService.selectBiAlertRuleById(id));
    }

    @PreAuthorize("@ss.hasPermi('bi:alert:add')")
    @ApiOperation("新增预警规则")
    @Log(title = "BI预警规则", businessType = BusinessType.INSERT)
    @PostMapping("/rule")
    public AjaxResult addRule(@RequestBody BiAlertRule rule) {
        return toAjax(alertRuleService.insertBiAlertRule(rule));
    }

    @PreAuthorize("@ss.hasPermi('bi:alert:edit')")
    @ApiOperation("修改预警规则")
    @Log(title = "BI预警规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult editRule(@RequestBody BiAlertRule rule) {
        return toAjax(alertRuleService.updateBiAlertRule(rule));
    }

    @PreAuthorize("@ss.hasPermi('bi:alert:remove')")
    @ApiOperation("删除预警规则")
    @Log(title = "BI预警规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rule/{ids}")
    public AjaxResult removeRule(@PathVariable Long[] ids) {
        return toAjax(alertRuleService.deleteBiAlertRuleByIds(ids));
    }

    // ==================== 预警记录查询 ====================

    @PreAuthorize("@ss.hasPermi('bi:alert:list')")
    @ApiOperation("查询预警记录列表")
    @GetMapping("/record/list")
    public TableDataInfo recordList(BiAlertRecord record) {
        startPage();
        List<BiAlertRecord> list = alertRecordMapper.selectBiAlertRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bi:alert:query')")
    @ApiOperation("获取预警记录详情")
    @GetMapping("/record/{id}")
    public AjaxResult getRecordInfo(@PathVariable("id") Long id) {
        return success(alertRecordMapper.selectBiAlertRecordById(id));
    }

    /** 处理预警记录（确认/解决） */
    @PreAuthorize("@ss.hasPermi('bi:alert:edit')")
    @ApiOperation("处理预警记录（确认/解决）")
    @PutMapping("/record/handle")
    public AjaxResult handleRecord(@RequestBody BiAlertRecord record) {
        try {
            record.setHandledBy(SecurityUtils.getUsername());
        } catch (Exception ignored) {
            // 非HTTP会话（如定时任务上下文）不设置处理人
        }
        return toAjax(alertRecordMapper.updateBiAlertRecord(record));
    }

    // ==================== 预警统计概览 ====================

    @PreAuthorize("@ss.hasPermi('bi:alert:list')")
    @ApiOperation("获取预警统计概览")
    @GetMapping("/record/stats")
    public AjaxResult alertStats() {
        List<BiAlertRecord> all = alertRecordMapper.selectBiAlertRecordList(new BiAlertRecord());
        LocalDate today = LocalDate.now();
        int pending = 0, todayCount = 0, critical = 0, resolved = 0;
        for (BiAlertRecord r : all) {
            if (AlertStatus.PENDING.getCode().equals(r.getStatus())) {
                pending++;
                if (AlertLevel.CRITICAL.getCode().equals(r.getAlertLevel())) critical++;
            }
            if (AlertStatus.RESOLVED.getCode().equals(r.getStatus())) resolved++;
            if (r.getAlertTime() != null && r.getAlertTime().toLocalDate().equals(today)) todayCount++;
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("today", todayCount);
        stats.put("critical", critical);
        stats.put("resolved", resolved);
        stats.put("total", all.size());
        return success(stats);
    }

    // ==================== 手动执行检查 ====================

    @PreAuthorize("@ss.hasPermi('bi:alert:list')")
    @ApiOperation("手动执行预警检查")
    @PostMapping("/check")
    public AjaxResult manualCheck() {
        int count = alertRuleService.scanAndCheckAlerts();
        return success("扫描完成，触发 " + count + " 条预警");
    }
}
