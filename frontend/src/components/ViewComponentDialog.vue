<template>
  <el-dialog
    v-model="visible"
    title="查看零部件"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="view-component-dialog">
      <!-- 基本信息 -->
      <div class="basic-info">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="零部件代号">
            <el-tag type="primary">{{ componentData.componentCode }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="零部件名称">
            {{ componentData.name }}
          </el-descriptions-item>
          <el-descriptions-item label="分类代码">
            <el-tag>{{ componentData.categoryCode }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="采购标识">
            <el-tag :type="componentData.procurementFlag ? 'success' : 'info'">
              {{ componentData.procurementFlag ? '采购' : '自制' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="通用件类型">
            <el-tag :type="componentData.commonPartsFlag === 1 ? 'warning' : (componentData.commonPartsFlag === 2 ? 'success' : 'info')">
              {{ componentData.commonPartsFlag === 1 ? '装箱紧固件' : (componentData.commonPartsFlag === 2 ? '装配紧固件' : '非紧固件') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(componentData.entryTs) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 备注信息 -->
      <div v-if="componentData.comment" class="comment-section">
        <h4>备注信息</h4>
        <div class="comment-content">
          {{ componentData.comment }}
        </div>
      </div>

      <!-- 规格信息 -->
      <div v-if="specs.length > 0" class="specs-section">
        <h4>规格参数</h4>
        <el-table :data="specs" stripe style="width: 100%" max-height="300">
          <el-table-column prop="specName" label="规格名称" min-width="150" />
          <el-table-column prop="specValue" label="规格值" min-width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="description" label="描述" min-width="200" />
        </el-table>
      </div>

      <!-- 工艺信息 -->
      <div v-if="processes.length > 0" class="processes-section">
        <h4>工艺信息</h4>
        <el-table :data="processes" stripe style="width: 100%" max-height="300">
          <el-table-column prop="processName" label="工艺名称" min-width="150" />
          <el-table-column prop="processCode" label="工艺代码" width="120" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="sequence" label="顺序" width="80" align="center" />
        </el-table>
      </div>

      <!-- 关联关系 -->
      <div v-if="relationships.length > 0" class="relationships-section">
        <h4>关联关系</h4>
        <el-table :data="relationships" stripe style="width: 100%" max-height="300">
          <el-table-column prop="type" label="关系类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 'PARENT' ? 'success' : 'warning'">
                {{ row.type === 'PARENT' ? '父组件' : '子组件' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="relatedComponentCode" label="关联零部件代号" width="150" />
          <el-table-column prop="relatedComponentName" label="关联零部件名称" min-width="200" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
        </el-table>
      </div>

      <!-- 空状态 -->
      <div v-if="!specs.length && !processes.length && !relationships.length" class="empty-state">
        <el-empty description="暂无详细信息" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { componentsApi } from '@/api/components'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  componentData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'edit'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const specs = ref([])
const processes = ref([])
const relationships = ref([])

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadComponentDetails = async () => {
  if (!props.componentData.id) return
  
  loading.value = true
  try {
    // 获取零部件详细信息
    const component = await componentsApi.getComponent(props.componentData.id)
    
    // 获取规格信息
    if (component.specs) {
      specs.value = component.specs
    }
    
    // 获取工艺信息
    if (component.processes) {
      processes.value = component.processes
    }
    
    // 获取关联关系
    const children = component.children || []
    const parents = component.parents || []
    
    relationships.value = [
      ...children.map(rel => ({
        type: 'CHILD',
        relatedComponentCode: rel.child?.componentCode,
        relatedComponentName: rel.child?.name,
        quantity: rel.quantity
      })),
      ...parents.map(rel => ({
        type: 'PARENT',
        relatedComponentCode: rel.parent?.componentCode,
        relatedComponentName: rel.parent?.name,
        quantity: rel.quantity
      }))
    ]
    
  } catch (error) {
    console.error('加载零部件详情失败:', error)
    ElMessage.error('加载零部件详情失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  emit('edit', props.componentData)
  handleClose()
}

const handleClose = () => {
  // 重置数据
  specs.value = []
  processes.value = []
  relationships.value = []
  
  visible.value = false
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal && props.componentData.id) {
    loadComponentDetails()
  }
})
</script>

<style lang="scss" scoped>
.view-component-dialog {
  .basic-info {
    margin-bottom: 20px;
  }
  
  .comment-section,
  .specs-section,
  .processes-section,
  .relationships-section {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      border-bottom: 2px solid #409EFF;
      padding-bottom: 8px;
    }
  }
  
  .comment-content {
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    border-left: 4px solid #409EFF;
    white-space: pre-wrap;
    line-height: 1.6;
  }
  
  .empty-state {
    margin: 40px 0;
    text-align: center;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-descriptions__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
