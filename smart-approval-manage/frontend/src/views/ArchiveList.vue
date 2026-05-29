<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">归档列表</h2>
      <p class="page-desc">查看已归档的晨会记录</p>
    </div>

    <el-table :data="archives" stripe border style="width: 100%" empty-text="暂无归档数据">
      <el-table-column prop="id" label="编号" width="80" align="center" />
      <el-table-column prop="title" label="会议标题" min-width="200" />
      <el-table-column label="会议类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ formatMeetingType(row.meetingType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="归档时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatTime(row.updateTime) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="action-bar">
      <el-button @click="router.push('/meetings')">返回列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request.js'

const router = useRouter()
const archives = ref([])

onMounted(async () => {
  try {
    const all = await request.get('/agenda/meetings')
    archives.value = all.filter(m => m.approveStatus === 4)
  } catch (e) {
    archives.value = []
  }
})

function formatTime(time) {
  if (!time) return '—'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
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
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
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
.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>