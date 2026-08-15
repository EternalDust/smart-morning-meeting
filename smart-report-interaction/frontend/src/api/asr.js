import request from './request'

export const transcribeAudio = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/meeting/asr/transcribe', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
