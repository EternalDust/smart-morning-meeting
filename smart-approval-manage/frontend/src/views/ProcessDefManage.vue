<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">审批流程定义</h2>
      <el-button type="primary" @click="openDialog()">新增流程</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="processName" label="流程名称" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="流程定义" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.processName" />
        </el-form-item>
        <el-form-item label="节点配置">
          <el-input v-model="form.nodesJson" type="textarea" :rows="4" placeholder="JSON格式节点配置" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '../api/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const visible = ref(false)
const form = reactive({ id: null, processName: '', nodesJson: '', status: 1 })

async function load() {
  list.value = await request.get('/process-def')
}
function openDialog(row) {
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, processName: '', nodesJson: '', status: 1 })
  visible.value = true
}
async function submit() {
  if (form.id) await request.put(`/process-def/${form.id}`, form)
  else await request.post('/process-def', form)
  visible.value = false
  load()
  ElMessage.success('保存成功')
}
async function remove(id) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await request.delete(`/process-def/${id}`)
  load()
  ElMessage.success('删除成功')
}
onMounted(load)
</script>