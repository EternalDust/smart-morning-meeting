<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>实时互动</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
      <span v-if="wsConnected" class="ws-status">● 已连接</span>
    </div>

    <div class="content">
      <div class="left-panel">
        <div class="filter-bar">
          <el-button-group>
            <el-button :type="filter===0?'primary':''" size="small" @click="filter=0">全部</el-button>
            <el-button :type="filter===1?'primary':''" size="small" @click="filter=1">提问</el-button>
            <el-button :type="filter===2?'primary':''" size="small" @click="filter=2">反馈</el-button>
            <el-button :type="filter===3?'primary':''" size="small" @click="filter=3">投票</el-button>
          </el-button-group>
        </div>

        <div class="message-stream">
          <div v-for="m in filteredMessages" :key="m.id" class="message">
            <el-avatar :size="26">{{ getUserName(m.userId).charAt(0) }}</el-avatar>
            <div class="msg-body">
              <div class="msg-meta">
                <strong>{{ getUserName(m.userId) }}</strong>
                <span>{{ m.createTime }}</span>
                <el-tag :type="typeTag(m.interactType)" size="small">
                  {{ typeText(m.interactType) }}
                </el-tag>
              </div>
              <div class="msg-content">{{ m.content }}</div>
              <div v-if="m.reply" class="msg-reply">
                <span class="reply-label">主持人回复</span>
                {{ m.reply }}
              </div>
            </div>
            <el-button v-if="!m.reply" link type="primary" size="small" @click="replyTo(m)">回复</el-button>
          </div>
        </div>

        <div class="compose">
          <div class="compose-type">
            <el-button-group size="small">
              <el-button :type="composeType===1?'primary':''" @click="composeType=1">提问</el-button>
              <el-button :type="composeType===2?'primary':''" @click="composeType=2">反馈</el-button>
              <el-button :type="composeType===3?'primary':''" @click="composeType=3">发起投票</el-button>
            </el-button-group>
          </div>
          <label class="field-label">你的工号</label>
          <el-input v-model="userId" placeholder="输入你的工号" style="margin-bottom:6px" />

          <label class="field-label">输入内容</label>
          <div class="compose-input">
            <el-input v-model="msgContent" placeholder="输入互动内容..." size="large" @keyup.enter="send" />
            <el-button type="primary" size="large" @click="send">发送</el-button>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="box">
          <h3>进行中的投票</h3>
          <div class="vote-list" v-if="activeVotes.length">
            <div v-for="v in activeVotes" :key="v.id" class="vote-card">
              <div class="vote-title">{{ v.title }}</div>
              <div class="vote-meta">已投 15/15 · 剩余 2 分钟</div>
            </div>
          </div>
          <div v-else class="empty">暂无进行中的投票</div>
        </div>

        <div class="box">
          <h3>互动统计</h3>
          <div class="stats-grid">
            <div class="s-card s-primary"><div class="s-num">{{ stats.questions }}</div><div class="s-label">提问</div></div>
            <div class="s-card s-purple"><div class="s-num">{{ stats.feedback }}</div><div class="s-label">反馈</div></div>
            <div class="s-card s-warn"><div class="s-num">{{ stats.votes }}</div><div class="s-label">投票</div></div>
            <div class="s-card s-success"><div class="s-num">{{ stats.replied }}</div><div class="s-label">已回复</div></div>
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
import { useWebSocket } from '../composables/useWebSocket'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const { connected: wsConnected, lastMessage, connect } = useWebSocket(meeting.id)
connect()

const filter = ref(0)
const composeType = ref(1)
const userId = ref('')
const msgContent = ref('')
const messages = ref([])
const nameMap = ref({})
const stats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })
const activeVotes = ref([])

const filteredMessages = computed(() =>
  filter.value === 0 ? messages.value : messages.value.filter(m => m.interactType === filter.value)
)

const loadData = async () => {
  const res = await getInteractionList(meeting.id, filter.value || undefined)
  messages.value = res.data.messages || []
  nameMap.value = res.data.nameMap || {}
  const s = await getStats(meeting.id)
  if (s.data) Object.assign(stats, s.data)
}

const getUserName = (uid) => nameMap.value[uid] || uid

const send = async () => {
  if (!msgContent.value) { ElMessage.warning('请输入内容'); return }
  if (!userId.value) { ElMessage.warning('请输入工号'); return }
  try {
    await sendMessage({ meetingId: meeting.id, userId: userId.value, content: msgContent.value, interactType: composeType.value })
    ElMessage.success('已发送')
    msgContent.value = ''
    await loadData()
  } catch (e) {
    // error already shown by interceptor
  }
}

const replyTo = async (m) => {
  const reply = prompt('输入回复内容：')
  if (reply) {
    await replyMessage(m.id, reply)
    await loadData()
  }
}

const typeTag = (t) => t === 1 ? '' : t === 2 ? 'info' : 'warning'
const typeText = (t) => t === 1 ? '提问' : t === 2 ? '反馈' : '投票'

watch(lastMessage, () => { if (lastMessage.value) loadData() })
watch(filter, loadData)
onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:10px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.ws-status { font-size:11px; color:var(--s) }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { flex:2; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.filter-bar { margin-bottom:10px; flex-shrink:0 }
.message-stream { flex:1; overflow-y:auto }
.message { display:flex; align-items:flex-start; gap:8px; padding:10px 0; border-bottom:1px solid var(--bd) }
.msg-body { flex:1; min-width:0 }
.msg-meta { display:flex; align-items:center; gap:8px; margin-bottom:4px }
.msg-meta strong { font-size:13px }
.msg-meta span { font-size:11px; color:var(--ts) }
.msg-content { background:var(--bg); padding:8px 12px; border-radius:6px; font-size:13px; margin-top:4px }
.msg-reply { margin-top:4px; margin-left:8px; border-left:2px solid var(--p); padding:4px 8px; font-size:12px }
.reply-label { font-size:10px; color:var(--p); font-weight:500; display:block }
.compose { flex-shrink:0; margin-top:10px; padding-top:10px; border-top:1px solid var(--bd) }
.compose-type { margin-bottom:6px }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.compose-input { display:flex; gap:8px }
.right-panel { width:240px; flex-shrink:0; display:flex; flex-direction:column; gap:10px }
.box { background:#fff; border:1px solid var(--bd); border-radius:8px; padding:14px }
.box h3 { font-size:14px; margin-bottom:8px }
.vote-card { border:1px solid var(--bd); border-radius:6px; padding:8px 10px; margin-bottom:6px }
.vote-title { font-weight:500; font-size:13px }
.vote-meta { font-size:11px; color:var(--ts) }
.empty { font-size:12px; color:var(--ts); text-align:center; padding:20px 0 }
.stats-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px }
.s-card { text-align:center; padding:8px; border-radius:6px }
.s-primary { background:var(--pb) } .s-primary .s-num { color:var(--p) }
.s-purple { background:#F3E8FF } .s-purple .s-num { color:#7C3AED }
.s-warn { background:var(--wb) } .s-warn .s-num { color:var(--w) }
.s-success { background:var(--sb) } .s-success .s-num { color:var(--s) }
.s-num { font-size:20px; font-weight:700 }
.s-label { font-size:11px; color:var(--ts) }
</style>
