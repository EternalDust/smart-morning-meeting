// 从 JWT 解析登录态（与统一 Shell/其他子系统一致：sub = 工号，工号 2 开头=管理员）
export function decodeJwt(token) {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

export function getCurrentUserId() {
  const token = localStorage.getItem('token')
  if (!token) return null
  const jwt = decodeJwt(token)
  return jwt ? (jwt.sub || jwt.username || null) : null
}

export function getCurrentUserName() {
  return localStorage.getItem('userName') || ''
}
