<template>
  <div class="dashboard-layout">
    <header class="header">
      <h1>数字医疗智慧晨会协同与决策支撑平台大屏</h1>
      <div class="user-info">
        <span>当前角色: {{ userInfo?.roleId === 1 ? '高层决策人员' : (userInfo?.roleId === 2 ? '中层管理人员' : '基层操作人员') }} ({{ userInfo?.deptName }})</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <div class="dashboard">
      <!-- 顶部核心指标卡片 -->
      <div class="metrics-row">
      <div class="metric-card">
        <h3>实时参会率</h3>
        <div class="value" :class="{ 'warning': realtimeData.attendanceRate < 85 }">
          {{ realtimeData.attendanceRate }}%
        </div>
      </div>
      <div class="metric-card">
        <h3>问题解决率</h3>
        <div class="value">{{ realtimeData.resolutionRate }}%</div>
      </div>
      <div class="metric-card">
        <h3>当前大盘风险级别</h3>
        <div class="value" :class="realtimeData.riskLevel.toLowerCase()">
          {{ realtimeData.riskLevel === 'NORMAL' ? '正常' : (realtimeData.riskLevel === 'WARNING' ? '预警' : '未知') }}
        </div>
      </div>
    </div>

    <!-- 图表展示区 -->
    <div class="charts-row">
      <div class="chart-container" ref="trendChartRef"></div>
      <div class="chart-container" ref="pieChartRef"></div>
    </div>

    <!-- 异常预警列表 -->
    <div class="warn-panel">
      <h3>实时异常预警 <span class="warn-badge" v-if="warnings.length">{{ warnings.length }}</span></h3>
      <div class="warn-list" v-if="warnings.length">
        <div v-for="w in warnings" :key="w.id" :class="['warn-item', 'level-' + w.warnLevel]">
          <span class="warn-level-tag">{{ {1:'一般',2:'中等',3:'重大'}[w.warnLevel] || '未知' }}</span>
          <span class="warn-detail">{{ w.detail || w.warnType }}</span>
          <span class="warn-time">{{ w.createTime }}</span>
        </div>
      </div>
      <div class="warn-empty" v-else>暂无预警信息</div>
    </div>

    <div class="actions">
      <button @click="exportData('excel')">导出报表 (Excel)</button>
      <button @click="exportData('pdf')">导出报告 (PDF)</button>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import axios from 'axios';

const router = useRouter();
const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'));

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  router.push('/login');
};

// 配置 axios 请求拦截器带上 Token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- 1. WebSocket 实时通信通道设定 ---
const realtimeData = ref({
  attendanceRate: 0,
  resolutionRate: 0,
  riskLevel: 'NORMAL',
  timestamp: ''
});
const warnings = ref([]);
let ws = null;

const initWebSocket = () => {
  ws = new WebSocket('ws://localhost:8080/ws/morning-meeting');
  ws.onopen = () => console.log('已连接到后端 WebSocket 推送通道');
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      realtimeData.value = data;
      // 在此处可以联动更新ECharts数据以实现动态态势监控
    } catch (e) {
      console.error('解析 WebSocket 数据失败', e);
    }
  };
  ws.onclose = () => console.log('WebSocket 连接断开');
};

// --- 2. ECharts 多维度可视化展现 ---
const trendChartRef = ref(null);
const pieChartRef = ref(null);
let trendChart = null;
let pieChart = null;

const initCharts = () => {
  // 折线图 - 核心指标趋势变化
  trendChart = echarts.init(trendChartRef.value);
  trendChart.setOption({
    backgroundColor: 'transparent',
    title: { text: '近七日参会率趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', max: 100 },
    series: [
      {
        name: '参会率(%)',
        type: 'line',
        smooth: true,
        data: [],
        areaStyle: { opacity: 0.1 }
      }
    ]
  });

  // 饼图/环形图 - 问题解决率部门分布
  pieChart = echarts.init(pieChartRef.value);
  pieChart.setOption({
    backgroundColor: 'transparent',
    title: { text: '异常事件问题部门分布' },
    tooltip: { trigger: 'item' },
    series: [
      {
        name: '异常归属',
        type: 'pie',
        radius: ['40%', '70%'],
        data: []
      }
    ]
  });

  fetchChartData();
  fetchWarnings();
};

const fetchChartData = async () => {
  try {
    // 获取趋势数据
    const trendRes = await axios.get('http://localhost:8080/api/dashboard/trend');
    if (trendRes.data.code === 200) {
      trendChart.setOption({
        xAxis: { data: trendRes.data.data.dates },
        series: [{ data: trendRes.data.data.rates }]
      });
    }

    // 获取科室督办数据
    const issuesRes = await axios.get('http://localhost:8080/api/dashboard/issues-distribution');
    if (issuesRes.data.code === 200) {
      pieChart.setOption({
        series: [{ data: issuesRes.data.data }]
      });
    }
  } catch (error) {
    console.error('获取图表数据失败', error);
  }
};

const fetchWarnings = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/warn/latest?limit=10');
    if (res.data.code === 200) {
      warnings.value = res.data.data || [];
    }
  } catch (e) {
    console.error('获取预警数据失败', e);
  }
};

const handleResize = () => {
  if (trendChart) trendChart.resize();
  if (pieChart) pieChart.resize();
};

const exportData = (type) => {
  // 此处模拟数据下钻及导出功能 (后端可提供/api/export接口，通过 axios Blob 下载)
  alert(`请求后端生成 ${type.toUpperCase()} 文件并开始下载...`);
};

onMounted(() => {
  initWebSocket();
  initCharts();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (ws) ws.close();
  window.removeEventListener('resize', handleResize);
  if (trendChart) trendChart.dispose();
  if (pieChart) pieChart.dispose();
});
</script>

<style scoped>
.dashboard-layout {
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
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-weight: bold;
  color: #606266;
}
.logout-btn {
  background-color: #f56565;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.logout-btn:hover {
  background-color: #c53030;
}
.dashboard {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}
.metrics-row {
  display: flex;
  gap: 20px;
}
.metric-card {
  flex: 1;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.metric-card h3 {
  margin-top: 0;
  color: #606266;
  font-size: 16px;
}
.metric-card .value {
  font-size: 36px;
  font-weight: bold;
  color: #3182ce;
}
.metric-card .value.warning { color: #f56565; }

.charts-row {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 400px;
}
.chart-container {
  flex: 1;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  padding: 10px;
}
.actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}
button {
  background-color: #3182ce;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
button:hover {
  background-color: #2b6cb0;
}

.warn-panel {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.warn-panel h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.warn-badge {
  background: #f56565;
  color: #fff;
  font-size: 12px;
  border-radius: 10px;
  padding: 0 8px;
  line-height: 20px;
}
.warn-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}
.warn-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
}
.warn-item.level-1 { background: #fff3e0; border-left: 3px solid #ff9800; }
.warn-item.level-2 { background: #ffebee; border-left: 3px solid #f44336; }
.warn-item.level-3 { background: #fce4ec; border-left: 3px solid #c62828; }
.warn-level-tag {
  font-weight: bold;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}
.warn-item.level-1 .warn-level-tag { background: #ffe0b2; color: #e65100; }
.warn-item.level-2 .warn-level-tag { background: #ffcdd2; color: #c62828; }
.warn-item.level-3 .warn-level-tag { background: #f8bbd0; color: #880e4f; }
.warn-detail { flex: 1; color: #303133; }
.warn-time { color: #909399; font-size: 12px; white-space: nowrap; }
.warn-empty { color: #c0c4cc; text-align: center; padding: 20px; font-size: 14px; }
</style>
