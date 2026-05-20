<template>
  <div class="datasource-page">
    <div class="page-header">
      <span class="page-title">数据源管理</span>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增数据源
      </el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="sourceCode" label="数据源编号" width="160" />
      <el-table-column prop="sourceName" label="数据源名称" min-width="160" />
      <el-table-column prop="sourceType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.sourceType === 'mysql' ? '' : 'success'" size="small">
            {{ row.sourceType?.toUpperCase() }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" @click="handleTest(row)">测试</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="total, prev, pager, next"
      @current-change="loadData"
      style="margin-top:16px; justify-content: flex-end;"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="isEdit ? '编辑数据源' : '新增数据源'"
      v-model="dialogVisible"
      width="560px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="数据源编号" prop="sourceCode">
          <el-input v-model="form.sourceCode" placeholder="如 MYSQL_HIS_001" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="数据源名称" prop="sourceName">
          <el-input v-model="form.sourceName" placeholder="如 HIS门诊业务库" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="sourceType">
          <el-select v-model="form.sourceType" placeholder="请选择" style="width:100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="Kafka" value="kafka" />
            <el-option label="HTTP API" value="http" />
            <el-option label="MongoDB" value="mongodb" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接配置" prop="configJson">
          <el-input v-model="form.configJson" type="textarea" :rows="4"
            placeholder='{"host":"192.168.1.100","port":3306}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDataSources, addDataSource, updateDataSource, deleteDataSource, testConnection } from '@/api/datasource'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const defaultForm = () => ({
  sourceCode: '',
  sourceName: '',
  sourceType: 'mysql',
  configJson: '{}'
})
const form = reactive(defaultForm())

const formRules = {
  sourceCode: [{ required: true, message: '请输入数据源编号', trigger: 'blur' }],
  sourceName: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择数据源类型', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listDataSources(page.value, size.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDataSource(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await addDataSource({ ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleTest = async (row) => {
  try {
    await testConnection(row.id)
    ElMessage.success('连接测试成功')
  } catch {
    ElMessage.error('连接测试失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除数据源"${row.sourceName}"？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteDataSource(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const toggleStatus = (row) => {
  updateDataSource(row.id, { status: row.status === 1 ? 0 : 1 }).then(() => {
    row.status = row.status === 1 ? 0 : 1
  })
}

onMounted(loadData)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #333; }
</style>
