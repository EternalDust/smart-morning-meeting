import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../api/request.js'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(null)

    function setToken(t) {
        token.value = t
        localStorage.setItem('token', t)
    }

    function clearUser() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
    }

    async function login(username, password) {
        const res = await request.post('/agenda/public/login', { username, password })
        setToken(res)
        return res
    }

    function logout() {
        clearUser()
        window.location.href = '/login'
    }

    return { token, userInfo, setToken, clearUser, login, logout }
})