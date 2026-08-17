<template>
  <el-container style="height: 100vh">
    <el-aside v-if="!isEmbed" width="200px" style="background:#001529">
      <h3 style="color:#fff; text-align:center; padding:15px">晨会督办</h3>
      <el-menu router background-color="#001529" text-color="#fff" active-text-color="#409EFF" style="border-right:none">
        <el-menu-item index="/dashboard">首页</el-menu-item>
        <el-menu-item index="/problems">问题列表</el-menu-item>
        <el-menu-item index="/progress">进度跟踪</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<script setup>
// 从统一门户以 iframe 嵌入时（?embed=1），隐藏子系统自己的侧边栏，避免菜单重复
const isEmbed = new URLSearchParams(window.location.search).get('embed') === '1'
</script>
