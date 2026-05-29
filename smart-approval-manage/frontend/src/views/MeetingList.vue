<template>
  <div class="page-card">
    <div class="page-header" style="display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h2 class="page-title">晨会列表</h2>
        <p class="page-desc">查看和管理所有晨会的基本信息及审批状态</p>
      </div>
      <el-button type="primary" @click="router.push('/meetings/create')">+ 新建晨会</el-button>
    </div>

    <el-table :data="meetings" stripe border style="width: 100%" empty-text="暂无晨会数据">
      <el-table-column prop="id" label="编号" width="80" align="center" />
      <el-table-column prop="title" label="会议标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="会议类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ formatMeetingType(row.meetingType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.approveStatus)" size="small">
            {{ statusText(row.approveStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" plain @click="goDetail(row.id)">详情</el-button>
          <el-button size="small" type="primary" plain @click="goAgenda(row.id)">议程</el-button>
          <el-button size="small" type="success" plain @click="goRecords(row.id)">审批</el-button>
          <el-button size="small" type="warning" plain @click="goAttendees(row.id)">参会人</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request.js'

const router = useRouter()
const meetings = ref([])

onMounted(async () => {
  try {
    meetings.value = await request.get('/agenda/meetings')
  } catch (e) {
    meetings.value = []
  }
})

function goDetail(id) { router.push('/meetings/' + id) }
function goAgenda(id) { router.push('/meetings/' + id + '/agenda') }
function goRecords(id) { router.push('/meetings/' + id + '/records') }
function goAttendees(id) { router.push('/meetings/' + id + '/attendees') }

function statusText(status) {
  const map = { 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已归档' }
  return map[status] ?? '未知'
}

function statusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'success' }
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
</style>