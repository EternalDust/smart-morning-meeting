<template>
  <div class="meeting-room">
    <!-- 顶部栏 -->
    <div class="mr-topbar">
      <h2>{{ meeting.title }}</h2>
      <el-tag :type="meeting.status === '进行中' ? 'success' : 'info'" size="small">{{ meeting.status }}</el-tag>
      <span class="mr-time">{{ meeting.time }} · {{ meeting.location }}</span>
      <el-button size="small" type="danger" plain style="margin-left:auto">结束会议</el-button>
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
          <template v-if="userStore.isLoggedIn">
            <div class="sign-user">
              <el-avatar :size="32" style="background:var(--p)">{{ userStore.userName.charAt(0) }}</el-avatar>
              <span>{{ userStore.userName }}</span>
            </div>
            <el-button type="primary" size="small" @click="doSignIn" style="width:100%">一键签到</el-button>
          </template>
          <template v-else>
            <div style="display:flex;gap:6px">
              <el-input v-model="signUserId" placeholder="工号" size="small" />
              <el-button type="primary" size="small" @click="doSignIn">签到</el-button>
            </div>
          </template>
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
            <el-avatar :size="24" style="background:var(--p)">{{ userStore.userName.charAt(0) }}</el-avatar>
            <strong>{{ userStore.userName }}</strong>
            <span style="color:var(--ts);font-size:12px">当前汇报人</span>
          </div>

          <template v-if="currentAgenda >= 2">
            <label class="field-label">发言要点</label>
            <el-input v-model="speechContent" type="textarea" :rows="3" placeholder="录入发言人要点或会议摘要..." style="margin-bottom:8px" />
            <div style="display:flex;justify-content:flex-end;gap:8px;margin-bottom:10px">
              <el-button size="small" @click="saveDraft">暂存</el-button>
              <el-button size="small" type="primary" @click="saveSpeech">保存发言</el-button>
            </div>
          </template>

          <div class="record-list">
            <div class="section-title">汇报记录</div>
            <div v-for="r in speechRecords" :key="r.id" class="record-item">
              <el-avatar :size="20" style="flex-shrink:0">{{ getSpeakerName(r.speakerId).charAt(0) }}</el-avatar>
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
              <el-avatar :size="20">{{ getInterUserName(m.userId).charAt(0) }}</el-avatar>
              <div class="msg-body">
                <div class="msg-hd"><strong>{{ getInterUserName(m.userId) }}</strong> · {{ m.createTime }}</div>
                <div>{{ m.content }}</div>
                <div v-if="m.reply" class="msg-reply"><span>回复</span>{{ m.reply }}</div>
              </div>
              <el-button v-if="!m.reply" link type="primary" size="small" @click="replyTo(m)">回复</el-button>
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

    <!-- 底部功能栏 -->
    <div class="mr-toolbar">
      <el-button size="small" type="primary" plain>签到</el-button>
      <el-button size="small" plain>汇报</el-button>
      <el-button size="small" plain>互动</el-button>
      <el-button size="small" plain @click="$router.push('/analytics')">分析</el-button>
      <el-divider direction="vertical" />
      <el-button size="small" disabled>审批</el-button>
      <el-button size="small" disabled>督办</el-button>
      <el-button size="small" disabled>可视化</el-button>
    </div>

    <!-- QR 弹窗 -->
    <el-dialog v-model="showQR" title="扫码签到" width="280px" center>
      <div style="text-align:center">
        <canvas ref="qrCanvas" style="width:200px;height:200px"></canvas>
        <p style="font-size:12px;color:var(--ts);margin-top:8px">手机扫码即可签到</p>
      </div>
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
import { useWebSocket } from '../composables/useWebSocket'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const { connected: wsConnected, lastMessage, connect } = useWebSocket(meeting.id)
connect()

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const currentAgenda = ref(2)

const signUserId = ref('')
const signRecords = ref([])
const signNameMap = ref({})
const signStats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })
const meetingQuality = ref(null)
const showQR = ref(false)
const qrCanvas = ref(null)

const speechContent = ref('')
const speechRecords = ref([])
const speechNameMap = ref({})

const interFilter = ref(0)
const interType = ref(1)
const interContent = ref('')
const interMessages = ref([])
const interNameMap = ref({})
const interStats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })

const loadSign = async () => {
  const res = await getSignList(meeting.id)
  signRecords.value = res.data.records
  signNameMap.value = res.data.nameMap || {}
  Object.assign(signStats, {
    normal: res.data.normal, late: res.data.late, absent: res.data.absent,
    shouldAttend: res.data.shouldAttend, signed: res.data.signed
  })
  try { const a = await getMeetingAnalytics(meeting.id); meetingQuality.value = a.data } catch {}
}

const doSignIn = async () => {
  const uid = userStore.userId || signUserId.value
  if (!uid) { ElMessage.warning('请输入工号'); return }
  try { await signIn(meeting.id, uid, 2); ElMessage.success('签到成功'); signUserId.value = ''; await loadSign() } catch {}
}

const loadSpeech = async () => {
  const res = await getSpeechList(meeting.id)
  speechRecords.value = res.data.records || []
  speechNameMap.value = res.data.nameMap || {}
}

const getSpeakerName = (sid) => speechNameMap.value[sid] || sid
const saveDraft = () => ElMessage.info('草稿已暂存')

const saveSpeech = async () => {
  if (!speechContent.value) { ElMessage.warning('请输入发言内容'); return }
  const speakerId = userStore.userId || '9999'
  try { await apiSave({ meetingId: meeting.id, speakerId, content: speechContent.value }); ElMessage.success('发言已保存'); speechContent.value = ''; await loadSpeech() } catch {}
}

const loadInteraction = async () => {
  const [list, stats] = await Promise.all([
    getInteractionList(meeting.id, interFilter.value || undefined),
    getStats(meeting.id)
  ])
  interMessages.value = list.data.messages || []
  interNameMap.value = list.data.nameMap || {}
  if (stats.data) Object.assign(interStats, stats.data)
}

const getInterUserName = (uid) => interNameMap.value[uid] || uid
const filteredMessages = computed(() =>
  interFilter.value === 0 ? interMessages.value : interMessages.value.filter(m => m.interactType === interFilter.value)
)

const sendInter = async () => {
  const uid = userStore.userId || '9999'
  if (!interContent.value) { ElMessage.warning('请输入内容'); return }
  try { await sendMessage({ meetingId: meeting.id, userId: uid, content: interContent.value, interactType: interType.value }); ElMessage.success('已发送'); interContent.value = ''; await loadInteraction() } catch {}
}

const replyTo = async (m) => {
  const reply = prompt('回复内容：')
  if (reply) { await replyMessage(m.id, reply); await loadInteraction() }
}

watch(showQR, async (v) => {
  if (v) { await nextTick(); if (qrCanvas.value) await QRCode.toCanvas(qrCanvas.value, `${location.origin}/meeting/${meeting.id}`, { width: 200, margin: 1 }) }
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
.mr-toolbar { display:flex; align-items:center; justify-content:center; gap:8px; padding:8px 16px; background:#fff; border-top:1px solid var(--bd); flex-shrink:0 }
.agenda-item { padding:6px 8px; font-size:13px; border-radius:4px; margin-bottom:2px }
.agenda-item.active { background:var(--pb); color:var(--p); font-weight:600 }
.agenda-item.done { color:var(--s) }
.agenda-num { display:inline-block; width:20px; text-align:center }
.sign-user { display:flex; align-items:center; gap:8px; margin-bottom:8px; font-size:13px }
.sign-stats { display:flex; gap:8px; font-size:12px; margin-top:6px; color:var(--ts) }
.quality-score { font-size:32px; font-weight:700; color:var(--p); text-align:center }
.quality-meta { font-size:11px; color:var(--ts); text-align:center; margin-top:4px }
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
