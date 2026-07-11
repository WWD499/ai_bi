package com.ruoyi.web.controller.bi;

import com.ruoyi.bi.domain.BiDatasource;
import com.ruoyi.bi.service.IBiDatasourceService;
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

/**
 * BI数据源配置 Controller
 * 
 * @author ruoyi-bi
 */
@Api("BI-数据源配置")
@RestController
@RequestMapping("/bi/datasource")
public class BiDatasourceController extends BaseController {

    @Autowired
    private IBiDatasourceService datasourceService;

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:list')")
    @ApiOperation("查询数据源列表")
    @GetMapping("/list")
    public TableDataInfo list(BiDatasource datasource) {
        startPage();
        List<BiDatasource> list = datasourceService.selectBiDatasourceList(datasource);
        return getDataTable(list);
    }

    /**
     * 获取数据源详细信息
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:query')")
    @ApiOperation("获取数据源详情")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(datasourceService.selectBiDatasourceById(id));
    }

    /**
     * 新增数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:add')")
    @ApiOperation("新增数据源")
    @Log(title = "BI数据源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BiDatasource datasource) {
        return toAjax(datasourceService.insertBiDatasource(datasource));
    }

    /**
     * 修改数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:edit')")
    @ApiOperation("修改数据源")
    @Log(title = "BI数据源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BiDatasource datasource) {
        return toAjax(datasourceService.updateBiDatasource(datasource));
    }

    /**
     * 删除数据源
     */
    @PreAuthorize("@ss.hasPermi('bi:datasource:remove')")
    @ApiOperation("删除数据源")
    @Log(title = "BI数据源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(datasourceService.deleteBiDatasourceByIds(ids));
    }

    /**
     * 测试数据源连接
     */
    @ApiOperation("测试数据源连接")
    @PostMapping("/test")
    public AjaxResult testConnection(@RequestBody BiDatasource datasource) {
        boolean result = datasourceService.testConnection(datasource);
        return result ? success("连接成功") : error("连接失败");
    }
}
