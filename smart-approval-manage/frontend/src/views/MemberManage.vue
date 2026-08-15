<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">人员管理</h2>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="workNo" label="工号" width="120" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="deptId" label="科室ID" width="100" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
            {{ row.role === 1 ? '管理层' : '医护' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const list = ref([])

onMounted(async () => {
  try {
    list.value = await request.get('/members')
  } catch (e) {
    list.value = []
  }
})
</script>