<template>
  <el-dialog
    v-model="visible"
    title="编辑零部件"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="零部件代号" prop="componentCode">
        <el-input
          v-model="form.componentCode"
          placeholder="请输入零部件代号"
          maxlength="50"
          show-word-limit
          @blur="handleComponentCodeBlur"
        />
      </el-form-item>
      
      <el-form-item label="分类代码" prop="categoryCode">
        <el-input
          v-model="form.categoryCode"
          placeholder="请输入分类代码"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="零部件名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入零部件名称"
          maxlength="511"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="采购标识" prop="procurementFlag">
        <el-radio-group v-model="form.procurementFlag">
          <el-radio :label="false">自制</el-radio>
          <el-radio :label="true">采购</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="通用件类型" prop="commonPartsFlag">
        <el-radio-group v-model="form.commonPartsFlag">
          <el-radio :label="0">非紧固件</el-radio>
          <el-radio :label="1">装箱紧固件</el-radio>
          <el-radio :label="2">装配紧固件</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入备注信息"
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
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
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

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  categoryCode: '',
  componentCode: '',
  name: '',
  procurementFlag: false,
  commonPartsFlag: 0,
  comment: ''
})

const rules = {
  categoryCode: [
    { required: true, message: '请选择分类代码', trigger: 'change' }
  ],
  componentCode: [
    { required: true, message: '请输入零部件代号', trigger: 'blur' },
    { min: 2, max: 50, message: '零部件代号长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入零部件名称', trigger: 'blur' },
    { min: 2, max: 511, message: '零部件名称长度在 2 到 511 个字符', trigger: 'blur' }
  ],
  procurementFlag: [
    { required: true, message: '请选择采购标识', trigger: 'change' }
  ],
  commonPartsFlag: [
    { required: true, message: '请选择通用件类型', trigger: 'change' }
  ]
}

const handleComponentCodeBlur = () => {
  // 失焦时检查零部件代号并更新分类代码
  if (form.componentCode && form.componentCode.length >= 8 && !form.componentCode.startsWith('GB')) {
    // 满足条件：自动填充前8位到分类代码
    form.categoryCode = form.componentCode.substring(0, 8)
  } else {
    // 不满足条件：清空分类代码
    form.categoryCode = ''
  }
}

const loadComponentData = () => {
  if (props.componentData.id) {
    Object.assign(form, {
      id: props.componentData.id,
      categoryCode: props.componentData.categoryCode || '',
      componentCode: props.componentData.componentCode || '',
      name: props.componentData.name || '',
      procurementFlag: props.componentData.procurementFlag || false,
      commonPartsFlag: typeof props.componentData.commonPartsFlag === 'number' ? props.componentData.commonPartsFlag : (props.componentData.commonPartsFlag ? 1 : 0),
      comment: props.componentData.comment || ''
    })
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const response = await componentsApi.updateComponent(form.id, form)
    
    ElMessage.success('零部件更新成功')
    emit('success', response)
    handleClose()
    
  } catch (error) {
    if (error.errors) {
      ElMessage.error('请填写完整的零部件信息')
      return
    }
    
    console.error('更新零部件失败:', error)
    
    // 处理特定的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message?.includes('零部件代号已存在')) {
      ElMessage.error('零部件代号已存在，请使用其他代号')
    } else {
      ElMessage.error('更新零部件失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  // 重置表单
  Object.assign(form, {
    id: null,
    categoryCode: '',
    componentCode: '',
    name: '',
    procurementFlag: false,
    commonPartsFlag: 0,
    comment: ''
  })
  
  // 清除验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  
  visible.value = false
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal && props.componentData.id) {
    loadComponentData()
  }
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-textarea__inner) {
  resize: vertical;
}
</style>