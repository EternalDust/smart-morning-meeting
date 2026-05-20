import request from './index'

export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}
