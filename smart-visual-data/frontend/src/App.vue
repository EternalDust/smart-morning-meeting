<template>
  <div id="app-container">
    <header class="header">
      <h1>数字医疗智慧晨会协同与决策支撑平台大屏</h1>
      <div class="user-info">权限角色: 高层决策人员 (演示)</div>
    </header>
    <main class="main-content">
      <Dashboard />
    </main>
  </div>
</template>

<script setup>
import Dashboard from './views/Dashboard.vue'
</script>

<style>
/* 简单的大屏深色风格基调 */
body {
  margin: 0;
  padding: 0;
  background-color: #0b0f19;
  color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
#app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.header {
  height: 60px;
  background-color: #1a2235;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  border-bottom: 2px solid #23314f;
}
.header h1 {
  font-size: 24px;
  margin: 0;
  color: #4fd2dd;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}
</style>
