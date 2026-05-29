<template>
  <div>
    <el-card>
      <div style="display:flex; justify-content:space-between; margin-bottom:15px">
        <span>问题列表</span>
        <el-button type="primary" @click="showAdd=true">新增问题</el-button>
      </div>
      <el-table :data="problems" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status===0?'info':'success'">{{ row.status===0?'待分派':'处理中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="新增问题">
      <el-form :model="newProblem">
        <el-form-item label="标题"><el-input v-model="newProblem.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="newProblem.content" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">取消</el-button>
        <el-button type="primary" @click="addProblem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const problems = ref([])
const showAdd = ref(false)
const newProblem = ref({ title: '', content: '' })

const loadProblems = async () => {
  const res = await request.get('/supervise/problem/list?page=1&size=10')
  if (res.success) problems.value = res.data?.records || []
}

const addProblem = async () => {
  if (!newProblem.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  const res = await request.post('/supervise/problem/add', newProblem.value)
  if (res.success) {
    ElMessage.success('添加成功')
    showAdd.value = false
    newProblem.value = { title: '', content: '' }
    loadProblems()
  }
}

onMounted(() => loadProblems())
</script>