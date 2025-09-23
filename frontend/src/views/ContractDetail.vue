<template>
  <div class="contract-detail">
    <div class="page-header">
      <el-button @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="title">合同详情</h1>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="contract" class="detail-content">
      <!-- 合同基本信息 -->
      <div class="info-card">
        <div class="card-title">合同信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同号">{{ contract.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ contract.clientName }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ contract.projectName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ contract.quantity }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(contract.status)">
              {{ getStatusText(contract.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(contract.entryTs) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 合同参数 -->
      <div class="info-card">
        <div class="card-title">
          合同参数
          <el-button 
            type="primary" 
            size="small" 
            @click="showEditParamsDialog"
          >
            <el-icon><Edit /></el-icon>
            编辑参数
          </el-button>
        </div>
        
        <div v-if="contract.parameters && contract.parameters.length > 0">
          <el-table :data="contract.parameters" stripe>
            <el-table-column prop="paramName" label="参数名称" />
            <el-table-column prop="paramValue" label="参数值" />
          </el-table>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无合同参数" />
        </div>
      </div>
      
      <!-- 装箱单信息 -->
      <div class="info-card">
        <div class="card-title">
          装箱单信息
          <div class="card-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="showUploadDialog"
            >
              <el-icon><Upload /></el-icon>
              上传装箱单
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="showCloneDialog"
            >
              <el-icon><CopyDocument /></el-icon>
              克隆装箱单
            </el-button>
            <el-button 
              v-if="!contract.containers || contract.containers.length === 0"
              type="primary" 
              size="small" 
              @click="generateContainer"
              :loading="generating"
            >
              生成装箱单
            </el-button>
          </div>
        </div>
        
        <div v-if="contract.containers && contract.containers.length > 0">
          <el-table :data="contract.containers" stripe>
            <el-table-column prop="containerNo" label="装箱单号" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="containerSize" label="尺寸" />
            <el-table-column prop="containerWeight" label="重量" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewContainer(row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无装箱单" />
        </div>
      </div>
      
      <!-- 工艺分解结果 -->
      <div class="info-card">
        <div class="card-title">
          工艺分解结果
          <el-button 
            v-if="contract.status === 'DRAFT'"
            type="success" 
            size="small" 
            @click="startBreakdown"
            :loading="processing"
          >
            开始分解
          </el-button>
          <el-button 
            v-if="contract.status === 'COMPLETED'"
            type="primary" 
            size="small" 
            @click="exportBreakdown"
          >
            导出分解表
          </el-button>
        </div>
        
        <div v-if="breakdownResult">
          <el-alert
            :title="breakdownResult.message"
            :type="breakdownResult.success ? 'success' : 'error'"
            :closable="false"
            style="margin-bottom: 20px;"
          />
          
          <div v-if="breakdownResult.breakdownData">
            <!-- 这里显示分解表数据 -->
            <el-table :data="breakdownResult.breakdownData" stripe>
              <el-table-column prop="componentCode" label="零部件代号" />
              <el-table-column prop="componentName" label="零部件名称" />
              <el-table-column prop="specification" label="规格" />
              <el-table-column prop="quantity" label="数量" />
              <el-table-column prop="unit" label="单位" />
              <el-table-column prop="source" label="来源" />
            </el-table>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无分解结果" />
        </div>
      </div>
    </div>
    
    <!-- 编辑参数对话框 -->
    <el-dialog
      v-model="editParamsDialogVisible"
      title="编辑合同参数"
      width="800px"
      @close="handleEditParamsDialogClose"
    >
      <div class="parameters-section">
        <div class="section-header">
          <h4>合同参数设置</h4>
          <el-button type="primary" size="small" @click="addParameter">
            <el-icon><Plus /></el-icon>
            添加参数
          </el-button>
        </div>
        
        <el-table :data="editParamsForm.parameters" style="width: 100%" border>
          <el-table-column prop="paramName" label="参数名称" width="200">
            <template #default="{ row, $index }">
              <el-input 
                v-model="row.paramName" 
                placeholder="请输入参数名称"
                @blur="validateParameterName($index)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="paramValue" label="参数值" min-width="300">
            <template #default="{ row }">
              <el-input 
                v-model="row.paramValue" 
                placeholder="请输入参数值"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button 
                type="danger" 
                size="small" 
                @click="removeParameter($index)"
                :disabled="editParamsForm.parameters.length <= 1"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="parameter-tips">
          <el-text type="info" size="small">
            <el-icon><InfoFilled /></el-icon>
            提示：参数名称不能重复，建议使用有意义的参数名称，如"电梯类型"、"载重"、"速度"等
          </el-text>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="editParamsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParameters" :loading="savingParams">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 上传装箱单对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传装箱单"
      width="600px"
      @close="handleUploadDialogClose"
    >
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传Excel文件(.xlsx/.xls)，且文件大小不超过10MB
            </div>
          </template>
        </el-upload>
        
        <div v-if="selectedFile" class="file-info">
          <el-text type="success">
            <el-icon><Document /></el-icon>
            已选择文件: {{ selectedFile.name }}
          </el-text>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="uploadFile" :loading="uploading" :disabled="!selectedFile">
          上传
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 克隆装箱单对话框 -->
    <el-dialog
      v-model="cloneDialogVisible"
      title="克隆装箱单"
      width="600px"
      @close="handleCloneDialogClose"
    >
      <div class="clone-section">
        <el-form :model="cloneForm" label-width="120px">
          <el-form-item label="源合同">
            <el-select
              v-model="cloneForm.sourceContractId"
              placeholder="请选择要克隆的合同"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="contract in availableContracts"
                :key="contract.id"
                :label="`${contract.contractNo} - ${contract.projectName}`"
                :value="contract.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        
        <div class="clone-tips">
          <el-alert
            title="克隆说明"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>• 将复制源合同的所有装箱单及其组件信息</p>
              <p>• 克隆后的装箱单将关联到当前合同</p>
              <p>• 装箱单号和组件信息将保持不变</p>
            </template>
          </el-alert>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="cloneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="cloneContainers" :loading="cloning" :disabled="!cloneForm.sourceContractId">
          克隆
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { contractsApi } from '@/api/contracts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const generating = ref(false)
const processing = ref(false)
const savingParams = ref(false)
const uploading = ref(false)
const cloning = ref(false)
const contract = ref(null)
const breakdownResult = ref(null)
const editParamsDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const cloneDialogVisible = ref(false)
const editParamsForm = ref({
  parameters: []
})
const selectedFile = ref(null)
const cloneForm = ref({
  sourceContractId: null
})
const availableContracts = ref([])

const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': '',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'ERROR': 'danger'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'ERROR': '错误'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadContract = async () => {
  loading.value = true
  try {
    const contractId = route.params.id
    contract.value = await contractsApi.getContract(contractId)
    
    // 加载分解结果
    if (contract.value.status !== 'DRAFT') {
      await loadBreakdownResult()
    }
  } catch (error) {
    ElMessage.error('加载合同详情失败')
  } finally {
    loading.value = false
  }
}

const loadBreakdownResult = async () => {
  try {
    const contractId = route.params.id
    breakdownResult.value = await contractsApi.getBreakdownResult(contractId)
  } catch (error) {
    console.error('加载分解结果失败:', error)
  }
}

const generateContainer = async () => {
  generating.value = true
  try {
    const contractId = route.params.id
    const result = await contractsApi.generateContainer(contractId)
    
    if (result.success) {
      ElMessage.success(result.message)
      await loadContract()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('生成装箱单失败')
  } finally {
    generating.value = false
  }
}

const startBreakdown = async () => {
  processing.value = true
  try {
    const contractId = route.params.id
    const result = await contractsApi.startBreakdown(contractId)
    
    if (result.success) {
      ElMessage.success(result.message)
      await loadContract()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('开始工艺分解失败')
  } finally {
    processing.value = false
  }
}

const exportBreakdown = async () => {
  try {
    const contractId = route.params.id
    const response = await contractsApi.exportBreakdown(contractId, 'excel')
    
    // 创建下载链接
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `分解表_${contract.value.contractNo}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const viewContainer = (container) => {
  // 跳转到装箱单详情页面
  console.log('查看装箱单:', container)
}

const goBack = () => {
  router.go(-1)
}

// 参数编辑相关方法
const showEditParamsDialog = () => {
  // 初始化编辑表单
  editParamsForm.value.parameters = contract.value.parameters && contract.value.parameters.length > 0
    ? [...contract.value.parameters]
    : [{ paramName: '', paramValue: '' }]
  
  editParamsDialogVisible.value = true
}

const addParameter = () => {
  editParamsForm.value.parameters.push({ paramName: '', paramValue: '' })
}

const removeParameter = (index) => {
  if (editParamsForm.value.parameters.length > 1) {
    editParamsForm.value.parameters.splice(index, 1)
  }
}

const validateParameterName = (index) => {
  const currentName = editParamsForm.value.parameters[index].paramName
  if (!currentName) return
  
  const duplicateIndex = editParamsForm.value.parameters.findIndex((param, i) => 
    i !== index && param.paramName === currentName
  )
  
  if (duplicateIndex !== -1) {
    ElMessage.warning('参数名称不能重复')
    editParamsForm.value.parameters[index].paramName = ''
  }
}

const saveParameters = async () => {
  savingParams.value = true
  try {
    const contractId = route.params.id
    
    // 过滤掉空的参数
    const validParameters = editParamsForm.value.parameters.filter(param => 
      param.paramName.trim() && param.paramValue.trim()
    )
    
    // 调用API保存参数
    await contractsApi.updateContractParameters(contractId, validParameters)
    
    ElMessage.success('参数保存成功')
    editParamsDialogVisible.value = false
    
    // 重新加载合同数据
    await loadContract()
  } catch (error) {
    console.error('保存参数失败:', error)
    ElMessage.error('保存参数失败')
  } finally {
    savingParams.value = false
  }
}

const handleEditParamsDialogClose = () => {
  editParamsForm.value.parameters = []
}

// 装箱单上传相关方法
const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

const showCloneDialog = async () => {
  cloneDialogVisible.value = true
  await loadAvailableContracts()
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                 file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!')
    return false
  }
  return true
}

const uploadFile = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    const contractId = route.params.id
    const response = await contractsApi.uploadContainerFile(contractId, selectedFile.value)
    
    if (response.success) {
      ElMessage.success(`上传成功！共创建 ${response.count} 个装箱单`)
      uploadDialogVisible.value = false
      await loadContract() // 重新加载合同数据
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error('上传文件失败')
  } finally {
    uploading.value = false
  }
}

const cloneContainers = async () => {
  if (!cloneForm.value.sourceContractId) {
    ElMessage.warning('请选择源合同')
    return
  }

  cloning.value = true
  try {
    const contractId = route.params.id
    const response = await contractsApi.cloneContainers(contractId, cloneForm.value.sourceContractId)
    
    if (response.success) {
      ElMessage.success(`克隆成功！共克隆 ${response.count} 个装箱单`)
      cloneDialogVisible.value = false
      await loadContract() // 重新加载合同数据
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('克隆装箱单失败:', error)
    ElMessage.error('克隆装箱单失败')
  } finally {
    cloning.value = false
  }
}

const loadAvailableContracts = async () => {
  try {
    const response = await contractsApi.getContracts({ page: 0, size: 1000 })
    availableContracts.value = response.content || []
  } catch (error) {
    console.error('加载合同列表失败:', error)
  }
}

const handleUploadDialogClose = () => {
  selectedFile.value = null
}

const handleCloneDialogClose = () => {
  cloneForm.value.sourceContractId = null
}

onMounted(() => {
  loadContract()
})
</script>

<style lang="scss" scoped>
.contract-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .back-btn {
    margin-right: 20px;
  }
  
  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.loading {
  padding: 20px;
}

.detail-content {
  .info-card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
    margin-bottom: 20px;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .card-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .empty-state {
    padding: 40px 0;
  }
}

.parameters-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h4 {
      margin: 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .parameter-tips {
    margin-top: 15px;
    padding: 10px;
    background-color: #f4f4f5;
    border-radius: 4px;
    
    .el-text {
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.upload-section {
  .file-info {
    margin-top: 15px;
    padding: 10px;
    background-color: #f0f9ff;
    border-radius: 4px;
    border: 1px solid #b3d8ff;
    
    .el-text {
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.clone-section {
  .clone-tips {
    margin-top: 20px;
    
    p {
      margin: 5px 0;
      font-size: 14px;
    }
  }
}
</style>
