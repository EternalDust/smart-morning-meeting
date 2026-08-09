<template>
  <div class="meeting-room">
    <!-- 顶部栏 -->
    <div class="mr-topbar">
      <h2>{{ meeting.title }}</h2>
      <el-tag :type="meeting.status === '进行中' ? 'success' : 'info'" size="small">{{ meeting.status }}</el-tag>
      <span class="mr-time">{{ meeting.time }} · {{ meeting.location }}</span>
    </div>

    <!-- 三栏主体 -->
    <div class="mr-body">
      <!-- 左栏 -->
      <div class="mr-left">
        <div class="mr-panel">
          <h3>议程</h3>
          <div class="agenda-list">
            <div v-for="(a, i) in agendas" :key="i" :class="['agenda-item', { active: currentAgenda === i + 1, done: i + 1 < currentAgenda }]">
              <span class="agenda-num">{{ currentAgenda > i + 1 ? '✓' : i + 1 }}</span>
              {{ a }}
            </div>
          </div>
        </div>

        <div class="mr-panel">
          <h3>签到</h3>
          <div class="sign-user">
            <el-avatar :size="32" style="background:var(--p)">{{ (userStore.userName || '?').charAt(0) }}</el-avatar>
            <span>{{ userStore.userName }}</span>
          </div>
          <el-button type="primary" size="small" @click="doSignIn" style="width:100%" :disabled="signing">一键签到</el-button>
          <div class="sign-stats">
            <span>已签 {{ signStats.signed }}/{{ signStats.shouldAttend }}</span>
            <span style="color:var(--w)">迟到 {{ signStats.late }}</span>
            <span style="color:var(--d)">缺席 {{ signStats.absent }}</span>
          </div>
          <el-button size="small" @click="showQR = true" style="width:100%;margin-top:6px">二维码</el-button>
        </div>

        <div class="mr-panel" v-if="meetingQuality">
          <h3>会议质量</h3>
          <div class="quality-score">{{ meetingQuality.qualityScore }}</div>
          <div class="quality-meta">出勤 {{ meetingQuality.attendRate }}% · 发言 {{ meetingQuality.speechCount }} · 互动 {{ meetingQuality.interactionCount }}</div>
          <div class="quality-note">综合评分，结合出勤、发言、互动等维度测算</div>
        </div>
      </div>

      <!-- 中栏：汇报 -->
      <div class="mr-center">
        <div class="mr-panel" style="flex:1;display:flex;flex-direction:column">
          <div class="section-hd">
            <h3>当前环节：{{ agendas[currentAgenda - 1] || '—' }}</h3>
            <div>
              <el-button size="small" @click="currentAgenda = Math.max(1, currentAgenda - 1)" :disabled="currentAgenda <= 1">上一步</el-button>
              <el-button size="small" @click="currentAgenda = Math.min(4, currentAgenda + 1)" :disabled="currentAgenda >= 4">下一步</el-button>
            </div>
          </div>

          <div v-if="userStore.isLoggedIn && currentAgenda >= 2" class="speaker-bar">
            <el-avatar :size="24" style="background:var(--p)">{{ (currentSpeaker || '?').charAt(0) }}</el-avatar>
            <strong>{{ currentSpeaker }}</strong>
            <span style="color:var(--ts);font-size:12px">当前汇报人</span>
          </div>

          <template v-if="currentAgenda >= 2">
            <label class="field-label">发言要点</label>
            <el-input v-model="speechContent" type="textarea" :rows="3" placeholder="录入发言人要点或会议摘要..." style="margin-bottom:8px" />
            <div style="display:flex;justify-content:flex-end;gap:8px;margin-bottom:10px">
              <el-button size="small" :type="recording ? 'danger' : 'primary'" @click="startVoiceTranscribe" :loading="transcribing">{{ recording ? `停止并转写 (${recSeconds}s)` : '语音转写' }}</el-button>
              <el-button size="small" type="primary" @click="saveSpeech">保存发言</el-button>
            </div>
          </template>

          <div class="record-list">
            <div class="section-title">汇报记录</div>
            <div v-for="r in speechRecords" :key="r.id" class="record-item">
              <el-avatar :size="20" style="flex-shrink:0">{{ (getSpeakerName(r.speakerId) || '?').charAt(0) }}</el-avatar>
              <div class="record-body">
                <div><strong>{{ getSpeakerName(r.speakerId) }}</strong> · <span class="record-time">{{ r.speechTime }}</span></div>
                <p>{{ r.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏：互动 -->
      <div class="mr-right">
        <div class="mr-panel" style="flex:1;display:flex;flex-direction:column">
          <h3>互动</h3>
          <div style="display:flex;gap:4px;margin-bottom:8px">
            <el-button size="small" :type="interFilter === 0 ? 'primary' : ''" @click="interFilter = 0">全部</el-button>
            <el-button size="small" :type="interFilter === 1 ? 'primary' : ''" @click="interFilter = 1">提问</el-button>
            <el-button size="small" :type="interFilter === 2 ? 'primary' : ''" @click="interFilter = 2">反馈</el-button>
            <el-button size="small" :type="interFilter === 3 ? 'primary' : ''" @click="interFilter = 3">投票</el-button>
          </div>
          <div class="msg-stream">
            <div v-for="m in filteredMessages" :key="m.id" class="msg-item">
              <el-avatar :size="20">{{ (getInterUserName(m.userId) || '?').charAt(0) }}</el-avatar>
              <div class="msg-body">
                <div class="msg-hd"><strong>{{ getInterUserName(m.userId) }}</strong> · {{ m.createTime }}</div>
                <template v-if="m.interactType === 3">
                  <div>{{ pollTitle(m) }}</div>
                  <PollOptions :poll="m" :votes="interVoteCasts" :userId="String(userStore.userId || '')" @vote="(idx) => castVote(m, idx)" />
                </template>
                <template v-else>
                  <div>{{ m.content }}</div>
                </template>
                <div v-if="m.reply" class="msg-reply"><span>回复</span>{{ m.reply }}</div>
              </div>
              <el-button v-if="!m.reply && isAdmin" link type="primary" size="small" @click="openReply(m)">回复</el-button>
            </div>
          </div>
          <div class="compose-bar">
            <div style="display:flex;gap:4px;margin-bottom:4px">
              <el-button size="small" :type="interType === 1 ? 'primary' : ''" @click="interType = 1">提问</el-button>
              <el-button size="small" :type="interType === 2 ? 'primary' : ''" @click="interType = 2">反馈</el-button>
              <el-button size="small" :type="interType === 3 ? 'primary' : ''" @click="interType = 3">投票</el-button>
            </div>
            <div style="display:flex;gap:6px">
              <el-input v-model="interContent" placeholder="输入互动..." size="small" @keyup.enter="sendInter" />
              <el-button type="primary" size="small" @click="sendInter">发送</el-button>
            </div>
          </div>
          <div class="inter-stats">
            <span>提问 {{ interStats.questions }}</span>
            <span>反馈 {{ interStats.feedback }}</span>
            <span>投票 {{ interStats.votes }}</span>
            <span>已回复 {{ interStats.replied }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- QR 弹窗 -->
    <el-dialog v-model="showQR" title="扫码签到" width="280px" center>
      <div style="text-align:center">
        <canvas ref="qrCanvas" style="width:200px;height:200px"></canvas>
        <p style="font-size:12px;color:var(--ts);margin-top:8px">手机扫码即可签到</p>
      </div>
    </el-dialog>

    <el-dialog v-model="showReplyDialog" title="回复互动消息" width="360px">
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="输入回复内容..." />
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!replyText" @click="submitReply">回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { signIn, getSignList } from '../api/sign'
import { saveSpeech as apiSave, getSpeechList } from '../api/report'
import { sendMessage, replyMessage, getInteractionList, getStats } from '../api/interaction'
import { getMeetingAnalytics } from '../api/analytics'
import { transcribeAudio } from '../api/asr'
import { useWebSocket } from '../composables/useWebSocket'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'
import PollOptions from '../components/PollOptions.vue'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const { lastMessage, connect } = useWebSocket(meeting.id)
connect()

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const savedAgenda = Number(sessionStorage.getItem('currentAgenda'))
const currentAgenda = ref(Number.isInteger(savedAgenda) && savedAgenda >= 1 && savedAgenda <= agendas.length ? savedAgenda : 2)
watch(currentAgenda, (v) => sessionStorage.setItem('currentAgenda', String(v)))
const isAdmin = computed(() => String(userStore.userId).startsWith('2'))
const currentSpeaker = computed(() => {
  const list = speechRecords.value
  for (let i = list.length - 1; i >= 0; i--) {
    const name = speechNameMap.value[list[i].speakerId]
    if (name) return name
  }
  return userStore.userName || '参会用户'
})

const signing = ref(false)
const signStats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })
const meetingQuality = ref(null)
const showQR = ref(false)
const qrCanvas = ref(null)

const speechContent = ref('')
const speechRecords = ref([])
const speechNameMap = ref({})
const recording = ref(false)
const transcribing = ref(false)
const recSeconds = ref(0)
const MAX_REC_SEC = 30
let mediaRecorder = null
let audioChunks = []
let recTimer = null

const startRecTimer = () => {
  recSeconds.value = MAX_REC_SEC
  clearInterval(recTimer)
  recTimer = setInterval(() => {
    recSeconds.value--
    if (recSeconds.value <= 0) stopVoiceTranscribe()
  }, 1000)
}

const interFilter = ref(0)
const interType = ref(1)
const interContent = ref('')
const interMessages = ref([])
const interNameMap = ref({})
const interStats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })
const showReplyDialog = ref(false)
const replyTargetId = ref(null)
const replyText = ref('')

const loadSign = async () => {
  const res = await getSignList(meeting.id)
  Object.assign(signStats, {
    normal: res.data.normal, late: res.data.late, absent: res.data.absent,
    shouldAttend: res.data.shouldAttend, signed: res.data.signed
  })
  try { const a = await getMeetingAnalytics(meeting.id); meetingQuality.value = a.data } catch {}
}

const doSignIn = async () => {
  const uid = userStore.userId
  if (!uid) { ElMessage.warning('请先登录'); return }
  signing.value = true
  try { await signIn(meeting.id, uid, 2); ElMessage.success('签到成功'); await loadSign() } catch {} finally { signing.value = false }
}

const loadSpeech = async () => {
  const res = await getSpeechList(meeting.id)
  speechRecords.value = res.data.records || []
  speechNameMap.value = res.data.nameMap || {}
}

const getSpeakerName = (sid) => speechNameMap.value[sid] || sid

const saveSpeech = async () => {
  if (!speechContent.value) { ElMessage.warning('请输入发言内容'); return }
  const speakerId = userStore.userId || '9999'
  try { await apiSave({ meetingId: meeting.id, speakerId, content: speechContent.value }); ElMessage.success('发言已保存'); speechContent.value = ''; await loadSpeech() } catch {}
}

const withTimeout = (promise, ms) => Promise.race([
  promise,
  new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), ms))
])

const startVoiceTranscribe = async () => {
  if (recording.value) { stopVoiceTranscribe(); return }
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    ElMessage.info('未获取到麦克风，使用模拟语音')
    await fallbackTranscribe()
    return
  }
  try {
    const stream = await withTimeout(navigator.mediaDevices.getUserMedia({ audio: true }), 3000)
    audioChunks = []
    mediaRecorder = new MediaRecorder(stream)
    mediaRecorder.ondataavailable = e => { if (e.data.size) audioChunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach(t => t.stop())
      await uploadAudio(new Blob(audioChunks, { type: mediaRecorder.mimeType || 'audio/webm' }))
    }
    mediaRecorder.start()
    recording.value = true
    startRecTimer()
    ElMessage.success('正在录音，最长 30 秒，再次点击或到时自动转写')
  } catch {
    ElMessage.info('未获取到麦克风，使用模拟语音')
    await fallbackTranscribe()
  }
}

const stopVoiceTranscribe = () => {
  clearInterval(recTimer)
  recTimer = null
  if (!recording.value) return
  recording.value = false
  if (mediaRecorder && mediaRecorder.state !== 'inactive') mediaRecorder.stop()
}

const fallbackTranscribe = async () => {
  await uploadAudio(new Blob(['mock-audio'], { type: 'audio/wav' }))
}

const uploadAudio = async (blob) => {
  transcribing.value = true
  try {
    const res = await transcribeAudio(blob)
    const text = res.data && res.data.text
    if (text) {
      speechContent.value = speechContent.value ? speechContent.value + '\n' + text : text
      ElMessage.success('语音转写完成')
    }
  } catch { ElMessage.error('语音转写失败') } finally { transcribing.value = false }
}

const loadInteraction = async () => {
  const [list, stats] = await Promise.all([
    getInteractionList(meeting.id),
    getStats(meeting.id)
  ])
  interMessages.value = list.data.messages || []
  interNameMap.value = list.data.nameMap || {}
  if (stats.data) Object.assign(interStats, stats.data)
}

const getInterUserName = (uid) => interNameMap.value[uid] || uid
const filteredMessages = computed(() => {
  const list = interMessages.value.filter(m => m.interactType !== 4)
  if (interFilter.value === 0) return list
  return list.filter(m => m.interactType === interFilter.value)
})
const interVoteCasts = computed(() => interMessages.value.filter(m => m.interactType === 4))
const pollTitle = (m) => {
  const first = String(m.content || '').split('\n')[0]
  return first.startsWith('【投票】') ? first : (first || '投票')
}
const castVote = async (poll, idx) => {
  if (!userStore.userId) { ElMessage.warning('请先登录'); return }
  try {
    await sendMessage({ meetingId: meeting.id, userId: String(userStore.userId), content: `VOTE:${poll.id}:${idx}`, interactType: 4 })
    ElMessage.success('投票成功')
    await loadInteraction()
  } catch {}
}

const sendInter = async () => {
  const uid = userStore.userId || '9999'
  if (!interContent.value) { ElMessage.warning('请输入内容'); return }
  try { await sendMessage({ meetingId: meeting.id, userId: uid, content: interContent.value, interactType: interType.value }); ElMessage.success('已发送'); interContent.value = ''; await loadInteraction() } catch {}
}

const openReply = (m) => {
  replyTargetId.value = m.id
  replyText.value = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyTargetId.value || !replyText.value) return
  try {
    await replyMessage(replyTargetId.value, replyText.value)
    ElMessage.success('已回复')
    showReplyDialog.value = false
    replyText.value = ''
    await loadInteraction()
  } catch {}
}

watch(showQR, async (v) => {
  if (v) { await nextTick(); if (qrCanvas.value) await QRCode.toCanvas(qrCanvas.value, `${location.origin}/sign?meetingId=${meeting.id}`, { width: 200, margin: 1 }) }
})

watch(lastMessage, () => { loadSign(); loadInteraction() })
watch(interFilter, () => loadInteraction())
onMounted(() => { loadSign(); loadSpeech(); loadInteraction() })
</script>

<style scoped>
.meeting-room { height:100vh; display:flex; flex-direction:column; background:var(--bg) }
.mr-topbar { display:flex; align-items:center; gap:10px; padding:10px 16px; background:#fff; border-bottom:1px solid var(--bd); flex-shrink:0 }
.mr-topbar h2 { font-size:16px; margin:0 }
.mr-time { color:var(--ts); font-size:13px }
.mr-body { flex:1; display:flex; gap:0; min-height:0; overflow:hidden }
.mr-left { width:240px; flex-shrink:0; display:flex; flex-direction:column; gap:8px; padding:10px; overflow-y:auto; border-right:1px solid var(--bd); background:#fff }
.mr-center { flex:1; padding:10px; overflow-y:auto; background:#fff }
.mr-right { width:280px; flex-shrink:0; display:flex; flex-direction:column; gap:8px; padding:10px; overflow-y:auto; border-left:1px solid var(--bd); background:#fff }
.mr-panel { background:#fff; padding:10px; border-radius:var(--radius); border:1px solid var(--bd); margin-bottom:8px }
.agenda-item { padding:6px 8px; font-size:13px; border-radius:4px; margin-bottom:2px }
.agenda-item.active { background:var(--pb); color:var(--p); font-weight:600 }
.agenda-item.done { color:var(--s) }
.agenda-num { display:inline-block; width:20px; text-align:center }
.sign-user { display:flex; align-items:center; gap:8px; margin-bottom:8px; font-size:13px }
.sign-stats { display:flex; gap:8px; font-size:12px; margin-top:6px; color:var(--ts) }
.quality-score { font-size:32px; font-weight:700; color:var(--p); text-align:center }
.quality-meta { font-size:11px; color:var(--ts); text-align:center; margin-top:4px }
.quality-note { font-size:10px; color:var(--ts); text-align:center; margin-top:6px; opacity:.85 }
.section-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px }
.section-hd h3 { font-size:14px; margin:0 }
.speaker-bar { display:flex; align-items:center; gap:8px; margin-bottom:10px; padding:8px; background:var(--pb); border-radius:6px }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.record-list { flex:1; overflow-y:auto; min-height:0; margin-top:8px }
.section-title { font-size:12px; color:var(--ts); margin-bottom:6px }
.record-item { display:flex; gap:8px; padding:6px 0; border-bottom:1px solid var(--bd) }
.record-body { flex:1; min-width:0 }
.record-body p { font-size:13px; margin-top:2px }
.record-time { font-size:11px; color:var(--ts) }
.msg-stream { flex:1; overflow-y:auto; min-height:0 }
.msg-item { display:flex; gap:6px; padding:6px 0; border-bottom:1px solid var(--bd); font-size:12px }
.msg-body { flex:1; min-width:0 }
.msg-hd { font-size:11px; color:var(--ts); margin-bottom:2px }
.msg-reply { margin-top:3px; padding:4px 8px; background:var(--pb); border-radius:4px; font-size:11px }
.msg-reply span { color:var(--p); font-weight:600; margin-right:4px }
.compose-bar { padding-top:8px; border-top:1px solid var(--bd); margin-top:8px }
.inter-stats { display:flex; gap:6px; font-size:11px; color:var(--ts); margin-top:6px; flex-wrap:wrap }
</style>
