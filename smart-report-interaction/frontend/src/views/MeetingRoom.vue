<template>
  <div class="meeting-room">
    <!-- 顶部栏 -->
    <div class="mr-topbar">
      <h2>{{ meeting.title }}</h2>
      <el-tag :type="meeting.status === '进行中' ? 'success' : 'info'" size="small">{{ meeting.status }}</el-tag>
      <span class="mr-time">{{ meeting.time }} · {{ meeting.location }}</span>
    </div>

    <!-- 主体 -->
    <div class="mr-body">
      <!-- 左栏：管理员专属（签到统计 + 质量 + 摘要） -->
      <div v-if="isAdmin" class="mr-left">
        <div class="mr-panel">
          <h3>签到</h3>
          <div class="sign-user">
            <el-avatar :size="32" style="background:var(--p)">{{ (userStore.userName || '?').charAt(0) }}</el-avatar>
            <span>{{ userStore.userName }}</span>
          </div>
          <el-button type="primary" size="small" @click="doSignIn" style="width:100%" :disabled="signing || alreadySigned">{{ alreadySigned ? '已签到' : '一键签到' }}</el-button>
          <div class="sign-stats">
            <span>已签 {{ signStats.signed }}/{{ signStats.shouldAttend }}</span>
            <span style="color:var(--w)">迟到 {{ signStats.late }}</span>
            <span style="color:var(--d)">缺席 {{ signStats.absent }}</span>
          </div>
        </div>

        <div class="mr-panel" v-if="meetingQuality">
          <h3>会议质量</h3>
          <div class="quality-score">{{ meetingQuality.qualityScore }}</div>
          <div class="quality-meta">出勤 {{ meetingQuality.attendRate }}% · 发言 {{ meetingQuality.speechCount }} · 互动 {{ meetingQuality.interactionCount }}</div>
          <div class="quality-formula">评分 = 出勤率40% + 准时率25% + 发言参与20% + 互动参与15%</div>
        </div>

        <div class="mr-panel" v-if="weeklyTrend.length">
          <h3>全平台周度趋势</h3>
          <div v-for="w in weeklyTrend.slice(-4)" :key="w.meetingWeek" class="trend-row">
            <span>{{ w.meetingWeek }}</span>
            <span>出勤 {{ w.avgAttendRate }}%</span>
            <span>评分 {{ w.avgQualityScore }}</span>
          </div>
        </div>
      </div>

      <!-- 非管理员：极简签到卡 -->
      <div v-else class="mr-staff-sign">
        <div class="mr-panel">
          <h3>签到</h3>
          <div class="staff-user">
            <el-avatar :size="48" style="background:var(--p);font-size:20px">{{ (userStore.userName || '?').charAt(0) }}</el-avatar>
            <div class="staff-name">{{ userStore.userName }}</div>
            <div class="staff-role">参会人员</div>
          </div>
          <el-button type="primary" size="large" @click="doSignIn" style="width:100%" :disabled="signing || alreadySigned">{{ alreadySigned ? '已签到' : '一键签到' }}</el-button>
        </div>
      </div>

      <!-- 中栏：签到记录 + 发言（管理员专属） -->
      <div v-if="isAdmin" class="mr-center">
        <div class="mr-panel">
          <div class="section-hd">
            <h3>签到记录</h3>
            <span class="count">已签 {{ signStats.signed }}/{{ signStats.shouldAttend }}</span>
          </div>
          <div class="sign-list">
            <div v-for="r in signRecords" :key="r.id" class="sign-row">
              <el-avatar :size="22" style="flex-shrink:0">{{ (getSignUserName(r.userId) || '?').charAt(0) }}</el-avatar>
              <span class="sign-name">{{ getSignUserName(r.userId) }}</span>
              <el-tag :type="r.signStatus === 0 ? 'success' : 'warning'" size="small">{{ r.signStatus === 0 ? '准时' : '迟到' }}</el-tag>
              <span class="sign-time">{{ r.signTime }}</span>
            </div>
          </div>
        </div>
        <div class="mr-panel" style="flex:1;display:flex;flex-direction:column">
          <div class="section-hd">
            <h3>发言要点</h3>
            <div>
              <el-button size="small" :type="recording ? 'danger' : 'primary'" @click="startVoiceTranscribe" :loading="transcribing">{{ recording ? `停止并转写 (${recSeconds}s)` : '语音转写' }}</el-button>
              <el-button size="small" type="primary" @click="saveSpeech">保存发言</el-button>
            </div>
          </div>
          <el-input v-model="speechContent" type="textarea" :rows="4" placeholder="录入发言人要点或会议摘要..." style="margin-bottom:8px" />
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

      <!-- 右栏：摘要（管理员）+ 互动（两种身份共用） -->
      <div class="mr-right" :class="{ 'mr-right-flex': !isAdmin }">
        <div v-if="isAdmin" class="mr-panel">
          <div class="panel-hd">
            <h3>会议摘要</h3>
            <el-button size="small" type="primary" link :loading="aiLoading" @click="genSummary">AI 生成摘要</el-button>
          </div>
          <template v-if="aiSummary">
            <div class="summary-text">{{ aiSummary.summary }}</div>
            <template v-if="aiSummary.keyPoints && aiSummary.keyPoints.length">
              <div class="ai-label">关键要点</div>
              <div v-for="(k, i) in aiSummary.keyPoints" :key="i" class="ai-kp">{{ i + 1 }}. {{ k }}</div>
            </template>
            <template v-if="aiSummary.medicalEntities && aiSummary.medicalEntities.length">
              <div class="ai-label">医疗实体</div>
              <div class="ai-tags">
                <el-tag v-for="(e, i) in aiSummary.medicalEntities" :key="i" size="small" effect="light" type="primary" style="margin:2px">{{ e }}</el-tag>
              </div>
            </template>
          </template>
          <div v-else class="summary-text">会议进行中...</div>
        </div>
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
              <div v-if="!m.reply && isAdmin" class="msg-actions">
                <el-button link type="primary" size="small" :loading="aiReplyingId === m.id" @click="aiReplyTo(m)">AI 答复</el-button>
                <el-button link type="primary" size="small" @click="openReply(m)">回复</el-button>
              </div>
            </div>
          </div>
          <div class="compose-bar">
            <div style="display:flex;gap:4px;margin-bottom:4px">
              <el-button size="small" :type="interType === 1 ? 'primary' : ''" @click="interType = 1">提问</el-button>
              <el-button size="small" :type="interType === 2 ? 'primary' : ''" @click="interType = 2">反馈</el-button>
              <el-button size="small" @click="showVoteDialog = true">投票</el-button>
            </div>
            <div style="display:flex;gap:6px">
              <el-input v-model="interContent" placeholder="输入互动..." size="small" @keyup.enter="sendInter" />
              <el-button type="primary" size="small" @click="sendInter">发送</el-button>
            </div>
          </div>
          <div v-if="isAdmin" class="inter-stats">
            <span>提问 {{ interStats.questions }}</span>
            <span>反馈 {{ interStats.feedback }}</span>
            <span>投票 {{ interStats.votes }}</span>
            <span>已回复 {{ interStats.replied }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="showReplyDialog" title="回复互动消息" width="360px">
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="输入回复内容..." />
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!replyText" @click="submitReply">回复</el-button>
      </template>
    </el-dialog>

    <!-- 投票弹窗 -->
    <el-dialog v-model="showVoteDialog" title="发起投票" width="360px">
      <el-input v-model="voteTitle" placeholder="投票标题" style="margin-bottom:10px" />
      <el-input v-model="voteOption" placeholder="添加选项" @keyup.enter="addVoteOption">
        <template #append><el-button @click="addVoteOption">添加</el-button></template>
      </el-input>
      <el-tag v-for="(o, i) in voteOptions" :key="i" closable @close="voteOptions.splice(i,1)" style="margin:4px">{{ o }}</el-tag>
      <template #footer>
        <el-button @click="showVoteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVote" :disabled="!voteTitle || voteOptions.length < 2">发起投票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { signIn, getSignList } from '../api/sign'
import { saveSpeech as apiSave, getSpeechList, getSummary, aiGenerateSummary } from '../api/report'
import { sendMessage, replyMessage, getInteractionList, getStats, aiAnswer } from '../api/interaction'
import { getMeetingAnalytics, getWeeklyTrend } from '../api/analytics'
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

const isAdmin = computed(() => String(userStore.userId).startsWith('2'))

const signing = ref(false)
const alreadySigned = ref(false)
const signStats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })
const signRecords = ref([])
const signNameMap = ref({})
const meetingQuality = ref(null)
const weeklyTrend = ref([])

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

const aiSummary = ref(null)
const aiLoading = ref(false)

const interFilter = ref(0)
const interType = ref(1)
const interContent = ref('')
const interMessages = ref([])
const interNameMap = ref({})
const interStats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })
const showReplyDialog = ref(false)
const replyTargetId = ref(null)
const replyText = ref('')
const aiReplyingId = ref(null)
const showVoteDialog = ref(false)
const voteTitle = ref('')
const voteOption = ref('')
const voteOptions = ref([])

const startRecTimer = () => {
  recSeconds.value = MAX_REC_SEC
  clearInterval(recTimer)
  recTimer = setInterval(() => {
    recSeconds.value--
    if (recSeconds.value <= 0) stopVoiceTranscribe()
  }, 1000)
}

const loadSign = async () => {
  const res = await getSignList(meeting.id)
  Object.assign(signStats, {
    normal: res.data.normal, late: res.data.late, absent: res.data.absent,
    shouldAttend: res.data.shouldAttend, signed: res.data.signed
  })
  signRecords.value = res.data.records || []
  signNameMap.value = res.data.nameMap || {}
  alreadySigned.value = userStore.userId ? signRecords.value.some(r => String(r.userId) === String(userStore.userId)) : false
  try { const a = await getMeetingAnalytics(meeting.id); meetingQuality.value = a.data } catch {}
  try { const w = await getWeeklyTrend(); weeklyTrend.value = w.data || [] } catch {}
}

const getSignUserName = (uid) => signNameMap.value[uid] || uid

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
  const speakerId = userStore.userId
  if (!speakerId) { ElMessage.warning('请先登录'); return }
  try { await apiSave({ meetingId: meeting.id, speakerId, content: speechContent.value }); ElMessage.success('发言已保存'); speechContent.value = ''; await loadSpeech() } catch {}
}

const normalizeSummary = (data) => {
  if (data && typeof data === 'object' && data.summary != null) return data
  if (typeof data === 'string' && data) return { summary: data, keyPoints: [], decisions: [], medicalEntities: [] }
  return null
}

const loadSummary = async () => {
  try { const s = await getSummary(meeting.id); aiSummary.value = normalizeSummary(s.data) } catch {}
}

const genSummary = async () => {
  aiLoading.value = true
  try {
    const res = await aiGenerateSummary(meeting.id)
    aiSummary.value = res.data || {}
    ElMessage.success('摘要已生成')
  } catch { ElMessage.error('摘要生成失败，请重试') } finally { aiLoading.value = false }
}

const withTimeout = (promise, ms) => Promise.race([
  promise,
  new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), ms))
])

const startVoiceTranscribe = async () => {
  if (recording.value) { stopVoiceTranscribe(); return }
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
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
  ElMessage.warning('未检测到麦克风，请检查麦克风权限，或直接手动输入发言内容')
}

const uploadAudio = async (blob) => {
  transcribing.value = true
  try {
    const res = await transcribeAudio(blob)
    const text = res.data && res.data.text
    if (text) {
      speechContent.value = speechContent.value ? speechContent.value + '\n' + text : text
      ElMessage.success('语音转写完成')
    } else {
      ElMessage.warning('未识别到语音内容，请重试')
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
  const uid = userStore.userId
  if (!uid) { ElMessage.warning('请先登录'); return }
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

const aiReplyTo = async (m) => {
  aiReplyingId.value = m.id
  try {
    await aiAnswer(m.id)
    ElMessage.success('AI 初步答复已生成')
    await loadInteraction()
  } catch { ElMessage.error('AI 答复生成失败') } finally { aiReplyingId.value = null }
}

const addVoteOption = () => {
  if (voteOption.value) { voteOptions.value.push(voteOption.value); voteOption.value = '' }
}

const submitVote = async () => {
  if (!userStore.userId) { ElMessage.warning('请先登录'); return }
  const content = `【投票】${voteTitle.value}\n` + voteOptions.value.map((o, i) => `${i + 1}. ${o}`).join('\n')
  try {
    await sendMessage({ meetingId: meeting.id, userId: userStore.userId, content, interactType: 3 })
    ElMessage.success('投票已发起')
    showVoteDialog.value = false
    voteTitle.value = ''; voteOptions.value = []
    await loadInteraction()
  } catch {}
}

watch(lastMessage, () => { loadSign(); loadInteraction() })
watch(interFilter, () => loadInteraction())
onMounted(() => { loadSign(); loadSpeech(); loadInteraction(); loadSummary() })
</script>

<style scoped>
.meeting-room { height:100vh; display:flex; flex-direction:column; background:var(--bg) }
.mr-topbar { display:flex; align-items:center; gap:10px; padding:10px 16px; background:#fff; border-bottom:1px solid var(--bd); flex-shrink:0 }
.mr-topbar h2 { font-size:16px; margin:0 }
.mr-time { color:var(--ts); font-size:13px }
.mr-body { flex:1; display:flex; min-height:0; overflow:hidden }
.mr-left { width:260px; flex-shrink:0; display:flex; flex-direction:column; gap:8px; padding:10px; overflow-y:auto; border-right:1px solid var(--bd); background:#fff }
.mr-center { flex:1; padding:10px; display:flex; flex-direction:column; gap:8px; min-height:0; background:#fff }
.mr-right { width:300px; flex-shrink:0; display:flex; flex-direction:column; gap:8px; padding:10px; overflow-y:auto; border-left:1px solid var(--bd); background:#fff }
.mr-right-flex { flex:1; width:auto; border-left:1px solid var(--bd) }
.mr-staff-sign { width:260px; flex-shrink:0; padding:10px; overflow-y:auto; border-right:1px solid var(--bd); background:#fff }
.mr-panel { background:#fff; padding:10px; border-radius:var(--radius); border:1px solid var(--bd); margin-bottom:8px }
.sign-user { display:flex; align-items:center; gap:8px; margin-bottom:8px; font-size:13px }
.sign-stats { display:flex; gap:8px; font-size:12px; margin-top:6px; color:var(--ts) }
.quality-score { font-size:32px; font-weight:700; color:var(--p); text-align:center }
.quality-meta { font-size:11px; color:var(--ts); text-align:center; margin-top:4px }
.quality-formula { font-size:10px; color:var(--ts); text-align:center; margin-top:6px; opacity:.85 }
.panel-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px }
.panel-hd h3 { font-size:14px; margin:0 }
.summary-text { font-size:12px; line-height:1.6; color:#475569 }
.ai-label { font-size:11px; font-weight:600; color:var(--ts); margin:6px 0 3px }
.ai-kp { font-size:12px; color:#334155; line-height:1.5 }
.ai-tags { display:flex; flex-wrap:wrap }
.staff-user { display:flex; flex-direction:column; align-items:center; gap:6px; margin-bottom:12px }
.staff-name { font-size:16px; font-weight:700 }
.staff-role { font-size:12px; color:var(--ts) }
.section-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px }
.section-hd h3 { font-size:14px; margin:0 }
.record-list { flex:1; overflow-y:auto; min-height:0; margin-top:8px }
.section-title { font-size:12px; color:var(--ts); margin-bottom:6px }
.record-item { display:flex; gap:8px; padding:6px 0; border-bottom:1px solid var(--bd) }
.record-body { flex:1; min-width:0 }
.record-body p { font-size:13px; margin-top:2px }
.record-time { font-size:11px; color:var(--ts) }
.sign-list { max-height:200px; overflow-y:auto }
.sign-row { display:flex; align-items:center; gap:8px; padding:5px 0; border-bottom:1px solid #F1F5F9; font-size:12px }
.sign-row:last-child { border-bottom:none }
.sign-name { flex:1; font-size:12px; font-weight:500 }
.sign-time { font-size:11px; color:var(--ts) }
.count { font-size:12px; color:var(--ts) }
.trend-row { display:flex; justify-content:space-between; font-size:11px; color:var(--ts); padding:2px 0 }
.msg-stream { flex:1; overflow-y:auto; min-height:0 }
.msg-item { display:flex; gap:6px; padding:6px 0; border-bottom:1px solid var(--bd); font-size:12px }
.msg-body { flex:1; min-width:0 }
.msg-hd { font-size:11px; color:var(--ts); margin-bottom:2px }
.msg-reply { margin-top:3px; padding:4px 8px; background:var(--pb); border-radius:4px; font-size:11px }
.msg-reply span { color:var(--p); font-weight:600; margin-right:4px }
.msg-actions { display:flex; align-items:center; gap:2px; flex-shrink:0 }
.compose-bar { padding-top:8px; border-top:1px solid var(--bd); margin-top:8px }
.inter-stats { display:flex; gap:6px; font-size:11px; color:var(--ts); margin-top:6px; flex-wrap:wrap }
</style>
