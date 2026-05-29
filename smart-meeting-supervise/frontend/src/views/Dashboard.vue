<template>
  <div>
    <h2>欢迎使用晨会问题督办系统</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="color:#666">总问题数</div>
            <div style="font-size:32px; color:#409EFF">{{ stats.total }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="color:#666">处理中</div>
            <div style="font-size:32px; color:#E6A23C">{{ stats.processing }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="color:#666">待复查</div>
            <div style="font-size:32px; color:#F56C6C">{{ stats.waitCheck }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="color:#666">已闭环</div>
            <div style="font-size:32px; color:#67C23A">{{ stats.closed }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const stats = ref({ total: 0, processing: 0, waitCheck: 0, closed: 0 })

onMounted(async () => {
  const res = await request.get('/supervise/statistics/overview')
  if (res.success) {
    stats.value.total = res.data.total || 0
    stats.value.processing = res.data.statusDetail?.processing || 0
    stats.value.waitCheck = res.data.statusDetail?.waitCheck || 0
    stats.value.closed = res.data.statusDetail?.closed || 0
  }
})
</script>