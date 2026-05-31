<template>
  <div style="display:flex; flex-direction:column; gap:20px; height:100%">
    <!-- 实时参会率 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <div style="text-align:center; padding:10px 0">
            <h3 style="margin:0 0 10px; color:#606266; font-size:16px">实时参会率</h3>
            <div :style="{ fontSize:'48px', fontWeight:'bold', color: realtimeData.attendanceRate < 85 ? '#f56c6c' : '#409EFF' }">
              {{ realtimeData.attendanceRate }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 近七日参会率趋势 -->
    <el-row :gutter="20" style="flex:1">
      <el-col :span="24" style="height:100%">
        <el-card shadow="hover" style="height:100%">
          <template #header><span style="font-weight:bold">近七日参会率趋势</span></template>
          <div ref="trendChartRef" style="height:400px; width:100%"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { useRealtime } from '../composables/useRealtime'

const { realtimeData, initWebSocket, closeWebSocket } = useRealtime()

const trendChartRef = ref(null)
let trendChart = null

const initChart = () => {
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      name: '参会率(%)',
      type: 'line',
      smooth: true,
      data: [],
      areaStyle: { opacity: 0.1 },
      itemStyle: { color: '#409EFF' }
    }]
  })
  fetchTrend()
}

const fetchTrend = async () => {
  try {
    const res = await axios.get('/api/dashboard/trend')
    if (res.data.code === 200) {
      trendChart.setOption({
        xAxis: { data: res.data.data.dates },
        series: [{ data: res.data.data.rates }]
      })
    }
  } catch (e) {
    console.error('获取趋势数据失败', e)
  }
}

const handleResize = () => { if (trendChart) trendChart.resize() }

onMounted(() => {
  initWebSocket()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) trendChart.dispose()
})
</script>
