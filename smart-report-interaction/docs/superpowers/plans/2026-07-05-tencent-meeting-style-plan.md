# 腾讯会议风格改造实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 三个页面改造为腾讯会议风格——删除所有工号输入框，身份来自 JWT 登录系统

**Architecture:** 复用现有 API 和 Store，仅改 Vue 模板和少量脚本逻辑

**Tech Stack:** Vue 3 + Element Plus + 现有 API/sign.js /report.js /interaction.js /analytics.js + QRCode

---

### Task 1: 改造签到页

**Files:**
- Modify: `smart-report-interaction/frontend/src/views/SignIn.vue`

完整替换为：

```vue
<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>{{ meeting.title }}</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }} · {{ meeting.location }}</span>
    </div>

    <div class="content">
      <div class="left-panel">
        <div class="user-card">
          <el-avatar :size="56" style="background:var(--p);font-size:24px">{{ userName.charAt(0) }}</el-avatar>
          <div class="user-name">{{ userName }}</div>
          <div class="user-role">{{ isAdmin ? '管理员' : '参会人员' }}</div>
        </div>

        <div class="quality-badge" v-if="meetingQuality">
          <div class="q-num">{{ meetingQuality.qualityScore }}</div>
          <div class="q-label">会议质量评分</div>
          <div class="q-meta">出勤 {{ meetingQuality.attendRate }}% · 发言 {{ meetingQuality.speechCount }} · 互动 {{ meetingQuality.interactionCount }}</div>
        </div>

        <el-button type="primary" size="large" @click="doSignIn" style="width:100%" :disabled="alreadySigned">
          {{ alreadySigned ? '已签到' : '一键签到' }}
        </el-button>
        <el-button size="small" @click="showQR = true" style="width:100%;margin-top:8px" v-if="isAdmin">分享签到二维码</el-button>
      </div>

      <div class="right-panel">
        <div class="stat-row">
          <div class="stat-box"><div class="sn green">{{ stats.normal }}</div><div>准时</div></div>
          <div class="stat-box"><div class="sn orange">{{ stats.late }}</div><div>迟到</div></div>
          <div class="stat-box"><div class="sn red">{{ stats.absent }}</div><div>缺席</div></div>
          <div class="stat-box"><div class="sn blue">{{ stats.shouldAttend }}</div><div>应到</div></div>
        </div>

        <div class="panel-hd">
          <h3>签到记录</h3>
          <span class="count">已签 {{ stats.signed }} / {{ stats.shouldAttend }}</span>
        </div>
        <div class="record-scroll">
          <div v-for="r in records" :key="r.id" class="sign-row">
            <el-avatar :size="28" style="flex-shrink:0">{{ getUserName(r.userId).charAt(0) }}</el-avatar>
            <span class="sign-name">{{ getUserName(r.userId) }}</span>
            <el-tag :type="r.signStatus === 0 ? 'success' : 'warning'" size="small">{{ r.signStatus === 0 ? '准时' : '迟到' }}</el-tag>
            <span class="sign-time">{{ r.signTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showQR" title="扫码签到" width="280px" center>
      <div style="text-align:center">
        <canvas ref="qrCanvas" style="width:200px;height:200px"></canvas>
        <p style="font-size:12px;color:var(--ts);margin-top:8px">参会人员扫描二维码签到</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { signIn, getSignList } from '../api/sign'
import { getMeetingAnalytics } from '../api/analytics'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const userName = computed(() => userStore.userName || '参会用户')
const isAdmin = computed(() => String(userStore.userId).startsWith('2'))
const alreadySigned = ref(false)
const records = ref([])
const nameMap = ref({})
const stats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })
const meetingQuality = ref(null)
const showQR = ref(false)
const qrCanvas = ref(null)

const loadData = async () => {
  const res = await getSignList(meeting.id)
  records.value = res.data.records || []
  nameMap.value = res.data.nameMap || {}
  stats.normal = res.data.normal; stats.late = res.data.late
  stats.absent = res.data.absent; stats.shouldAttend = res.data.shouldAttend
  stats.signed = res.data.signed
  alreadySigned.value = userStore.userId ? records.value.some(r => String(r.userId) === String(userStore.userId)) : false
  try { const a = await getMeetingAnalytics(meeting.id); meetingQuality.value = a.data } catch {}
}

const getUserName = (uid) => nameMap.value[uid] || uid

const doSignIn = async () => {
  try { await signIn(meeting.id, userStore.userId, 2); ElMessage.success('签到成功'); await loadData() } catch {}
}

watch(showQR, async (v) => {
  if (v) { await nextTick(); if (qrCanvas.value) await QRCode.toCanvas(qrCanvas.value, `${location.origin}/sign?meetingId=${meeting.id}`, { width: 200, margin: 1 }) }
})

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px; height:100%; overflow:hidden }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:16px; flex-shrink:0 }
.top-bar h2 { font-size:18px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:16px; min-height:0; overflow:hidden }
.left-panel { width:240px; flex-shrink:0; display:flex; flex-direction:column; align-items:center; gap:16px }
.user-card { text-align:center }
.user-name { font-size:18px; font-weight:700; margin-top:10px }
.user-role { font-size:12px; color:var(--ts); margin-top:2px }
.quality-badge { text-align:center; padding:12px; background:var(--pb); border-radius:12px; width:100% }
.q-num { font-size:32px; font-weight:700; color:var(--p) }
.q-label { font-size:12px; color:var(--ts); margin-top:2px }
.q-meta { font-size:11px; color:var(--ts); margin-top:6px }
.right-panel { flex:1; display:flex; flex-direction:column; min-height:0; overflow:hidden }
.stat-row { display:flex; gap:8px; margin-bottom:16px; flex-shrink:0 }
.stat-box { flex:1; text-align:center; padding:12px 8px; background:#fff; border:1px solid var(--bd); border-radius:8px; font-size:12px; color:var(--ts) }
.sn { font-size:24px; font-weight:700 }
.sn.green { color:var(--s) } .sn.orange { color:var(--w) } .sn.red { color:var(--d) } .sn.blue { color:var(--p) }
.panel-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; flex-shrink:0 }
.panel-hd h3 { font-size:14px }
.count { font-size:12px; color:var(--ts) }
.record-scroll { flex:1; overflow-y:auto; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:8px 12px }
.sign-row { display:flex; align-items:center; gap:10px; padding:8px 0; border-bottom:1px solid #F1F5F9 }
.sign-row:last-child { border-bottom:none }
.sign-name { flex:1; font-size:13px; font-weight:500 }
.sign-time { font-size:11px; color:var(--ts) }
</style>
```

- [ ] 构建验证：`npx vite build`
- [ ] 提交：`git commit -m "refactor: tencent-meeting-style sign-in page"`

---

### Task 2: 改造汇报页

**Files:**
- Modify: `smart-report-interaction/frontend/src/views/MeetingReport.vue`

完整替换为：

```vue
<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>会议汇报</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
    </div>

    <div class="content">
      <div class="main-area">
        <div class="agenda-steps">
          <div v-for="(a,i) in agendas" :key="i" :class="['step', { done: i+1 < currentAgenda, active: i+1 === currentAgenda }]" @click="currentAgenda = i+1">
            <span class="step-num">{{ i+1 < currentAgenda ? '✓' : i+1 }}</span> {{ a }}
          </div>
        </div>

        <div class="voice-zone">
          <div class="vz-icon">🎙</div>
          <div class="vz-title">语音实时转写</div>
          <div class="vz-speaker">当前主讲人：{{ currentSpeaker }}</div>
          <div class="vz-hint">语音接入后自动转写为文字，AI 提取关键要点生成会议摘要</div>
        </div>

        <div class="section-label">发言记录</div>
        <div class="speech-list">
          <div v-for="r in records" :key="r.id" class="speech-row">
            <el-avatar :size="24" style="flex-shrink:0">{{ getSpeakerName(r.speakerId).charAt(0) }}</el-avatar>
            <div class="speech-body">
              <div class="speech-meta"><strong>{{ getSpeakerName(r.speakerId) }}</strong> · {{ r.speechTime }}</div>
              <p>{{ r.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="side-panel">
        <div class="side-card" v-if="speechStats">
          <div class="sc-title">会议质量</div>
          <div class="sc-score">{{ speechStats.qualityScore }}</div>
          <div class="sc-meta">出勤 {{ speechStats.attendRate }}% · 发言 {{ speechStats.speechCount }} · 互动 {{ speechStats.interactionCount }}</div>
        </div>

        <div class="side-card">
          <div class="sc-title">会议摘要</div>
          <div class="summary-text" v-html="summaryHtml"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSpeechList, getSummary } from '../api/report'
import { getMeetingAnalytics } from '../api/analytics'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const currentAgenda = ref(2)
const currentSpeaker = computed(() => userStore.userName || '参会用户')
const speechStats = ref(null)
const records = ref([])
const nameMap = ref({})
const summaryHtml = ref('<p style="color:#64748B">会议进行中...</p>')

const loadData = async () => {
  const res = await getSpeechList(meeting.id)
  records.value = res.data.records || []
  nameMap.value = res.data.nameMap || {}
  const s = await getSummary(meeting.id)
  if (s.data) summaryHtml.value = s.data.summary || summaryHtml.value
  try { const a = await getMeetingAnalytics(meeting.id); speechStats.value = a.data } catch {}
}

const getSpeakerName = (sid) => nameMap.value[sid] || sid

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px; height:100%; overflow:hidden }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:16px; flex-shrink:0 }
.top-bar h2 { font-size:18px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:16px; min-height:0; overflow:hidden }
.main-area { flex:2; display:flex; flex-direction:column; min-height:0; overflow:hidden }
.agenda-steps { display:flex; gap:8px; margin-bottom:14px; flex-shrink:0 }
.step { padding:8px 16px; border-radius:8px; background:#F1F5F9; color:var(--ts); font-size:13px; cursor:pointer; text-align:center; min-width:80px }
.step.active { background:var(--p); color:#fff }
.step.done { background:var(--sb); color:var(--s) }
.step-num { font-weight:700; margin-right:4px }
.voice-zone { border:2px dashed var(--bd); border-radius:12px; padding:32px; text-align:center; margin-bottom:14px; flex-shrink:0; background:var(--pb) }
.vz-icon { font-size:36px; margin-bottom:8px }
.vz-title { font-size:16px; font-weight:700; color:var(--p) }
.vz-speaker { font-size:13px; color:var(--ts); margin-top:6px }
.vz-hint { font-size:12px; color:var(--ts); margin-top:8px }
.section-label { font-size:13px; font-weight:600; margin-bottom:8px; flex-shrink:0 }
.speech-list { flex:1; overflow-y:auto; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:8px 12px }
.speech-row { display:flex; gap:10px; padding:8px 0; border-bottom:1px solid #F1F5F9 }
.speech-row:last-child { border-bottom:none }
.speech-body { flex:1; min-width:0 }
.speech-meta { font-size:12px; color:var(--ts); margin-bottom:2px }
.speech-body p { font-size:13px; color:#1E293B; margin:0; line-height:1.5 }
.side-panel { width:220px; flex-shrink:0; display:flex; flex-direction:column; gap:12px }
.side-card { background:#fff; border:1px solid var(--bd); border-radius:10px; padding:14px; text-align:center }
.sc-title { font-size:12px; color:var(--ts); margin-bottom:6px }
.sc-score { font-size:36px; font-weight:700; color:var(--p) }
.sc-meta { font-size:11px; color:var(--ts); margin-top:6px }
.summary-text { text-align:left; font-size:13px; line-height:1.7; color:#475569 }
</style>
```

- [ ] 构建验证：`npx vite build`
- [ ] 提交：`git commit -m "refactor: tencent-meeting-style report page"`

---

### Task 3: 改造互动页

**Files:**
- Modify: `smart-report-interaction/frontend/src/views/Interaction.vue`

完整替换为：

```vue
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
        <div class="pp-count">{{ interStats.signed || stats.signed }} / {{ stats.shouldAttend }}</div>
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
const interStats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })
const stats = reactive({ signed: 0, shouldAttend: 15 })

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
```

- [ ] 构建验证：`npx vite build`
- [ ] 提交：`git commit -m "refactor: tencent-meeting-style interaction page with participant list"`

---

### Task 4: 集成验证

- [ ] Playwright 验证三个页面
- [ ] 构建通过
- [ ] 提交
