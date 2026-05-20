<template>
  <div class="label-page">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 智能标签 Tab -->
      <el-tab-pane label="智能标签" name="labels">
        <div class="tab-header">
          <span>标签管理（医生维度）</span>
          <el-button type="primary" size="small" @click="handleGenerateLabels">
            <el-icon><Refresh /></el-icon> 生成标签
          </el-button>
        </div>
        <el-table :data="labels" border stripe style="width:100%; margin-top:12px">
          <el-table-column prop="labelName" label="标签名称" min-width="150" />
          <el-table-column prop="rule" label="生成规则" min-width="280" />
          <el-table-column prop="level" label="等级" width="100">
            <template #default="{ row }">
              <el-tag :type="row.level === 'high' ? '' : row.level === 'medium' ? 'success' : 'info'" size="small">
                {{ row.level === 'high' ? '高优先级' : row.level === 'medium' ? '中等' : '低优先级' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 异常检测 Tab -->
      <el-tab-pane label="异常检测" name="anomalies">
        <div class="tab-header">
          <span>异常记录列表</span>
          <div>
            <el-select v-model="filterLevel" placeholder="筛选等级" clearable size="small" style="width:130px">
              <el-option label="高" value="high" />
              <el-option label="中" value="medium" />
              <el-option label="低" value="low" />
            </el-select>
            <el-button type="primary" size="small" @click="showDetectDialog = true" style="margin-left:8px">
              手动检测
            </el-button>
          </div>
        </div>
        <el-row :gutter="16" style="margin:12px 0">
          <el-col :span="8">
            <el-card class="mini-stat">异常总数：<b>{{ anomalyStats.totalCount }}</b></el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="mini-stat color-red">未处理：<b>{{ anomalyStats.unhandledCount }}</b></el-card>
          </el-col>
        </el-row>
        <el-table :data="anomalies" border stripe style="width:100%">
          <el-table-column prop="indicatorName" label="指标名称" width="130" />
          <el-table-column prop="indicatorValue" label="指标值" width="100" />
          <el-table-column prop="expectedRange" label="正常范围" width="150" />
          <el-table-column prop="anomalyLevel" label="等级" width="80">
            <template #default="{ row }">
              <el-tag :type="row.anomalyLevel === 'high' ? 'danger' : row.anomalyLevel === 'medium' ? 'warning' : 'info'" size="small">
                {{ row.anomalyLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <span :style="{ color: row.status === 0 ? '#f5222d' : '#52c41a' }">
                {{ row.status === 0 ? '未处理' : '已处理' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" size="small" type="primary" @click="handleDispose(row)">
                处理
              </el-button>
              <span v-else style="color:#999">--</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- NLP解析 Tab -->
      <el-tab-pane label="非结构化文本解析" name="nlp">
        <div class="tab-header">
          <span>晨会记录 / 病程描述解析（异步+分层处理模式）</span>
        </div>
        <el-input
          v-model="nlpText"
          type="textarea"
          :rows="5"
          placeholder="输入非结构化文本，如：主诉：患者近一周胸闷气短。诊断：冠状动脉粥样硬化。治疗：口服阿司匹林。科室：心内科。"
          style="margin-top:12px"
        />
        <el-button type="primary" @click="handleNlpParse" style="margin-top:12px">解析</el-button>
        <div v-if="nlpResult" class="nlp-result">
          <h4>解析结果（结构化JSON）</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item v-for="(val, key) in nlpResult" :key="key" :label="getNlpLabel(key)">
              {{ val }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="nlp-hint">
            当前使用正则表达式实现基础提取。完整实现中通过独立消费组异步调用大模型API，
            支持私有化部署大模型，调用前自动对敏感信息进行脱敏处理。
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 手动检测弹窗 -->
    <el-dialog title="指标异常检测" v-model="showDetectDialog" width="480px">
      <el-form :model="detectForm" label-width="110px">
        <el-form-item label="门诊量">
          <el-input-number v-model="detectForm.outpatientVolume" :min="0" />
        </el-form-item>
        <el-form-item label="基线门诊量">
          <el-input-number v-model="detectForm.baselineVolume" :min="0" />
        </el-form-item>
        <el-form-item label="药占比">
          <el-input-number v-model="detectForm.drugRatio" :min="0" :max="1" :precision="2" :step="0.05" />
        </el-form-item>
        <el-form-item label="手术成功率">
          <el-input-number v-model="detectForm.surgerySuccessRate" :min="0" :max="1" :precision="2" :step="0.01" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDetectDialog = false">取消</el-button>
        <el-button type="primary" @click="handleDetect">开始检测</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listLabels, generateLabels, detectAnomalies,
  listAnomalies, getAnomalyStats, handleAnomaly, parseNlp
} from '@/api/labeling'

const activeTab = ref('labels')
const filterLevel = ref('')

// 标签
const labels = ref([])
const handleGenerateLabels = async () => {
  await generateLabels('doctor')
  ElMessage.success('标签生成任务已启动')
}

// 异常检测
const anomalies = ref([])
const anomalyStats = reactive({ totalCount: 0, unhandledCount: 0 })
const showDetectDialog = ref(false)
const detectForm = reactive({ outpatientVolume: 50, baselineVolume: 100, drugRatio: 0.35, surgerySuccessRate: 0.92 })

const loadAnomalies = async () => {
  const res = await listAnomalies(1, 100, filterLevel.value || undefined)
  anomalies.value = res.data.records
}

const loadAnomalyStats = async () => {
  const res = await getAnomalyStats()
  Object.assign(anomalyStats, res.data)
}

const handleDetect = async () => {
  await detectAnomalies({ ...detectForm })
  ElMessage.success('检测完成')
  showDetectDialog.value = false
  loadAnomalies()
  loadAnomalyStats()
}

const handleDispose = async (row) => {
  await handleAnomaly(row.id, 'admin', '已确认处理')
  ElMessage.success('已处理')
  loadAnomalies()
  loadAnomalyStats()
}

watch(filterLevel, loadAnomalies)

// NLP
const nlpText = ref('')
const nlpResult = ref(null)
const handleNlpParse = async () => {
  if (!nlpText.value) return
  const res = await parseNlp(nlpText.value)
  nlpResult.value = res.data
}
const getNlpLabel = (key) => {
  const map = { chiefComplaint: '患者主诉', diagnosis: '诊断结果', treatment: '治疗方案', department: '科室' }
  return map[key] || key
}

onMounted(async () => {
  labels.value = (await listLabels('doctor')).data
  loadAnomalies()
  loadAnomalyStats()
})
</script>

<style scoped>
.tab-header {
  display: flex; justify-content: space-between; align-items: center;
}
.mini-stat {
  text-align: center; font-size: 14px; color: #666;
}
.mini-stat b { font-size: 20px; color: #333; }
.mini-stat.color-red b { color: #f5222d; }
.nlp-result { margin-top: 16px; }
.nlp-result h4 { margin-bottom: 8px; }
.nlp-hint {
  margin-top: 12px; font-size: 12px; color: #999; line-height: 1.6;
  background: #fafafa; padding: 12px; border-radius: 4px;
}
</style>
