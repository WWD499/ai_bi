<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="数据源名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入数据源名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 150px">
          <el-option label="MySQL" value="mysql" />
          <el-option label="PostgreSQL" value="postgresql" />
          <el-option label="Oracle" value="oracle" />
          <el-option label="SQLServer" value="sqlserver" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bi:datasource:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['bi:datasource:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['bi:datasource:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="datasourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="数据源名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="主机地址" align="center" :show-overflow-tooltip="true">
        <template #default="scope">
          {{ scope.row.host }}:{{ scope.row.port }}
        </template>
      </el-table-column>
      <el-table-column label="数据库" align="center" prop="databaseName" :show-overflow-tooltip="true" />
      <el-table-column label="用户名" align="center" prop="username" width="100" />
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
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bi:datasource:edit']">修改</el-button>
          <el-button link type="primary" icon="Connection" @click="handleTest(scope.row)">测试连接</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bi:datasource:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="datasourceRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="Oracle" value="oracle" />
            <el-option label="SQLServer" value="sqlserver" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" placeholder="如 localhost 或 192.168.1.100" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据库名" prop="databaseName">
          <el-input v-model="form.databaseName" placeholder="请输入数据库名称" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BiDatasource">
import { listDatasource, getDatasource, addDatasource, updateDatasource, delDatasource, testDatasource } from '@/api/bi/datasource'

const { proxy } = getCurrentInstance()

const datasourceList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const testing = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    type: undefined,
    status: undefined
  },
  rules: {
    name: [{ required: true, message: "数据源名称不能为空", trigger: "blur" }],
    type: [{ required: true, message: "数据库类型不能为空", trigger: "change" }],
    host: [{ required: true, message: "主机地址不能为空", trigger: "blur" }],
    port: [{ required: true, message: "端口不能为空", trigger: "blur" }],
    databaseName: [{ required: true, message: "数据库名不能为空", trigger: "blur" }],
    username: [{ required: true, message: "用户名不能为空", trigger: "blur" }],
    password: [{ required: true, message: "密码不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 类型标签颜色 */
function getTypeTag(type) {
  const map = { mysql: '', postgresql: 'success', oracle: 'warning', sqlserver: 'info' }
  return map[type] || ''
}

/** 查询列表 */
function getList() {
  loading.value = true
  listDatasource(queryParams.value).then(response => {
    datasourceList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置搜索 */
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
    name: undefined,
    type: 'mysql',
    host: 'localhost',
    port: 3306,
    databaseName: undefined,
    username: undefined,
    password: undefined,
    status: 1,
    remark: undefined
  }
  proxy.resetForm("datasourceRef")
}

/** 新增 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增数据源"
}

/** 修改 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getDatasource(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改数据源"
  })
}

/** 提交 */
function submitForm() {
  proxy.$refs["datasourceRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateDatasource(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDatasource(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除数据源编号为"' + delIds + '"的数据项？').then(() => {
    return delDatasource(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 测试连接 */
function handleTest(row) {
  testing.value = true
  testDatasource(row).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess("连接成功")
    } else {
      proxy.$modal.msgError("连接失败：" + response.msg)
    }
  }).catch(() => {
    proxy.$modal.msgError("连接失败")
  }).finally(() => {
    testing.value = false
  })
}

/** 端口默认值跟随类型 */
watch(() => form.value.type, (newType) => {
  if (newType === 'mysql') form.value.port = 3306
  else if (newType === 'postgresql') form.value.port = 5432
  else if (newType === 'oracle') form.value.port = 1521
  else if (newType === 'sqlserver') form.value.port = 1433
})

getList()
</script>
