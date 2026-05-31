import { ref } from 'vue'

const realtimeData = ref({
  attendanceRate: 0,
  resolutionRate: 0,
  riskLevel: 'NORMAL',
  timestamp: ''
})
const warnings = ref([])

let ws = null
let wsInitialized = false
let warnInterval = null

export function useRealtime() {
  const initWebSocket = () => {
    if (wsInitialized) return
    wsInitialized = true
    try {
      ws = new WebSocket('ws://localhost:8080/ws/morning-meeting')
      ws.onopen = () => console.log('已连接到 WebSocket')
      ws.onmessage = (event) => {
        try {
          realtimeData.value = JSON.parse(event.data)
        } catch (e) {
          console.error('WS 解析失败', e)
        }
      }
      ws.onclose = () => {
        console.log('WebSocket 断开')
        wsInitialized = false
      }
    } catch (e) {
      console.error('WebSocket 连接失败', e)
      wsInitialized = false
    }
  }

  const closeWebSocket = () => {
    if (ws) {
      ws.close()
      ws = null
      wsInitialized = false
    }
  }

  return {
    realtimeData,
    warnings,
    initWebSocket,
    closeWebSocket
  }
}
