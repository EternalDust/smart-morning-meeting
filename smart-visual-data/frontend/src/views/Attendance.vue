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
    <!-- 会议数据概览（真实业务数据：签到/发言/互动/医疗质量分） -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:bold">会议数据概览（最近有数据会议日）</span>
          </template>
          <el-table :data="overviewList" stripe border style="width:100%">
            <el-table-column prop="date" label="日期" width="120" align="center" />
            <el-table-column label="应到" prop="shouldNum" width="90" align="center" />
            <el-table-column label="实到" prop="realNum" width="90" align="center" />
            <el-table-column label="参会率" align="center" width="110">
              <template #default="{ row }">
                <el-tag :type="Number(row.attendRate) < 85 ? 'danger' : 'success'">{{ row.attendRate }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="speechCount" label="发言数" width="90" align="center" />
            <el-table-column prop="interactionCount" label="互动数" width="90" align="center" />
            <el-table-column label="医疗质量分" align="center">
              <template #default="{ row }">
                {{ Number(row.qualityScore).toFixed(1) }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="overviewList.length === 0" style="text-align:center; color:#909399; padding:12px">
            暂无真实会议数据，请确认已加载 sm_meeting_* 演示数据
          </div>
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
const overviewList = ref([])

const fetchOverview = async () => {
  try {
    const res = await axios.get('/api/dashboard/meeting-overview')
    if (res.data.code === 200) {
      overviewList.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取会议数据概览失败', e)
  }
}

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
  fetchOverview()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) trendChart.dispose()
})
</script>
