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
    <el-container>
      <el-header style="background:#fff; border-bottom:1px solid #eee; display:flex; justify-content:flex-end; align-items:center">
        <span style="color:#666; margin-right:8px">演示身份</span>
        <el-select v-model="demoAccount" size="small" style="width:170px" @change="switchIdentity">
          <el-option label="督办专员（2001 杨辉）" value="2001" />
          <el-option label="执行责任人（1003 王芳）" value="1003" />
        </el-select>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'

const demoAccount = ref(localStorage.getItem('account') || '2001')

const switchIdentity = (account) => {
  localStorage.setItem('account', account)
  location.reload()
}
</script>
