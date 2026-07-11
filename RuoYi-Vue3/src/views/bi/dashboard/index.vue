<template>
  <div class="app-container">
    <!-- ========== 大屏列表 ========== -->
    <template v-if="!editMode">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch">
        <el-form-item label="大屏名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bi:dashboard:add']">新建大屏</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="dashboardList">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="大屏名称" prop="name" min-width="160" />
        <el-table-column label="描述" prop="description" :show-overflow-tooltip="true" min-width="200" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="公开" width="70" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === 1 ? 'primary' : 'info'" size="small">
              {{ scope.row.isPublic === 1 ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="enterEdit(scope.row)" v-hasPermi="['bi:dashboard:edit']">编辑</el-button>
            <el-button link type="success" icon="View" @click="enterPreview(scope.row)">预览</el-button>
            <el-button link type="primary" icon="CopyDocument" @click="handleCopy(scope.row)">复制</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bi:dashboard:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </template>

    <!-- ========== 大屏编辑/预览模式 ========== -->
    <template v-else>
      <el-card shadow="never" class="dashboard-editor">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>
              <el-button icon="ArrowLeft" @click="exitEdit">{{ previewMode ? '返回' : '返回列表' }}</el-button>
              <span style="font-size:16px;font-weight:bold;margin-left:12px">{{ currentDashboard.name }}</span>
            </span>
            <span v-if="!previewMode">
              <el-button icon="Plus" type="primary" @click="addWidget">添加图表</el-button>
              <el-button icon="Picture" type="info" @click="addImageWidget">添加图片</el-button>
              <el-button icon="Check" type="success" @click="saveDashboard">保存布局</el-button>
              <el-button icon="FullScreen" @click="enterPreview(currentDashboard)">预览</el-button>
            </span>
          </div>
        </template>

        <!-- 编辑模式 — 拖拽网格 -->
        <div class="dashboard-grid" v-if="!previewMode" ref="gridRef">
          <grid-layout
            :layout="widgets"
            :col-num="12"
            :row-height="80"
            :is-draggable="true"
            :is-resizable="true"
            :margin="[12, 12]"
            @layout-updated="onLayoutUpdated">
            <grid-item v-for="(widget, index) in widgets" :key="widget.i"
              :x="widget.x" :y="widget.y" :w="widget.w" :h="widget.h" :i="widget.i">
              <div class="widget-card" :class="{ 'widget-loading': widget.loading }">
                <div class="widget-toolbar">
                  <span class="widget-title">{{ widget.title || '未命名图表' }}</span>
                  <span>
                    <el-button link type="primary" icon="Setting" size="small" @click="configWidget(index)">配置</el-button>
                    <el-button link type="primary" icon="Refresh" size="small" @click="refreshWidget(index)">刷新</el-button>
                    <el-button link type="danger" icon="Delete" size="small" @click="removeWidget(index)">删除</el-button>
                  </span>
                </div>
                <div class="widget-body" v-loading="widget.loading">
                  <!-- 图片 Widget -->
                  <template v-if="widget.config && widget.config.chartType === 'image'">
                    <div v-if="!widget.config.imageUrl" class="widget-placeholder">
                      <el-icon :size="40"><Picture /></el-icon>
                      <p>点击"配置"上传或填写图片URL</p>
                    </div>
                    <div v-else class="widget-image-container">
                      <img :src="resolveImageUrl(widget.config.imageUrl)" class="widget-image" :style="{ objectFit: widget.config.imageFit || 'contain' }" />
                    </div>
                  </template>
                  <!-- 图表 Widget -->
                  <template v-else>
                    <div v-if="!widget.chartOption" class="widget-placeholder">
                      <el-icon :size="40"><PieChart /></el-icon>
                      <p>点击"配置"设置数据源和图表</p>
                    </div>
                    <div v-else :ref="el => widgetRefs[index] = el" class="widget-chart"></div>
                  </template>
                </div>
              </div>
            </grid-item>
          </grid-layout>
        </div>

        <!-- 预览模式 — 网格只读展示（与编辑模式布局一致） -->
        <div class="dashboard-preview" v-else>
          <grid-layout
            :layout="widgets"
            :col-num="12"
            :row-height="80"
            :is-draggable="false"
            :is-resizable="false"
            :margin="[12, 12]"
            :vertical-compact="true">
            <grid-item v-for="(widget, index) in widgets" :key="widget.i"
              :x="widget.x" :y="widget.y" :w="widget.w" :h="widget.h" :i="widget.i">
              <div class="widget-card preview-card">
                <div class="widget-toolbar" v-if="widget.title">
                  <span class="widget-title">{{ widget.title }}</span>
                </div>
                <div class="widget-body">
                  <!-- 图片 Widget -->
                  <template v-if="widget.config && widget.config.chartType === 'image'">
                    <div v-if="widget.config.imageUrl" class="widget-image-container">
                      <img :src="resolveImageUrl(widget.config.imageUrl)" class="widget-image" :style="{ objectFit: widget.config.imageFit || 'contain' }" />
                    </div>
                    <div v-else class="widget-placeholder">
                      <el-icon :size="32"><Picture /></el-icon>
                      <p>未配置图片</p>
                    </div>
                  </template>
                  <!-- 图表 Widget -->
                  <template v-else>
                    <div v-if="!widget.chartOption" class="widget-placeholder">
                      <el-icon :size="32"><PieChart /></el-icon>
                      <p>无数据</p>
                    </div>
                    <div v-else :ref="el => previewRefs[index] = el" class="widget-chart"></div>
                  </template>
                </div>
              </div>
            </grid-item>
          </grid-layout>
        </div>
      </el-card>
    </template>

    <!-- ========== 新建/编辑大屏对话框 ========== -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入大屏名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公开分享">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitDashboard">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ========== Widget 配置对话框 ========== -->
    <el-dialog :title="currentWidget && currentWidget.config && currentWidget.config.chartType === 'image' ? '图片配置' : '图表配置'" v-model="widgetConfigVisible" width="680px" append-to-body>
      <el-form v-if="currentWidget" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="currentWidget.title" placeholder="如：月度营收趋势" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="currentWidget.config.chartType" placeholder="选择类型" @change="onTypeChange">
            <el-option label="柱状图" value="bar" />
            <el-option label="折线图" value="line" />
            <el-option label="饼图" value="pie" />
            <el-option label="散点图" value="scatter" />
            <el-option label="雷达图" value="radar" />
            <el-option label="表格" value="table" />
            <el-option label="图片" value="image" />
          </el-select>
        </el-form-item>

        <!-- 图片配置 -->
        <template v-if="currentWidget.config.chartType === 'image'">
          <el-form-item label="图片URL">
            <el-input v-model="currentWidget.config.imageUrl" placeholder="可直接粘贴图片URL" clearable>
              <template #append>
                <el-button icon="Refresh" @click="previewImageUrl">预览</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="上传图片">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :on-success="handleImageUploadSuccess"
              :on-error="handleImageUploadError"
              accept="image/*">
              <el-button type="primary" icon="Upload">点击上传</el-button>
              <template #tip>
                <div style="font-size:12px;color:#999">支持 jpg/png/gif/webp，建议不超过5MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="预览" v-if="currentWidget.config.imageUrl">
            <div style="width:100%;max-height:200px;overflow:hidden;border:1px solid #eee;border-radius:4px;display:flex;align-items:center;justify-content:center;background:#f5f7fa">
              <img :src="resolveImageUrl(currentWidget.config.imageUrl)" style="max-width:100%;max-height:200px" />
            </div>
          </el-form-item>
          <el-form-item label="填充方式">
            <el-radio-group v-model="currentWidget.config.imageFit">
              <el-radio value="contain">等比缩放</el-radio>
              <el-radio value="cover">铺满裁切</el-radio>
              <el-radio value="fill">拉伸填充</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <!-- 图表配置 -->
        <template v-else>
          <el-form-item label="数据源">
            <el-select v-model="currentWidget.config.datasourceId" placeholder="请选择数据源" style="width:100%" @change="onDatasourceChange">
              <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.name" :value="ds.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="查询模式">
            <el-radio-group v-model="currentWidget.config.queryMode">
              <el-radio value="visual">可视化构建</el-radio>
              <el-radio value="custom">自定义SQL</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 可视化构建模式 -->
          <template v-if="currentWidget.config.queryMode !== 'custom'">
            <el-form-item label="选择表">
              <el-select v-model="currentWidget.config.tableName" placeholder="请选择表" style="width:100%" filterable @change="onTableChange">
                <el-option v-for="t in tableSchemas" :key="t.tableName"
                  :label="t.remark ? t.tableName + '（' + t.remark + '）' : t.tableName"
                  :value="t.tableName" />
              </el-select>
            </el-form-item>

            <el-form-item label="分组字段">
              <el-select v-model="currentWidget.config.groupField" placeholder="选择维度字段（可选）" style="width:100%" clearable>
                <el-option v-for="col in currentTableColumns" :key="col.name"
                  :label="col.remark ? col.name + '（' + col.remark + '）' : col.name + ' [' + col.type + ']'"
                  :value="col.name" />
              </el-select>
              <div style="font-size:12px;color:#999;margin-top:4px">用于X轴/饼图标签，留空则只查汇总值</div>
            </el-form-item>

            <el-form-item label="指标字段">
              <div v-for="(metric, idx) in currentWidget.config.metrics" :key="idx" class="metric-row">
                <el-select v-model="metric.field" placeholder="选择字段" style="width:45%" filterable>
                  <el-option v-for="col in currentTableColumns" :key="col.name"
                    :label="col.remark ? col.name + '（' + col.remark + '）' : col.name + ' [' + col.type + ']'"
                    :value="col.name" />
                </el-select>
                <el-select v-model="metric.aggregation" placeholder="聚合" style="width:35%;margin-left:8px">
                  <el-option label="求和 (SUM)" value="SUM" />
                  <el-option label="计数 (COUNT)" value="COUNT" />
                  <el-option label="平均值 (AVG)" value="AVG" />
                  <el-option label="最大值 (MAX)" value="MAX" />
                  <el-option label="最小值 (MIN)" value="MIN" />
                </el-select>
                <el-button v-if="currentWidget.config.metrics.length > 1" type="danger" link icon="Delete"
                  style="margin-left:8px" @click="removeMetric(idx)" />
              </div>
              <el-button type="primary" link icon="Plus" @click="addMetric">添加指标</el-button>
            </el-form-item>

            <el-form-item label="SQL预览">
              <el-input :model-value="generatedSql" type="textarea" :rows="2" readonly />
            </el-form-item>
          </template>

          <!-- 自定义SQL模式 -->
          <template v-else>
            <el-form-item label="查询SQL">
              <el-input v-model="currentWidget.config.sql" type="textarea" :rows="3"
                placeholder="SELECT category, SUM(total_amount) FROM demo_order GROUP BY category" />
            </el-form-item>
          </template>
        </template>

        <el-form-item label="宽度比例">
          <el-slider v-model="currentWidget.w" :min="2" :max="12" show-input />
        </el-form-item>
        <el-form-item label="高度比例">
          <el-slider v-model="currentWidget.h" :min="2" :max="8" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="widgetConfigVisible = false">取 消</el-button>
        <el-button type="primary" @click="applyWidgetConfig">应 用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BiDashboard">
import * as echarts from 'echarts'
import { listDashboard, getDashboard, addDashboard, updateDashboard, delDashboard, queryWidgetData, getTableSchemas } from '@/api/bi/dashboard'
import { listDatasource } from '@/api/bi/datasource'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

// ========== 图片上传配置 ==========
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }))

// ========== 大屏列表 ==========
const loading = ref(false)
const total = ref(0)
const showSearch = ref(true)
const dashboardList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const datasourceOptions = ref([])
const tableSchemas = ref([])

// ========== 可视化查询构建 ==========
const currentTableColumns = computed(() => {
  if (!currentWidget.value || !currentWidget.value.config || !currentWidget.value.config.tableName) return []
  const table = tableSchemas.value.find(t => t.tableName === currentWidget.value.config.tableName)
  return table ? table.columns : []
})

const generatedSql = computed(() => {
  if (!currentWidget.value || !currentWidget.value.config) return ''
  const cfg = currentWidget.value.config
  if (!cfg.tableName) return ''
  const metrics = (cfg.metrics || []).filter(m => m.field)
  if (metrics.length === 0) return ''

  const selectParts = []
  if (cfg.groupField) {
    selectParts.push(cfg.groupField)
  }
  metrics.forEach(m => {
    const alias = m.aggregation === 'COUNT' ? `cnt_${m.field}` : `${m.aggregation.toLowerCase()}_${m.field}`
    selectParts.push(`${m.aggregation}(${m.field}) AS ${alias}`)
  })
  const sql = `SELECT ${selectParts.join(', ')} FROM ${cfg.tableName}`
  return cfg.groupField ? `${sql} GROUP BY ${cfg.groupField}` : sql
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: null, status: null },
  formRules: {
    name: [{ required: true, message: '请输入大屏名称', trigger: 'blur' }]
  }
})
const { queryParams, form, formRules } = toRefs(data)
const queryFormRef = ref(null)
const formRef = ref(null)

function getList() {
  loading.value = true
  listDashboard(queryParams.value).then(response => {
    dashboardList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleAdd() {
  resetDashboardForm()
  dialogTitle.value = '新建大屏'
  dialogVisible.value = true
  form.value.status = 1
  form.value.isPublic = 0
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除大屏 "' + row.name + '" ？').then(() => {
    return delDashboard(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  })
}

function handleCopy(row) {
  getDashboard(row.id).then(response => {
    const source = response.data
    const copy = { name: source.name + ' - 副本', description: source.description,
      configJson: source.configJson, status: source.status, isPublic: source.isPublic }
    addDashboard(copy).then(() => {
      proxy.$modal.msgSuccess('复制成功')
      getList()
    })
  })
}

function resetDashboardForm() {
  form.value = {}
  formRef.value?.resetFields()
}

function submitDashboard() {
  formRef.value.validate(valid => {
    if (!valid) return
    if (form.value.id) {
      updateDashboard(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        dialogVisible.value = false
        getList()
      })
    } else {
      addDashboard(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        dialogVisible.value = false
        getList()
      })
    }
  })
}

// ========== 大屏编辑 ==========
const editMode = ref(false)
const previewMode = ref(false)
const currentDashboard = ref(null)
const widgets = ref([])
const widgetRefs = reactive({})
const previewRefs = reactive({})
const chartInstances = {}
const widgetConfigVisible = ref(false)
const currentWidgetIndex = ref(-1)
const currentWidget = ref(null)
const gridRef = ref(null)

function enterEdit(row) {
  getDashboard(row.id).then(response => {
    currentDashboard.value = response.data
    previewMode.value = false

    // 解析配置
    if (currentDashboard.value.configJson) {
      try {
        const config = JSON.parse(currentDashboard.value.configJson)
        widgets.value = (config.widgets || []).map(w => ({
          ...w,
          loading: false,
          chartOption: null
        }))
      } catch (e) {
        widgets.value = []
      }
    } else {
      widgets.value = []
    }

    // 加载所有 widget 数据
    nextTick(() => loadAllWidgets())
    editMode.value = true
  })
}

function enterPreview(row) {
  getDashboard(row.id).then(response => {
    currentDashboard.value = response.data
    editMode.value = true
    previewMode.value = true

    if (currentDashboard.value.configJson) {
      try {
        const config = JSON.parse(currentDashboard.value.configJson)
        widgets.value = (config.widgets || []).map(w => ({
          ...w,
          loading: false,
          chartOption: null
        }))
      } catch (e) {
        widgets.value = []
      }
    } else {
      widgets.value = []
    }

    nextTick(() => loadPreviewWidgets())
  })
}

function exitEdit() {
  editMode.value = false
  previewMode.value = false
  // 清理图表实例
  Object.values(chartInstances).forEach(inst => inst?.dispose())
  Object.keys(chartInstances).forEach(k => delete chartInstances[k])
  getList()
}

function addWidget() {
  widgets.value.push({
    i: 'widget_' + Date.now(),
    x: (widgets.value.length * 2) % 12,
    y: Math.floor(widgets.value.length / 3) * 3,
    w: 4,
    h: 4,
    title: '图表 ' + (widgets.value.length + 1),
    loading: false,
    chartOption: null,
    config: {
      datasourceId: null,
      chartType: 'bar',
      queryMode: 'visual',
      tableName: '',
      groupField: '',
      metrics: [{ field: '', aggregation: 'SUM' }],
      sql: ''
    }
  })
  currentDashboard.value.changed = true
}

function addImageWidget() {
  widgets.value.push({
    i: 'widget_' + Date.now(),
    x: (widgets.value.length * 2) % 12,
    y: Math.floor(widgets.value.length / 3) * 3,
    w: 4,
    h: 4,
    title: '图片 ' + (widgets.value.length + 1),
    loading: false,
    chartOption: null,
    config: {
      chartType: 'image',
      imageUrl: '',
      imageFit: 'contain'
    }
  })
  currentDashboard.value.changed = true
  // 新增图片后自动弹出配置
  nextTick(() => {
    configWidget(widgets.value.length - 1)
  })
}

// ========== 图片上传 ==========
function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    proxy.$modal.msgError('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    proxy.$modal.msgError('图片大小不能超过5MB')
    return false
  }
  return true
}

function handleImageUploadSuccess(response) {
  if (response.code === 200 && response.url) {
    currentWidget.value.config.imageUrl = response.url
    proxy.$modal.msgSuccess('上传成功')
  } else {
    proxy.$modal.msgError('上传失败：' + (response.msg || '未知错误'))
  }
}

function handleImageUploadError() {
  proxy.$modal.msgError('上传失败，请检查网络或联系管理员')
}

function previewImageUrl() {
  if (!currentWidget.value.config.imageUrl) {
    proxy.$modal.msgWarning('请先填写图片URL')
    return
  }
  // 预览已通过配置弹窗中的预览项实时展示，这里只做提示
  proxy.$modal.msgSuccess('预览已更新')
}

function resolveImageUrl(url) {
  if (!url) return ''
  // 如果已经是完整URL（http/https开头）直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 否则拼接后端base URL（dev-api代理会自动转发）
  return import.meta.env.VITE_APP_BASE_API + url
}

function onTypeChange(newType) {
  // 切换到图片类型时，补充图片配置字段
  if (newType === 'image') {
    if (!currentWidget.value.config.imageUrl) currentWidget.value.config.imageUrl = ''
    if (!currentWidget.value.config.imageFit) currentWidget.value.config.imageFit = 'contain'
  }
}

// ========== 可视化查询构建辅助函数 ==========
function loadTableSchemas(datasourceId) {
  if (!datasourceId) {
    tableSchemas.value = []
    return
  }
  getTableSchemas(datasourceId).then(response => {
    tableSchemas.value = response.data || []
  })
}

function onDatasourceChange(dsId) {
  // 切换数据源时重置表和字段选择
  currentWidget.value.config.tableName = ''
  currentWidget.value.config.groupField = ''
  currentWidget.value.config.metrics = [{ field: '', aggregation: 'SUM' }]
  tableSchemas.value = []
  if (dsId) loadTableSchemas(dsId)
}

function onTableChange() {
  // 切换表时重置字段选择
  currentWidget.value.config.groupField = ''
  currentWidget.value.config.metrics = [{ field: '', aggregation: 'SUM' }]
}

function addMetric() {
  currentWidget.value.config.metrics.push({ field: '', aggregation: 'SUM' })
}

function removeMetric(idx) {
  currentWidget.value.config.metrics.splice(idx, 1)
}

function removeWidget(index) {
  const widget = widgets.value[index]
  if (chartInstances[widget.i]) {
    chartInstances[widget.i].dispose()
    delete chartInstances[widget.i]
  }
  widgets.value.splice(index, 1)
  currentDashboard.value.changed = true
}

function configWidget(index) {
  currentWidgetIndex.value = index
  currentWidget.value = JSON.parse(JSON.stringify(widgets.value[index]))
  // 兼容旧数据：确保新字段存在
  const cfg = currentWidget.value.config
  if (cfg.chartType !== 'image') {
    if (!cfg.queryMode) cfg.queryMode = cfg.sql ? 'custom' : 'visual'
    if (!cfg.metrics) cfg.metrics = [{ field: '', aggregation: 'SUM' }]
    if (!cfg.tableName) cfg.tableName = ''
    if (!cfg.groupField) cfg.groupField = ''
  }
  // 如果已有数据源，加载表结构
  if (cfg.datasourceId) {
    loadTableSchemas(cfg.datasourceId)
  }
  widgetConfigVisible.value = true
}

function applyWidgetConfig() {
  const w = widgets.value[currentWidgetIndex.value]
  w.title = currentWidget.value.title
  w.w = currentWidget.value.w
  w.h = currentWidget.value.h
  w.config = { ...currentWidget.value.config }
  // 可视化模式下，把生成的SQL写入config.sql供后端使用
  if (w.config.queryMode === 'visual') {
    w.config.sql = generatedSql.value
  }
  widgetConfigVisible.value = false
  currentDashboard.value.changed = true

  // 图片widget无需查询数据，图表widget才需要
  if (w.config.chartType !== 'image') {
    refreshWidget(currentWidgetIndex.value)
  }
}

function refreshWidget(index) {
  const widget = widgets.value[index]
  // 图片widget无需查询数据
  if (widget.config && widget.config.chartType === 'image') {
    widget.chartOption = null  // 图片不走ECharts
    return
  }
  if (!widget.config || !widget.config.datasourceId || !widget.config.sql) {
    // 初始加载时缺少配置不提示（可能是新建未配置的widget）
    return
  }

  widget.loading = true
  queryWidgetData({
    datasourceId: widget.config.datasourceId,
    sql: widget.config.sql,
    chartType: widget.config.chartType
  }).then(response => {
    const result = response.data
    const columns = result.columns || []
    const rows = result.rows || []

    // 检查后端返回的业务错误
    if (result.error) {
      console.warn('Widget查询返回错误:', result.error)
      widget.loading = false
      return
    }

    // 根据 chartType 和 columns 生成 ECharts option
    widget.chartOption = buildEChartsOption(widget.config.chartType, columns, rows)
    widget.loading = false

    nextTick(() => {
      renderChart(widget.i, widget.chartOption, widgetRefs[index])
    })
  }).catch(error => {
    console.warn('Widget数据查询失败:', widget.title, error?.message || error)
    widget.loading = false
  })
}

function loadAllWidgets() {
  widgets.value.forEach((w, index) => {
    // 图片widget无需查询数据
    if (w.config && w.config.chartType === 'image') return
    if (w.config && w.config.datasourceId && w.config.sql) {
      refreshWidget(index)
    } else {
      nextTick(() => renderChart(widgets.value[index].i))
    }
  })
}

function loadPreviewWidgets() {
  widgets.value.forEach((w, index) => {
    // 图片widget无需查询数据
    if (w.config && w.config.chartType === 'image') return
    if (w.config && w.config.datasourceId && w.config.sql) {
      w.loading = true
      queryWidgetData({
        datasourceId: w.config.datasourceId,
        sql: w.config.sql,
        chartType: w.config.chartType
      }).then(response => {
        const result = response.data
        if (!result.error) {
          w.chartOption = buildEChartsOption(w.config.chartType, result.columns || [], result.rows || [])
        }
        w.loading = false
        nextTick(() => {
          renderChart(w.i, w.chartOption, previewRefs[index])
        })
      }).catch(() => { w.loading = false })
    }
  })
}

function renderChart(widgetId, option, refEl) {
  if (!option) return
  const el = refEl
  if (!el) return

  if (chartInstances[widgetId]) {
    chartInstances[widgetId].dispose()
  }
  chartInstances[widgetId] = echarts.init(el)
  chartInstances[widgetId].setOption(option, true)
}

function buildEChartsOption(chartType, columns, rows) {
  if (!columns.length || !rows.length) return null

  const option = {
    tooltip: { trigger: chartType === 'pie' ? 'item' : 'axis' },
    legend: { data: columns.slice(1), bottom: 0, textStyle: { fontSize: 11 } },
    grid: { top: 20, right: 20, bottom: 40, left: 50 }
  }

  if (chartType === 'pie') {
    option.series = [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      data: rows.map(r => ({ name: r[columns[0]], value: parseFloat(r[columns[1]]) || 0 }))
    }]
  } else if (chartType === 'scatter') {
    option.xAxis = { type: 'value', name: columns[0] }
    option.yAxis = { type: 'value', name: columns[1] }
    option.series = [{
      type: 'scatter',
      symbolSize: 10,
      data: rows.map(r => [parseFloat(r[columns[0]]) || 0, parseFloat(r[columns[1]]) || 0])
    }]
  } else if (chartType === 'radar') {
    const indicators = rows.map(r => ({ name: r[columns[0]], max: Math.max(...columns.slice(1).map(c =>
      Math.max(...rows.map(rr => parseFloat(rr[c]) || 0)))) * 1.2 }))
    option.radar = { indicators }
    option.series = columns.slice(1).map(col => ({
      type: 'radar',
      data: [{ value: rows.map(r => parseFloat(r[col]) || 0), name: col }]
    }))
  } else {
    // bar / line / table
    option.xAxis = { data: rows.map(r => r[columns[0]]), axisLabel: { rotate: rows.length > 8 ? 30 : 0, fontSize: 10 } }
    option.yAxis = { type: 'value' }
    option.series = columns.slice(1).map(col => ({
      name: col,
      type: chartType === 'line' ? 'line' : 'bar',
      data: rows.map(r => parseFloat(r[col]) || 0),
      smooth: chartType === 'line'
    }))
  }

  return option
}

function onLayoutUpdated(newLayout) {
  newLayout.forEach(item => {
    const widget = widgets.value.find(w => w.i === item.i)
    if (widget) {
      widget.x = item.x
      widget.y = item.y
      widget.w = item.w
      widget.h = item.h
    }
  })
  currentDashboard.value.changed = true
}

function saveDashboard() {
  const config = { widgets: widgets.value.map(w => ({
    i: w.i, x: w.x, y: w.y, w: w.w, h: w.h,
    title: w.title, config: w.config
  })) }
  currentDashboard.value.configJson = JSON.stringify(config)

  updateDashboard(currentDashboard.value).then(() => {
    proxy.$modal.msgSuccess('保存成功')
    currentDashboard.value.changed = false
  })
}

// ========== 窗口 resize ==========
function handleResize() {
  Object.values(chartInstances).forEach(inst => inst?.resize())
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(inst => inst?.dispose())
})

// 初始化
function loadDatasources() {
  listDatasource({ pageNum: 1, pageSize: 100, status: 1 }).then(response => {
    datasourceOptions.value = response.rows || []
  })
}

loadDatasources()
getList()
</script>

<style scoped>
.dashboard-editor { min-height: 600px; }

.dashboard-grid { background: #f0f2f5; border-radius: 4px; padding: 4px; min-height: 400px; }

.widget-card {
  background: #fff; border-radius: 4px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.1); height: 100%; display: flex; flex-direction: column;
}
.widget-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,.15); }

.widget-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 12px; background: #fafafa; border-bottom: 1px solid #eee; flex-shrink: 0;
  cursor: move;
}
.widget-title { font-size: 13px; font-weight: 600; color: #333; }

.widget-body { flex: 1; min-height: 0; position: relative; }
.widget-chart { width: 100%; height: 100%; }
.widget-image-container { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.widget-image { max-width: 100%; max-height: 100%; }
.widget-placeholder {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100%; color: #bbb;
}
.widget-placeholder p { margin-top: 8px; font-size: 13px; }
/* 预览模式下的占位符适配深色背景 */
.preview-card .widget-placeholder { color: rgba(255,255,255,0.35); }
.preview-card .widget-placeholder p { color: rgba(255,255,255,0.3); font-size: 12px; }

.widget-loading .widget-body::after {
  content: ''; position: absolute; inset: 0; background: rgba(255,255,255,.6);
}

.preview-card {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(4px);
}
.preview-card .widget-toolbar {
  background: rgba(0,0,0,0.2);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.preview-card .widget-title { color: rgba(255,255,255,0.85); font-size: 13px; }

.dashboard-preview {
  background: linear-gradient(135deg, #0a1628 0%, #0d1f3c 50%, #0a1628 100%);
  padding: 16px;
  min-height: calc(100vh - 200px);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}
/* 大屏背景装饰 */
.dashboard-preview::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 10% 20%, rgba(64,158,255,0.06) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(99,102,241,0.05) 0%, transparent 50%);
  pointer-events: none;
}

.metric-row { display: flex; align-items: center; margin-bottom: 8px; }
</style>
