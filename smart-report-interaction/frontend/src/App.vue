<template>
  <div class="app-container">
    <el-menu mode="horizontal" :default-active="activeMenu" router>
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
@import url('https://fonts.googleapis.com/css2?family=Figtree:wght@400;500;600;700&family=Noto+Sans:wght@400;500;700&display=swap');

* { margin:0; padding:0; box-sizing:border-box }
body {
  font-family:'Noto Sans','Figtree','Microsoft YaHei',sans-serif;
  background:#ECFEFF; color:#164E63;
  -webkit-font-smoothing:antialiased;
}
:root {
  --p:#0891B2; --pb:#CFFAFE; --s:#059669; --sb:#D1FAE5;
  --w:#D97706; --wb:#FEF3C7; --d:#DC2626; --db:#FEE2E2;
  --bg:#ECFEFF; --ts:#64748B; --bd:#A5F3FC;
  --radius:8px;
  --touch:44px;
  --el-color-primary:#0891B2;
  --el-color-primary-light-3:#22D3EE;
  --el-color-primary-light-5:#67E8F9;
  --el-color-primary-dark-2:#0E7490;
}
:focus-visible { outline:3px solid var(--p); outline-offset:2px; border-radius:2px }
@media (max-width:767px) {
  body { font-size:15px }
}
@media (min-width:768px) and (max-width:1023px) {
  body { font-size:16px }
}
@media (min-width:1024px) {
  body { font-size:16px }
}
@media (prefers-reduced-motion:reduce) {
  *, *::before, *::after { animation-duration:0.01ms !important; transition-duration:0.01ms !important }
}
.app-container { height:100vh; display:flex; flex-direction:column; overflow:hidden }
</style>
