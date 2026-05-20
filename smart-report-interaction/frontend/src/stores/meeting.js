import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMeetingStore = defineStore('meeting', () => {
  const currentMeeting = ref({
    id: 1,
    title: '周一科室晨会',
    time: '08:30–09:30',
    location: '会议室A',
    status: '进行中'
  })
  return { currentMeeting }
})
