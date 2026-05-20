<template>
  <div id="app-container">
    <!-- 将头部挪入Dashboard中，这里交给路由处理页面的切换 -->
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
/* 简单的大屏浅色风格基调 */
body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
  color: #333333;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
#app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.header {
  height: 60px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  border-bottom: 2px solid #e4e7ed;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.header h1 {
  font-size: 24px;
  margin: 0;
  color: #3182ce;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}
</style>
