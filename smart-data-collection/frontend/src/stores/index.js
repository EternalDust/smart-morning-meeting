import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'
import { getCurrentUserId, getCurrentUserName } from '@/utils/jwt'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    // 统一角色模型：工号 2 开头=管理员、1 开头=参会人，从 JWT 的 sub 解出工号
    userId: getCurrentUserId(),
    userName: getCurrentUserName(),
    userInfo: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    // 统一从工号前缀判断管理员，与后端一致（兼容 userInfo.role）
    isAdmin: (state) => {
      if (state.userInfo?.role === 'admin' || state.userInfo?.role === 'manager') return true
      return String(state.userId || '').startsWith('2')
    },
    role: (state) => {
      if (state.userInfo?.role) return state.userInfo.role
      return String(state.userId || '').startsWith('2') ? 'admin' : 'operator'
    }
  },

  actions: {
    async login(username, password) {
      const res = await login(username, password)
      this.token = res.token
      this.userInfo = res.data
      this.userId = username
      localStorage.setItem('token', this.token)
      return res
    },

    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data.data
      // 从 JWT 兜底解出工号（统一门户注入的 token 可能无 /info 返回值差异）
      const uid = getCurrentUserId()
      if (uid) this.userId = uid
    },

    async logout() {
      await logout()
      this.token = ''
      this.userId = null
      this.userName = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
