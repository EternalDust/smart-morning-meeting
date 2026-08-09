<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>数据分析</h2>
    </div>

    <div class="content">
      <div class="panel">
        <h3>全平台会议趋势</h3>
        <div ref="trendChart" style="width:100%;height:320px"></div>
      </div>

      <div class="panel">
        <h3>科室对比</h3>
        <div ref="deptChart" style="width:100%;height:320px"></div>
      </div>

      <div class="panel">
        <h3>科室排名详情</h3>
        <el-table :data="departments" stripe style="width:100%">
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="department" label="科室" />
          <el-table-column prop="avgAttendRate" label="平均出勤率(%)" />
          <el-table-column prop="meetingCount" label="参会次数" />
          <el-table-column prop="totalSpeechCount" label="累计发言" />
          <el-table-column prop="totalInteractionCount" label="累计互动" />
        </el-table>
      </div>

      <div class="panel">
        <h3>会议评分明细</h3>
        <el-table :data="meetings" stripe style="width:100%">
          <el-table-column prop="meetingTitle" label="会议" />
          <el-table-column prop="meetingDate" label="日期" />
          <el-table-column prop="attendRate" label="出勤率(%)" />
          <el-table-column prop="speechCount" label="发言数" />
          <el-table-column prop="interactionCount" label="互动数" />
          <el-table-column prop="qualityScore" label="质量评分" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMeetingTrend, getDepartmentRanking } from '../api/analytics'

const trendChart = ref(null)
const deptChart = ref(null)
const departments = ref([])
const meetings = ref([])
let trendChartInstance = null
let deptChartInstance = null

const onResize = () => {
  if (trendChartInstance) trendChartInstance.resize()
  if (deptChartInstance) deptChartInstance.resize()
}

onMounted(async () => {
  const [trendData, deptData] = await Promise.all([
    getMeetingTrend(),
    getDepartmentRanking()
  ])

  meetings.value = trendData.data || []
  departments.value = deptData.data || []

  await nextTick()
  renderTrendChart()
  renderDeptChart()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (trendChartInstance) trendChartInstance.dispose()
  if (deptChartInstance) deptChartInstance.dispose()
  trendChartInstance = null
  deptChartInstance = null
})

function renderTrendChart() {
  if (!trendChart.value) return
  trendChartInstance = echarts.init(trendChart.value)
  const list = meetings.value
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['出勤率', '发言数', '互动数'] },
    xAxis: { type: 'category', data: list.map(m => m.meetingDate || m.meetingTitle) },
    yAxis: [
      { type: 'value', name: '出勤率(%)', max: 100 },
      { type: 'value', name: '数量' }
    ],
    series: [
      { name: '出勤率', type: 'line', data: list.map(m => m.attendRate), smooth: true },
      { name: '发言数', type: 'line', yAxisIndex: 1, data: list.map(m => m.speechCount), smooth: true },
      { name: '互动数', type: 'line', yAxisIndex: 1, data: list.map(m => m.interactionCount), smooth: true }
    ]
  })
}

function renderDeptChart() {
  if (!deptChart.value) return
  deptChartInstance = echarts.init(deptChart.value)
  const list = departments.value
  deptChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: list.map(d => d.department) },
    yAxis: { type: 'value', name: '出勤率(%)', max: 100 },
    series: [{
      name: '平均出勤率', type: 'bar', data: list.map(d => d.avgAttendRate),
      itemStyle: { color: getComputedStyle(document.documentElement).getPropertyValue('--p').trim() || '#0891B2' }
    }]
  })
}
</script>
