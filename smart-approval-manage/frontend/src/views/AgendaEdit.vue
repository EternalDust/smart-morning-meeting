<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">议程管理</h2>
      <p class="page-desc">为当前会议智能生成或手动管理议程内容</p>
    </div>

    <el-alert
      title="使用说明"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      填写科室名称与会议类型，点击"AI 生成议程"按钮，系统将根据规则自动推荐合适的议程内容。
    </el-alert>

    <el-form :model="form" label-width="100px" class="agenda-form">
      <el-form-item label="科室名称" required>
        <el-input
          v-model="form.deptName"
          placeholder="请输入科室名称，例如：内科、外科"
          clearable
          style="max-width: 400px"
        />
      </el-form-item>
      <el-form-item label="会议类型" required>
        <el-select v-model="form.meetingType" placeholder="请选择会议类型" style="max-width: 400px">
          <el-option label="科室例会" :value="1" />
          <el-option label="质量复盘" :value="2" />
          <el-option label="专题讨论" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="MagicStick" @click="generateAgenda">
          AI 生成议程
        </el-button>
        <el-button @click="clearAgenda" :icon="Delete" plain>
          清空列表
        </el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <div class="agenda-section">
      <h3 class="section-title">
        <el-icon><List /></el-icon> 议程列表
        <el-tag v-if="agendaList.length > 0" type="success" size="small">共 {{ agendaList.length }} 项</el-tag>
      </h3>

      <el-table
        :data="agendaList"
        stripe
        border
        style="width: 100%"
        empty-text="暂无议程"
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="title" label="议程标题" min-width="300">
          <template #default="{ row }">
            <span class="agenda-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary">第 {{ row.sort }} 项</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="agendaList.length === 0"
        description="暂无议程内容，请填写科室和会议类型后点击上方按钮生成"
        :image-size="120"
      />
    </div>

    <div class="action-bar">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Delete, List, ArrowLeft } from '@element-plus/icons-vue'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()

const form = ref({
  meetingId: route.params.id,
  deptName: '',
  meetingType: 1
})

const agendaList = ref([])

async function generateAgenda() {
  if (!form.value.deptName.trim()) {
    ElMessage.warning('请输入科室名称')
    return
  }
  try {
    const res = await request.post('/agenda/ai-generate', {
      meeting_id: Number(form.value.meetingId),
      dept_name: form.value.deptName,
      meeting_type: form.value.meetingType
    })
    agendaList.value = res.map((title, idx) => ({ title, sort: idx + 1 }))
    ElMessage.success(`成功生成 ${agendaList.value.length} 条议程`)
  } catch (e) {
    ElMessage.error(e.message || '请求失败')
  }
}

function clearAgenda() {
  agendaList.value = []
  ElMessage.info('已清空议程列表')
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

.agenda-form {
  margin-top: 8px;
}

.agenda-section {
  margin-top: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.agenda-title {
  font-size: 14px;
  color: #303133;
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>