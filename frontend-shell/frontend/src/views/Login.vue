<template>
  <div class="login-page">
    <div class="login-card">
      <h1>数字医疗智慧晨会平台</h1>
      <p class="subtitle">协同与决策支撑</p>
      <el-form @submit.prevent="doLogin">
        <el-input v-model="userId" placeholder="工号" size="large" style="margin-bottom:12px" />
        <el-input v-model="password" type="password" placeholder="密码" size="large" show-password style="margin-bottom:20px" />
        <el-button type="primary" size="large" @click="doLogin" :loading="loading" style="width:100%">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userId = ref('')
const password = ref('')
const loading = ref(false)

const doLogin = async () => {
  if (!userId.value) { ElMessage.warning('请输入工号'); return }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', { userId: userId.value, password: password.value })
    if (res.data.success) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('userName', res.data.data.userName)
      router.push('/home')
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  }
  loading.value = false
}
</script>

<style scoped>
.login-page { height:100vh; display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg, #1e3a5f 0%, #2563EB 100%) }
.login-card { background:#fff; padding:40px; border-radius:12px; width:380px; box-shadow:0 4px 20px rgba(0,0,0,.15) }
.login-card h1 { font-size:20px; text-align:center; color:#1E293B; margin-bottom:4px }
.subtitle { text-align:center; color:#64748B; font-size:13px; margin-bottom:28px }
</style>
