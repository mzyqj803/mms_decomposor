<template>
  <el-dialog
    v-model="visible"
    title="编辑组件"
    width="500px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="组件编号" prop="componentNo">
        <el-input
          v-model="form.componentNo"
          placeholder="请输入组件编号"
        />
      </el-form-item>
      
      <el-form-item label="组件名称" prop="componentName">
        <el-input
          v-model="form.componentName"
          placeholder="请输入组件名称"
        />
      </el-form-item>
      
      <el-form-item label="单位" prop="unitCode">
        <el-input
          v-model="form.unitCode"
          placeholder="请输入单位"
        />
      </el-form-item>
      
      <el-form-item label="数量" prop="quantity">
        <el-input-number
          v-model="form.quantity"
          :min="1"
          :max="9999"
          placeholder="请输入数量"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.comments"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import containersApi from '@/api/containers'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  componentData: {
    type: Object,
    default: () => ({})
  },
  containerId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  componentNo: '',
  componentName: '',
  unitCode: '',
  quantity: 1,
  comments: ''
})

const rules = {
  componentNo: [
    { required: true, message: '请输入组件编号', trigger: 'blur' }
  ],
  componentName: [
    { required: true, message: '请输入组件名称', trigger: 'blur' }
  ],
  unitCode: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ]
}

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal && props.componentData.id) {
    loadComponentData()
  }
})

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const loadComponentData = () => {
  Object.assign(form, {
    id: props.componentData.id,
    componentNo: props.componentData.componentNo || '',
    componentName: props.componentData.componentName || '',
    unitCode: props.componentData.unitCode || '',
    quantity: props.componentData.quantity || 1,
    comments: props.componentData.comments || ''
  })
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const componentData = {
      componentNo: form.componentNo,
      componentName: form.componentName,
      unitCode: form.unitCode,
      quantity: form.quantity,
      comments: form.comments
    }
    
    await containersApi.updateContainerComponent(props.containerId, form.id, componentData)
    
    ElMessage.success('组件更新成功')
    emit('success')
    handleClose()
    
  } catch (error) {
    console.error('更新组件失败:', error)
    ElMessage.error('更新组件失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    componentNo: '',
    componentName: '',
    unitCode: '',
    quantity: 1,
    comments: ''
  })
}
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
