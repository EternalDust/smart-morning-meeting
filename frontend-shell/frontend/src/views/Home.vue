<template>
  <el-container class="shell">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="22" color="#fff"><FirstAidKit /></el-icon>
        <span>数字医疗智慧晨会</span>
      </div>
      <el-menu
        :default-active="activeKey"
        background-color="#001529"
        text-color="#cfd8e3"
        active-text-color="#fff"
        @select="onSelect"
      >
        <el-menu-item-group title="晨会线">
          <el-sub-menu v-if="isAdmin" index="approval">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>会议审批与议程</span>
            </template>
            <el-menu-item v-for="p in MENU.approval.pages" :key="p.route" :index="'approval' + p.route">{{ p.title }}</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="report/meetingroom">
            <el-icon><ChatDotRound /></el-icon>
            <span>签到汇报与互动</span>
          </el-menu-item>
          <el-sub-menu v-if="isAdmin" index="supervise">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>问题督办与闭环</span>
            </template>
            <el-menu-item v-for="p in MENU.supervise.pages" :key="p.route" :index="'supervise' + p.route">{{ p.title }}</el-menu-item>
          </el-sub-menu>
        </el-menu-item-group>
        <el-menu-item-group v-if="isAdmin" title="数据线">
          <el-sub-menu index="collection">
            <template #title>
              <el-icon><DataBoard /></el-icon>
              <span>多源数据采集治理</span>
            </template>
            <el-menu-item v-for="p in MENU.collection.pages" :key="p.route" :index="'collection' + p.route">{{ p.title }}</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="visual">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>可视化与决策</span>
            </template>
            <el-menu-item v-for="p in MENU.visual.pages" :key="p.route" :index="'visual' + p.route">{{ p.title }}</el-menu-item>
          </el-sub-menu>
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

const MENU = {
  approval: {
    title: '会议审批与议程',
    url: 'http://localhost:5175',
    icon: Document,
    pages: [
      { title: '会议列表', route: '/meetings' },
      { title: '创建会议', route: '/meetings/create' },
      { title: '归档中心', route: '/archives' },
      { title: '成员管理', route: '/members' }
    ]
  },
  report: {
    title: '签到汇报与互动',
    url: 'http://localhost:5174',
    icon: ChatDotRound,
    pages: [{ title: '晨会主屏', route: '/meetingroom' }]
  },
  supervise: {
    title: '问题督办与闭环',
    url: 'http://localhost:5177',
    icon: Warning,
    pages: [
      { title: '首页', route: '/dashboard' },
      { title: '问题列表', route: '/problems' },
      { title: '进度跟踪', route: '/progress' },
      { title: '数据统计', route: '/statistics' }
    ]
  },
  collection: {
    title: '多源数据采集治理',
    url: 'http://localhost:5176',
    icon: DataBoard,
    pages: [
      { title: '工作台首页', route: '/dashboard' },
      { title: '数据源管理', route: '/datasource' },
      { title: '质量监控', route: '/quality' },
      { title: '数据溯源', route: '/lineage' },
      { title: '标签管理', route: '/label' }
    ]
  },
  visual: {
    title: '可视化与决策',
    url: 'http://localhost:5173',
    icon: Monitor,
    pages: [
      { title: '参会率', route: '/attendance' },
      { title: '问题解决', route: '/problem-solving' },
      { title: '风险预测', route: '/risk-prediction' },
      { title: '复盘报告', route: '/review-report' }
    ]
  }
}

const activeMod = ref('report')
const activeRoute = ref('/meetingroom')
const userName = ref(localStorage.getItem('userName') || '用户')
const token = localStorage.getItem('token') || ''
const userId = getUserId()
const isAdmin = computed(() => String(userId || '').startsWith('2'))

const activeKey = computed(() => activeMod.value + activeRoute.value)
const activeTitle = computed(() => MENU[activeMod.value].title)
const currentSrc = computed(() => {
  const q = '?token=' + encodeURIComponent(token) +
    '&userName=' + encodeURIComponent(userName.value) + '&embed=1'
  return MENU[activeMod.value].url + activeRoute.value + q
})

const onSelect = (key) => {
  const idx = key.indexOf('/')
  activeMod.value = key.slice(0, idx)
  activeRoute.value = key.slice(idx)
}

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
