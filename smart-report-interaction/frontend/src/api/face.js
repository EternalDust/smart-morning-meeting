import request from './request'

export const recognizeFace = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/meeting/face/recognize', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
