<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">晨会详情</h2>
      <p class="page-desc">查看会议的完整信息及当前审批状态</p>
    </div>

    <el-descriptions :column="2" border class="detail-descriptions">
      <el-descriptions-item label="会议编号" width="120">
        <span class="desc-value">{{ meeting.id }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="会议标题" width="120">
        <span class="desc-value title-text">{{ meeting.title }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="会议类型" width="120">
        <el-tag size="small" type="info">{{ formatMeetingType(meeting.meetingType) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="审批状态" width="120">
        <el-tag :type="statusType(meeting.approveStatus)" size="small">
          {{ statusText(meeting.approveStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="会议地点" width="120">
        <span class="desc-value"><el-icon><Location /></el-icon> {{ meeting.location || '未设置' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="主持人" width="120">
        <span class="desc-value">{{ meeting.hostId ? 'ID: ' + meeting.hostId : '未指定' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="开始时间" width="120">
        <span class="desc-value"><el-icon><Clock /></el-icon> {{ formatTime(meeting.startTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="结束时间" width="120">
        <span class="desc-value"><el-icon><Clock /></el-icon> {{ formatTime(meeting.endTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" width="120">
        <span class="desc-value">{{ formatTime(meeting.createTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="更新时间" width="120">
        <span class="desc-value">{{ formatTime(meeting.updateTime) }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <div class="action-bar">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      <el-button type="primary" @click="goAgenda(meeting.id)">
        <el-icon><List /></el-icon> 查看议程
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Clock, ArrowLeft, List } from '@element-plus/icons-vue'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()
const meeting = ref({})

onMounted(async () => {
  try {
    meeting.value = await request.get('/agenda/meetings/' + route.params.id)
  } catch (e) {
    meeting.value = { id: route.params.id }
  }
})

function goAgenda(id) {
  router.push('/meetings/' + id + '/agenda')
}

function formatTime(time) {
  if (!time) return '—'
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusText(status) {
  const map = { 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回' }
  return map[status] ?? '未知'
}

function statusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] ?? 'info'
}

function formatMeetingType(type) {
  const map = { 1: '科室例会', 2: '质量复盘', 3: '专题讨论' }
  return map[type] ?? ('类型' + type)
}
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.detail-descriptions {
  margin-top: 8px;
}

.desc-value {
  color: #303133;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.title-text {
  font-weight: 600;
  font-size: 15px;
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>