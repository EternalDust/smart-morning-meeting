<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background:#001529">
      <h3 style="color:#fff; text-align:center; padding:15px; margin:0; font-size:18px">可视化大屏</h3>
      <el-menu
        router
        :default-active="currentRoute"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        style="border-right:none"
      >
        <el-menu-item index="/attendance">
          <el-icon><DataLine /></el-icon>
          <span>参会率</span>
        </el-menu-item>
        <el-menu-item index="/problem-solving">
          <el-icon><CircleCheck /></el-icon>
          <span>问题解决</span>
        </el-menu-item>
        <el-menu-item index="/risk-prediction">
          <el-icon><WarningFilled /></el-icon>
          <span>风险预测</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff; border-bottom:1px solid #eee; display:flex; justify-content:space-between; align-items:center; padding:0 20px">
        <span style="font-size:16px; font-weight:bold; color:#303133">数字医疗智慧晨会协同与决策支撑平台</span>
        <div style="display:flex; align-items:center; gap:15px">
          <span style="color:#606266">{{ userName }}</span>
          <el-button type="danger" text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
const userName = computed(() => {
  return userInfo?.deptName
    ? `${userInfo.name || userInfo.userId} (${userInfo.deptName})`
    : localStorage.getItem('userName') || '用户'
})
const currentRoute = computed(() => route.path)

const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出')
  router.push('/login')
}
</script>
