<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align:center; margin-bottom:30px">可视化数据大屏</h2>
      <el-form>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <div v-if="error" style="text-align:center; color:#f56c6c; font-size:13px; margin-top:10px">{{ error }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const form = reactive({ username: 'admin', password: 'hqh123' })
const error = ref('')

const handleLogin = async () => {
  try {
    const res = await axios.post('/api/auth/login', form)
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data.user))
      ElMessage.success('登录成功')
      router.push('/attendance')
    } else {
      error.value = res.data.message || '登录失败'
    }
  } catch (err) {
    error.value = '服务器连接异常'
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
