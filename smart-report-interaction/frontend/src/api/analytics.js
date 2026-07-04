import request from './request'

export const getMeetingAnalytics = (meetingId) =>
  request.get(`/analytics/meeting/${meetingId}`)

export const getMeetingTrend = () =>
  request.get('/analytics/meetings/trend')

export const getDepartmentRanking = () =>
  request.get('/analytics/departments')

export const getMemberProfile = (userId) =>
  request.get(`/analytics/member/${userId}`)
