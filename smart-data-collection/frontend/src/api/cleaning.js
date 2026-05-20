import request from './index'

export function listCleaningRules() {
  return request.get('/cleaning-rule/list')
}

export function addCleaningRule(data) {
  return request.post('/cleaning-rule/add', data)
}

export function updateCleaningRule(id, data) {
  return request.put(`/cleaning-rule/${id}`, data)
}

export function deleteCleaningRule(id) {
  return request.delete(`/cleaning-rule/${id}`)
}

export function toggleCleaningRule(id) {
  return request.post(`/cleaning-rule/${id}/toggle`)
}

export function triggerCleaning(type = 'batch') {
  return request.post('/cleaning/trigger', null, { params: { type } })
}

export function getCleaningStats() {
  return request.get('/cleaning/stats')
}

export function getQualityTrend(days = 7) {
  return request.get('/cleaning/quality-trend', { params: { days } })
}
