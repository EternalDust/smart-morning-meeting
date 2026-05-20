<template>
  <div style="padding: 20px">
    <h2>晨会列表</h2>
    <el-table :data="meetings" style="margin-top: 16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="approveStatus" label="审批状态" width="120" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="goDetail(row.id)">详情</el-button>
          <el-button size="small" type="primary" @click="goAgenda(row.id)">议程</el-button>
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

function goDetail(id) {
  router.push('/meetings/' + id)
}

function goAgenda(id) {
  router.push('/meetings/' + id + '/agenda')
}
</script>
