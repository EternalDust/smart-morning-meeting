<template>
  <div class="problem-detail">
    <el-card>
      <template #header>
        <div class="header">
          <span>问题详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="问题ID">{{ problem.id }}</el-descriptions-item>
        <el-descriptions-item label="问题标题">{{ problem.title }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ problem.content || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(problem.status)">{{ getStatusName(problem.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ assigneeName || '未分派' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">
          {{ formatTime(problem.deadline) }}
          <el-tag v-if="problem.deadline" :type="getDeadlineStatus(problem.deadline).type" size="small" style="margin-left:8px">
            {{ getDeadlineStatus(problem.deadline).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ problem.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <template v-if="isAdmin">
          <div class="deadline-row">
            <el-date-picker v-model="newDeadline" type="datetime" placeholder="设置截止时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:220px; margin-right:10px" />
            <el-button type="primary" plain :loading="savingDeadline" @click="saveDeadline">保存截止时间</el-button>
          </div>
          <template v-if="problem.status === 0">
            <el-button type="primary" :loading="assigning" @click="autoAssign">AI自动分派</el-button>
          </template>
          <template v-else>
            <el-select v-model="manualUserId" placeholder="选择负责人" style="width:200px; margin-right:10px">
              <el-option v-for="u in assignableUsers" :key="u.id" :label="`${u.name}（${u.dept || '未分科室'}）`" :value="u.id" />
            </el-select>
            <el-input v-model="manualReason" placeholder="改派原因（可选）" style="width:220px; margin-right:10px" />
            <el-button type="warning" :loading="assigning" @click="manualAssign">人工改派</el-button>
          </template>
          <template v-if="problem.status === 2">
            <el-divider style="margin:8px 0" />
            <el-button type="success" :loading="closing" @click="closeProblem">复查通过（闭环）</el-button>
          </template>
        </template>
        <template v-else>
          <el-alert type="info" :closable="false" show-icon
            title="当前为执行责任人身份，分派、改派、审核等督办操作仅督办专员（管理员）可用" />
        </template>
      </div>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header><span>督办文书（AI生成）</span></template>
      <div style="margin-bottom:12px">
        <el-button v-if="isAdmin" type="warning" :loading="generating" @click="generateDoc">一键催办（AI生成催办通知书）</el-button>
        <span v-else style="color:#999; font-size:13px">仅督办专员可发起催办</span>
      </div>

      <el-table :data="documents" stripe empty-text="暂无文书">
        <el-table-column label="类型" width="120">
          <template #default="{row}">{{ getDocTypeName(row.docType) }}</template>
        </el-table-column>
        <el-table-column label="生成方式" width="100">
          <template #default="{row}">{{ row.genType === 1 ? 'AI生成' : '人工编辑' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{row}">
            <el-tag :type="getCheckType(row.checkStatus)">{{ getCheckName(row.checkStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="180" />
        <el-table-column label="操作" width="210">
          <template #default="{row}">
            <el-button size="small" @click="previewDoc(row)">查看</el-button>
            <template v-if="isAdmin && row.checkStatus === 0">
              <el-button size="small" type="success" @click="auditDoc(row, 1)">通过</el-button>
              <el-button size="small" type="danger" @click="auditDoc(row, 2)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="docDialogVisible" :title="docDialogTitle" width="720px">
      <el-input v-model="docDialogContent" type="textarea" :rows="16" readonly />
      <template #footer>
        <el-button @click="docDialogVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { isAdmin as checkAdmin } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const isAdmin = checkAdmin()

const problem = ref({})
const assigneeName = ref('')
const assignableUsers = ref([])
const manualUserId = ref(null)
const manualReason = ref('')
const assigning = ref(false)
const newDeadline = ref(null)
const savingDeadline = ref(false)

const docType = ref(4)
const documents = ref([])
const generating = ref(false)
const docDialogVisible = ref(false)
const docDialogTitle = ref('')
const docDialogContent = ref('')
const closing = ref(false)

const getStatusName = (status) => ({ 0: '待分派', 1: '处理中', 2: '待复查', 3: '已闭环' }[status] || '未知')
const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'danger', 3: 'success' }[status] || 'info')
const getDocTypeName = (type) => ({ 1: '督办通知书', 2: '整改通知书', 3: '闭环报告', 4: '催办通知书' }[type] || '未知')
const getCheckName = (status) => ({ 0: '待审核', 1: '已通过', 2: '已驳回' }[status] || '未知')
const getCheckType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const formatTime = (time) => {
  if (!time) return '未设置'
  return time.includes('T') ? time.replace('T', ' ').substring(0, 19) : time
}
const URGENT_THRESHOLD_HOURS = 24
const getDeadlineStatus = (time) => {
  if (!time) return { text: '未设置', type: 'info' }
  const diff = new Date(time).getTime() - Date.now()
  if (diff < 0) return { text: '已逾期', type: 'danger' }
  if (diff <= URGENT_THRESHOLD_HOURS * 3600000) return { text: '临期', type: 'warning' }
  return { text: '正常', type: 'success' }
}

const loadDetail = async () => {
  const res = await request.get(`/supervise/problem/detail/${id}`)
  if (res.success) {
    problem.value = res.data
    newDeadline.value = res.data?.deadline ? formatTime(res.data.deadline) : null
  }
}

const saveDeadline = async () => {
  if (!newDeadline.value) {
    ElMessage.warning('请选择截止时间')
    return
  }
  savingDeadline.value = true
  try {
    const res = await request.put(`/supervise/problem/deadline/${id}`, null, {
      params: { deadline: newDeadline.value }
    })
    if (res.success) {
      ElMessage.success('截止时间已保存')
      loadDetail()
    }
  } finally {
    savingDeadline.value = false
  }
}

const loadAssignee = async () => {
  const res = await request.get(`/supervise/assign/current/${id}`)
  if (res.success) assigneeName.value = res.data?.assigneeName || ''
}

const loadUsers = async () => {
  const res = await request.get('/supervise/assign/users')
  if (res.success) assignableUsers.value = res.data || []
}

const loadDocuments = async () => {
  const res = await request.get(`/supervise/document/list/${id}`)
  if (res.success) documents.value = res.data || []
}

const autoAssign = async () => {
  assigning.value = true
  try {
    const res = await request.post(`/supervise/assign/auto/${id}`)
    if (res.success) {
      ElMessage.success('分派成功')
      loadDetail()
      loadAssignee()
    }
  } finally {
    assigning.value = false
  }
}

const manualAssign = async () => {
  if (!manualUserId.value) {
    ElMessage.warning('请选择负责人')
    return
  }
  assigning.value = true
  try {
    const res = await request.post('/supervise/assign/manual', {
      problemId: Number(id),
      userId: manualUserId.value,
      operatorId: localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null,
      reason: manualReason.value || null
    })
    if (res.success) {
      ElMessage.success('改派成功')
      manualUserId.value = null
      manualReason.value = ''
      loadDetail()
      loadAssignee()
    }
  } finally {
    assigning.value = false
  }
}

const generateDoc = async () => {
  generating.value = true
  try {
    const res = await request.post('/supervise/document/generate', {
      problemId: Number(id),
      docType: docType.value
    })
    if (res.success) {
      docDialogTitle.value = getDocTypeName(docType.value)
      docDialogContent.value = res.data?.content || ''
      docDialogVisible.value = true
      loadDocuments()
    }
  } finally {
    generating.value = false
  }
}

const previewDoc = (row) => {
  docDialogTitle.value = getDocTypeName(row.docType)
  docDialogContent.value = row.content || ''
  docDialogVisible.value = true
}

const auditDoc = async (row, status) => {
  const res = await request.post(`/supervise/document/audit/${row.id}`, { status })
  if (res.success) {
    ElMessage.success(status === 1 ? '审核通过' : '已驳回')
    loadDocuments()
  }
}

const closeProblem = async () => {
  try {
    await ElMessageBox.confirm('确认复查通过并闭环该问题？闭环后不能再上报进度。', '复查审核', {
      type: 'warning',
      confirmButtonText: '复查通过',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  closing.value = true
  try {
    const res = await request.post(`/supervise/problem/close/${id}`)
    if (res.success) {
      ElMessage.success('复查通过，问题已闭环')
      loadDetail()
    }
  } finally {
    closing.value = false
  }
}

const goBack = () => router.push('/problems')

onMounted(() => {
  loadDetail()
  loadAssignee()
  loadUsers()
  loadDocuments()
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin-top: 20px;
  text-align: center;
}

.deadline-row {
  margin-bottom: 12px;
}
</style>
