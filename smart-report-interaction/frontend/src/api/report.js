import request from './request'

export const saveSpeech = (data) => request.post('/meeting/speech/save', data)
export const updateSpeech = (id, content, keyPoints) =>
  request.put(`/meeting/speech/${id}`, { content, keyPoints })
export const getSpeechList = (meetingId) => request.get(`/meeting/speech/list/${meetingId}`)
export const saveSummary = (meetingId, summary) =>
  request.post('/meeting/summary/save', { meetingId, summary })
export const getSummary = (meetingId) => request.get(`/meeting/summary/${meetingId}`)
