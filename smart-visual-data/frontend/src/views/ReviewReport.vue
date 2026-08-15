<template>
  <div style="display:flex; flex-direction:column; gap:20px; height:100%">
    <!-- 报告生成区 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">复盘报告生成</span></template>
          <div style="display:flex; align-items:center; gap:16px; flex-wrap:wrap">
            <el-radio-group v-model="reportType">
              <el-radio-button value="REVIEW">晨会复盘报告</el-radio-button>
              <el-radio-button value="ADVICE">管理决策建议</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="max-width:320px"
            />
            <el-button type="primary" :loading="generating" @click="onGenerate">生成最新报告</el-button>
            <span style="color:#909399; font-size:12px">说明：未配置大模型服务时自动使用模板模拟生成</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新报告展示 -->
    <el-row :gutter="20" style="flex:1">
      <el-col :span="24" style="height:100%">
        <el-card shadow="hover" style="height:100%">
          <template #header>
            <div style="display:flex; align-items:center; justify-content:space-between">
              <span style="font-weight:bold">{{ current ? current.title : '最新报告' }}</span>
              <div style="display:flex; align-items:center; gap:8px">
                <el-tag v-if="current" :type="current.status === 1 ? 'success' : (current.status === 2 ? 'warning' : 'danger')" size="small">
                  {{ statusText(current.status) }}
                </el-tag>
                <el-button v-if="current" size="small" @click="exportWord">导出Word</el-button>
                <el-button v-if="current" size="small" @click="exportPdf">导出PDF</el-button>
              </div>
            </div>
          </template>
          <div v-if="current" class="report-content" v-html="renderedContent"></div>
          <div v-else style="text-align:center; color:#c0c4cc; padding:40px 0">暂无报告，点击「生成最新报告」开始</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史报告 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">历史报告</span></template>
          <el-table :data="history" stripe>
            <el-table-column prop="title" label="标题" min-width="120" />
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column prop="createTime" label="生成时间" width="170" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : (row.status === 2 ? 'warning' : 'danger')" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="viewReport(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import { generateReport, getLatestReport, getReportList } from '../api/reviewReport'

const md = new MarkdownIt({ html: false, linkify: true })

const reportType = ref('REVIEW')
const dateRange = ref([
  new Date(Date.now() - 6 * 864e5).toISOString().slice(0, 10),
  new Date().toISOString().slice(0, 10)
])
const generating = ref(false)
const current = ref(null)
const history = ref([])

const renderedContent = computed(() => (current.value ? md.render(current.value.content || '') : ''))

const statusText = (s) => ({ 1: '真实生成', 2: '模拟生成', 0: '失败' }[s] || '未知')

const onGenerate = async () => {
  generating.value = true
  try {
    const data = await generateReport({
      reportType: reportType.value,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    current.value = data
    await loadHistory()
    ElMessage.success(data.status === 1 ? '报告生成成功' : '报告已生成（模拟模式）')
  } catch (e) {
    // 错误提示已由 request 拦截器统一处理
  } finally {
    generating.value = false
  }
}

const loadLatest = async () => {
  try {
    current.value = await getLatestReport(reportType.value)
  } catch (e) {
    // 首次进入无报告属正常
  }
}

const loadHistory = async () => {
  try {
    history.value = (await getReportList({ reportType: reportType.value })) || []
  } catch (e) {
    history.value = []
  }
}

const viewReport = (row) => { current.value = row }

// 导出 Word：将 Markdown 渲染后的 HTML 打包成 .doc（Word 可直接打开）
const exportWord = () => {
  if (!current.value) return
  const html = `<!DOCTYPE html>
<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word">
<head><meta charset="utf-8"><title>${current.value.title}</title>
<style>body{font-family:'Microsoft YaHei',sans-serif;line-height:1.8;padding:20px;color:#303133}h1,h2,h3{color:#001529}li{margin:4px 0}</style>
</head><body>${renderedContent.value}</body></html>`
  const blob = new Blob(['﻿' + html], { type: 'application/msword' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${current.value.title}_${current.value.endDate}.doc`
  a.click()
  URL.revokeObjectURL(a.href)
}

// 导出 PDF：走浏览器打印，可选择保存为 PDF
const exportPdf = () => {
  if (!current.value) return
  window.print()
}

onMounted(() => {
  loadLatest()
  loadHistory()
})
</script>

<style scoped>
.report-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
}
.report-content :deep(h1) { font-size: 20px; color: #001529; margin: 16px 0 8px; }
.report-content :deep(h2) { font-size: 17px; color: #001529; margin: 14px 0 6px; border-left: 3px solid #409EFF; padding-left: 8px; }
.report-content :deep(h3) { font-size: 15px; margin: 10px 0 4px; }
.report-content :deep(li) { margin: 3px 0; }
.report-content :deep(strong) { color: #001529; }
@media print {
  .el-aside, .el-header { display: none !important; }
  .report-content { font-size: 12px; }
}
</style>
