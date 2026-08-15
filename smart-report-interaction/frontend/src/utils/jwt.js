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
  return jwt ? (jwt.sub || jwt.userId || null) : null
}

export function getCurrentUserName() {
  return localStorage.getItem('userName') || ''
}
