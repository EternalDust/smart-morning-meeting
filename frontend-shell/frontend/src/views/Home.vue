<template>
  <el-container class="shell">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="22" color="#fff"><FirstAidKit /></el-icon>
        <span>数字医疗智慧晨会</span>
      </div>
      <el-menu
        :default-active="active"
        background-color="#001529"
        text-color="#cfd8e3"
        active-text-color="#fff"
        @select="select"
      >
        <el-menu-item-group title="晨会线">
          <el-menu-item v-if="isAdmin" index="approval">
            <el-icon><Document /></el-icon>
            <span>会议审批与议程</span>
          </el-menu-item>
          <el-menu-item index="report">
            <el-icon><ChatDotRound /></el-icon>
            <span>签到汇报与互动</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="supervise">
            <el-icon><Warning /></el-icon>
            <span>问题督办与闭环</span>
          </el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group v-if="isAdmin" title="数据线">
          <el-menu-item v-if="isAdmin" index="collection">
            <el-icon><DataBoard /></el-icon>
            <span>多源数据采集治理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="visual">
            <el-icon><Monitor /></el-icon>
            <span>可视化与决策</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>

    <el-container class="body">
      <el-header class="topbar">
        <span class="mod-title">{{ activeTitle }}</span>
        <span class="spacer"></span>
        <span class="user-name">{{ userName }}</span>
        <el-tag :type="isAdmin ? 'danger' : 'success'" size="small" effect="dark">
          {{ isAdmin ? '管理员' : '参会人' }}
        </el-tag>
        <el-button link class="logout" @click="logout">退出</el-button>
      </el-header>
      <el-main class="main">
        <iframe :src="currentSrc" class="frame" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, ChatDotRound, Document, DataBoard, Warning, FirstAidKit } from '@element-plus/icons-vue'

const router = useRouter()

const MODULES = {
  approval: { title: '会议审批与议程', url: 'http://localhost:5175' },
  report: { title: '签到汇报与互动', url: 'http://localhost:5174' },
  supervise: { title: '问题督办与闭环', url: 'http://localhost:5177' },
  collection: { title: '多源数据采集治理', url: 'http://localhost:5176' },
  visual: { title: '可视化与决策', url: 'http://localhost:5173' }
}

const active = ref('report')
const userName = ref(localStorage.getItem('userName') || '用户')
const token = localStorage.getItem('token') || ''
const userId = getUserId()
const isAdmin = computed(() => String(userId || '').startsWith('2'))

const activeTitle = computed(() => MODULES[active.value].title)
const currentSrc = computed(() => {
  const q = '?token=' + encodeURIComponent(token) + '&userName=' + encodeURIComponent(userName.value)
  return MODULES[active.value].url + q
})

const select = (key) => { active.value = key }

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

function getUserId() {
  const uid = localStorage.getItem('userId')
  if (uid) return uid
  const t = localStorage.getItem('token')
  if (t) {
    try {
      const p = JSON.parse(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
      return p.sub || p.userId || ''
    } catch (e) {}
  }
  return ''
}

onMounted(() => {
  if (!localStorage.getItem('token')) router.push('/login')
})
</script>

<style scoped>
.shell { height: 100vh }
.sidebar { background: #001529 }
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.body { flex-direction: column }
.topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}
.mod-title { font-size: 15px; font-weight: 600; color: #1e293b }
.spacer { flex: 1 }
.user-name { font-size: 13px; color: #475569 }
.logout { color: #dc2626 }
.main { padding: 0; overflow: hidden }
.frame { width: 100%; height: 100%; border: none; display: block }
</style>
