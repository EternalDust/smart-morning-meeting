import { ref, onUnmounted } from 'vue'

export function useWebSocket(meetingId) {
  const connected = ref(false)
  const lastMessage = ref(null)
  let ws = null

  const connect = () => {
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    ws = new WebSocket(`${protocol}//${location.host}/api/meeting/realtime/push/${meetingId}`)
    ws.onopen = () => { connected.value = true }
    ws.onmessage = (e) => { lastMessage.value = JSON.parse(e.data) }
    ws.onclose = () => { connected.value = false }
  }

  const close = () => { if (ws) ws.close() }

  onUnmounted(close)

  return { connected, lastMessage, connect, close }
}
