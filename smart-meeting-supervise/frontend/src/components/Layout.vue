<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background:#001529">
      <h3 style="color:#fff; text-align:center; padding:15px">晨会督办</h3>
      <el-menu router background-color="#001529" text-color="#fff" active-text-color="#409EFF" style="border-right:none">
        <el-menu-item index="/dashboard">首页</el-menu-item>
        <el-menu-item index="/problems">问题列表</el-menu-item>
        <el-menu-item index="/progress">进度跟踪</el-menu-item>
        <el-menu-item index="/statistics">数据统计</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff; border-bottom:1px solid #eee; display:flex; justify-content:flex-end; align-items:center">
        <span>{{ userName }}</span>
        <el-button type="danger" text @click="logout" style="margin-left:15px">退出</el-button>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userName = ref(localStorage.getItem('userName') || '管理员')

const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出')
  router.push('/login')
}
</script>