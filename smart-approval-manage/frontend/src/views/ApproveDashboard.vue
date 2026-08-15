<template>
  <div class="page-card">
    <h2 class="page-title">审批进度追踪</h2>
    <el-empty v-if="!records.length" description="暂无审批记录" />
    <el-timeline v-else>
      <el-timeline-item
          v-for="r in records"
          :key="r.id"
          :type="r.action === 1 ? 'success' : r.action === 2 ? 'danger' : 'primary'"
          :timestamp="r.approveTime"
      >
        <p>{{ r.nodeName }} - {{ r.action === 1 ? '通过' : r.action === 2 ? '驳回' : '待审批' }}</p>
        <p v-if="r.opinion" style="color:#909399;font-size:12px">意见：{{ r.opinion }}</p>
      </el-timeline-item>
    </el-timeline>

    <el-alert
        v-if="isTimeout"
        title="该会议审批已超时，请联系管理员"
        type="error"
        :closable="false"
        style="margin-top:16px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '../api/request.js'

const route = useRoute()
const records = ref([])
const meeting = ref(null)

const isTimeout = computed(() => {
  if (!meeting.value || !meeting.value.createTime) return false
  return meeting.value.approveStatus === 1
      && (Date.now() - new Date(meeting.value.createTime).getTime() > 86400000)
})

onMounted(async () => {
  const mid = route.params.id
  records.value = await request.get(`/agenda/${mid}/records`)
  meeting.value = await request.get(`/agenda/meetings/${mid}`)
})
</script>