<template>
  <div>
    <el-card>
      <div style="display:flex; gap:10px">
        <el-input v-model="problemId" placeholder="问题ID" style="width:200px" />
        <el-button type="primary" @click="loadProgress">查询</el-button>
      </div>
      <div v-if="currentProgress !== null" style="margin-top:20px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前进度">
            <el-progress :percentage="currentProgress" />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else-if="searched" style="margin-top:20px">
        <el-alert title="暂无进度数据" type="info" :closable="false" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../api/request'

const problemId = ref('')
const currentProgress = ref(null)
const searched = ref(false)

const loadProgress = async () => {
  if (!problemId.value) {
    return
  }
  searched.value = true
  const res = await request.get(`/supervise/progress/current/${problemId.value}`)
  if (res.success && res.data) {
    currentProgress.value = res.data.progress
  } else {
    currentProgress.value = null
  }
}
</script>