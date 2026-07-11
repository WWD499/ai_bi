<template>
  <div class="app-container bi-query-page">
    <!-- 查询区域 -->
    <el-card class="query-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span><el-icon><ChatLineSquare /></el-icon> 自然语言查询</span>
          <el-button type="success" plain size="small" icon="Check" @click="handleTestLlm" :loading="testingLlm">测试LLM</el-button>
        </div>
      </template>
      <el-form :model="queryForm" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="数据源">
              <el-select v-model="queryForm.datasourceId" placeholder="请选择数据源" filterable style="width: 100%">
                <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.name + ' (' + ds.type + ')'" :value="ds.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="查询问题">
              <div class="query-input-group">
                <el-input v-model="queryForm.query" placeholder="用自然语言描述你要查询的数据，如：查询每个部门的员工数量" clearable @keyup.enter="handleQuery" />
                <el-button type="primary" icon="Search" @click="handleQuery" :loading="querying">查询</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 快捷示例 -->
        <div class="quick-examples">
          <el-text type="info" size="small">快捷示例：</el-text>
          <el-tag v-for="(ex, i) in examples" :key="i" class="example-tag" size="small" @click="queryForm.query = ex; handleQuery()" effect="plain">
            {{ ex }}
          </el-tag>
        </div>
      </el-form>
    </el-card>

    <!-- 结果区域 -->
    <div v-if="hasResult" class="result-area">
      <!-- 结果概要 -->
      <el-card shadow="never" class="result-summary">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="数据行数" :value="result.rowCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="图表类型" :value="result.chartName || result.chartType || '-'" />
          </el-col>
          <el-col :span="12">
            <div class="summary-label">生成SQL</div>
            <el-text class="sql-preview" type="primary" @click="copySql">{{ result.sql }}</el-text>
          </el-col>
        </el-row>
      </el-card>

      <!-- 图表 + 表格切换 -->
      <el-card shadow="never">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 图表Tab -->
          <el-tab-pane label="图表" name="chart">
            <div v-if="result.echartsOption" class="chart-container">
              <div ref="chartRef" style="width: 100%; height: 420px"></div>
            </div>
            <el-empty v-else description="该数据类型不适合图表展示" />
          </el-tab-pane>

          <!-- 数据Tab -->
          <el-tab-pane :label="'数据 (' + result.rowCount + '行)'" name="data">
            <el-table :data="tableData" border stripe style="width: 100%" max-height="420">
              <el-table-column v-for="col in result.columns" :key="col" :prop="col" :label="col" align="center" :show-overflow-tooltip="true" />
            </el-table>
          </el-tab-pane>

          <!-- SQL + 解读Tab -->
          <el-tab-pane label="SQL & 解读" name="sql">
            <div class="sql-block">
              <div class="block-title">生成的SQL语句</div>
              <el-input type="textarea" :model-value="result.sql" readonly :rows="4" class="sql-textarea" />
            </div>
            <div class="interpretation-block" v-if="result.interpretation">
              <div class="block-title">AI 数据解读</div>
              <div class="interpretation-text">{{ result.interpretation }}</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-card v-else shadow="never" class="empty-card">
      <el-empty description="输入自然语言问题，点击查询获取数据分析结果" :image-size="120">
        <template #image>
          <el-icon :size="80" color="#c0c4cc"><DataAnalysis /></el-icon>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup name="BiQuery">
import * as echarts from 'echarts'
import { naturalLanguageQuery, testLlm } from '@/api/bi/query'
import { listDatasource } from '@/api/bi/datasource'

const { proxy } = getCurrentInstance()

const datasourceOptions = ref([])
const querying = ref(false)
const testingLlm = ref(false)
const hasResult = ref(false)
const activeTab = ref('chart')
const chartRef = ref(null)
let chartInstance = null

const queryForm = reactive({
  datasourceId: undefined,
  query: '',
  tableName: undefined
})

const result = reactive({
  sql: '',
  columns: [],
  data: [],
  chartType: '',
  chartName: '',
  echartsOption: null,
  interpretation: '',
  rowCount: 0
})

const tableData = computed(() => {
  if (!result.data || result.data.length === 0) return []
  return result.data
})

const examples = [
  '查询每个部门的员工数量',
  '统计每月销售额趋势',
  '各产品类别的销售占比',
  '客户消费金额排行榜TOP10'
]

/** 加载数据源列表 */
function loadDatasources() {
  listDatasource({ pageNum: 1, pageSize: 100, status: 1 }).then(response => {
    datasourceOptions.value = response.rows || []
    if (datasourceOptions.value.length > 0 && !queryForm.datasourceId) {
      queryForm.datasourceId = datasourceOptions.value[0].id
    }
  })
}

/** 执行查询 */
function handleQuery() {
  if (!queryForm.query) {
    proxy.$modal.msgWarning('请输入查询问题')
    return
  }
  if (!queryForm.datasourceId) {
    proxy.$modal.msgWarning('请选择数据源')
    return
  }

  querying.value = true
  hasResult.value = false

  naturalLanguageQuery({
    query: queryForm.query,
    datasourceId: queryForm.datasourceId,
    tableName: queryForm.tableName
  }).then(response => {
    if (response.code === 200 && response.data) {
      Object.assign(result, response.data)
      hasResult.value = true
      activeTab.value = response.data.echartsOption ? 'chart' : 'data'
      // 等待DOM渲染后初始化图表
      nextTick(() => {
        renderChart()
      })
    } else {
      proxy.$modal.msgError(response.msg || '查询失败')
    }
  }).catch(err => {
    proxy.$modal.msgError('查询失败：' + (err.message || '未知错误'))
  }).finally(() => {
    querying.value = false
  })
}

/** 渲染ECharts图表 */
function renderChart() {
  if (!result.echartsOption) return
  if (!chartRef.value) return

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(result.echartsOption, true)

  // 窗口resize
  window.addEventListener('resize', handleResize)
}

/** resize */
function handleResize() {
  if (chartInstance) {
    chartInstance.resize()
  }
}

/** 测试LLM连接 */
function handleTestLlm() {
  testingLlm.value = true
  testLlm().then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess('LLM连接正常')
    } else {
      proxy.$modal.msgError('LLM连接失败：' + response.msg)
    }
  }).catch(() => {
    proxy.$modal.msgError('LLM连接失败')
  }).finally(() => {
    testingLlm.value = false
  })
}

/** 复制SQL */
function copySql() {
  navigator.clipboard.writeText(result.sql).then(() => {
    proxy.$modal.msgSuccess('SQL已复制到剪贴板')
  })
}

/** 组件卸载时清理 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
  }
})

// 初始化
loadDatasources()
</script>

<style scoped>
.bi-query-page .query-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-input-group {
  display: flex;
  gap: 10px;
  width: 100%;
}

.quick-examples {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 4px;
}

.example-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.example-tag:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.result-area {
  margin-top: 16px;
}

.result-summary {
  margin-bottom: 16px;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.sql-preview {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  word-break: break-all;
  cursor: pointer;
  display: block;
  line-height: 1.6;
}

.chart-container {
  padding: 10px 0;
}

.empty-card {
  margin-top: 16px;
}

.block-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  color: #303133;
}

.sql-block {
  margin-bottom: 20px;
}

.sql-textarea :deep(.el-textarea__inner) {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.interpretation-text {
  background: var(--el-fill-color-light);
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}
</style>
