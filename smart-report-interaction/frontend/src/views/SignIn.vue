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
          <el-avatar :size="56" style="background:var(--p);font-size:24px">{{ (userName || '?').charAt(0) }}</el-avatar>
          <div class="user-name">{{ userName }}</div>
          <div class="user-role">{{ isAdmin ? '管理员' : '参会人员' }}</div>
        </div>

        <div class="quality-badge" v-if="isAdmin && meetingQuality">
          <div class="q-num">{{ meetingQuality.qualityScore }}</div>
          <div class="q-label">会议质量评分</div>
          <div class="q-meta">出勤 {{ meetingQuality.attendRate }}% · 发言 {{ meetingQuality.speechCount }} · 互动 {{ meetingQuality.interactionCount }}</div>
          <div class="q-note">综合评分，结合出勤、发言、互动等维度测算</div>
          <div v-if="meetingQuality.isAnomaly === 1" class="anomaly-tag">异常</div>
        </div>

        <el-button type="primary" size="large" @click="doSignIn" style="width:100%" :disabled="alreadySigned" :loading="signing">
          {{ alreadySigned ? '已签到' : '一键签到' }}
        </el-button>
        <el-button type="success" size="large" @click="doFaceSignIn" style="width:100%;margin-top:8px" :disabled="alreadySigned" :loading="faceSigning">人脸识别签到</el-button>
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
        <div class="record-scroll" v-loading="loading">
          <div v-for="r in records" :key="r.id" class="sign-row">
            <el-avatar :size="28" style="flex-shrink:0">{{ (getUserName(r.userId) || '?').charAt(0) }}</el-avatar>
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

    <el-dialog v-model="showFaceDialog" title="人脸识别确认" width="300px" center>
      <div style="text-align:center">
        <img v-if="facePreviewUrl" :src="facePreviewUrl" style="width:100%;max-height:240px;object-fit:cover;border-radius:8px" />
        <p style="font-size:13px;margin-top:10px">确认以 <strong>{{ userName }}</strong> 的身份自动签到？</p>
      </div>
      <template #footer>
        <el-button @click="retakeFace">重新拍摄</el-button>
        <el-button type="primary" :loading="faceSigning" @click="confirmFaceSignIn">确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { signIn, getSignList } from '../api/sign'
import { getMeetingAnalytics } from '../api/analytics'
import { recognizeFace } from '../api/face'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const route = useRoute()
const meeting = store.currentMeeting
const meetingId = computed(() => {
  const q = route.query.meetingId
  const parsed = q ? Number(q) : NaN
  return Number.isFinite(parsed) ? parsed : meeting.id
})

const userName = computed(() => {
  if (userStore.userName) return userStore.userName
  const token = localStorage.getItem('token')
  if (token) {
    try { const p = JSON.parse(atob(token.split('.')[1])); return p.sub || '参会用户' } catch {}
  }
  return '参会用户'
})
const isAdmin = computed(() => String(userStore.userId).startsWith('2'))
const signing = ref(false)
const loading = ref(false)
const faceSigning = ref(false)
const alreadySigned = ref(false)
const records = ref([])
const nameMap = ref({})
const stats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })
const meetingQuality = ref(null)
const showQR = ref(false)
const qrCanvas = ref(null)
const showFaceDialog = ref(false)
const facePreviewUrl = ref('')
let faceBlob = null

const loadData = async () => {
  loading.value = true
  try {
    try {
      const res = await getSignList(meetingId.value)
      records.value = res.data.records || []
      nameMap.value = res.data.nameMap || {}
      stats.normal = res.data.normal; stats.late = res.data.late
      stats.absent = res.data.absent; stats.shouldAttend = res.data.shouldAttend
      stats.signed = res.data.signed
      alreadySigned.value = userStore.userId ? records.value.some(r => String(r.userId) === String(userStore.userId)) : false
    } catch {}
    try { const a = await getMeetingAnalytics(meetingId.value); meetingQuality.value = a.data } catch {}
  } finally { loading.value = false }
}

const getUserName = (uid) => nameMap.value[uid] || uid

const doSignIn = async () => {
  if (!userStore.userId) { ElMessage.warning('请先登录'); return }
  signing.value = true
  try { await signIn(meetingId.value, userStore.userId, 2); ElMessage.success('签到成功'); await loadData() } catch {}
  signing.value = false
}

const withTimeout = (promise, ms) => Promise.race([
  promise,
  new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), ms))
])

const capturePhoto = async () => {
  const stream = await withTimeout(navigator.mediaDevices.getUserMedia({ video: true }), 3000)
  const video = document.createElement('video')
  video.srcObject = stream
  video.setAttribute('playsinline', '')
  await new Promise(resolve => { video.onloadedmetadata = () => resolve(); video.play() })
  const canvas = document.createElement('canvas')
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  canvas.getContext('2d').drawImage(video, 0, 0)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9))
  stream.getTracks().forEach(t => t.stop())
  return blob
}

const doFaceSignIn = async () => {
  if (!userStore.userId) { ElMessage.warning('请先登录'); return }
  if (faceSigning.value || alreadySigned.value) return
  faceSigning.value = true
  try {
    let photo
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      try { photo = await capturePhoto() } catch { photo = new Blob(['mock-face'], { type: 'image/jpeg' }); ElMessage.info('未获取到摄像头，使用模拟人脸') }
    } else {
      photo = new Blob(['mock-face'], { type: 'image/jpeg' })
    }
    faceBlob = photo
    if (facePreviewUrl.value) URL.revokeObjectURL(facePreviewUrl.value)
    facePreviewUrl.value = URL.createObjectURL(photo)
    showFaceDialog.value = true
  } catch { ElMessage.error('拍照失败') } finally { faceSigning.value = false }
}

const retakeFace = () => {
  showFaceDialog.value = false
  doFaceSignIn()
}

const confirmFaceSignIn = async () => {
  if (!faceBlob || faceSigning.value) return
  faceSigning.value = true
  try {
    const res = await recognizeFace(faceBlob, userStore.userId)
    const f = res.data
    if (!f.matched) { ElMessage.warning(f.message || '未识别到人脸'); showFaceDialog.value = false; return }
    ElMessage.success(`人脸识别成功：${f.name || '参会人员'}（${f.role || '参会人员'}），置信度 ${Math.round((f.confidence || 0) * 100)}%`)
    await signIn(meetingId.value, f.userId, 3)
    ElMessage.success('已自动签到')
    showFaceDialog.value = false
    await loadData()
  } catch { ElMessage.error('人脸识别失败') } finally { faceSigning.value = false }
}

watch(showQR, async (v) => {
  if (v) { await nextTick(); if (qrCanvas.value) await QRCode.toCanvas(qrCanvas.value, `${location.origin}/sign?meetingId=${meetingId.value}`, { width: 200, margin: 1 }) }
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
.q-note { font-size:10px; color:var(--ts); margin-top:4px; opacity:.85 }
.anomaly-tag { display:inline-block;padding:2px 10px;background:var(--db);color:var(--d);border-radius:4px;font-size:11px;font-weight:700;margin-top:6px }
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
