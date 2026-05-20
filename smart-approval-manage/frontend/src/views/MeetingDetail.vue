<template>
  <div style="padding: 20px">
    <h2>晨会详情</h2>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="ID">{{ meeting.id }}</el-descriptions-item>
      <el-descriptions-item label="标题">{{ meeting.title }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ meeting.meetingType }}</el-descriptions-item>
      <el-descriptions-item label="地点">{{ meeting.location }}</el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ meeting.startTime }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">{{ meeting.endTime }}</el-descriptions-item>
    </el-descriptions>
    <el-button style="margin-top: 16px" @click="router.back()">返回</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
</script>
