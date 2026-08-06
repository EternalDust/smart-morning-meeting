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
        <div class="chat-stream" ref="chatStream">
          <div v-for="m in messages" :key="m.id" class="chat-msg">
            <el-avatar :size="28" style="flex-shrink:0">{{ getInterName(m.userId).charAt(0) }}</el-avatar>
            <div class="chat-body">
              <div class="chat-hd">
                <strong>{{ getInterName(m.userId) }}</strong>
                <el-tag v-if="m.interactType === 3" size="small" type="warning">投票</el-tag>
                <span class="chat-time">{{ m.createTime }}</span>
              </div>
              <div class="chat-content">{{ m.content }}</div>
              <div v-if="m.reply" class="chat-reply"><span>回复</span>{{ m.reply }}</div>
            </div>
            <div v-if="!m.reply && isAdmin" class="chat-actions">
              <el-button link type="primary" size="small" :loading="aiReplyingId === m.id" @click="aiReplyTo(m)">AI 答复</el-button>
              <el-button link type="primary" size="small" @click="replyTo(m)">回复</el-button>
            </div>
          </div>
        </div>

        <div class="chat-compose">
          <div class="compose-row">
            <span class="compose-as">{{ userName }}</span>
            <el-input v-model="msgContent" placeholder="输入互动内容..." size="large" @keyup.enter="send" style="flex:1" />
            <el-button type="primary" size="large" @click="send">发送</el-button>
            <el-button size="large" @click="showVoteDialog = true">投票</el-button>
          </div>
        </div>
      </div>

      <div class="people-panel">
        <div class="pp-title">参会人员</div>
        <div class="pp-count">{{ Object.keys(allAttendees).length }} 人在线</div>
        <div class="pp-list">
          <div class="pp-item" v-for="(name, uid) in allAttendees" :key="uid" :class="{ host: String(uid).startsWith('2') }">
            <el-avatar :size="24" style="flex-shrink:0;font-size:11px">{{ name.charAt(0) }}</el-avatar>
            <span>{{ name }}</span>
            <el-tag v-if="String(uid).startsWith('2')" size="small" type="warning" style="margin-left:auto">主持人</el-tag>
          </div>
        </div>
        <div v-if="timePattern.length" style="margin-top:8px;padding-top:8px;border-top:1px solid var(--bd);font-size:11px">
          <div style="font-weight:600;margin-bottom:4px">时段准时率</div>
          <div v-for="t in timePattern" :key="t.period" style="display:flex;justify-content:space-between;padding:2px 0">
            <span>{{ t.period }}</span>
            <span :style="{color: t.punctualRate > 60 ? 'var(--s)' : 'var(--d)', fontWeight:'700'}">{{ t.punctualRate }}%</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showVoteDialog" title="发起投票" width="360px">
      <el-input v-model="voteTitle" placeholder="投票标题" style="margin-bottom:10px" />
      <el-input v-model="voteOption" placeholder="添加选项" @keyup.enter="addVoteOption">
        <template #append><el-button @click="addVoteOption">添加</el-button></template>
      </el-input>
      <el-tag v-for="(o,i) in voteOptions" :key="i" closable @close="voteOptions.splice(i,1)" style="margin:4px">{{ o }}</el-tag>
      <template #footer>
        <el-button @click="showVoteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVote" :disabled="!voteTitle || voteOptions.length < 2">发起投票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMessage, replyMessage, aiAnswer, getInteractionList } from '../api/interaction'
import { getSignList } from '../api/sign'
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
const msgContent = ref('')
const messages = ref([])
const interNameMap = ref({})
const allAttendees = ref({})
const timePattern = ref([])
const chatStream = ref(null)

const showVoteDialog = ref(false)
const voteTitle = ref('')
const voteOption = ref('')
const voteOptions = ref([])

const loadData = async () => {
  try {
    const res = await getInteractionList(meeting.id)
    messages.value = res.data.messages || []
    interNameMap.value = res.data.nameMap || {}
  } catch {}
  try {
    const s = await getSignList(meeting.id)
    allAttendees.value = s.data.nameMap || {}
  } catch {}
  try { const tp = await getTimePattern(); timePattern.value = tp.data || [] } catch {}
}

const getInterName = (uid) => interNameMap.value[uid] || allAttendees.value[uid] || uid

const send = async () => {
  if (!msgContent.value) { ElMessage.warning('请输入内容'); return }
  try {
    await sendMessage({ meetingId: meeting.id, userId: userStore.userId || '9999', content: msgContent.value, interactType: 2 })
    ElMessage.success('已发送')
    msgContent.value = ''
    await loadData()
    nextTick(() => { if (chatStream.value) chatStream.value.scrollTop = chatStream.value.scrollHeight })
  } catch {}
}

const aiReplyingId = ref(null)

const aiReplyTo = async (m) => {
  aiReplyingId.value = m.id
  try {
    await aiAnswer(m.id)
    ElMessage.success('AI 初步答复已生成')
    await loadData()
  } catch {} finally { aiReplyingId.value = null }
}

const replyTo = async (m) => {
  const reply = prompt('回复内容：')
  if (reply) { await replyMessage(m.id, reply); await loadData() }
}

const addVoteOption = () => {
  if (voteOption.value) { voteOptions.value.push(voteOption.value); voteOption.value = '' }
}

const submitVote = async () => {
  const content = `【投票】${voteTitle.value}\n` + voteOptions.value.map((o, i) => `${i + 1}. ${o}`).join('\n')
  try {
    await sendMessage({ meetingId: meeting.id, userId: userStore.userId || '9999', content, interactType: 3 })
    ElMessage.success('投票已发起')
    showVoteDialog.value = false
    voteTitle.value = ''; voteOptions.value = []
    await loadData()
  } catch {}
}

watch(lastMessage, () => loadData())
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
.chat-stream { flex:1; overflow-y:auto; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:10px; margin-bottom:10px }
.chat-msg { display:flex; gap:10px; padding:10px 0; border-bottom:1px solid #F1F5F9 }
.chat-msg:last-child { border-bottom:none }
.chat-body { flex:1; min-width:0 }
.chat-hd { display:flex; align-items:center; gap:6px; margin-bottom:4px; font-size:12px }
.chat-time { font-size:11px; color:var(--ts); margin-left:auto }
.chat-content { font-size:13px; color:#1E293B; margin:4px 0; white-space:pre-wrap }
.chat-reply { margin-top:4px; padding:4px 8px; background:var(--pb); border-radius:4px; font-size:12px }
.chat-actions { display:flex; align-items:center; gap:2px; flex-shrink:0 }
.chat-reply span { color:var(--p); font-weight:600; margin-right:6px }
.chat-compose { flex-shrink:0; padding:12px; background:#fff; border:1px solid var(--bd); border-radius:8px }
.compose-row { display:flex; align-items:center; gap:10px }
.compose-as { font-size:13px; color:var(--p); font-weight:600; white-space:nowrap; min-width:60px }
.people-panel { width:200px; flex-shrink:0; background:#fff; border:1px solid var(--bd); border-radius:10px; padding:14px; display:flex; flex-direction:column }
.pp-title { font-size:14px; font-weight:600; margin-bottom:4px }
.pp-count { font-size:12px; color:var(--ts); margin-bottom:10px }
.pp-list { flex:1; overflow-y:auto; display:flex; flex-direction:column; gap:4px }
.pp-item { display:flex; align-items:center; gap:8px; font-size:12px }
.pp-item.host { font-weight:600 }
</style>
