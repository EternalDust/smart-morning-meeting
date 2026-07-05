import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUserId, getCurrentUserName } from '../utils/jwt'

export const useUserStore = defineStore('user', () => {
  const userId = ref(getCurrentUserId())
  const userName = ref(getCurrentUserName())

  const isLoggedIn = computed(() => !!userId.value)

  return { userId, userName, isLoggedIn }
})
