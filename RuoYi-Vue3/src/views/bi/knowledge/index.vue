<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务领域" prop="businessDomain">
        <el-input v-model="queryParams.businessDomain" placeholder="如：财务、销售" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="全部" clearable style="width: 130px">
          <el-option label="手动录入" value="manual" />
          <el-option label="OCR识别" value="ocr" />
          <el-option label="文件上传" value="file" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Refresh" @click="handleBatchReEmbed" :loading="batchEmbedding">批量向量化</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="knowledgeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="标题" align="left" prop="title" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="内容预览" align="left" :show-overflow-tooltip="true" min-width="220">
        <template #default="scope">
          <span class="content-preview">{{ scope.row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column label="业务领域" align="center" prop="businessDomain" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.businessDomain" type="success" size="small">{{ scope.row.businessDomain }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="sourceType" width="90">
        <template #default="scope">
          <el-tag :type="getSourceTag(scope.row.sourceType)" size="small">{{ getSourceLabel(scope.row.sourceType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="切片" align="center" width="70">
        <template #default="scope">
          {{ scope.row.chunkIndex != null ? scope.row.chunkIndex + '/' + (scope.row.totalChunks || 1) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="View" @click="handleViewContent(scope.row)">查看</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="knowledgeRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入知识内容，可以是业务术语解释、表结构说明、指标定义等" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务领域" prop="businessDomain">
              <el-input v-model="form.businessDomain" placeholder="如：财务、销售、库存" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源类型" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
                <el-option label="手动录入" value="manual" />
                <el-option label="OCR识别" value="ocr" />
                <el-option label="文件上传" value="file" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：月报,财务,利润" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 内容查看对话框 -->
    <el-dialog title="知识内容详情" v-model="contentVisible" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ viewContent.title }}</el-descriptions-item>
        <el-descriptions-item label="业务领域">{{ viewContent.businessDomain || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ getSourceLabel(viewContent.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">{{ viewContent.tags || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div class="full-content">{{ viewContent.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="BiKnowledge">
import { listKnowledge, getKnowledge, addKnowledge, updateKnowledge, delKnowledge, batchReEmbed, reEmbedKnowledge } from '@/api/bi/knowledge'

const { proxy } = getCurrentInstance()

const knowledgeList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const contentVisible = ref(false)
const batchEmbedding = ref(false)
const viewContent = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    businessDomain: undefined,
    sourceType: undefined,
    status: undefined
  },
  rules: {
    title: [{ required: true, message: "标题不能为空", trigger: "blur" }],
    content: [{ required: true, message: "内容不能为空", trigger: "blur" }],
    sourceType: [{ required: true, message: "来源类型不能为空", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 来源标签颜色 */
function getSourceTag(type) {
  const map = { manual: '', ocr: 'warning', file: 'success' }
  return map[type] || ''
}

/** 来源中文名 */
function getSourceLabel(type) {
  const map = { manual: '手动录入', ocr: 'OCR识别', file: '文件上传' }
  return map[type] || type || '-'
}

/** 查询列表 */
function getList() {
  loading.value = true
  listKnowledge(queryParams.value).then(response => {
    knowledgeList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 重置表单 */
function reset() {
  form.value = {
    id: undefined,
    title: undefined,
    content: undefined,
    businessDomain: undefined,
    sourceType: 'manual',
    tags: undefined,
    status: 1
  }
  proxy.resetForm("knowledgeRef")
}

/** 新增 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增知识条目"
}

/** 修改 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getKnowledge(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改知识条目"
  })
}

/** 提交 */
function submitForm() {
  proxy.$refs["knowledgeRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateKnowledge(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addKnowledge(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除 */
function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除知识编号为"' + delIds + '"的数据项？').then(() => {
    return delKnowledge(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 查看内容 */
function handleViewContent(row) {
  viewContent.value = row
  contentVisible.value = true
}

/** 批量重新向量化 */
function handleBatchReEmbed() {
  proxy.$modal.confirm('确认对全部知识条目重新进行向量化？此操作可能耗时较长。').then(() => {
    batchEmbedding.value = true
    return batchReEmbed()
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || '批量向量化完成')
    getList()
  }).catch(() => {}).finally(() => {
    batchEmbedding.value = false
  })
}

getList()
</script>

<style scoped>
.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: #606266;
}

.full-content {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
}
</style>
