<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>会议汇报</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
    </div>

    <div class="content">
      <div class="left-panel">
        <div class="section-header">
          <h3>议程与汇报</h3>
          <span class="step">环节 {{ currentAgenda }} / 4</span>
        </div>

        <div class="agenda-tabs">
          <div v-for="(a,i) in agendas" :key="i"
            :class="['tab', { active: currentAgenda === i+1 }]"
            @click="currentAgenda = i+1">{{ i+1 }}. {{ a }}</div>
        </div>

        <div class="speaker-info">
          当前汇报人：
          <el-avatar :size="28" style="background:#2563EB">{{ speaker }}</el-avatar>
          <strong>{{ speaker }}</strong>
          <span>· 外科</span>
        </div>

        <label class="field-label">汇报人工号</label>
        <el-input v-model="speakerId" placeholder="输入汇报人工号" style="margin-bottom:8px" />

        <label class="field-label">发言内容</label>
        <el-input v-model="content" type="textarea" :rows="3" placeholder="录入发言内容或要点..." />
        <div class="action-row">
          <el-button @click="saveDraft">暂存草稿</el-button>
          <el-button type="primary" @click="saveSpeech">保存发言</el-button>
        </div>

        <div class="record-list">
          <div class="section-title">汇报记录</div>
          <div v-for="r in records" :key="r.id" class="record-item">
            <el-avatar :size="24">{{ getSpeakerName(r.speakerId).charAt(0) }}</el-avatar>
            <div class="record-body">
              <div class="record-meta">
                <strong>{{ getSpeakerName(r.speakerId) }}</strong>
                <span>{{ r.speechTime }}</span>
                <el-tag v-if="r.id === editingId" type="success" size="small">当前发言</el-tag>
              </div>
              <p>{{ r.content || '—' }}</p>
              <p class="key-points" v-if="r.keyPoints">要点：{{ r.keyPoints }}</p>
            </div>
            <el-button link type="primary" size="small" @click="editRecord(r)">编辑</el-button>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="panel-header">
          <h3>会议摘要</h3>
          <el-button size="small" @click="exportDoc">导出文档</el-button>
        </div>
        <div class="summary-body" v-html="summaryHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { saveSpeech as apiSave, getSpeechList, getSummary } from '../api/report'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const currentAgenda = ref(2)
const speaker = ref('李明辉')
const speakerId = ref('')
const content = ref('')
const editingId = ref(null)
const records = ref([])
const nameMap = ref({})
const summaryHtml = ref('<p style="color:#475569">会议进行中...</p>')

const loadData = async () => {
  const res = await getSpeechList(meeting.id)
  records.value = res.data.records || []
  nameMap.value = res.data.nameMap || {}
  const s = await getSummary(meeting.id)
  if (s.data) summaryHtml.value = s.data.summary || ''
}

const getSpeakerName = (sid) => nameMap.value[sid] || sid

const saveDraft = async () => {
  ElMessage.info('草稿已暂存')
}

const saveSpeech = async () => {
  if (!content.value) { ElMessage.warning('请输入发言内容'); return }
  if (!speakerId.value) { ElMessage.warning('请输入汇报人工号'); return }
  try {
    await apiSave({ meetingId: meeting.id, speakerId: speakerId.value, content: content.value })
    ElMessage.success('发言已保存')
    content.value = ''
    speakerId.value = ''
    await loadData()
  } catch (e) {
    // error already shown by interceptor
  }
}

const editRecord = (r) => {
  content.value = r.content
  editingId.value = r.id
}

const exportDoc = () => ElMessage.success('文档导出中...')

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:10px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { flex:2; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.section-header { display:flex; align-items:center; gap:10px; margin-bottom:8px; flex-shrink:0 }
.section-header h3 { font-size:14px }
.step { font-size:12px; color:var(--ts) }
.agenda-tabs { display:flex; margin-bottom:10px; flex-shrink:0 }
.agenda-tabs .tab { padding:5px 14px; font-size:12px; border:1px solid var(--bd); border-left:0; background:var(--pb); color:var(--p); cursor:pointer }
.agenda-tabs .tab:first-child { border-left:1px solid var(--bd); border-radius:6px 0 0 6px }
.agenda-tabs .tab:last-child { border-radius:0 6px 6px 0 }
.agenda-tabs .tab.active { background:var(--p); color:#fff; border-color:var(--p) }
.speaker-info { display:flex; align-items:center; gap:8px; margin-bottom:10px; font-size:13px; flex-shrink:0 }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.action-row { display:flex; justify-content:flex-end; gap:8px; margin:8px 0; flex-shrink:0 }
.record-list { flex:1; overflow-y:auto; min-height:0 }
.section-title { font-size:12px; color:var(--ts); font-weight:500; margin-bottom:8px }
.record-item { display:flex; align-items:flex-start; gap:8px; padding:8px 0; border-bottom:1px solid var(--bd) }
.record-body { flex:1; min-width:0 }
.record-meta { display:flex; align-items:center; gap:8px; margin-bottom:2px }
.record-meta strong { font-size:13px }
.record-meta span { font-size:11px; color:var(--ts) }
.record-body p { font-size:13px; color:var(--t); line-height:1.6 }
.key-points { font-size:11px; color:var(--ts); margin-top:3px }
.right-panel { flex:1; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.panel-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; flex-shrink:0 }
.panel-header h3 { font-size:14px }
.summary-body { flex:1; overflow-y:auto; border:1px solid var(--bd); border-radius:6px; padding:12px; font-size:13px; line-height:1.8 }
</style>
