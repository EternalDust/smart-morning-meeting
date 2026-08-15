<template>
  <div class="page-card">
    <div class="page-header">
      <h2 class="page-title">审批流程配置</h2>
      <div>
        <el-button @click="addNode('serial')">+ 会签节点</el-button>
        <el-button @click="addNode('parallel')">+ 或签节点</el-button>
        <el-button @click="addNode('condition')">+ 条件分支</el-button>
        <el-button type="primary" @click="saveProcess">保存流程</el-button>
      </div>
    </div>

    <div class="process-canvas" @dragover.prevent @drop="onDrop">
      <div
          v-for="(node, index) in nodes"
          :key="node.uid"
          class="process-node"
          :class="[node.type, { dragging: dragIndex === index }]"
          draggable="true"
          @dragstart="onDragStart(index)"
          @dragend="onDragEnd"
      >
        <div class="node-title">
          <el-icon v-if="node.type === 'serial'"><UserFilled /></el-icon>
          <el-icon v-else-if="node.type === 'parallel'"><CircleCheck /></el-icon>
          <el-icon v-else><Share /></el-icon>
          <span>{{ node.name }}</span>
        </div>
        <div class="node-ops">
          <el-button size="small" text @click="editNode(index)">编辑</el-button>
          <el-button size="small" text type="danger" @click="removeNode(index)">删除</el-button>
        </div>
        <div v-if="index < nodes.length - 1" class="node-arrow">↓</div>
      </div>
    </div>

    <el-dialog v-model="visible" title="节点配置" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="节点名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="审批人ID">
          <el-input v-model="form.approverId" placeholder="多个用逗号分隔" />
        </el-form-item>
        <el-form-item v-if="form.type === 'condition'" label="条件表达式">
          <el-input v-model="form.conditionExpr" placeholder="例如: amount > 1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, CircleCheck, Share } from '@element-plus/icons-vue'

let uid = 0
const nodes = ref([])
const visible = ref(false)
const form = reactive({ index: -1, name: '', type: '', approverId: '', conditionExpr: '' })
const dragIndex = ref(-1)

function addNode(type) {
  const map = { serial: '会签审批', parallel: '或签审批', condition: '条件分支' }
  nodes.value.push({
    uid: ++uid,
    type,
    name: map[type] + uid,
    approverId: '',
    conditionExpr: ''
  })
}

function removeNode(i) {
  nodes.value.splice(i, 1)
}

function editNode(i) {
  const n = nodes.value[i]
  Object.assign(form, { index: i, name: n.name, type: n.type, approverId: n.approverId, conditionExpr: n.conditionExpr
        || '' })
  visible.value = true
}

function confirmEdit() {
  const n = nodes.value[form.index]
  n.name = form.name
  n.approverId = form.approverId
  if (n.type === 'condition') n.conditionExpr = form.conditionExpr
  visible.value = false
}

function onDragStart(i) {
  dragIndex.value = i
}

function onDragEnd() {
  dragIndex.value = -1
}

function onDrop(e) {
  const target = e.target.closest('.process-node')
  if (!target) return
  const children = Array.from(target.parentNode.children)
  const toIndex = children.indexOf(target)
  const fromIndex = dragIndex.value
  if (fromIndex > -1 && fromIndex !== toIndex) {
    const item = nodes.value.splice(fromIndex, 1)[0]
    nodes.value.splice(toIndex, 0, item)
  }
}

function saveProcess() {
  const payload = nodes.value.map(n => ({
    name: n.name,
    type: n.type,
    approverId: n.approverId,
    conditionExpr: n.conditionExpr
  }))
  console.log(JSON.stringify(payload))
  ElMessage.success('流程 JSON 已生成（见控制台）')
}
</script>

<style scoped>
.process-canvas {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  min-height: 400px;
  background: #f5f7fa;
  border-radius: 8px;
}
.process-node {
  width: 320px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  cursor: move;
  position: relative;
  transition: box-shadow 0.2s;
}
.process-node.dragging {
  opacity: 0.6;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.process-node.serial { border-left: 4px solid #409eff; }
.process-node.parallel { border-left: 4px solid #67c23a; }
.process-node.condition { border-left: 4px solid #e6a23c; }
.node-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 8px;
}
.node-ops {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.node-arrow {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  color: #909399;
  font-size: 16px;
}
</style>