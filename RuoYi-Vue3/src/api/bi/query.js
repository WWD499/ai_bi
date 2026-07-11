import request from '@/utils/request'

// 自然语言查询
export function naturalLanguageQuery(data) {
  return request({
    url: '/bi/query',
    method: 'post',
    data: data,
    timeout: 60000
  })
}

// 测试LLM连接
export function testLlm() {
  return request({
    url: '/bi/query/test-llm',
    method: 'get'
  })
}
