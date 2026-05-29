<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align:center; margin-bottom:30px">晨会问题督办系统</h2>
      <el-form>
        <el-form-item><el-input v-model="form.account" placeholder="账号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" /></el-form-item>
        <el-form-item><el-button type="primary" size="large" style="width:100%" @click="handleLogin">登录</el-button></el-form-item>
      </el-form>
      <div style="text-align:center; color:#999; font-size:12px">测试账号：admin / 123456</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const form = reactive({ account: 'admin', password: '123456' })

const handleLogin = async () => {
  try {
    const res = await request.post('/auth/login', form)
    if (res.success) {
      localStorage.setItem('token', 'token')
      localStorage.setItem('userName', res.data.userName)
      ElMessage.success('登录成功')
      await router.push('/dashboard')
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 400px; border-radius: 8px; }
</style>