import request from '@/utils/request'

// 查询数据源列表
export function listDatasource(query) {
  return request({
    url: '/bi/datasource/list',
    method: 'get',
    params: query
  })
}

// 查询数据源详细
export function getDatasource(id) {
  return request({
    url: '/bi/datasource/' + id,
    method: 'get'
  })
}

// 新增数据源
export function addDatasource(data) {
  return request({
    url: '/bi/datasource',
    method: 'post',
    data: data
  })
}

// 修改数据源
export function updateDatasource(data) {
  return request({
    url: '/bi/datasource',
    method: 'put',
    data: data
  })
}

// 删除数据源
export function delDatasource(id) {
  return request({
    url: '/bi/datasource/' + id,
    method: 'delete'
  })
}

// 测试数据源连接
export function testDatasource(data) {
  return request({
    url: '/bi/datasource/test',
    method: 'post',
    data: data
  })
}

// 获取数据源当前库的表列表（预警规则级联选择）
export function listTables(id) {
  return request({
    url: '/bi/datasource/' + id + '/tables',
    method: 'get'
  })
}

// 获取指定表的字段列表（预警规则级联选择）
export function listColumns(id, table) {
  return request({
    url: '/bi/datasource/' + id + '/columns',
    method: 'get',
    params: { table }
  })
}
