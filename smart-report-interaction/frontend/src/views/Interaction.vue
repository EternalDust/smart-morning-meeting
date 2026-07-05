<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>实时互动</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
      <span v-if="wsConnected" class="ws-dot">● 在线</span>
    </div>

    <div class="content">
      <div class="chat-area">
        <div class="chat-filters">
          <el-button size="small" :type="filter===0?'primary':''" @click="filter=0">全部</el-button>
          <el-button size="small" :type="filter===1?'primary':''" @click="filter=1">提问</el-button>
          <el-button size="small" :type="filter===2?'primary':''" @click="filter=2">反馈</el-button>
          <el-button size="small" :type="filter===3?'primary':''" @click="filter=3">投票</el-button>
        </div>

        <div class="chat-stream">
          <div v-for="m in filteredMessages" :key="m.id" class="chat-msg">
            <el-avatar :size="28" style="flex-shrink:0">{{ getInterName(m.userId).charAt(0) }}</el-avatar>
            <div class="chat-body">
              <div class="chat-hd">
                <strong>{{ getInterName(m.userId) }}</strong>
                <el-tag size="small" :type="m.interactType===1?'':m.interactType===2?'info':'warning'">{{ ['','提问','反馈','投票'][m.interactType] }}</el-tag>
                <span class="chat-time">{{ m.createTime }}</span>
              </div>
              <div class="chat-content">{{ m.content }}</div>
              <div v-if="m.reply" class="chat-reply"><span>回复</span>{{ m.reply }}</div>
            </div>
            <el-button v-if="!m.reply && isAdmin" link type="primary" size="small" @click="replyTo(m)">回复</el-button>
          </div>
        </div>

        <div class="chat-compose">
          <div class="compose-type">
            <el-button size="small" :type="composeType===1?'primary':''" @click="composeType=1">提问</el-button>
            <el-button size="small" :type="composeType===2?'primary':''" @click="composeType=2">反馈</el-button>
            <el-button size="small" :type="composeType===3?'primary':''" @click="composeType=3">发起投票</el-button>
          </div>
          <div class="compose-row">
            <span class="compose-as">{{ userName }}</span>
            <el-input v-model="msgContent" placeholder="输入互动内容..." size="small" @keyup.enter="send" />
            <el-button type="primary" size="small" @click="send">发送</el-button>
          </div>
        </div>
      </div>

      <div class="people-panel">
        <div class="pp-title">参会人员</div>
        <div class="pp-count">{{ Object.keys(nameMap).length }} 人在线</div>
        <div class="pp-list">
          <div class="pp-item" v-for="(name, uid) in nameMap" :key="uid" :class="{ host: String(uid).startsWith('2') }">
            <el-avatar :size="24" style="flex-shrink:0;font-size:11px">{{ name.charAt(0) }}</el-avatar>
            <span>{{ name }}</span>
            <el-tag v-if="String(uid).startsWith('2')" size="small" type="warning" style="margin-left:auto">主持人</el-tag>
          </div>
        </div>
        <div class="inter-stats-grid">
          <div><span class="is-num">{{ interStats.questions }}</span>提问</div>
          <div><span class="is-num">{{ interStats.feedback }}</span>反馈</div>
          <div><span class="is-num">{{ interStats.votes }}</span>投票</div>
          <div><span class="is-num">{{ interStats.replied }}</span>已回复</div>
        </div>

        <div v-if="timePattern.length" style="margin-top:10px;padding-top:10px;border-top:1px solid var(--bd);font-size:11px">
          <div style="font-weight:600;margin-bottom:4px">时段准时率</div>
          <div v-for="t in timePattern" :key="t.period" style="display:flex;justify-content:space-between;padding:2px 0">
            <span>{{ t.period }}</span>
            <span :style="{color: t.punctualRate > 60 ? 'var(--s)' : 'var(--d)', fontWeight:'700'}">{{ t.punctualRate }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMessage, replyMessage, getInteractionList, getStats } from '../api/interaction'
import { getTimePattern } from '../api/analytics'
import { useWebSocket } from '../composables/useWebSocket'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const { connected: wsConnected, lastMessage, connect } = useWebSocket(meeting.id)
connect()

const userName = computed(() => userStore.userName || '参会用户')
const isAdmin = computed(() => String(userStore.userId).startsWith('2'))
const filter = ref(0)
const composeType = ref(1)
const msgContent = ref('')
const messages = ref([])
const nameMap = ref({})
const timePattern = ref([])
const interStats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })

const filteredMessages = computed(() =>
  filter.value === 0 ? messages.value : messages.value.filter(m => m.interactType === filter.value)
)

const loadData = async () => {
  const [list, st] = await Promise.all([
    getInteractionList(meeting.id, filter.value || undefined),
    getStats(meeting.id)
  ])
  messages.value = list.data.messages || []
  nameMap.value = list.data.nameMap || {}
  if (st.data) Object.assign(interStats, st.data)
  try { const tp = await getTimePattern(); timePattern.value = tp.data || [] } catch {}
}

const getInterName = (uid) => nameMap.value[uid] || uid

const send = async () => {
  if (!msgContent.value) { ElMessage.warning('请输入内容'); return }
  try {
    await sendMessage({ meetingId: meeting.id, userId: userStore.userId, content: msgContent.value, interactType: composeType.value })
    ElMessage.success('已发送')
    msgContent.value = ''
    await loadData()
  } catch {}
}

const replyTo = async (m) => {
  const reply = prompt('回复内容：')
  if (reply) { await replyMessage(m.id, reply); await loadData() }
}

watch(lastMessage, () => loadData())
watch(filter, () => loadData())
onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px; height:100%; overflow:hidden }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:16px; flex-shrink:0 }
.top-bar h2 { font-size:18px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.ws-dot { font-size:11px; color:var(--s) }
.content { flex:1; display:flex; gap:16px; min-height:0; overflow:hidden }
.chat-area { flex:1; display:flex; flex-direction:column; min-height:0; overflow:hidden }
.chat-filters { display:flex; gap:4px; margin-bottom:10px; flex-shrink:0 }
.chat-stream { flex:1; overflow-y:auto; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:10px }
.chat-msg { display:flex; gap:10px; padding:10px 0; border-bottom:1px solid #F1F5F9 }
.chat-msg:last-child { border-bottom:none }
.chat-body { flex:1; min-width:0 }
.chat-hd { display:flex; align-items:center; gap:6px; margin-bottom:4px; font-size:12px }
.chat-time { font-size:11px; color:var(--ts); margin-left:auto }
.chat-content { font-size:13px; color:#1E293B; margin:4px 0 }
.chat-reply { margin-top:4px; padding:4px 8px; background:var(--pb); border-radius:4px; font-size:12px }
.chat-reply span { color:var(--p); font-weight:600; margin-right:6px }
.chat-compose { flex-shrink:0; margin-top:10px }
.compose-type { display:flex; gap:4px; margin-bottom:6px }
.compose-row { display:flex; align-items:center; gap:8px }
.compose-as { font-size:12px; color:var(--p); font-weight:600; white-space:nowrap }
.people-panel { width:220px; flex-shrink:0; background:#fff; border:1px solid var(--bd); border-radius:10px; padding:14px; display:flex; flex-direction:column }
.pp-title { font-size:14px; font-weight:600; margin-bottom:4px }
.pp-count { font-size:12px; color:var(--ts); margin-bottom:10px }
.pp-list { flex:1; overflow-y:auto; display:flex; flex-direction:column; gap:6px }
.pp-item { display:flex; align-items:center; gap:8px; font-size:12px }
.pp-item.host { font-weight:600 }
.inter-stats-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px; margin-top:10px; padding-top:10px; border-top:1px solid var(--bd); font-size:11px; color:var(--ts) }
.is-num { font-weight:700; color:#1E293B; font-size:14px; display:block }
</style>
