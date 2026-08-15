import { ref } from 'vue'
import { defineStore } from 'pinia'
import request from '../api/request.js'

export const useMeetingStore = defineStore('meeting', () => {
    const meetingList = ref([])
    const loading = ref(false)

    async function loadMeetings() {
        loading.value = true
        try {
            meetingList.value = await request.get('/agenda/meetings')
        } catch (e) {
            meetingList.value = []
        } finally {
            loading.value = false
        }
    }

    function clear() {
        meetingList.value = []
    }

    return { meetingList, loading, loadMeetings, clear }
})