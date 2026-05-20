import request from './index'

export function getTableLineage(tableName) {
  return request.get(`/lineage/table/${tableName}`)
}

export function getRecordLineage(tableName, recordId) {
  return request.get(`/lineage/record/${tableName}/${recordId}`)
}

export function getLineageGraph(level = 'table') {
  return request.get('/lineage/graph', { params: { level } })
}
