import request from './request'

export const recognizeFace = (file, userId) => {
  const fd = new FormData()
  fd.append('file', file)
  if (userId) fd.append('userId', userId)
  return request.post('/meeting/face/recognize', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
