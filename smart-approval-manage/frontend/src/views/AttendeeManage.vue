<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">参会人管理</h2>
      <p class="page-desc">管理当前会议的参会人员</p>
    </div>

    <el-form :inline="true" :model="form" style="margin-bottom:16px">
      <el-form-item label="用户ID">
        <el-input v-model="form.userId" placeholder="请输入用户ID" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.roleType" placeholder="角色" style="width:120px">
          <el-option label="参会人" :value="1" />
          <el-option label="记录员" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addAttendee">添加</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="attendees" stripe border style="width: 100%" empty-text="暂无参会人">
      <el-table-column prop="id" label="编号" width="80" align="center" />
      <el-table-column prop="userId" label="用户ID" width="120" align="center" />
      <el-table-column prop="roleType" label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.roleType === 2 ? '记录员' : '参会人' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="attendStatus" label="出席状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.attendStatus === 1 ? 'success' : 'info'">
            {{ row.attendStatus === 1 ? '已出席' : '未确认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain @click="removeAttendee(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="action-bar">
      <el-button @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request.js'

const route = useRoute()
const router = useRouter()
const attendees = ref([])

const form = reactive({ userId: '', roleType: 1 })

onMounted(async () => {
  loadAttendees()
})

async function loadAttendees() {
  try {
    attendees.value = await request.get('/agenda/' + route.params.id + '/attendees')
  } catch (e) {
    attendees.value = []
  }
}

async function addAttendee() {
  if (!form.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  try {
    await request.post('/agenda/' + route.params.id + '/attendees', {
      userId: Number(form.userId),
      roleType: form.roleType
    })
    ElMessage.success('添加成功')
    form.userId = ''
    loadAttendees()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

async function removeAttendee(id) {
  try {
    await ElMessageBox.confirm('确认删除该参会人？', '提示', { type: 'warning' })
    await request.delete('/agenda/attendees/' + id)
    ElMessage.success('删除成功')
    loadAttendees()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
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
.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>