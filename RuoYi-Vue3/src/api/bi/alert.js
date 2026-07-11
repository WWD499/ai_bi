import request from '@/utils/request'

// ==================== 预警规则 ====================

export function listAlertRule(query) {
  return request({ url: '/bi/alert/rule/list', method: 'get', params: query })
}

export function getAlertRule(id) {
  return request({ url: '/bi/alert/rule/' + id, method: 'get' })
}

export function addAlertRule(data) {
  return request({ url: '/bi/alert/rule', method: 'post', data: data })
}

export function updateAlertRule(data) {
  return request({ url: '/bi/alert/rule', method: 'put', data: data })
}

export function delAlertRule(id) {
  return request({ url: '/bi/alert/rule/' + id, method: 'delete' })
}

// ==================== 预警记录 ====================

export function listAlertRecord(query) {
  return request({ url: '/bi/alert/record/list', method: 'get', params: query })
}

export function getAlertRecord(id) {
  return request({ url: '/bi/alert/record/' + id, method: 'get' })
}

export function handleAlertRecord(data) {
  return request({ url: '/bi/alert/record/handle', method: 'put', data: data })
}

// ==================== 手动执行检查 ====================

export function manualCheck() {
  return request({ url: '/bi/alert/check', method: 'post', timeout: 60000 })
}

// ==================== 统计概览 ====================

export function alertStats() {
  return request({ url: '/bi/alert/record/stats', method: 'get' })
}
