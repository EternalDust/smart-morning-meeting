<template>
  <div class="app-wrapper">
    <div class="app-header">
      <div class="header-inner">
        <div class="header-title">
          <el-icon :size="24" color="#fff"><Calendar /></el-icon>
          <span>数字医疗智慧晨会协同平台</span>
        </div>
        <div class="header-sub">晨会全流程审批与议程管理子系统</div>
      </div>
    </div>
    <div class="app-main">
      <router-view />
    </div>
    <div class="app-footer">
      四川华迪信息技术有限公司 · 企业实习项目
    </div>
  </div>
</template>

<script setup>
import { Calendar } from '@element-plus/icons-vue'
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
}

.header-sub {
  font-size: 14px;
  opacity: 0.9;
}

.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}

.app-footer {
  text-align: center;
  padding: 16px;
  font-size: 12px;
  color: #909399;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}
</style>