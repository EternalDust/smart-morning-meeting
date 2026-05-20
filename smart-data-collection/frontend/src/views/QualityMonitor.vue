<template>
  <div class="quality-page">
    <div class="page-header">
      <span class="page-title">质量监控</span>
      <div>
        <el-button @click="handleTriggerCleaning">
          <el-icon><RefreshRight /></el-icon> 触发清洗任务
        </el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalCleanedRecords }}</div>
          <div class="stat-tag">累计清洗记录</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalDedupedRecords }}</div>
          <div class="stat-tag">已去重记录</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num">{{ stats.totalFilledRecords }}</div>
          <div class="stat-tag">已填充记录</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-num color-red">{{ stats.totalAnomalyRecords }}</div>
          <div class="stat-tag">异常记录</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card title="清洗规则" class="section-card">
      <template #header>
        <div class="card-header">
          <span>清洗规则配置</span>
          <el-button type="primary" size="small" @click="openRuleDialog()">新增规则</el-button>
        </div>
      </template>
      <el-table :data="rules" border stripe style="width:100%">
        <el-table-column prop="ruleId" label="ID" width="70" />
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="ruleType" label="规则类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ ruleTypeMap[row.ruleType] || row.ruleType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled === 1" @change="toggleRule(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openRuleDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 规则编辑弹窗 -->
    <el-dialog title="清洗规则" v-model="ruleDialogVisible" width="500px">
      <el-form :model="ruleForm" label-width="80px">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="如：就诊记录去重" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="ruleForm.ruleType" style="width:100%">
            <el-option label="去重 (DEDUP)" value="DEDUP" />
            <el-option label="格式化 (FORMAT)" value="FORMAT" />
            <el-option label="填充 (FILL)" value="FILL" />
            <el-option label="校验 (VALIDATE)" value="VALIDATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="配置JSON">
          <el-input v-model="ruleForm.ruleConfig" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCleaningRules, addCleaningRule, updateCleaningRule,
  deleteCleaningRule, toggleCleaningRule, getCleaningStats, triggerCleaning
} from '@/api/cleaning'

const ruleTypeMap = { DEDUP: '去重', FORMAT: '格式化', FILL: '填充', VALIDATE: '校验' }

const stats = reactive({ totalCleanedRecords: 0, totalDedupedRecords: 0, totalFilledRecords: 0, totalAnomalyRecords: 0 })
const rules = ref([])
const ruleDialogVisible = ref(false)
const editingRuleId = ref(null)

const defaultRule = () => ({ ruleName: '', ruleType: 'DEDUP', ruleConfig: '{}', priority: 0 })
const ruleForm = reactive(defaultRule())

const loadRules = async () => {
  const res = await listCleaningRules()
  rules.value = res.data
}

const loadStats = async () => {
  const res = await getCleaningStats()
  Object.assign(stats, res.data)
}

const openRuleDialog = (row) => {
  editingRuleId.value = row?.ruleId || null
  Object.assign(ruleForm, row ? { ...row } : defaultRule())
  ruleDialogVisible.value = true
}

const saveRule = async () => {
  if (editingRuleId.value) {
    await updateCleaningRule(editingRuleId.value, { ...ruleForm })
    ElMessage.success('规则已更新')
  } else {
    await addCleaningRule({ ...ruleForm })
    ElMessage.success('规则已添加')
  }
  ruleDialogVisible.value = false
  loadRules()
}

const deleteRule = (row) => {
  ElMessageBox.confirm(`确认删除规则"${row.ruleName}"？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCleaningRule(row.ruleId)
      ElMessage.success('已删除')
      loadRules()
    })
    .catch(() => {})
}

const toggleRule = async (row) => {
  await toggleCleaningRule(row.ruleId)
  row.enabled = row.enabled === 1 ? 0 : 1
}

const handleTriggerCleaning = async () => {
  await triggerCleaning('batch')
  ElMessage.success('清洗任务已触发')
}

onMounted(() => { loadRules(); loadStats() })
</script>

<style scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #333; }
.stat-card { text-align: center; margin-bottom: 16px; }
.stat-num { font-size: 32px; font-weight: 700; color: #1890ff; }
.stat-num.color-red { color: #f5222d; }
.stat-tag { font-size: 13px; color: #999; margin-top: 4px; }
.section-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
