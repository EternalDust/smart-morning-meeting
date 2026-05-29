<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">新建晨会</h2>
      <p class="page-desc">填写会议基础信息并提交创建</p>
    </div>

    <el-form :model="form" label-width="100px" style="max-width:600px">
      <el-form-item label="会议标题" required>
        <el-input v-model="form.title" placeholder="请输入会议标题" />
      </el-form-item>
      <el-form-item label="会议类型">
        <el-select v-model="form.meetingType" placeholder="请选择" style="width:100%">
          <el-option label="科室例会" :value="1" />
          <el-option label="质量复盘" :value="2" />
          <el-option label="专题讨论" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="会议地点">
        <el-input v-model="form.location" placeholder="请输入会议地点" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交创建</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request.js'

const router = useRouter()
const form = reactive({
  title: '',
  meetingType: 1,
  location: '',
  startTime: null,
  endTime: null
})

async function submit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入会议标题')
    return
  }
  try {
    await request.post('/agenda/meetings', form)
    ElMessage.success('创建成功')
    router.push('/meetings')
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  }
}
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}
.page-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.page-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
</style>