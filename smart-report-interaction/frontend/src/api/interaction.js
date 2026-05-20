import request from './request'

export const sendMessage = (data) => request.post('/meeting/interaction/message', data)
export const replyMessage = (id, reply) =>
  request.post(`/meeting/interaction/reply/${id}`, { reply })
export const getInteractionList = (meetingId, type) =>
  request.get(`/meeting/interaction/list/${meetingId}`, { params: { type } })
export const getStats = (meetingId) =>
  request.get(`/meeting/interaction/stats/${meetingId}`)
