<template>
  <div style="padding: 20px">
    <h2>议程管理</h2>
    <el-form :model="form" label-width="120px">
      <el-form-item label="科室名称">
        <el-input v-model="form.deptName" />
      </el-form-item>
      <el-form-item label="会议类型">
        <el-input v-model.number="form.meetingType" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="generateAgenda">AI 生成议程</el-button>
      </el-form-item>
    </el-form>
    <el-divider />
    <h3>议程列表</h3>
    <el-table :data="agendaList" style="margin-top: 16px">
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column prop="title" label="标题" />
    </el-table>
    <el-button style="margin-top: 16px" @click="router.back()">返回</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()

const form = ref({
  meetingId: route.params.id,
  deptName: '',
  meetingType: 1
})

const agendaList = ref([])

async function generateAgenda() {
  try {
    const res = await request.post('/agenda/ai-generate', {
      meeting_id: Number(form.value.meetingId),
      dept_name: form.value.deptName,
      meeting_type: form.value.meetingType
    })
    agendaList.value = res.map((title, idx) => ({ title, sort: idx + 1 }))
  } catch (e) {
    ElMessage.error(e.message || '请求失败')
  }
}
</script>