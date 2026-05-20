<template>
  <div class="app-layout">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="24"><Monitor /></el-icon>
          <span>数据治理平台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#001529"
          text-color="#ffffffa6"
          active-text-color="#fff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>工作台首页</span>
          </el-menu-item>
          <el-menu-item index="/datasource">
            <el-icon><Connection /></el-icon>
            <span>数据源管理</span>
          </el-menu-item>
          <el-menu-item index="/quality">
            <el-icon><Monitor /></el-icon>
            <span>质量监控</span>
          </el-menu-item>
          <el-menu-item index="/lineage">
            <el-icon><Share /></el-icon>
            <span>数据溯源</span>
          </el-menu-item>
          <el-menu-item index="/label">
            <el-icon><Collection /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="topbar">
          <div class="breadcrumb">
            <el-breadcrumb>
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="user-area">
            <span class="username">{{ userStore.userInfo?.username || '管理员' }}</span>
            <el-tag size="small">{{ roleLabel }}</el-tag>
            <el-button type="danger" text @click="handleLogout">退出</el-button>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '管理者', operator: '操作员' }
  return map[userStore.role] || '操作员'
})

const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-layout { height: 100vh; }
.sidebar {
  background-color: #001529;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ffffff1a;
}
.el-menu { border-right: none; }
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
  height: 52px;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username { color: #333; }
.el-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
