import request from '@/utils/request'

// ==================== 大屏配置 CRUD ====================

export function listDashboard(query) {
  return request({ url: '/bi/dashboard/list', method: 'get', params: query })
}

export function getDashboard(id) {
  return request({ url: '/bi/dashboard/' + id, method: 'get' })
}

export function addDashboard(data) {
  return request({ url: '/bi/dashboard', method: 'post', data: data })
}

export function updateDashboard(data) {
  return request({ url: '/bi/dashboard', method: 'put', data: data })
}

export function delDashboard(id) {
  return request({ url: '/bi/dashboard/' + id, method: 'delete' })
}

// ==================== Widget 数据查询 ====================

export function queryWidgetData(data) {
  // timeout=30s; repeatSubmit=false 避免并发加载多个widget时被防重复提交拦截器误拦
  return request({
    url: '/bi/dashboard/widget/data',
    method: 'post',
    data: data,
    timeout: 30000,
    headers: { repeatSubmit: false }
  })
}

export function getTableSchemas(datasourceId) {
  return request({ url: '/bi/dashboard/tables/' + datasourceId, method: 'get' })
}
