import request from '@/utils/request'

// 查询知识库列表
export function listKnowledge(query) {
  return request({
    url: '/bi/knowledge/list',
    method: 'get',
    params: query
  })
}

// 查询知识库详细
export function getKnowledge(id) {
  return request({
    url: '/bi/knowledge/' + id,
    method: 'get'
  })
}

// 新增知识库
export function addKnowledge(data) {
  return request({
    url: '/bi/knowledge',
    method: 'post',
    data: data
  })
}

// 修改知识库
export function updateKnowledge(data) {
  return request({
    url: '/bi/knowledge',
    method: 'put',
    data: data
  })
}

// 删除知识库
export function delKnowledge(id) {
  return request({
    url: '/bi/knowledge/' + id,
    method: 'delete'
  })
}

// 向量相似度检索
export function searchKnowledge(data) {
  return request({
    url: '/bi/knowledge/search',
    method: 'post',
    data: data
  })
}

// 重新向量化
export function reEmbedKnowledge(id) {
  return request({
    url: '/bi/knowledge/reembed/' + id,
    method: 'post'
  })
}

// 批量重新向量化
export function batchReEmbed() {
  return request({
    url: '/bi/knowledge/reembed/batch',
    method: 'post'
  })
}
