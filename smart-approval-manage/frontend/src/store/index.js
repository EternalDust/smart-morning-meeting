import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import request from '../api/request.js'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(null)

    const isLoggedIn = computed(() => !!token.value)

    function setToken(val) {
        token.value = val
        localStorage.setItem('token', token.value || '')
    }

    function clearUser() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
    }

    async function fetchUserInfo() {
        if (!token.value) return
        try {
            const res = await request.get('/user/info')
            userInfo.value = res
        } catch (e) {
            clearUser()
        }
    }

    return { token, userInfo, isLoggedIn, setToken, clearUser, fetchUserInfo }
})