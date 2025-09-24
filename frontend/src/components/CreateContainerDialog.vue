<template>
  <el-dialog
    v-model="visible"
    title="新建装箱单"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="create-container-dialog">
      <!-- 步骤指示器 -->
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="选择合同" />
        <el-step title="创建装箱单" />
      </el-steps>
      
      <!-- 步骤1: 选择合同 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="step-title">
          <h3>选择合同</h3>
          <p>请选择现有合同或创建新合同</p>
        </div>
        
        <el-tabs v-model="contractTab" class="contract-tabs">
          <!-- 选择现有合同 -->
          <el-tab-pane label="选择现有合同" name="existing">
            <div class="contract-search">
              <el-form :model="searchForm" inline>
                <el-form-item label="合同号">
                  <el-input
                    v-model="searchForm.contractNo"
                    placeholder="请输入合同号"
                    clearable
                    style="width: 200px;"
                  />
                </el-form-item>
                <el-form-item label="项目名称">
                  <el-input
                    v-model="searchForm.projectName"
                    placeholder="请输入项目名称"
                    clearable
                    style="width: 200px;"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchContracts">
                    <el-icon><Search /></el-icon>
                    搜索
                  </el-button>
                  <el-button @click="resetSearch">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="contract-list">
              <el-table
                :data="contracts"
                v-loading="contractsLoading"
                @row-click="selectContract"
                highlight-current-row
                style="width: 100%"
              >
                <el-table-column prop="contractNo" label="合同号" width="150" />
                <el-table-column prop="clientName" label="客户名称" min-width="150" />
                <el-table-column prop="projectName" label="项目名称" min-width="200" />
                <el-table-column prop="quantity" label="数量" width="80" align="center" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">
                      {{ getStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="contractPagination.currentPage"
                  v-model:page-size="contractPagination.pageSize"
                  :page-sizes="[10, 20, 50]"
                  :total="contractPagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleContractSizeChange"
                  @current-change="handleContractCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 创建新合同 -->
          <el-tab-pane label="创建新合同" name="new">
            <el-form
              ref="newContractFormRef"
              :model="newContractForm"
              :rules="newContractRules"
              label-width="100px"
            >
              <el-form-item label="合同号" prop="contractNo">
                <el-input v-model="newContractForm.contractNo" placeholder="请输入合同号" />
              </el-form-item>
              <el-form-item label="客户名称" prop="clientName">
                <el-input v-model="newContractForm.clientName" placeholder="请输入客户名称" />
              </el-form-item>
              <el-form-item label="项目名称" prop="projectName">
                <el-input v-model="newContractForm.projectName" placeholder="请输入项目名称" />
              </el-form-item>
              <el-form-item label="数量" prop="quantity">
                <el-input-number v-model="newContractForm.quantity" :min="1" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 步骤2: 创建装箱单 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="step-title">
          <h3>创建装箱单</h3>
          <p>为合同 "{{ selectedContract?.contractNo }}" 创建装箱单</p>
        </div>
        
        <el-tabs v-model="containerTab" class="container-tabs">
          <!-- 上传Excel文件 -->
          <el-tab-pane label="上传Excel文件" name="upload">
            <div class="upload-section">
              <!-- 文件上传区域 -->
              <div v-if="!previewData" class="upload-area">
                <el-upload
                  ref="uploadRef"
                  class="upload-demo"
                  drag
                  :action="previewUrl"
                  :headers="uploadHeaders"
                  :before-upload="beforeUpload"
                  :on-success="handlePreviewSuccess"
                  :on-error="handleUploadError"
                  :file-list="fileList"
                  accept=".xlsx,.xls"
                  :auto-upload="true"
                >
                  <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                  <div class="el-upload__text">
                    将文件拖到此处，或<em>点击上传</em>
                  </div>
                  <template #tip>
                    <div class="el-upload__tip">
                      只能上传 xlsx/xls 文件，且不超过 10MB
                    </div>
                  </template>
                </el-upload>
              </div>
              
              <!-- 预览区域 -->
              <div v-if="previewData" class="preview-area">
                <div class="preview-header">
                  <h4>文件预览</h4>
                  <el-button type="danger" size="small" @click="clearPreview">
                    <el-icon><Delete /></el-icon>
                    删除文件
                  </el-button>
                </div>
                
                <div class="preview-info">
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="装箱单数量">{{ previewData.containerCount }}</el-descriptions-item>
                    <el-descriptions-item label="组件总数">{{ previewData.totalComponents }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                
                <div class="preview-containers">
                  <h5>装箱单列表</h5>
                  <el-table :data="previewData.containers" stripe style="width: 100%" max-height="300">
                    <el-table-column prop="containerNo" label="箱包号" width="120" />
                    <el-table-column prop="containerType" label="箱包名称" min-width="150" />
                    <el-table-column label="组件数量" width="100" align="center">
                      <template #default="{ row }">
                        {{ row.components.length }}
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 克隆现有装箱单 -->
          <el-tab-pane label="克隆现有装箱单" name="clone">
            <div class="clone-section">
              <div class="search-similar">
                <el-button type="primary" @click="searchSimilarContainers" :loading="similarLoading">
                  <el-icon><Search /></el-icon>
                  搜索相似装箱单
                </el-button>
                <p class="search-tip">系统将根据合同参数自动搜索相似的装箱单</p>
              </div>
              
              <div v-if="similarContainers.length > 0" class="similar-containers">
                <h4>找到 {{ similarContainers.length }} 个相似装箱单：</h4>
                <el-table
                  :data="similarContainers"
                  v-loading="similarLoading"
                  @row-click="selectSimilarContainer"
                  highlight-current-row
                  style="width: 100%"
                >
                  <el-table-column prop="containerNo" label="装箱单号" width="150" />
                  <el-table-column prop="contract.contractNo" label="合同号" width="150" />
                  <el-table-column prop="name" label="装箱单名称" min-width="200" />
                  <el-table-column prop="containerSize" label="尺寸" width="120" />
                  <el-table-column prop="containerWeight" label="重量" width="120" />
                  <el-table-column prop="components.length" label="组件数量" width="100" align="center" />
                </el-table>
              </div>
              
              <div v-if="similarContainers.length === 0 && !similarLoading" class="no-similar">
                <el-empty description="未找到相似的装箱单" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button v-if="currentStep === 0" type="primary" @click="handleNext" :disabled="!canProceed">
          下一步
        </el-button>
        <el-button v-if="currentStep === 1" @click="handlePrev">上一步</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="handleFinish" :disabled="!canFinish">
          完成
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Search, Refresh } from '@element-plus/icons-vue'
import api from '@/api'
import containersApi from '@/api/containers'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 当前步骤
const currentStep = ref(0)

// 合同相关
const contractTab = ref('existing')
const contracts = ref([])
const contractsLoading = ref(false)
const selectedContract = ref(null)

const searchForm = reactive({
  contractNo: '',
  projectName: ''
})

const contractPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新合同表单
const newContractFormRef = ref()
const newContractForm = reactive({
  contractNo: '',
  clientName: '',
  projectName: '',
  quantity: 1
})

const newContractRules = {
  contractNo: [
    { required: true, message: '请输入合同号', trigger: 'blur' }
  ],
  clientName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' }
  ]
}

// 装箱单相关
const containerTab = ref('upload')
const fileList = ref([])
const similarContainers = ref([])
const similarLoading = ref(false)

// 预览相关
const previewData = ref(null)
const previewFile = ref(null)

// 上传相关
const uploadRef = ref()
const previewUrl = '/api/containers/preview'
const uploadUrl = computed(() => `/api/contracts/${selectedContract.value?.id}/containers/upload`)
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 计算属性
const canProceed = computed(() => {
  if (contractTab.value === 'existing') {
    return selectedContract.value !== null
  } else {
    return newContractForm.contractNo && newContractForm.clientName && newContractForm.projectName
  }
})

const canFinish = computed(() => {
  if (containerTab.value === 'upload') {
    return previewData.value !== null
  } else {
    return similarContainers.value.length > 0
  }
})

// 方法
const searchContracts = async () => {
  contractsLoading.value = true
  try {
    const response = await api.get('/contracts', {
      params: {
        contractNo: searchForm.contractNo,
        projectName: searchForm.projectName,
        page: contractPagination.currentPage - 1,
        size: contractPagination.pageSize
      }
    })
    
    contracts.value = response.content || []
    contractPagination.total = response.totalElements || 0
  } catch (error) {
    ElMessage.error('搜索合同失败')
  } finally {
    contractsLoading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    contractNo: '',
    projectName: ''
  })
  searchContracts()
}

const selectContract = (contract) => {
  selectedContract.value = contract
}

const handleContractSizeChange = (size) => {
  contractPagination.pageSize = size
  searchContracts()
}

const handleContractCurrentChange = (page) => {
  contractPagination.currentPage = page
  searchContracts()
}

const handleNext = async () => {
  if (contractTab.value === 'new') {
    // 验证新合同表单
    try {
      await newContractFormRef.value.validate()
      
      // 创建新合同
      const response = await api.post('/contracts', newContractForm)
      selectedContract.value = response
      ElMessage.success('合同创建成功')
    } catch (error) {
      if (error.errors) {
        ElMessage.error('请填写完整的合同信息')
        return
      }
      ElMessage.error('创建合同失败')
      return
    }
  }
  
  currentStep.value = 1
}

const handlePrev = () => {
  currentStep.value = 0
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel' ||
                  file.name.endsWith('.xlsx') ||
                  file.name.endsWith('.xls')
  
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  
  // 保存文件引用，用于后续实际上传
  previewFile.value = file
  
  return true
}

const handlePreviewSuccess = (response) => {
  if (response.success) {
    previewData.value = response
    ElMessage.success('文件预览成功')
  } else {
    ElMessage.error(response.message || '预览失败')
  }
}

const clearPreview = () => {
  previewData.value = null
  previewFile.value = null
  fileList.value = []
  uploadRef.value?.clearFiles()
}

const handleUploadError = (error) => {
  ElMessage.error('上传失败，请重试')
}

const searchSimilarContainers = async () => {
  if (!selectedContract.value) {
    ElMessage.error('请先选择合同')
    return
  }
  
  similarLoading.value = true
  try {
    const response = await containersApi.searchSimilarContainers(selectedContract.value.id)
    
    if (response.success) {
      similarContainers.value = response.data || []
      if (similarContainers.value.length === 0) {
        ElMessage.info('未找到相似的装箱单')
      }
    } else {
      ElMessage.error(response.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error('搜索相似装箱单失败')
  } finally {
    similarLoading.value = false
  }
}

const selectSimilarContainer = async (container) => {
  try {
    await ElMessageBox.confirm(
      `确定要克隆装箱单 "${container.name}" 吗？`,
      '确认克隆',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await containersApi.cloneContainer(selectedContract.value.id, container.contract.id)
    
    if (response.success) {
      ElMessage.success('装箱单克隆成功')
      emit('success', response.data)
      handleClose()
    } else {
      ElMessage.error(response.message || '克隆失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('克隆装箱单失败')
    }
  }
}

const handleFinish = async () => {
  if (containerTab.value === 'upload') {
    if (!previewData.value) {
      ElMessage.info('请先上传Excel文件')
      return
    }
    
    // 执行实际上传
    try {
      const response = await containersApi.uploadContainer(selectedContract.value.id, previewFile.value)
      
      if (response.success) {
        ElMessage.success('装箱单创建成功')
        emit('success', response.data)
        handleClose()
      } else {
        ElMessage.error(response.message || '创建失败')
      }
    } catch (error) {
      ElMessage.error('创建失败，请重试')
      console.error('Upload error:', error)
    }
  } else {
    ElMessage.info('请先选择要克隆的装箱单')
  }
}

const handleClose = () => {
  // 重置状态
  currentStep.value = 0
  contractTab.value = 'existing'
  containerTab.value = 'upload'
  selectedContract.value = null
  fileList.value = []
  similarContainers.value = []
  
  // 重置预览数据
  previewData.value = null
  previewFile.value = null
  
  // 重置表单
  Object.assign(newContractForm, {
    contractNo: '',
    clientName: '',
    projectName: '',
    quantity: 1
  })
  
  Object.assign(searchForm, {
    contractNo: '',
    projectName: ''
  })
  
  visible.value = false
}

// 状态相关方法
const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'ERROR': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PROCESSING': '处理中',
    'COMPLETED': '完成',
    'ERROR': '错误'
  }
  return statusMap[status] || '未知'
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal) {
    searchContracts()
  }
})
</script>

<style lang="scss" scoped>
.create-container-dialog {
  .step-content {
    margin-top: 30px;
    
    .step-title {
      margin-bottom: 20px;
      
      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
      
      p {
        margin: 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
  
  .contract-tabs,
  .container-tabs {
    margin-top: 20px;
  }
  
  .contract-search {
    margin-bottom: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 4px;
  }
  
  .contract-list {
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
  
  .upload-section {
    padding: 20px;
    
    .upload-demo {
      width: 100%;
    }
    
    .preview-area {
      .preview-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        h4 {
          margin: 0;
          color: #409EFF;
        }
      }
      
      .preview-info {
        margin-bottom: 20px;
      }
      
      .preview-containers {
        h5 {
          margin: 0 0 10px 0;
          color: #606266;
        }
      }
    }
  }
  
  .clone-section {
    padding: 20px;
    
    .search-similar {
      margin-bottom: 30px;
      text-align: center;
      
      .search-tip {
        margin-top: 10px;
        color: #909399;
        font-size: 14px;
      }
    }
    
    .similar-containers {
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }
    }
    
    .no-similar {
      margin-top: 40px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
