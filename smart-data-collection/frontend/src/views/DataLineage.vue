<template>
  <div class="lineage-page">
    <div class="page-header">
      <span class="page-title">数据血缘溯源</span>
      <el-radio-group v-model="level" size="small">
        <el-radio-button value="table">表级血缘</el-radio-button>
        <el-radio-button value="record">记录级血缘</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 血缘图 -->
    <el-card class="section-card">
      <template #header><span>数据流转DAG图</span></template>
      <div ref="graphRef" style="height:400px"></div>
    </el-card>

    <!-- 表级血缘查询 -->
    <el-card class="section-card" v-if="level === 'table'">
      <template #header>
        <div class="card-header">
          <span>表级血缘查询</span>
          <div>
            <el-input v-model="tableName" placeholder="输入表名，如 ods_raw_data" style="width:220px" size="small" />
            <el-button type="primary" size="small" @click="queryTableLineage" style="margin-left:8px">查询</el-button>
          </div>
        </div>
      </template>
      <div v-if="tableLineage">
        <div class="lineage-section">
          <h4>上游数据源</h4>
          <el-table :data="tableLineage.upstream" border size="small">
            <el-table-column prop="table" label="表名" />
            <el-table-column prop="description" label="说明" />
            <el-table-column prop="process" label="处理过程" />
          </el-table>
        </div>
        <div class="lineage-section">
          <h4>下游数据目标</h4>
          <el-table :data="tableLineage.downstream" border size="small">
            <el-table-column prop="table" label="表名" />
            <el-table-column prop="description" label="说明" />
            <el-table-column prop="process" label="处理过程" />
          </el-table>
        </div>
      </div>
      <el-empty v-else description="请搜索表名查看血缘" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getTableLineage, getLineageGraph } from '@/api/lineage'

const level = ref('table')
const tableName = ref('')
const tableLineage = ref(null)
const graphRef = ref(null)

const initGraph = async () => {
  const res = await getLineageGraph(level.value)
  const { nodes, edges } = res.data
  await nextTick()
  if (!graphRef.value) return
  const chart = echarts.init(graphRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}' },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      force: { repulsion: 200, edgeLength: 150 },
      data: nodes.map(n => ({
        name: n.id,
        symbolSize: 40,
        itemStyle: { color: n.type === 'source' ? '#52c41a' : n.type === 'target' ? '#1890ff' : '#faad14' },
        label: { show: true, formatter: '{b}' }
      })),
      links: edges.map(e => ({ source: e.source, target: e.target, label: { show: true, formatter: e.label } }))
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const queryTableLineage = async () => {
  if (!tableName.value) return
  const res = await getTableLineage(tableName.value)
  tableLineage.value = res.data
}

watch(level, initGraph)
onMounted(initGraph)
</script>

<style scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #333; }
.section-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.lineage-section { margin-bottom: 16px; }
.lineage-section h4 { margin: 0 0 8px; color: #555; }
</style>
