<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">晨会详情</h2>
      <p class="page-desc">查看会议的完整信息及当前审批状态</p>
    </div>

    <el-descriptions :column="2" border class="detail-descriptions">
      <el-descriptions-item label="会议编号" width="120">
        <span class="desc-value">{{ meeting.id }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="会议标题" width="120">
        <span class="desc-value title-text">{{ meeting.title }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="会议类型" width="120">
        <el-tag size="small" type="info">{{ formatMeetingType(meeting.meetingType) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="审批状态" width="120">
        <el-tag :type="statusType(meeting.approveStatus)" size="small">
          {{ statusText(meeting.approveStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="会议地点" width="120">
        <span class="desc-value"><el-icon><Location /></el-icon> {{ meeting.location || '未设置' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="主持人" width="120">
        <span class="desc-value">{{ meeting.hostId ? 'ID: ' + meeting.hostId : '未指定' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="开始时间" width="120">
        <span class="desc-value"><el-icon><Clock /></el-icon> {{ formatTime(meeting.startTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="结束时间" width="120">
        <span class="desc-value"><el-icon><Clock /></el-icon> {{ formatTime(meeting.endTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" width="120">
        <span class="desc-value">{{ formatTime(meeting.createTime) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="更新时间" width="120">
        <span class="desc-value">{{ formatTime(meeting.updateTime) }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <h3 class="section-title">会议材料</h3>
    <el-upload
        :action="uploadUrl"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :show-file-list="false">
      <el-button type="primary" size="small">上传材料</el-button>
    </el-upload>

    <el-table :data="materials" style="margin-top: 12px" empty-text="暂无材料">
      <el-table-column prop="materialName" label="文件名" min-width="180" />
      <el-table-column prop="fileSize" label="大小" width="120" :formatter="formatSize" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="downloadFile(scope.row)">下载</el-button>
          <el-button link type="danger" size="small" @click="deleteMaterial(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />
    <h3 class="section-title">审批操作</h3>
    <div class="action-bar" v-if="meeting.approveStatus === 0">
      <el-button type="primary" @click="submitApprove">提交审批</el-button>
    </div>
    <div class="action-bar" v-if="meeting.approveStatus === 1">
      <el-button type="success" @click="handleApprove(1)">通过</el-button>
      <el-button type="danger" @click="handleApprove(2)">驳回</el-button>
    </div>
    <div class="action-bar" v-if="meeting.approveStatus === 2">
      <el-button type="warning" @click="archiveMeeting">归档会议</el-button>
    </div>

    <el-divider />
    <div class="action-bar">
      <el-button @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
      <el-button type="primary" @click="goAgenda(meeting.id)"><el-icon><List /></el-icon> 查看议程</el-button>
      <el-button type="success" @click="goRecords(meeting.id)"><el-icon><DocumentChecked /></el-icon>
        审批记录</el-button>
      <el-button type="warning" @click="goAttendees(meeting.id)"><el-icon><User /></el-icon> 参会人</el-button>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {Location, Clock, ArrowLeft, List, DocumentChecked, User} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()
const meeting = ref({})
const materials = ref([])

const uploadUrl = computed(() => {
  return `/api/agenda/${meeting.value.id}/materials/upload`
})

onMounted(async () => {
  try {
    meeting.value = await request.get('/agenda/meetings/' + route.params.id)
    loadMaterials()
  } catch (e) {
    meeting.value = {id: route.params.id}
  }
})

function loadMaterials() {
  if (!meeting.value.id) return
  request.get('/agenda/' + meeting.value.id + '/materials').then(res => {
    materials.value = res || []
  }).catch(() => {
    materials.value = []
  })
}

function beforeUpload(file) {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件不能超过50MB')
    return false
  }
  return true
}

function handleUploadSuccess(res) {
  if (res.success) {
    ElMessage.success('上传成功')
    loadMaterials()
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

function handleUploadError() {
  ElMessage.error('上传失败')
}

function formatSize(row, column, cellValue) {
  if (!cellValue) return '-'
  if (cellValue < 1024) return cellValue + ' B'
  if (cellValue < 1024 * 1024) return (cellValue / 1024).toFixed(2) + ' KB'
  return (cellValue / 1024 / 1024).toFixed(2) + ' MB'
}

function downloadFile(row) {
  window.open(row.fileUrl)
}

function deleteMaterial(id) {
  request.delete('/agenda/materials/' + id).then(() => {
    ElMessage.success('删除成功')
    loadMaterials()
  }).catch(() => {
    ElMessage.error('删除失败')
  })
}

function goAgenda(id) {
  router.push('/meetings/' + id + '/agenda')
}

function goRecords(id) {
  router.push('/meetings/' + id + '/records')
}

function goAttendees(id) {
  router.push('/meetings/' + id + '/attendees')
}

async function submitApprove() {
  try {
    await request.post('/agenda/' + meeting.value.id + '/submit')
    ElMessage.success('提交审批成功')
    meeting.value.approveStatus = 1
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}

async function handleApprove(action) {
  const opinion = action === 1 ? '审批通过' : '审批驳回'
  try {
    await request.post('/agenda/' + meeting.value.id + '/handle', {
      approver_id: 1,
      action: action,
      opinion: opinion
    })
    ElMessage.success(opinion)
    meeting.value.approveStatus = action === 1 ? 2 : 3
  } catch (e) {
    ElMessage.error(e.message || '处理失败')
  }
}

async function archiveMeeting() {
  try {
    await ElMessageBox.confirm('确认归档该会议？', '提示', {type: 'warning'})
    await request.post('/agenda/' + meeting.value.id + '/archive')
    ElMessage.success('归档成功')
    meeting.value.approveStatus = 4
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '归档失败')
  }
}

function formatTime(time) {
  if (!time) return '—'
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusText(status) {
  const map = {0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已归档'}
  return map[status] ?? '未知'
}

function statusType(status) {
  const map = {0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'success'}
  return map[status] ?? 'info'
}

function formatMeetingType(type) {
  const map = {1: '科室例会', 2: '质量复盘', 3: '专题讨论'}
  return map[type] ?? ('类型' + type)
}
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.detail-descriptions {
  margin-top: 8px;
}

.desc-value {
  color: #303133;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.title-text {
  font-weight: 600;
  font-size: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.action-bar {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>