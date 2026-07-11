<template>
  <div class="app-container">
    <!-- ========== 预警统计概览 ========== -->
    <el-row :gutter="20" class="mb8 stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card pending">
          <div class="stats-value">{{ stats.pending }}</div>
          <div class="stats-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card today">
          <div class="stats-value">{{ stats.today }}</div>
          <div class="stats-label">今日预警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card critical">
          <div class="stats-value">{{ stats.critical }}</div>
          <div class="stats-label">严重未处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card resolved">
          <div class="stats-value">{{ stats.resolved }}</div>
          <div class="stats-label">已解决</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== Tab 1: 预警规则 ========== -->
      <el-tab-pane label="预警规则" name="rule">
        <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch">
          <el-form-item label="预警名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="监控表名" prop="tableName">
            <el-input v-model="queryParams.tableName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bi:alert:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Bell" @click="handleManualCheck">手动检查</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
        </el-row>

        <el-table v-loading="loading" :data="ruleList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="预警名称" prop="name" :show-overflow-tooltip="true" min-width="150" />
          <el-table-column label="监控表" prop="tableName" width="150" />
          <el-table-column label="监控字段" prop="metricField" width="120" />
          <el-table-column label="条件SQL" prop="conditionSql" :show-overflow-tooltip="true" min-width="200" />
          <el-table-column label="阈值" prop="thresholdValue" width="100" />
          <el-table-column label="运算符" prop="comparisonOperator" width="70" />
          <el-table-column label="检查间隔" width="100">
            <template #default="scope">{{ scope.row.checkInterval }}分钟</template>
          </el-table-column>
          <el-table-column label="AI分析" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.analysisEnabled === 1 ? 'success' : 'info'" size="small">
                {{ scope.row.analysisEnabled === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="70" align="center">
            <template #default="scope">
              <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0"
                @change="handleStatusChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="上次检查" width="160">
            <template #default="scope">{{ fmtTime(scope.row.lastCheckTime) }}</template>
          </el-table-column>
          <el-table-column label="上次预警" width="160">
            <template #default="scope">{{ fmtTime(scope.row.lastAlertTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bi:alert:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bi:alert:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-tab-pane>

      <!-- ========== Tab 2: 预警记录 ========== -->
      <el-tab-pane label="预警记录" name="record">
        <el-form :model="recordQuery" :inline="true">
          <el-form-item label="预警级别">
            <el-select v-model="recordQuery.alertLevel" placeholder="全部" clearable @change="getRecordList">
              <el-option label="严重" value="critical" />
              <el-option label="警告" value="warning" />
              <el-option label="提示" value="info" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="recordQuery.status" placeholder="全部" clearable @change="getRecordList">
              <el-option label="待处理" value="pending" />
              <el-option label="已确认" value="confirmed" />
              <el-option label="已解决" value="resolved" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="getRecordList">搜索</el-button>
            <el-button icon="Refresh" @click="resetRecordQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="recordLoading" :data="recordList">
          <el-table-column label="规则名称" prop="ruleName" min-width="140" />
          <el-table-column label="预警消息" prop="alertMessage" :show-overflow-tooltip="true" min-width="250" />
          <el-table-column label="实际值" prop="actualValue" width="100" />
          <el-table-column label="阈值" prop="thresholdValue" width="100" />
          <el-table-column label="预警级别" width="90" align="center">
            <template #default="scope">
              <el-tag :type="levelTagType(scope.row.alertLevel)" size="small">{{ levelLabel(scope.row.alertLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警时间" width="160">
            <template #default="scope">{{ fmtTime(scope.row.alertTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="showRecordDetail(scope.row)">详情</el-button>
              <el-button link type="primary" icon="Check" @click="handleAlert(scope.row, 'confirmed')" v-if="scope.row.status === 'pending'">确认</el-button>
              <el-button link type="success" icon="CircleCheck" @click="handleAlert(scope.row, 'resolved')" v-if="scope.row.status !== 'resolved'">解决</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" @pagination="getRecordList" />
      </el-tab-pane>
    </el-tabs>

    <!-- ========== 新增/编辑预警规则对话框 ========== -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="650px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="预警名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入预警名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="数据源" prop="datasourceId">
          <el-select v-model="form.datasourceId" placeholder="请选择数据源" @change="onDatasourceChange">
            <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.name" :value="ds.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="监控表" prop="tableName">
          <el-input v-model="form.tableName" placeholder="请输入表名" maxlength="100" />
        </el-form-item>
        <el-form-item label="监控字段" prop="metricField">
          <el-input v-model="form.metricField" placeholder="如：total_amount（可选）" maxlength="100" />
        </el-form-item>
        <el-form-item label="检查SQL" prop="conditionSql">
          <el-input v-model="form.conditionSql" type="textarea" :rows="3" placeholder="如：SELECT SUM(total_amount) FROM demo_order" />
          <span style="font-size:12px;color:#999">应返回单个数值，用于和阈值比较</span>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="运算符" prop="comparisonOperator">
              <el-select v-model="form.comparisonOperator" placeholder="选择">
                <el-option label="大于 >" value=">" />
                <el-option label="大于等于 >=" value=">=" />
                <el-option label="小于 <" value="<" />
                <el-option label="小于等于 <=" value="<=" />
                <el-option label="等于 =" value="=" />
                <el-option label="不等于 !=" value="!=" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阈值" prop="thresholdValue">
              <el-input-number v-model="form.thresholdValue" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检查间隔" prop="checkInterval">
              <el-input-number v-model="form.checkInterval" :min="1" :max="1440" style="width:100%" /> 分钟
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AI分析">
              <el-switch v-model="form.analysisEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ========== 预警记录详情对话框 ========== -->
    <el-dialog title="预警详情" v-model="detailVisible" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="规则名称">{{ currentRecord.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="预警级别">
          <el-tag :type="levelTagType(currentRecord.alertLevel)">{{ levelLabel(currentRecord.alertLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实际值">{{ currentRecord.actualValue }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{ currentRecord.thresholdValue }} {{ currentRecord.comparisonOperator }}</el-descriptions-item>
        <el-descriptions-item label="预警时间" :span="2">{{ fmtTime(currentRecord.alertTime) }}</el-descriptions-item>
        <el-descriptions-item label="预警消息" :span="2">{{ currentRecord.alertMessage }}</el-descriptions-item>
        <el-descriptions-item label="检查SQL" :span="2">
          <pre style="margin:0;font-size:12px;max-height:120px;overflow:auto">{{ currentRecord.checkSql }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRecord && currentRecord.analysisResult" style="margin-top:16px">
        <el-divider content-position="left">AI分析结果</el-divider>
        <div style="background:#f5f7fa;padding:12px;border-radius:4px;white-space:pre-wrap;font-size:13px;line-height:1.6">{{ currentRecord.analysisResult }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="BiAlert">
import { listAlertRule, getAlertRule, addAlertRule, updateAlertRule, delAlertRule, listAlertRecord, getAlertRecord, handleAlertRecord, manualCheck, alertStats } from '@/api/bi/alert'
import { listDatasource } from '@/api/bi/datasource'

const { proxy } = getCurrentInstance()

// Tab 状态
const activeTab = ref('rule')

// ========== 预警统计概览 ==========
const stats = reactive({ pending: 0, today: 0, critical: 0, resolved: 0, total: 0 })

function loadStats() {
  alertStats().then(res => {
    Object.assign(stats, res.data || {})
  }).catch(() => {})
}

// ========== 预警规则 ==========
const ruleList = ref([])
const loading = ref(false)
const total = ref(0)
const showSearch = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const datasourceOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: null, tableName: null, status: null },
  rules: {
    name: [{ required: true, message: '请输入预警名称', trigger: 'blur' }],
    datasourceId: [{ required: true, message: '请选择数据源', trigger: 'change' }],
    tableName: [{ required: true, message: '请输入监控表名', trigger: 'blur' }],
    conditionSql: [{ required: true, message: '请输入检查SQL', trigger: 'blur' }],
    comparisonOperator: [{ required: true, message: '请选择运算符', trigger: 'change' }],
    thresholdValue: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)
const queryFormRef = ref(null)
const formRef = ref(null)

function getList() {
  loading.value = true
  listAlertRule(queryParams.value).then(response => {
    ruleList.value = response.rows
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

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增预警规则'
  dialogVisible.value = true
  form.value.status = 1
  form.value.analysisEnabled = 1
  form.value.checkInterval = 60
}

function handleUpdate(row) {
  resetForm()
  dialogTitle.value = '修改预警规则'
  getAlertRule(row.id).then(response => {
    form.value = response.data
    dialogVisible.value = true
  })
}

function resetForm() {
  form.value = {}
  formRef.value?.resetFields()
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitting.value = true
    if (form.value.id != null) {
      updateAlertRule(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        dialogVisible.value = false
        getList()
      }).finally(() => submitting.value = false)
    } else {
      addAlertRule(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        dialogVisible.value = false
        getList()
      }).finally(() => submitting.value = false)
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除预警规则 "' + row.name + '" ？').then(() => {
    return delAlertRule(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  })
}

function handleStatusChange(row) {
  updateAlertRule({ id: row.id, status: row.status }).then(() => {
    proxy.$modal.msgSuccess('状态更新成功')
  })
}

function onDatasourceChange() { /* 可选：加载表结构 */ }

function handleManualCheck() {
  proxy.$modal.confirm('将立即扫描所有启用的预警规则，该操作可能需要一些时间，确认继续？').then(() => {
    proxy.$modal.loading('正在执行检查...')
    manualCheck().then(response => {
      proxy.$modal.msgSuccess(response.msg || '检查完成')
      // 切到记录tab并刷新
      activeTab.value = 'record'
      getRecordList()
    }).catch(error => {
      proxy.$modal.msgError('检查执行失败：' + (error.message || '请求超时或异常'))
    }).finally(() => {
      proxy.$modal.closeLoading()
    })
  })
}

// ========== 预警记录 ==========
const recordList = ref([])
const recordLoading = ref(false)
const recordTotal = ref(0)
const recordQuery = reactive({ pageNum: 1, pageSize: 10, alertLevel: null, status: null })

function getRecordList() {
  recordLoading.value = true
  listAlertRecord(recordQuery).then(response => {
    recordList.value = response.rows
    recordTotal.value = response.total
    recordLoading.value = false
  })
}

function resetRecordQuery() {
  recordQuery.pageNum = 1
  recordQuery.alertLevel = null
  recordQuery.status = null
  getRecordList()
}

// 记录详情
const detailVisible = ref(false)
const currentRecord = ref(null)

function showRecordDetail(row) {
  getAlertRecord(row.id).then(response => {
    currentRecord.value = response.data
    detailVisible.value = true
  })
}

function handleAlert(row, status) {
  const label = status === 'confirmed' ? '确认' : '解决'
  proxy.$modal.confirm('确认' + label + '该预警？').then(() => {
    handleAlertRecord({ id: row.id, status: status }).then(() => {
      proxy.$modal.msgSuccess('处理成功')
      getRecordList()
      currentRecord.value = null
    })
  })
}

// 时间格式化（LocalDateTime 经 Jackson 序列化为 ISO 字符串，统一格式化展示）
function fmtTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  const p = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' +
    p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
}

// 标签类型映射
function levelTagType(level) {
  const map = { critical: 'danger', warning: 'warning', info: 'info' }
  return map[level] || 'info'
}

function levelLabel(level) {
  const map = { critical: '严重', warning: '警告', info: '提示' }
  return map[level] || level
}

function statusTagType(status) {
  const map = { pending: 'warning', confirmed: 'primary', resolved: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待处理', confirmed: '已确认', resolved: '已解决' }
  return map[status] || status
}

// 加载数据源
function loadDatasources() {
  listDatasource({ pageNum: 1, pageSize: 100, status: 1 }).then(response => {
    datasourceOptions.value = response.rows || []
  })
}

// 初始化
loadDatasources()
getList()
getRecordList()
loadStats()
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stats-card {
  text-align: center;
  border-radius: 6px;
  transition: all 0.2s;
}
.stats-card .stats-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.stats-card .stats-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.stats-card.pending .stats-value { color: #e6a23c; }
.stats-card.today .stats-value { color: #409eff; }
.stats-card.critical .stats-value { color: #f56c6c; }
.stats-card.resolved .stats-value { color: #67c23a; }
</style>
