<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">审计日志</h2>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="operationType" label="操作类型" width="140" />
      <el-table-column prop="targetType" label="对象类型" width="100" />
      <el-table-column prop="targetId" label="对象ID" width="100" />
      <el-table-column prop="operatorId" label="操作人" width="100" />
      <el-table-column prop="newValue" label="详情" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const list = ref([])

onMounted(async () => {
  try {
    list.value = await request.get('/audit')
  } catch (e) {
    list.value = []
  }
})
</script>