<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">议程模板管理</h2>
      <el-button type="primary" @click="openDialog()">新增模板</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="templateName" label="模板名称" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
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

    <el-dialog v-model="visible" title="模板编辑" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.templateName" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
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
const form = reactive({ id: null, templateName: '', content: '', status: 1 })

async function load() {
  list.value = await request.get('/agenda-template')
}
function openDialog(row) {
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, templateName: '', content: '', status: 1 })
  visible.value = true
}
async function submit() {
  if (form.id) await request.put(`/agenda-template/${form.id}`, form)
  else await request.post('/agenda-template', form)
  visible.value = false
  load()
  ElMessage.success('保存成功')
}
async function remove(id) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await request.delete(`/agenda-template/${id}`)
  load()
  ElMessage.success('删除成功')
}
onMounted(load)
</script>