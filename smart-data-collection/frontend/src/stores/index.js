import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.userInfo?.role || 'operator'
  },

  actions: {
    async login(username, password) {
      const res = await login(username, password)
      this.token = res.data.token
      this.userInfo = res.data.data
      localStorage.setItem('token', this.token)
      return res
    },

    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data.data
    },

    async logout() {
      await logout()
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
