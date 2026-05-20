<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 质量评分趋势 -->
      <el-col :span="14">
        <el-card title="质量评分趋势（近7天）" class="chart-card">
          <template #header><span>质量评分趋势（近7天）</span></template>
          <div ref="trendChartRef" style="height:300px"></div>
        </el-card>
      </el-col>

      <!-- 数据源状态 -->
      <el-col :span="10">
        <el-card class="chart-card">
          <template #header><span>数据源接入状态</span></template>
          <div class="source-status-list">
            <div v-for="src in sourceList" :key="src.id" class="source-item">
              <span :class="['status-dot', src.status === 'connected' ? 'online' : 'offline']"></span>
              <span class="source-name">{{ src.sourceName }}</span>
              <span class="source-type">{{ src.sourceType.toUpperCase() }}</span>
              <span class="source-time">{{ src.lastSyncTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- 数据源类型分布 -->
      <el-col :span="8">
        <el-card title="数据源类型分布" class="chart-card">
          <template #header><span>数据源类型分布</span></template>
          <div ref="pieChartRef" style="height:260px"></div>
        </el-card>
      </el-col>

      <!-- 处理延迟监控 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header><span>服务处理延迟</span></template>
          <div class="delay-list">
            <div v-for="(val, key) in delayData" :key="key" class="delay-item">
              <span class="delay-label">{{ key }}</span>
              <span class="delay-value">{{ val }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 异常统计 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header><span>异常记录统计</span></template>
          <div class="anomaly-summary">
            <div class="anomaly-big">{{ overview.anomalyRecordCount }}</div>
            <div class="anomaly-sub">条异常待处理</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getSourceStatus, getQualityTrend, getProcessingDelay } from '@/api/dashboard'

const trendChartRef = ref(null)
const pieChartRef = ref(null)

const overview = reactive({
  totalDataSourceCount: 0,
  activeDataSourceCount: 0,
  todayDataVolume: 0,
  averageQualityScore: 0,
  anomalyRecordCount: 0,
  systemUptime: ''
})

const statCards = computed(() => [
  { label: '数据源总数', value: overview.totalDataSourceCount, color: '#1890ff' },
  { label: '活跃数据源', value: overview.activeDataSourceCount, color: '#52c41a' },
  { label: '今日数据量', value: overview.todayDataVolume?.toLocaleString(), color: '#722ed1' },
  { label: '平均质量分', value: overview.averageQualityScore, color: '#fa8c16' },
  { label: '异常记录', value: overview.anomalyRecordCount, color: '#f5222d' },
  { label: '系统可用率', value: overview.systemUptime, color: '#13c2c2' }
])

const sourceList = ref([])
const delayData = ref({})

import { computed } from 'vue'

const initTrendChart = async (trendData) => {
  await nextTick()
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: Object.keys(trendData) },
    yAxis: { type: 'value', min: 80, max: 100 },
    series: [{
      data: Object.values(trendData),
      type: 'line',
      smooth: true,
      lineStyle: { color: '#1890ff', width: 2 },
      areaStyle: { color: 'rgba(24,144,255,0.15)' },
      itemStyle: { color: '#1890ff' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const initPieChart = async (distribution) => {
  await nextTick()
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      data: Object.entries(distribution).map(([name, value]) => ({ name, value })),
      label: { formatter: '{b}\n{d}%' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  const ov = await getOverview()
  Object.assign(overview, ov.data)

  const ss = await getSourceStatus()
  sourceList.value = ss.data.sources

  const qt = await getQualityTrend()
  initTrendChart(qt.data)

  initPieChart(ov.data.sourceTypeDistribution || {})

  const pd = await getProcessingDelay()
  delayData.value = pd.data
})
</script>

<style scoped>
.stat-cards { margin-bottom: 16px; }
.stat-item { text-align: center; padding: 8px 0; }
.stat-value { font-size: 26px; font-weight: 700; }
.stat-label { font-size: 13px; color: #8c8c8c; margin-top: 4px; }
.chart-card { margin-bottom: 0; }
.source-status-list { max-height: 300px; overflow-y: auto; }
.source-item { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; gap: 8px; font-size: 13px; }
.source-item:last-child { border-bottom: none; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-dot.online { background: #52c41a; }
.status-dot.offline { background: #f5222d; }
.source-name { flex: 1; color: #333; }
.source-type { color: #1890ff; font-size: 11px; background: #e6f7ff; padding: 1px 6px; border-radius: 2px; }
.source-time { color: #999; font-size: 11px; }
.delay-list { padding: 8px 0; }
.delay-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.delay-item:last-child { border-bottom: none; }
.delay-label { color: #666; }
.delay-value { color: #333; font-weight: 600; }
.anomaly-summary { text-align: center; padding: 40px 0; }
.anomaly-big { font-size: 48px; font-weight: 700; color: #f5222d; }
.anomaly-sub { color: #999; margin-top: 8px; }
</style>
