<template>
  <el-dialog
    v-model="visible"
    title="编辑箱包"
    width="80%"
    :before-close="handleClose"
  >
    <div class="edit-container-dialog">
      <!-- 箱包信息 -->
      <div class="container-info">
        <el-descriptions title="箱包信息" :column="2" border>
          <el-descriptions-item label="箱包号">{{ containerInfo.containerNo }}</el-descriptions-item>
          <el-descriptions-item label="箱包名称">{{ containerInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="合同号">{{ containerInfo.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ containerInfo.projectName }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 组件列表 -->
      <div class="components-section">
        <div class="section-header">
          <h3>箱包内组件列表</h3>
          <el-button type="primary" size="small" @click="refreshComponents">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        
        <el-table
          :data="components"
          v-loading="loading"
          stripe
          style="width: 100%"
          max-height="400"
        >
          <el-table-column prop="componentNo" label="组件编号" width="150" />
          <el-table-column prop="componentName" label="组件名称" min-width="200" />
          <el-table-column prop="unitCode" label="单位" width="80" />
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column prop="comments" label="备注" min-width="150" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEditComponent(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDeleteComponent(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="components.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无组件数据" />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleSave">保存修改</el-button>
      </div>
    </template>
  </el-dialog>
  
  <!-- 编辑组件对话框 -->
  <EditComponentDialog
    v-model="showEditComponentDialog"
    :component-data="selectedComponent"
    :container-id="containerInfo.id"
    @success="handleEditComponentSuccess"
  />
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import containersApi from '@/api/containers'
import EditComponentDialog from './EditComponentDialog.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  containerData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const loading = ref(false)
const components = ref([])
const showEditComponentDialog = ref(false)
const selectedComponent = ref({})

const containerInfo = reactive({
  id: null,
  containerNo: '',
  name: '',
  contractNo: '',
  projectName: ''
})

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal && props.containerData.id) {
    loadContainerData()
    loadComponents()
  }
})

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const loadContainerData = () => {
  Object.assign(containerInfo, {
    id: props.containerData.id,
    containerNo: props.containerData.containerNo,
    name: props.containerData.name,
    contractNo: props.containerData.contractNo,
    projectName: props.containerData.projectName
  })
}

const loadComponents = async () => {
  if (!containerInfo.id) return
  
  loading.value = true
  try {
    const response = await containersApi.getContainerComponents(containerInfo.id)
    components.value = response || []
  } catch (error) {
    console.error('加载组件列表失败:', error)
    ElMessage.error('加载组件列表失败')
    components.value = []
  } finally {
    loading.value = false
  }
}

const refreshComponents = () => {
  loadComponents()
}

const handleEditComponent = (row) => {
  selectedComponent.value = row
  showEditComponentDialog.value = true
}

const handleDeleteComponent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除组件 "${row.componentName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await containersApi.deleteContainerComponent(containerInfo.id, row.id)
    ElMessage.success('删除成功')
    loadComponents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除组件失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSave = () => {
  ElMessage.success('保存成功')
  emit('success')
  handleClose()
}

const handleEditComponentSuccess = () => {
  loadComponents()
}

const handleClose = () => {
  visible.value = false
  // 重置数据
  components.value = []
  showEditComponentDialog.value = false
  selectedComponent.value = {}
  Object.assign(containerInfo, {
    id: null,
    containerNo: '',
    name: '',
    contractNo: '',
    projectName: ''
  })
}
</script>

<style lang="scss" scoped>
.edit-container-dialog {
  .container-info {
    margin-bottom: 24px;
  }

  .components-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .empty-state {
      padding: 40px 0;
      text-align: center;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

