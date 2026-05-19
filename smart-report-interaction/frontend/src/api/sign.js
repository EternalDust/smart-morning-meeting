import request from './request'

export const signIn = (meetingId, userId, signType = 2) =>
  request.post('/meeting/sign/in', { meetingId, userId, signType })

export const getSignList = (meetingId) =>
  request.get(`/meeting/sign/list/${meetingId}`)
