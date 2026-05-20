<template>
  <div class="login-wrapper">
    <div class="login-box">
      <h2>智慧晨会系统登录</h2>
      <div class="form-group">
        <label>用户名</label>
        <input type="text" v-model="username" placeholder="admin(高层) / ken101(中层) / staff101(基层)" />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input type="password" v-model="password" placeholder="请输入 hqh123" @keyup.enter="handleLogin" />
      </div>
      <button @click="handleLogin" class="login-btn">登录</button>
      <p class="error-msg" v-if="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('admin')
const password = ref('hqh123')
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: username.value,
      password: password.value
    });
    
    if (res.data.code === 200) {
      // 登录成功，保存 token 和用户信息
      localStorage.setItem('token', res.data.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.data.user));
      // 跳转到大屏
      router.push('/dashboard');
    } else {
      error.value = res.data.message || '登录失败';
    }
  } catch (err) {
    error.value = '服务器连接异常';
    console.error(err);
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
.login-box {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  width: 320px;
  text-align: center;
}
.login-box h2 {
  margin-bottom: 20px;
  color: #3182ce;
}
.form-group {
  margin-bottom: 15px;
  text-align: left;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
}
.form-group input {
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background-color: #3182ce;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}
.login-btn:hover {
  background-color: #2b6cb0;
}
.error-msg {
  color: #f56565;
  margin-top: 10px;
}
</style>