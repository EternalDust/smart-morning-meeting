<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">审批记录</h2>
      <p class="page-desc">查看当前会议的审批流转历史</p>
    </div>

    <el-timeline v-if="records.length > 0">
      <el-timeline-item
          v-for="r in records"
          :key="r.id"
          :type="r.action === 1 ? 'success' : 'danger'"
          :timestamp="formatTime(r.approveTime)"
      >
        <p><strong>{{ r.nodeName || '审批节点' }}</strong> — {{ r.action === 1 ? '通过' : '驳回' }}</p>
        <p style="color:#606266; font-size:13px">审批人ID: {{ r.approverId }} | 意见: {{ r.opinion }}</p>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-else description="暂无审批记录" />

    <div class="action-bar">
      <el-button @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()
const records = ref([])

onMounted(async () => {
  try {
    records.value = await request.get('/agenda/' + route.params.id + '/records')
  } catch (e) {
    records.value = []
  }
})

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
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