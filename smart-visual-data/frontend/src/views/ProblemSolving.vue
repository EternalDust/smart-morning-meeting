<template>
  <div style="display:flex; flex-direction:column; gap:20px; height:100%">
    <!-- 问题解决率 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <div style="text-align:center; padding:10px 0">
            <h3 style="margin:0 0 10px; color:#606266; font-size:16px">问题解决率</h3>
            <div style="font-size:48px; font-weight:bold; color:#67c23a">
              {{ realtimeData.resolutionRate }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 异常事件问题部门分布 -->
    <el-row :gutter="20" style="flex:1">
      <el-col :span="24" style="height:100%">
        <el-card shadow="hover" style="height:100%">
          <template #header><span style="font-weight:bold">异常事件问题部门分布</span></template>
          <div ref="pieChartRef" style="height:400px; width:100%"></div>
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

const pieChartRef = ref(null)
let pieChart = null

const initChart = () => {
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      name: '异常归属',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: [],
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: true, formatter: '{b}: {d}%' }
    }]
  })
  fetchData()
}

const fetchData = async () => {
  try {
    const res = await axios.get('/api/dashboard/issues-distribution')
    if (res.data.code === 200) {
      pieChart.setOption({ series: [{ data: res.data.data }] })
    }
  } catch (e) {
    console.error('获取部门分布数据失败', e)
  }
}

const handleResize = () => { if (pieChart) pieChart.resize() }

onMounted(() => {
  initWebSocket()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) pieChart.dispose()
})
</script>
