<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>{{ meeting.title }}</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }} · {{ meeting.location }}</span>
    </div>

    <div class="content">
      <div class="left-panel">
        <h3>签到操作</h3>
        <label class="field-label">工号/扫码</label>
        <div class="input-row">
          <el-input v-model="userId" placeholder="输入工号或扫描二维码签到" size="large" />
          <el-button type="primary" size="large" @click="doSignIn">签到</el-button>
        </div>

        <div class="stat-grid">
          <div class="stat-card success"><div class="num">{{ stats.normal }}</div><div class="label">已签到</div></div>
          <div class="stat-card warning"><div class="num">{{ stats.late }}</div><div class="label">迟到</div></div>
          <div class="stat-card danger"><div class="num">{{ stats.absent }}</div><div class="label">缺席</div></div>
          <div class="stat-card primary"><div class="num">{{ stats.shouldAttend }}</div><div class="label">应到</div></div>
        </div>

        <div class="btn-row">
          <el-button @click="refresh">刷新列表</el-button>
          <el-button>导出签到表</el-button>
        </div>
      </div>

      <div class="right-panel">
        <div class="panel-header">
          <h3>签到记录</h3>
          <span class="count">已签 {{ stats.signed }} / 应到 {{ stats.shouldAttend }}</span>
        </div>
        <div class="table-scroll">
          <el-table :data="records" stripe style="width:100%" max-height="100%">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="姓名">
              <template #default="{ row }">
                <el-avatar :size="24" style="margin-right:8px;vertical-align:middle">{{ getUserName(row.userId).charAt(0) }}</el-avatar>
                {{ getUserName(row.userId) }}
              </template>
            </el-table-column>
            <el-table-column label="签到方式" width="80">
              <template #default="{ row }">{{ row.signType === 1 ? '扫码' : '手动' }}</template>
            </el-table-column>
            <el-table-column prop="signTime" label="签到时间" width="160" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusType(row.signStatus)" size="small">
                  {{ statusText(row.signStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { signIn, getSignList } from '../api/sign'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const userId = ref('')
const records = ref([])
const nameMap = ref({})
const stats = reactive({ normal: 0, late: 0, absent: 0, shouldAttend: 0, signed: 0 })

const loadData = async () => {
  const res = await getSignList(meeting.id)
  records.value = res.data.records
  nameMap.value = res.data.nameMap || {}
  stats.normal = res.data.normal
  stats.late = res.data.late
  stats.absent = res.data.absent
  stats.shouldAttend = res.data.shouldAttend
  stats.signed = res.data.signed
}

const getUserName = (uid) => nameMap.value[uid] || uid

const doSignIn = async () => {
  if (!userId.value) { ElMessage.warning('请输入工号'); return }
  try {
    await signIn(meeting.id, userId.value, 2)
    ElMessage.success('签到成功')
    userId.value = ''
    await loadData()
  } catch (e) {
    // error already shown by request interceptor
  }
}

const refresh = () => loadData()

const statusType = (s) => s === 0 ? 'success' : s === 1 ? 'warning' : 'danger'
const statusText = (s) => s === 0 ? '正常' : s === 1 ? '迟到' : '缺席'

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:12px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { width:320px; flex-shrink:0; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; justify-content:center }
.left-panel h3 { font-size:14px; margin-bottom:12px }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.input-row { display:flex; gap:8px; margin-bottom:14px }
.stat-grid { display:flex; gap:8px; margin-bottom:20px }
.stat-card { flex:1; text-align:center; padding:10px 6px; border-radius:6px }
.stat-card.success { background:var(--sb) }  .stat-card.success .num { color:var(--s) }
.stat-card.warning { background:var(--wb) }  .stat-card.warning .num { color:var(--w) }
.stat-card.danger  { background:var(--db) }  .stat-card.danger .num  { color:var(--d) }
.stat-card.primary { background:var(--pb) }  .stat-card.primary .num { color:var(--p) }
.stat-card .num { font-size:24px; font-weight:700 }
.stat-card .label { font-size:11px; color:var(--ts); margin-top:2px }
.btn-row { display:flex; gap:8px }
.right-panel { flex:1; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.panel-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; flex-shrink:0 }
.panel-header h3 { font-size:14px }
.count { font-size:12px; color:var(--ts) }
.table-scroll { flex:1; overflow-y:auto }
</style>
