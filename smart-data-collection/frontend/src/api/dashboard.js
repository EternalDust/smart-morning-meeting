import request from './index'

export function getOverview() {
  return request.get('/dashboard/overview')
}

export function getSourceStatus() {
  return request.get('/dashboard/source-status')
}

export function getQualityTrend(days = 7) {
  return request.get('/dashboard/quality-trend', { params: { days } })
}

export function getProcessingDelay() {
  return request.get('/dashboard/processing-delay')
}
