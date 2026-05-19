<template>
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

    <div class="actions">
      <button @click="exportData('excel')">导出报表 (Excel)</button>
      <button @click="exportData('pdf')">导出报告 (PDF)</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

// --- 1. WebSocket 实时通信通道设定 ---
const realtimeData = ref({
  attendanceRate: 0,
  resolutionRate: 0,
  riskLevel: 'NORMAL',
  timestamp: ''
});
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
  trendChart = echarts.init(trendChartRef.value, 'dark');
  trendChart.setOption({
    backgroundColor: 'transparent',
    title: { text: '近七日参会率趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['一', '二', '三', '四', '五', '六', '日'] },
    yAxis: { type: 'value', max: 100 },
    series: [
      {
        name: '参会率(%)',
        type: 'line',
        smooth: true,
        data: [92, 95, 88, 98, 96, 99, 93],
        areaStyle: { opacity: 0.1 }
      }
    ]
  });

  // 饼图/环形图 - 问题解决率部门分布
  pieChart = echarts.init(pieChartRef.value, 'dark');
  pieChart.setOption({
    backgroundColor: 'transparent',
    title: { text: '异常事件问题部门分布' },
    tooltip: { trigger: 'item' },
    series: [
      {
        name: '异常归属',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 1048, name: '心内科' },
          { value: 735, name: '神经内科' },
          { value: 580, name: '急诊科' },
          { value: 484, name: '外科' }
        ]
      }
    ]
  });
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
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}
.metrics-row {
  display: flex;
  gap: 20px;
}
.metric-card {
  flex: 1;
  background-color: #1a2235;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #23314f;
}
.metric-card h3 {
  margin-top: 0;
  color: #a0aec0;
  font-size: 16px;
}
.metric-card .value {
  font-size: 36px;
  font-weight: bold;
  color: #4fd2dd;
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
  background-color: #1a2235;
  border-radius: 8px;
  border: 1px solid #23314f;
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
</style>
