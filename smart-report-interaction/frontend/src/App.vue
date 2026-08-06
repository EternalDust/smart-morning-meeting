<template>
  <div class="app-container">
    <el-menu mode="horizontal" :default-active="activeMenu" router>
      <el-menu-item index="/meetingroom">主屏</el-menu-item>
      <el-menu-item index="/sign">签到</el-menu-item>
      <el-menu-item index="/report">汇报</el-menu-item>
      <el-menu-item index="/interaction">互动</el-menu-item>
      <el-menu-item index="/analytics">分析</el-menu-item>
    </el-menu>
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* { margin:0; padding:0; box-sizing:border-box }
body { font-family:'Microsoft YaHei',sans-serif; background:#F8FAFC; color:#1E293B }
:root {
  --p:#2563EB; --pb:#EFF6FF; --s:#059669; --sb:#ECFDF5;
  --w:#D97706; --wb:#FFFBEB; --d:#DC2626; --db:#FEF2F2;
  --bg:#F8FAFC; --ts:#475569; --bd:#E2E8F0;
  --radius:8px;
  --touch:44px;
}
:focus-visible { outline:3px solid var(--p); outline-offset:2px; border-radius:2px }
@media (prefers-reduced-motion:reduce) {
  *, *::before, *::after { animation-duration:0.01ms !important; transition-duration:0.01ms !important }
}
.app-container { height:100vh; display:flex; flex-direction:column; overflow:hidden }
</style>
