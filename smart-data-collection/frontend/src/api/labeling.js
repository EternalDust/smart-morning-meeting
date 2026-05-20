import request from './index'

export function detectAnomalies(data) {
  return request.post('/anomaly/detect', data)
}

export function listAnomalies(page = 1, size = 10, level) {
  return request.get('/anomaly/list', { params: { page, size, level } })
}

export function getAnomalyStats() {
  return request.get('/anomaly/stats')
}

export function handleAnomaly(id, handler, remark) {
  return request.put(`/anomaly/${id}/handle`, { handler, remark })
}

export function generateLabels(entityType) {
  return request.post('/label/generate', null, { params: { entityType } })
}

export function listLabels(entityType = 'doctor') {
  return request.get('/label/list', { params: { entityType } })
}

export function parseNlp(text, recordType = 'morning_meeting') {
  return request.post('/label/nlp/parse', { text, recordType })
}
