<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content:space-between; margin-bottom:15px">
        <span>问题列表</span>
        <div>
          <el-button type="success" @click="showImport=true">从会议导入问题</el-button>
          <el-button type="primary" @click="showAdd=true">新增问题</el-button>
        </div>
      </div>
      <el-table :data="problems" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标题">
          <template #default="{row}">
            <router-link :to="`/problems/${row.id}`" style="color:#409EFF">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="220">
          <template #default="{row}">
            <template v-if="row.deadline">
              {{ formatTime(row.deadline) }}
              <el-tag :type="getDeadlineStatus(row.deadline).type" size="small" style="margin-left:6px">{{ getDeadlineStatus(row.deadline).text }}</el-tag>
            </template>
            <span v-else>未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column v-if="isAdmin" label="操作" width="100">
          <template #default="{row}">
            <el-button size="small" type="danger" :loading="deletingId === row.id" @click="deleteProblem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="新增问题">
      <el-form :model="newProblem">
        <el-form-item label="标题"><el-input v-model="newProblem.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="newProblem.content" type="textarea" /></el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="newProblem.deadline" type="datetime" placeholder="选择截止时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">取消</el-button>
        <el-button type="primary" @click="addProblem">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showImport" title="从会议导入问题" width="420px">
      <el-form label-width="90px">
        <el-form-item label="会议ID">
          <el-input v-model="importMeetingId" placeholder="输入晨会会议ID" />
        </el-form-item>
        <el-alert type="info" :closable="false" style="margin-bottom:10px"
          title="从汇报交互的互动提问/反馈和会议摘要中自动生成问题（来源标记为自动采集）" />
      </el-form>
      <template #footer>
        <el-button @click="showImport=false">取消</el-button>
        <el-button type="success" :loading="importing" @click="importMeeting">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { getAccount } from '../utils/auth'

const problems = ref([])
const showAdd = ref(false)
const showImport = ref(false)
const importMeetingId = ref('')
const importing = ref(false)
const newProblem = ref({ title: '', content: '', deadline: null })
const isAdmin = getAccount().startsWith('2')
const deletingId = ref(null)

const getStatusName = (status) => ({ 0: '待分派', 1: '处理中', 2: '待复查', 3: '已闭环' }[status] || '未知')
const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'danger', 3: 'success' }[status] || 'info')
const formatTime = (time) => (time ? (time.includes('T') ? time.replace('T', ' ').substring(0, 19) : time) : '')
const URGENT_THRESHOLD_HOURS = 24
const getDeadlineStatus = (time) => {
  if (!time) return { text: '未设置', type: 'info' }
  const diff = new Date(time).getTime() - Date.now()
  if (diff < 0) return { text: '已逾期', type: 'danger' }
  if (diff <= URGENT_THRESHOLD_HOURS * 3600000) return { text: '临期', type: 'warning' }
  return { text: '正常', type: 'success' }
}

const loadProblems = async () => {
  const res = await request.get('/supervise/problem/list?page=1&size=100')
  if (res.success) problems.value = res.data?.records || []
}

const addProblem = async () => {
  if (!newProblem.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  const res = await request.post('/supervise/problem/add', newProblem.value)
  if (res.success) {
    ElMessage.success('添加成功')
    showAdd.value = false
    newProblem.value = { title: '', content: '', deadline: null }
    loadProblems()
  }
}

const importMeeting = async () => {
  if (!importMeetingId.value) {
    ElMessage.warning('请输入会议ID')
    return
  }
  importing.value = true
  try {
    const res = await request.post(`/supervise/problem/import-meeting?meetingId=${Number(importMeetingId.value)}`)
    if (res.success) {
      ElMessage.success(`导入 ${res.data?.imported || 0} 条，跳过重复 ${res.data?.skipped || 0} 条`)
      showImport.value = false
      importMeetingId.value = ''
      loadProblems()
    }
  } finally {
    importing.value = false
  }
}

const deleteProblem = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除问题「${row.title}」？关联的分派、进度、文书记录会一并删除。`, '删除问题', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  deletingId.value = row.id
  try {
    const res = await request.delete(`/supervise/problem/${row.id}`)
    if (res.success) {
      ElMessage.success('删除成功')
      loadProblems()
    }
  } finally {
    deletingId.value = null
  }
}

onMounted(() => loadProblems())
</script>
