import request from '@/utils/request'

// 上传图片识别文字（默认简体中文）
export function recognize(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/bi/ocr/recognize',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

// 上传图片识别文字（指定语言）
export function recognizeAdvanced(file, language) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('language', language)
  return request({
    url: '/bi/ocr/recognize-advanced',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

// 识别服务器本地图片文件
export function recognizeFile(filePath, language) {
  const formData = new FormData()
  formData.append('filePath', filePath)
  if (language) formData.append('language', language)
  return request({
    url: '/bi/ocr/recognize-file',
    method: 'post',
    data: formData,
    timeout: 120000
  })
}

// 健康检查（验证 Python 环境 + 脚本文件）
export function checkOcrHealth() {
  return request({
    url: '/bi/ocr/health',
    method: 'get'
  })
}
