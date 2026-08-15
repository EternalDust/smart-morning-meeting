<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">归档高级检索</h2>
    </div>
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="标题">
        <el-input v-model="query.title" placeholder="模糊搜索" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.approveStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="草稿" :value="0" />
          <el-option label="审批中" :value="1" />
          <el-option label="已通过" :value="2" />
          <el-option label="已驳回" :value="3" />
          <el-option label="已归档" :value="4" />
          <el-option label="已发布" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="query.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="query.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">检索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.approveStatus)" size="small">{{ statusText(row.approveStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '../api/request.js'

const query = reactive({ title: '', approveStatus: null, startDate: '', endDate: '' })
const list = ref([])

async function search() {
  const params = new URLSearchParams()
  if (query.title) params.append('title', query.title)
  if (query.approveStatus !== null) params.append('approveStatus', query.approveStatus)
  if (query.startDate) params.append('startDate', query.startDate)
  if (query.endDate) params.append('endDate', query.endDate)
  list.value = await request.get('/agenda/meetings/search?' + params.toString())
}

function statusText(status) {
  const map = {0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已归档', 5: '已发布'}
  return map[status] ?? '未知'
}
function statusType(status) {
  const map = {0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'success', 5: 'primary'}
  return map[status] ?? 'info'
}
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
</style>