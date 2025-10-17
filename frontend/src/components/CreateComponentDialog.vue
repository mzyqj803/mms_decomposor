<template>
  <el-dialog
    v-model="visible"
    title="新增零部件"
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
      
      <el-form-item label="父工件ID">
        <div style="display: flex; gap: 10px;">
          <el-input
            v-model="form.parentComponentId"
            placeholder="请输入父工件ID"
            maxlength="50"
            style="flex: 1;"
            readonly
          />
          <el-button type="primary" @click="showParentSearchDialog">
            <el-icon><Search /></el-icon>
            查找
          </el-button>
        </div>
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
      
      <!-- 新增的可选字段 -->
      <el-divider content-position="left">规格信息（可选）</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="单位">
            <el-input
              v-model="form.specs.unit"
              placeholder="请输入单位"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数量">
            <el-input
              v-model="form.specs.quantity"
              placeholder="请输入数量"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="材质">
            <el-input
              v-model="form.specs.material"
              placeholder="请输入材质"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="规格">
            <el-input
              v-model="form.specs.shapeSpec"
              placeholder="请输入规格"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="板厚">
            <el-input
              v-model="form.specs.thickness"
              placeholder="请输入板厚"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="板宽">
            <el-input
              v-model="form.specs.width"
              placeholder="请输入板宽"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="板长">
            <el-input
              v-model="form.specs.length"
              placeholder="请输入板长"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="程序代码">
            <el-input
              v-model="form.specs.programCode"
              placeholder="请输入程序代码"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="表面状态">
            <el-input
              v-model="form.specs.surfaceTech"
              placeholder="请输入表面状态"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="工艺流程">
        <el-input
          v-model="form.specs.processes"
          type="textarea"
          :rows="3"
          placeholder="请输入工艺流程"
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
  
  <!-- 父工件查找对话框 -->
  <ParentComponentSearchDialog 
    v-model="parentSearchDialogVisible"
    @select="handleParentSelect"
  />
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { componentsApi } from '@/api/components'
import ParentComponentSearchDialog from './ParentComponentSearchDialog.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const formRef = ref()
const parentSearchDialogVisible = ref(false)

const form = reactive({
  categoryCode: '',
  componentCode: '',
  name: '',
  parentComponentId: '',
  procurementFlag: false,
  commonPartsFlag: 0,
  comment: '',
  specs: {
    unit: '',
    quantity: '',
    material: '',
    shapeSpec: '',
    thickness: '',
    width: '',
    length: '',
    programCode: '',
    surfaceTech: '',
    processes: '',
    comments: '',
    procurement: '',
    commonParts: ''
  }
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

const showParentSearchDialog = () => {
  parentSearchDialogVisible.value = true
}

const handleParentSelect = (parentComponent) => {
  form.parentComponentId = parentComponent.componentCode
  parentSearchDialogVisible.value = false
  ElMessage.success(`已选择父工件: ${parentComponent.name}`)
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 构建请求数据，包含规格信息
    const requestData = {
      categoryCode: form.categoryCode,
      componentCode: form.componentCode,
      name: form.name,
      procurementFlag: form.procurementFlag,
      commonPartsFlag: form.commonPartsFlag,
      comment: form.comment,
      specs: []
    }
    
    // 将规格数据转换为ComponentsSpec格式
    const specMappings = [
      { key: 'unit', specCode: 'unit' },
      { key: 'quantity', specCode: 'quantity' },
      { key: 'material', specCode: 'material' },
      { key: 'shapeSpec', specCode: 'shapeSpec' },
      { key: 'thickness', specCode: 'thickness' },
      { key: 'width', specCode: 'width' },
      { key: 'length', specCode: 'length' },
      { key: 'programCode', specCode: 'programCode' },
      { key: 'surfaceTech', specCode: 'surfaceTech' },
      { key: 'processes', specCode: 'processes' },
      { key: 'comments', specCode: 'comments' },
      { key: 'procurement', specCode: 'procurement' },
      { key: 'commonParts', specCode: 'commonParts' }
    ]
    
    // 只添加有值的规格字段
    specMappings.forEach(mapping => {
      if (form.specs[mapping.key] && form.specs[mapping.key].trim()) {
        requestData.specs.push({
          specCode: mapping.specCode,
          specValue: form.specs[mapping.key].trim()
        })
      }
    })
    
    const response = await componentsApi.createComponent(requestData)
    
    ElMessage.success('零部件创建成功')
    emit('success', response)
    handleClose()
    
  } catch (error) {
    if (error.errors) {
      ElMessage.error('请填写完整的零部件信息')
      return
    }
    
    console.error('创建零部件失败:', error)
    
    // 处理特定的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message?.includes('零部件代号已存在')) {
      ElMessage.error('零部件代号已存在，请使用其他代号')
    } else {
      ElMessage.error('创建零部件失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  // 重置表单
  Object.assign(form, {
    categoryCode: '',
    componentCode: '',
    name: '',
    parentComponentId: '',
    procurementFlag: false,
    commonPartsFlag: 0,
    comment: '',
    specs: {
      unit: '',
      quantity: '',
      material: '',
      shapeSpec: '',
      thickness: '',
      width: '',
      length: '',
      programCode: '',
      surfaceTech: '',
      processes: '',
      comments: '',
      procurement: '',
      commonParts: ''
    }
  })
  
  // 清除验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  
  visible.value = false
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal) {
    // 对话框打开时可以做一些初始化工作
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
