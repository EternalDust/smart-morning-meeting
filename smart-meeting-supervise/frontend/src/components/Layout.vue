<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background:#001529">
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
