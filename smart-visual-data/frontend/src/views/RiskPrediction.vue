<template>
  <div style="display:flex; flex-direction:column; gap:20px; height:100%">
    <!-- 当前大盘风险级别 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <div style="text-align:center; padding:10px 0">
            <h3 style="margin:0 0 10px; color:#606266; font-size:16px">当前大盘风险级别</h3>
            <div :style="{ fontSize:'48px', fontWeight:'bold', color: riskColor }">
              {{ riskLabel }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 实时异常预警 -->
    <el-row :gutter="20" style="flex:1">
      <el-col :span="24" style="height:100%">
        <el-card shadow="hover" style="height:100%">
          <template #header>
            <div style="display:flex; align-items:center; gap:8px">
              <span style="font-weight:bold">实时异常预警</span>
              <el-tag v-if="warnings.length" type="danger" size="small">{{ warnings.length }}</el-tag>
            </div>
          </template>
          <div v-if="warnings.length" style="display:flex; flex-direction:column; gap:8px">
            <div
              v-for="w in warnings"
              :key="w.id"
              :style="{
                display:'flex', alignItems:'center', gap:'12px',
                padding:'10px 14px', borderRadius:'6px', fontSize:'14px',
                borderLeft: '3px solid ' + levelColor(w.warnLevel),
                background: levelBg(w.warnLevel)
              }"
            >
              <el-tag :type="w.warnLevel >= 3 ? 'danger' : (w.warnLevel === 2 ? 'warning' : 'info')" size="small">
                {{ {1:'一般',2:'中等',3:'重大'}[w.warnLevel] || '未知' }}
              </el-tag>
              <span style="flex:1; color:#303133">{{ w.detail || w.warnType }}</span>
              <span style="color:#909399; font-size:12px; white-space:nowrap">{{ w.createTime }}</span>
            </div>
          </div>
          <div v-else style="text-align:center; color:#c0c4cc; padding:40px 0">暂无预警信息</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import axios from 'axios'
import { useRealtime } from '../composables/useRealtime'

const { realtimeData, warnings, initWebSocket } = useRealtime()

const riskLabel = computed(() => {
  const level = realtimeData.value.riskLevel
  return level === 'NORMAL' ? '正常' : (level === 'WARNING' ? '预警' : '未知')
})
const riskColor = computed(() => {
  const level = realtimeData.value.riskLevel
  return level === 'WARNING' ? '#e6a23c' : '#67c23a'
})

const levelColor = (level) => {
  if (level >= 3) return '#c62828'
  if (level === 2) return '#f44336'
  return '#ff9800'
}
const levelBg = (level) => {
  if (level >= 3) return '#fce4ec'
  if (level === 2) return '#ffebee'
  return '#fff3e0'
}

const fetchWarnings = async () => {
  try {
    const res = await axios.get('/api/warn/latest?limit=10')
    if (res.data.code === 200) {
      warnings.value = res.data.data || []
    }
  } catch (e) {
    console.error('获取预警数据失败', e)
  }
}

onMounted(() => {
  initWebSocket()
  fetchWarnings()
})
</script>
