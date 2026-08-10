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
        <el-descriptions-item label="分类">
          <el-tag :type="getCategoryType(problem.category)">{{ getCategoryName(problem.category) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskType(problem.riskLevel)">{{ getRiskName(problem.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(problem.status)">{{ getStatusName(problem.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ assigneeName || '未分派' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatTime(problem.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ problem.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
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
      </div>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header><span>督办文书（AI生成）</span></template>
      <div style="margin-bottom:12px; display:flex; gap:10px; align-items:center">
        <el-select v-model="docType" style="width:180px">
          <el-option label="督办通知书" :value="1" />
          <el-option label="整改通知书" :value="2" />
          <el-option label="闭环报告" :value="3" />
        </el-select>
        <el-button type="primary" :loading="generating" @click="generateDoc">AI生成文书</el-button>
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
            <template v-if="row.checkStatus === 0">
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
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const problem = ref({})
const assigneeName = ref('')
const assignableUsers = ref([])
const manualUserId = ref(null)
const manualReason = ref('')
const assigning = ref(false)
const newDeadline = ref(null)
const savingDeadline = ref(false)

const docType = ref(1)
const documents = ref([])
const generating = ref(false)
const docDialogVisible = ref(false)
const docDialogTitle = ref('')
const docDialogContent = ref('')

const getCategoryName = (type) => ({ 1: '医疗类', 2: '运维类', 3: '管理类' }[type] || '未分类')
const getCategoryType = (type) => ({ 1: 'danger', 2: 'warning', 3: 'info' }[type] || 'info')
const getRiskName = (level) => ({ 1: '一般', 2: '重要', 3: '紧急' }[level] || '未定')
const getRiskType = (level) => ({ 1: 'info', 2: 'warning', 3: 'danger' }[level] || 'info')
const getStatusName = (status) => ({ 0: '待分派', 1: '处理中', 2: '待复查', 3: '已闭环' }[status] || '未知')
const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'danger', 3: 'success' }[status] || 'info')
const getDocTypeName = (type) => ({ 1: '督办通知书', 2: '整改通知书', 3: '闭环报告' }[type] || '未知')
const getCheckName = (status) => ({ 0: '待审核', 1: '已通过', 2: '已驳回' }[status] || '未知')
const getCheckType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const formatTime = (time) => {
  if (!time) return '未设置'
  return time.includes('T') ? time.replace('T', ' ').substring(0, 19) : time
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
