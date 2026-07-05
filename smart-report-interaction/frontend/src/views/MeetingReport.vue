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

        <div class="side-card" v-if="weeklyTrend.length">
          <div class="sc-title">周度趋势</div>
          <div v-for="w in weeklyTrend.slice(-4)" :key="w.meetingWeek" style="font-size:11px;display:flex;justify-content:space-between;padding:2px 0">
            <span>{{ w.meetingWeek }}</span>
            <span>出勤{{ w.avgAttendRate }}%</span>
            <span>评分{{ w.avgQualityScore }}</span>
          </div>
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
import { getMeetingAnalytics, getWeeklyTrend } from '../api/analytics'
import { useMeetingStore } from '../stores/meeting'
import { useUserStore } from '../stores/user'

const store = useMeetingStore()
const userStore = useUserStore()
const meeting = store.currentMeeting

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const currentAgenda = ref(2)
const currentSpeaker = computed(() => userStore.userName || '参会用户')
const speechStats = ref(null)
const weeklyTrend = ref([])
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
  try { const w = await getWeeklyTrend(); weeklyTrend.value = w.data || [] } catch {}
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
