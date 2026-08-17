<template>
  <div>
    <el-card style="margin-bottom:16px">
      <template #header><span>我的任务（执行责任人）</span></template>
      <div style="display:flex; gap:10px; margin-bottom:12px">
        <el-input v-model="myAccount" placeholder="工号，如 1001" style="width:200px" />
        <el-button type="primary" :loading="loadingMine" @click="loadMyTasks">加载我的任务</el-button>
      </div>
      <el-alert type="info" :closable="false" style="margin-bottom:10px"
        title="工号自动取当前演示身份，可在右上角切换督办专员/执行责任人后自动刷新" />
      <el-table :data="myProblems" stripe empty-text="当前身份暂无待办任务" max-height="260">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'warning' : 'danger'">{{ row.status === 1 ? '处理中' : '待复查' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="selectMyProblem(row)">上报进度</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <div style="display:flex; gap:10px">
        <el-input v-model="problemId" placeholder="问题ID" style="width:200px" />
        <el-button type="primary" @click="loadProgress">查询</el-button>
      </div>

      <template v-if="searched">
        <div style="margin-top:20px">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="当前进度">
              <el-progress :percentage="currentProgress" />
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider>上报进度</el-divider>
        <el-form label-width="90px" style="max-width:560px">
          <el-form-item label="进度百分比">
            <el-input v-model="newProgress" type="number" min="0" max="100" placeholder="0-100" style="width:200px" />
          </el-form-item>
          <el-form-item label="备注说明">
            <el-input v-model="newRemark" type="textarea" :rows="3" placeholder="填写本次进展" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitProgress">上报进度</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="history" stripe style="margin-top:16px" empty-text="暂无进度记录">
          <el-table-column label="进度" width="100">
            <template #default="{row}">{{ row.progress }}%</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="createTime" label="上报时间" width="180" />
        </el-table>
      </template>
      <div v-else style="margin-top:20px">
        <el-alert title="输入问题ID后查询进度" type="info" :closable="false" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { getAccount } from '../utils/auth'

const problemId = ref('')
const currentProgress = ref(0)
const history = ref([])
const searched = ref(false)
const newProgress = ref(0)
const newRemark = ref('')
const submitting = ref(false)
const myAccount = ref('')
const myProblems = ref([])
const loadingMine = ref(false)

myAccount.value = getAccount()

const loadMyTasks = async () => {
  if (!myAccount.value) {
    ElMessage.warning('请输入工号')
    return
  }
  loadingMine.value = true
  try {
    const res = await request.get(`/supervise/problem/mine?account=${myAccount.value}`)
    if (res.success) {
      myProblems.value = res.data?.problems || []
      if (myProblems.value.length === 0) {
        ElMessage.info('当前没有待办任务')
      }
    }
  } finally {
    loadingMine.value = false
  }
}

const selectMyProblem = (row) => {
  problemId.value = String(row.id)
  loadProgress()
}

onMounted(() => {
  if (myAccount.value) {
    loadMyTasks()
  }
})

const loadProgress = async () => {
  if (!problemId.value) return
  searched.value = true
  const res = await request.get(`/supervise/progress/current/${problemId.value}`)
  if (res.success && res.data) {
    currentProgress.value = res.data.progress || 0
  } else {
    currentProgress.value = 0
  }
  const res2 = await request.get(`/supervise/progress/history/${problemId.value}`)
  if (res2.success) history.value = res2.data || []
}

const submitProgress = async () => {
  if (!problemId.value) {
    ElMessage.warning('请先查询问题')
    return
  }
  submitting.value = true
  try {
    const progress = Number(newProgress.value)
    if (isNaN(progress) || progress < 0 || progress > 100) {
      ElMessage.warning('进度必须在0-100之间')
      return
    }
    const payload = {
      problemId: Number(problemId.value),
      progress,
      remark: newRemark.value || null,
      account: getAccount() || null
    }
    const res = await request.post('/supervise/progress/submit', payload)
    if (res.success) {
      ElMessage.success('进度上报成功')
      newProgress.value = ''
      newRemark.value = ''
      loadProgress()
    }
  } finally {
    submitting.value = false
  }
}
</script>
