package com.ruoyi.web.controller.bi;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bi.domain.BiDashboard;
import com.ruoyi.bi.service.IBiDashboardService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

/**
 * BI大屏 Controller
 *
 * @author ruoyi-bi
 */
@Api("BI-大屏管理")
@RestController
@RequestMapping("/bi/dashboard")
public class BiDashboardController extends BaseController {

    @Autowired
    private IBiDashboardService dashboardService;

    // ==================== 大屏配置 CRUD ====================

    @PreAuthorize("@ss.hasPermi('bi:dashboard:list')")
    @ApiOperation("查询大屏列表")
    @GetMapping("/list")
    public TableDataInfo list(BiDashboard dashboard) {
        startPage();
        List<BiDashboard> list = dashboardService.selectBiDashboardList(dashboard);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @ApiOperation("获取大屏详情")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(dashboardService.selectBiDashboardById(id));
    }

    @PreAuthorize("@ss.hasPermi('bi:dashboard:add')")
    @ApiOperation("新增大屏")
    @Log(title = "BI大屏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BiDashboard dashboard) {
        return toAjax(dashboardService.insertBiDashboard(dashboard));
    }

    @PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
    @ApiOperation("修改大屏")
    @Log(title = "BI大屏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BiDashboard dashboard) {
        return toAjax(dashboardService.updateBiDashboard(dashboard));
    }

    @PreAuthorize("@ss.hasPermi('bi:dashboard:remove')")
    @ApiOperation("删除大屏")
    @Log(title = "BI大屏", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(dashboardService.deleteBiDashboardByIds(ids));
    }

    // ==================== Widget 数据查询 ====================

    /** 查询单个 Widget 的数据 */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @ApiOperation("查询Widget数据")
    @PostMapping("/widget/data")
    public AjaxResult queryWidgetData(@RequestBody JSONObject widgetConfig) {
        JSONObject result = dashboardService.queryWidgetData(widgetConfig);
        if (result.containsKey("error")) {
            return error(result.getString("error"));
        }
        return success(result);
    }

    /** 获取数据源的所有表结构（供大屏配置时选择） */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @ApiOperation("获取数据源表结构")
    @GetMapping("/tables/{datasourceId}")
    public AjaxResult getTables(@PathVariable("datasourceId") Long datasourceId) {
        List<Map<String, Object>> tables = dashboardService.getTableSchemas(datasourceId);
        return success(tables);
    }
}
