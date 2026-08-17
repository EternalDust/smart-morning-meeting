/**
 * 当前登录身份：从 JWT 的 sub（工号）解析，
 * 演示模式（非 JWT token）回退到 localStorage.account。
 */
export const getAccount = () => {
  const token = localStorage.getItem('token')
  if (token) {
    const parts = token.split('.')
    if (parts.length === 3) {
      try {
        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
        if (payload.sub) return payload.sub
      } catch (e) {
        /* 非 JWT token，忽略 */
      }
    }
  }
  return localStorage.getItem('account') || ''
}

/**
 * 管理员（工号 2 开头）承担督办专员职责
 */
export const isAdmin = () => getAccount().startsWith('2')
