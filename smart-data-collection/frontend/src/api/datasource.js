import request from './index'

export function listDataSources(page = 1, size = 10) {
  return request.get('/datasource/list', { params: { page, size } })
}

export function addDataSource(data) {
  return request.post('/datasource/add', data)
}

export function updateDataSource(id, data) {
  return request.put(`/datasource/${id}`, data)
}

export function deleteDataSource(id) {
  return request.delete(`/datasource/${id}`)
}

export function testConnection(id) {
  return request.post(`/datasource/test/${id}`)
}
