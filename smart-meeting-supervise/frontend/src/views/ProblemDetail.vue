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
        <el-descriptions-item label="创建时间">{{ problem.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions" v-if="problem.status === 0">
        <el-button type="primary" @click="autoAssign">AI自动分派</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const problem = ref({})

const getCategoryName = (type) => {
  const map = { 1: '医疗类', 2: '运维类', 3: '管理类' }
  return map[type] || '未知'
}

const getCategoryType = (type) => {
  const map = { 1: 'danger', 2: 'warning', 3: 'info' }
  return map[type] || ''
}

const getRiskName = (level) => {
  const map = { 1: '一般', 2: '重要', 3: '紧急' }
  return map[level] || '未知'
}

const getRiskType = (level) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] || ''
}

const getStatusName = (status) => {
  const map = { 0: '待分派', 1: '处理中', 2: '待复查', 3: '已闭环' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'danger', 3: 'success' }
  return map[status] || ''
}

const loadDetail = async () => {
  const id = route.params.id
  const res = await request.get(`/supervise/problem/detail/${id}`)
  if (res.success) {
    problem.value = res.data
  }
}

const autoAssign = async () => {
  const res = await request.post(`/supervise/assign/auto/${problem.value.id}`)
  if (res.success) {
    ElMessage.success('分派成功')
    loadDetail()
  }
}

const goBack = () => {
  router.push('/problems')
}

onMounted(() => {
  loadDetail()
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
</style>