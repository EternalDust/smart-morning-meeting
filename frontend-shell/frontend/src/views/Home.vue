<template>
  <div class="home">
    <div class="topbar">
      <span class="greeting">{{ userName }}，欢迎</span>
      <span class="platform">数字医疗智慧晨会协同与决策支撑平台</span>
      <el-button link @click="logout" style="color:#fff">退出</el-button>
    </div>
    <div class="cards">
      <div class="card" v-for="sys in systems" :key="sys.name" @click="open(sys.url)">
        <div class="card-icon" :style="{background:sys.color}">
          <el-icon :size="28"><component :is="sys.icon" /></el-icon>
        </div>
        <div class="card-title">{{ sys.name }}</div>
        <div class="card-desc">{{ sys.desc }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, ChatDotRound, Document, DataBoard, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const userName = ref('')

const systems = [
  { name:'大数据可视化大屏', desc:'晨会数据实时监控与分析决策', url:'http://localhost:5173', color:'#2563EB', icon: Monitor },
  { name:'晨会汇报与实时交互', desc:'签到 · 汇报 · 提问 · 投票', url:'http://localhost:5174', color:'#059669', icon: ChatDotRound },
  { name:'晨会审批与议程管理', desc:'发起晨会 · 拟定议程 · 材料审核 · 流程审批', url:'http://localhost:5175', color:'#7C3AED', icon: Document },
  { name:'多源数据采集与治理', desc:'数据接入 · 清洗 · 标签化 · 溯源', url:'http://localhost:5176', color:'#D97706', icon: DataBoard },
  { name:'问题督办与闭环管理', desc:'问题登记 · 分派 · 进度跟踪 · 结案', url:'http://localhost:5177', color:'#DC2626', icon: Warning },
]

const open = (url) => window.open(url, '_blank')

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

onMounted(() => {
  userName.value = localStorage.getItem('userName') || ''
  if (!localStorage.getItem('token')) router.push('/login')
})
</script>

<style scoped>
.home { min-height:100vh; background:#F1F5F9 }
.topbar { display:flex; align-items:center; padding:0 24px; height:56px; background:#1E293B; color:#fff }
.greeting { font-size:14px }
.platform { flex:1; text-align:center; font-size:15px; font-weight:500 }
.cards { display:flex; justify-content:center; gap:20px; padding:60px 24px; flex-wrap:wrap }
.card { width:200px; padding:28px 20px; background:#fff; border-radius:10px; text-align:center; cursor:pointer; transition: all .2s; box-shadow:0 1px 3px rgba(0,0,0,.06) }
.card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,.1) }
.card-icon { width:56px; height:56px; border-radius:12px; display:flex; align-items:center; justify-content:center; margin:0 auto 14px }
.card-icon :deep(.el-icon) { color:#fff }
.card-title { font-size:14px; font-weight:600; margin-bottom:6px; color:#1E293B }
.card-desc { font-size:12px; color:#64748B; line-height:1.5 }
</style>
