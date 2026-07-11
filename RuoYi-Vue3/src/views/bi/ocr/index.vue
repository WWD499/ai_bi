<template>
  <div class="app-container">
    <!-- 健康检查状态条 -->
    <el-alert
      :type="healthStatus.type"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    >
      <template #default>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <span>{{ healthStatus.text }}</span>
          <el-button text size="small" @click="checkHealth" :loading="healthLoading">
            <el-icon><Refresh /></el-icon> 重新检测
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="20">
      <!-- 左侧：上传 + 配置 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>图片上传</span>
              <el-tag size="small" type="info">支持 PNG / JPG / BMP / PDF</el-tag>
            </div>
          </template>

          <!-- 上传区域 -->
          <el-upload
            ref="uploadRef"
            class="ocr-uploader"
            drag
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*,.pdf"
          >
            <div v-if="!previewUrl" class="upload-placeholder">
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                拖拽图片到此处，或<em>点击上传</em>
              </div>
            </div>
            <div v-else class="upload-preview">
              <img v-if="!isPdf" :src="previewUrl" class="preview-img" />
              <div v-else class="pdf-preview">
                <el-icon :size="60"><Document /></el-icon>
                <p>{{ currentFile?.name }}</p>
              </div>
            </div>
          </el-upload>

          <!-- 文件信息 -->
          <div v-if="currentFile" class="file-info">
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="文件名">{{ currentFile.name }}</el-descriptions-item>
              <el-descriptions-item label="大小">{{ formatFileSize(currentFile.size) }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ currentFile.raw?.type || '未知' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 识别选项 -->
          <el-form label-width="80px" style="margin-top: 16px">
            <el-form-item label="识别语言">
              <el-select v-model="language" style="width: 100%">
                <el-option label="简体中文" value="ch" />
                <el-option label="英语" value="en" />
                <el-option label="中英混合" value="ch+en" />
                <el-option label="日语" value="japan" />
                <el-option label="韩语" value="korean" />
              </el-select>
            </el-form-item>
          </el-form>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :icon="Position"
              :loading="recognizing"
              :disabled="!currentFile || healthStatus.type !== 'success'"
              @click="doRecognize"
            >
              {{ recognizing ? '识别中...' : '开始识别' }}
            </el-button>
            <el-button
              v-if="currentFile"
              size="large"
              @click="clearFile"
            >
              清除
            </el-button>
          </div>

          <!-- 识别进度提示 -->
          <div v-if="recognizing" class="recognize-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在调用 PaddleOCR 识别，首次加载模型需 30-60 秒，请耐心等待...</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：识别结果 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>识别结果</span>
              <div v-if="result">
                <el-tag size="small" type="success">耗时 {{ result.totalTimeMs }}ms</el-tag>
                <el-tag size="small" style="margin-left: 8px">{{ result.text ? result.text.length : 0 }} 字</el-tag>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty v-if="!result && !recognizing" description="上传图片后点击「开始识别」" />

          <!-- 识别中骨架屏 -->
          <div v-else-if="recognizing" class="skeleton-area">
            <el-skeleton :rows="6" animated />
          </div>

          <!-- 结果展示 -->
          <div v-else-if="result">
            <el-input
              v-model="result.text"
              type="textarea"
              :rows="12"
              readonly
              resize="vertical"
              placeholder="识别结果将显示在这里"
            />

            <!-- 操作栏 -->
            <div class="result-actions">
              <el-button :icon="CopyDocument" @click="copyText">复制文字</el-button>
              <el-button :icon="DocumentAdd" type="success" @click="saveToKnowledge">保存到知识库</el-button>
              <el-button :icon="Download" @click="downloadText">下载为TXT</el-button>
            </div>
          </div>
        </el-card>

        <!-- 识别历史 -->
        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>识别历史（本次会话）</span>
              <el-button text size="small" @click="clearHistory" v-if="history.length">
                清空历史
              </el-button>
            </div>
          </template>
          <el-table v-if="history.length" :data="history" size="small" stripe>
            <el-table-column label="时间" prop="time" width="100" />
            <el-table-column label="文件名" prop="filename" width="150" show-overflow-tooltip />
            <el-table-column label="语言" prop="language" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ langLabel(row.language) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="识别内容" prop="preview" show-overflow-tooltip />
            <el-table-column label="耗时" prop="timeMs" width="80" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="restoreResult(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无识别历史" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 保存到知识库对话框 -->
    <el-dialog title="保存到知识库" v-model="knowledgeDialogVisible" width="500px" append-to-body>
      <el-form :model="knowledgeForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="knowledgeForm.title" placeholder="如：XX报表OCR识别结果" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="knowledgeForm.content"
            type="textarea"
            :rows="6"
            readonly
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="knowledgeForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="业务域">
          <el-input v-model="knowledgeForm.businessDomain" placeholder="如：销售、库存、财务" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="knowledgeDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="doSaveKnowledge">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BiOcr">
import { recognize as apiRecognize, recognizeAdvanced, checkOcrHealth } from '@/api/bi/ocr'
import { addKnowledge } from '@/api/bi/knowledge'
import { UploadFilled, Position, CopyDocument, DocumentAdd, Download, Document, Refresh, Loading } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// === 健康检查 ===
const healthLoading = ref(false)
const healthStatus = ref({
  type: 'info',
  text: '正在检查 OCR 服务状态...'
})

function checkHealth() {
  healthLoading.value = true
  healthStatus.value.text = '正在检查 OCR 服务状态...'
  checkOcrHealth().then(response => {
    healthStatus.value = {
      type: 'success',
      text: 'OCR 服务正常 — ' + response.msg
    }
  }).catch(error => {
    healthStatus.value = {
      type: 'error',
      text: 'OCR 服务异常 — ' + (error.msg || error.message || 'Python 环境或脚本配置错误')
    }
  }).finally(() => {
    healthLoading.value = false
  })
}

// === 文件上传 ===
const uploadRef = ref()
const currentFile = ref(null)
const previewUrl = ref('')
const isPdf = ref(false)
const language = ref('ch')

function handleFileChange(file) {
  currentFile.value = file
  const raw = file.raw
  if (!raw) return

  // 释放旧预览
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)

  isPdf.value = raw.type === 'application/pdf'
  if (!isPdf.value) {
    previewUrl.value = URL.createObjectURL(raw)
  } else {
    previewUrl.value = 'pdf'
  }
}

function clearFile() {
  currentFile.value = null
  if (previewUrl.value && previewUrl.value !== 'pdf') {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

// === OCR 识别 ===
const recognizing = ref(false)
const result = ref(null)

function doRecognize() {
  if (!currentFile.value) {
    proxy.$modal.msgWarning('请先上传图片')
    return
  }

  recognizing.value = true
  result.value = null

  const file = currentFile.value.raw
  const apiCall = language.value === 'ch'
    ? apiRecognize(file)
    : recognizeAdvanced(file, language.value)

  apiCall.then(response => {
    result.value = response.data

    // 添加到历史
    addHistory({
      filename: currentFile.value.name,
      language: language.value,
      text: response.data.text,
      totalTimeMs: response.data.totalTimeMs,
      length: response.data.text ? response.data.text.length : 0
    })

    proxy.$modal.msgSuccess('识别完成，共 ' + (response.data.text ? response.data.text.length : 0) + ' 字')
  }).catch(error => {
    proxy.$modal.msgError('识别失败：' + (error.msg || error.message || '未知错误'))
  }).finally(() => {
    recognizing.value = false
  })
}

// === 结果操作 ===
function copyText() {
  if (!result.value?.text) return
  navigator.clipboard.writeText(result.value.text).then(() => {
    proxy.$modal.msgSuccess('已复制到剪贴板')
  }).catch(() => {
    proxy.$modal.msgError('复制失败')
  })
}

function downloadText() {
  if (!result.value?.text) return
  const blob = new Blob([result.value.text], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = (currentFile.value?.name || 'ocr_result').replace(/\.[^.]+$/, '') + '_ocr.txt'
  a.click()
  URL.revokeObjectURL(url)
}

// === 保存到知识库 ===
const knowledgeDialogVisible = ref(false)
const saving = ref(false)
const knowledgeForm = reactive({
  title: '',
  content: '',
  tags: '',
  businessDomain: ''
})

function saveToKnowledge() {
  if (!result.value?.text) return
  knowledgeForm.title = (currentFile.value?.name || 'OCR识别结果').replace(/\.[^.]+$/, '')
  knowledgeForm.content = result.value.text
  knowledgeForm.tags = ''
  knowledgeForm.businessDomain = ''
  knowledgeDialogVisible.value = true
}

function doSaveKnowledge() {
  if (!knowledgeForm.title) {
    proxy.$modal.msgWarning('请输入标题')
    return
  }
  saving.value = true
  addKnowledge({
    title: knowledgeForm.title,
    content: knowledgeForm.content,
    sourceType: 'ocr',
    tags: knowledgeForm.tags,
    businessDomain: knowledgeForm.businessDomain,
    status: 1
  }).then(() => {
    proxy.$modal.msgSuccess('已保存到知识库')
    knowledgeDialogVisible.value = false
  }).catch(error => {
    proxy.$modal.msgError('保存失败：' + (error.msg || error.message))
  }).finally(() => {
    saving.value = false
  })
}

// === 识别历史 ===
const history = ref([])

function addHistory(item) {
  const now = new Date()
  const timeStr = now.getHours().toString().padStart(2, '0') + ':' +
    now.getMinutes().toString().padStart(2, '0') + ':' +
    now.getSeconds().toString().padStart(2, '0')

  history.value.unshift({
    time: timeStr,
    filename: item.filename,
    language: item.language,
    preview: item.text.substring(0, 50) + (item.text.length > 50 ? '...' : ''),
    fullText: item.text,
    timeMs: item.totalTimeMs + 'ms'
  })

  // 最多保留 20 条
  if (history.value.length > 20) {
    history.value = history.value.slice(0, 20)
  }
}

function restoreResult(row) {
  result.value = {
    text: row.fullText,
    length: row.fullText.length,
    totalTimeMs: row.timeMs
  }
}

function clearHistory() {
  history.value = []
}

// === 辅助函数 ===
function langLabel(lang) {
  const map = { ch: '中文', en: '英文', 'ch+en': '中英', japan: '日语', korean: '韩语' }
  return map[lang] || lang
}

// === 初始化 ===
onMounted(() => {
  checkHealth()
})

// 清理
onUnmounted(() => {
  if (previewUrl.value && previewUrl.value !== 'pdf') {
    URL.revokeObjectURL(previewUrl.value)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ocr-uploader {
  width: 100%;
}

.ocr-uploader :deep(.el-upload-dragger) {
  width: 100%;
  padding: 20px;
}

.upload-placeholder {
  padding: 40px 0;
}

.upload-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.preview-img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
}

.pdf-preview {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 20px;
}

.file-info {
  margin-top: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.action-buttons .el-button {
  flex: 1;
}

.recognize-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  background: var(--el-color-primary-light-9);
  border-radius: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.skeleton-area {
  padding: 20px 0;
}

.result-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.result-actions .el-button {
  flex: 1;
}
</style>
