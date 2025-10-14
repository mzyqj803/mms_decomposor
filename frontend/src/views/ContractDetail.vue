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
      
      <!-- 页面操作菜单栏 -->
      <div class="action-menu-bar">
        <div class="menu-group">
          <span class="group-label">编辑合同</span>
          <el-button-group>
            <el-button @click="openEditContractDialog" size="small">
              <el-icon><Edit /></el-icon>
              更改合同信息
            </el-button>
            <el-button @click="showEditParamsDialog" size="small">
              <el-icon><Setting /></el-icon>
              编辑合同参数
            </el-button>
            <el-button @click="showCloneDialog" size="small">
              <el-icon><CopyDocument /></el-icon>
              克隆合同
            </el-button>
            <el-button @click="handleDeleteContract" :disabled="deleting" size="small" type="danger">
              <el-icon><Delete /></el-icon>
              删除合同
            </el-button>
          </el-button-group>
        </div>
        
        <div class="menu-group">
          <span class="group-label">工艺分解</span>
          <el-button-group>
            <el-button @click="openViewContainers" size="small" type="primary">
              <el-icon><View /></el-icon>
              查看装箱单
            </el-button>
            <el-button @click="showUploadDialog" size="small" type="primary">
              <el-icon><Upload /></el-icon>
              上传装箱单
            </el-button>
            <el-button @click="openViewBreakdown" size="small" type="primary">
              <el-icon><Document /></el-icon>
              查看工艺分解
            </el-button>
            <el-button @click="startBreakdown" :disabled="processing" size="small" type="primary">
              <el-icon><VideoPlay /></el-icon>
              执行工艺分解
            </el-button>
            <el-button @click="downloadBreakdownTable" :disabled="contract.status !== 2" size="small" type="success">
              <el-icon><Download /></el-icon>
              下载工艺分解合并表
            </el-button>
          </el-button-group>
        </div>
        
        <div class="menu-group">
          <span class="group-label">生产执行</span>
          <el-button-group>
            <el-button @click="openProductionPlan" size="small">
              <el-icon><Calendar /></el-icon>
              查看生产计划
            </el-button>
            <el-button @click="generateProductionPlan" size="small">
              <el-icon><Plus /></el-icon>
              生成生产计划
            </el-button>
            <el-button @click="openInventory" size="small">
              <el-icon><Box /></el-icon>
              零部件库存
            </el-button>
            <el-button @click="openCostAnalysis" size="small">
              <el-icon><TrendCharts /></el-icon>
              成本分析
            </el-button>
          </el-button-group>
        </div>
      </div>
      
      <!-- 合同基本信息 -->
      <div class="info-card">
        <div class="card-title">合同信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同号">{{ contract.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ contract.clientName }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ contract.projectName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(contract.status)">
              {{ getStatusText(contract.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(contract.entryTs) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 编辑合同信息对话框 -->
      <el-dialog
        v-model="editContractDialogVisible"
        title="更改合同信息"
        width="600px"
      >
        <el-form :model="editContractForm" label-width="100px">
          <el-form-item label="合同号">
            <el-input v-model="editContractForm.contractNo" />
          </el-form-item>
          <el-form-item label="客户名称">
            <el-input v-model="editContractForm.clientName" />
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="editContractForm.projectName" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="editContractDialogVisible = false">取 消</el-button>
            <el-button type="primary" :loading="saving" @click="saveContractInfo">保 存</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 合同参数 -->
      <div class="info-card">
        <div class="card-title" @click="toggleParamsCard">
          <div class="card-title-left">
            <el-icon class="collapse-icon" :class="{ 'is-expanded': paramsCardExpanded }">
              <ArrowRight />
            </el-icon>
            合同参数 (点击查看)
          </div>
          <div class="card-actions" @click.stop>
            <el-button 
              type="primary" 
              size="small" 
              @click="showEditParamsDialog"
            >
              <el-icon><Edit /></el-icon>
              编辑参数
            </el-button>
          </div>
        </div>
        
        <div v-show="paramsCardExpanded" class="card-content">
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
      </div>
      
      <!-- 装箱单信息 -->
      <div class="info-card">
        <div class="card-title" @click="toggleContainersCard">
          <div class="card-title-left">
            <el-icon class="collapse-icon" :class="{ 'is-expanded': containersCardExpanded }">
              <ArrowRight />
            </el-icon>
            装箱单信息 (点击查看)
          </div>
          <div class="card-actions" @click.stop>
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
        
        <div v-show="containersCardExpanded" class="card-content">
          <div v-if="containersLoading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="contract.containers && contract.containers.length > 0">
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
      </div>
      
      <!-- 工艺分解结果 -->
      <div class="info-card">
        <div class="card-title" @click="toggleBreakdownCard">
          <div class="card-title-left">
            <el-icon class="collapse-icon" :class="{ 'is-expanded': breakdownCardExpanded }">
              <ArrowRight />
            </el-icon>
            工艺分解结果 (点击查看)
          </div>
          <div class="card-actions" @click.stop>
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
        </div>
        
        <div v-show="breakdownCardExpanded" class="card-content">
          <div v-if="breakdownLoading" class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="breakdownResult">
            <el-alert
              :title="breakdownResult.message"
              :type="breakdownResult.success ? 'success' : 'error'"
              :closable="false"
              style="margin-bottom: 20px;"
            />
            
            <div v-if="breakdownResult.breakdownData">
              <!-- 这里显示分解表数据 -->
              <el-table :data="breakdownResult.breakdownData" stripe>
                <el-table-column prop="containerName" label="所属箱包" width="150" />
                <el-table-column prop="componentCode" label="部件代号" width="150" />
                <el-table-column prop="componentName" label="部件名称" min-width="200" />
                <el-table-column prop="quantity" label="数量" width="80" align="center" />
                <el-table-column prop="erpCode" label="ERP代码" width="120" />
                <el-table-column prop="procurementFlag" label="是否外购" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.procurementFlag ? 'success' : 'info'" size="small">
                      {{ row.procurementFlag ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="150" />
              </el-table>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty description="暂无分解结果" />
          </div>
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

// 顶部菜单交互状态
const editContractDialogVisible = ref(false)
const editContractForm = ref({ contractNo: '', clientName: '', projectName: '' })
const saving = ref(false)
const deleting = ref(false)

const openEditContractDialog = () => {
  if (!contract.value) return
  editContractForm.value = {
    contractNo: contract.value.contractNo || '',
    clientName: contract.value.clientName || '',
    projectName: contract.value.projectName || ''
  }
  editContractDialogVisible.value = true
}

const saveContractInfo = async () => {
  try {
    if (!contract.value) return
    saving.value = true
    const payload = {
      ...contract.value,
      contractNo: editContractForm.value.contractNo,
      clientName: editContractForm.value.clientName,
      projectName: editContractForm.value.projectName
    }
    await contractsApi.updateContract(contract.value.id, payload)
    ElMessage.success('合同信息已更新')
    editContractDialogVisible.value = false
    await loadContract()
  } catch (e) {
    ElMessage.error('更新合同信息失败')
  } finally {
    saving.value = false
  }
}

const handleDeleteContract = async () => {
  if (!contract.value) return
  try {
    deleting.value = true
    await contractsApi.deleteContract(contract.value.id)
    ElMessage.success('合同已删除')
    router.push('/contracts')
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deleting.value = false
  }
}

// 菜单占位函数
const openViewContainers = () => { containersCardExpanded.value = true }
const openViewBreakdown = () => { breakdownCardExpanded.value = true }
const openProductionPlan = () => { ElMessage.info('查看生产计划（待实现）') }
const generateProductionPlan = () => { ElMessage.info('生成生产计划（待实现）') }
const openInventory = () => { ElMessage.info('零部件库存（待实现）') }
const openCostAnalysis = () => { ElMessage.info('成本分析（待实现）') }

// 卡片折叠状态
const paramsCardExpanded = ref(false)
const containersCardExpanded = ref(false)
const breakdownCardExpanded = ref(false)

// 异步加载状态
const containersLoading = ref(false)
const breakdownLoading = ref(false)

// 数据加载标记
const containersLoaded = ref(false)
const breakdownLoaded = ref(false)

// 将后端返回的状态标准化为字符串键值
const toStatusKey = (status) => {
  if (typeof status === 'number') {
    switch (status) {
      case 0: return 'DRAFT'
      case 1: return 'PROCESSING'
      case 2: return 'COMPLETED'
      case 3: return 'ERROR'
      default: return 'DRAFT'
    }
  }
  if (!status) return 'DRAFT'
  return String(status).toUpperCase()
}

const getStatusType = (status) => {
  const key = toStatusKey(status)
  const statusMap = {
    'DRAFT': '',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'ERROR': 'danger'
  }
  return statusMap[key] || ''
}

const getStatusText = (status) => {
  const key = toStatusKey(status)
  const statusMap = {
    'DRAFT': '草稿',
    'PROCESSING': '处理中',
    'COMPLETED': '工艺分解成功',
    'ERROR': '错误'
  }
  return statusMap[key] || '未知'
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 切换合同参数卡片
const toggleParamsCard = () => {
  paramsCardExpanded.value = !paramsCardExpanded.value
}

const loadContract = async () => {
  loading.value = true
  try {
    const contractId = route.params.id
    contract.value = await contractsApi.getContract(contractId)
    
    // 检查是否有装箱单信息，如果没有则自动弹出上传装箱单窗口
    if (!contract.value.containers || contract.value.containers.length === 0) {
      // 延迟一点时间确保页面完全加载后再弹出窗口
      setTimeout(() => {
        uploadDialogVisible.value = true
      }, 500)
    }
    
    // 不再自动加载装箱单和分解结果，等待用户点击展开时再加载
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

// 切换装箱单卡片
const toggleContainersCard = async () => {
  containersCardExpanded.value = !containersCardExpanded.value
  
  // 如果展开且未加载过数据，则异步加载
  if (containersCardExpanded.value && !containersLoaded.value) {
    containersLoading.value = true
    try {
      // 重新加载合同数据以获取装箱单信息
      await loadContract()
      containersLoaded.value = true
    } catch (error) {
      console.error('加载装箱单数据失败:', error)
      ElMessage.error('加载装箱单数据失败')
    } finally {
      containersLoading.value = false
    }
  }
}

// 切换工艺分解结果卡片
const toggleBreakdownCard = async () => {
  breakdownCardExpanded.value = !breakdownCardExpanded.value
  
  // 如果展开且未加载过数据，则异步加载
  if (breakdownCardExpanded.value && !breakdownLoaded.value) {
    breakdownLoading.value = true
    try {
      await loadBreakdownResult()
      breakdownLoaded.value = true
    } catch (error) {
      console.error('加载分解结果失败:', error)
      ElMessage.error('加载分解结果失败')
    } finally {
      breakdownLoading.value = false
    }
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

const downloadBreakdownTable = async () => {
  try {
    const contractId = route.params.id
    const response = await contractsApi.exportBreakdown(contractId, 'pdf')
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `工艺分解合并表_${contract.value.contractNo}_${dayjs().format('YYYY-MM-DD')}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
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
      
      // 上传成功后自动开始工艺分解
      try {
        processing.value = true
        const breakdownResult = await contractsApi.startBreakdown(contractId)
        
        if (breakdownResult.success) {
          ElMessage.success('装箱单上传成功，工艺分解已自动开始')
          await loadContract() // 重新加载合同数据以更新状态
        } else {
          ElMessage.warning(`装箱单上传成功，但工艺分解启动失败：${breakdownResult.message}`)
        }
      } catch (breakdownError) {
        console.error('自动开始工艺分解失败:', breakdownError)
        ElMessage.warning('装箱单上传成功，但工艺分解启动失败')
      } finally {
        processing.value = false
      }
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
.action-menu-bar {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-label {
  font-weight: 600;
  color: #495057;
  min-width: 80px;
  font-size: 14px;
}

.el-button-group {
  display: flex;
  gap: 4px;
}
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
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 8px;
        margin: -8px -8px 8px -8px;
      }
      
      .card-title-left {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .collapse-icon {
          transition: transform 0.3s ease;
          font-size: 14px;
          color: #909399;
          
          &.is-expanded {
            transform: rotate(90deg);
          }
        }
      }
      
      .card-actions {
        display: flex;
        gap: 8px;
      }
    }
    
    .card-content {
      animation: slideDown 0.3s ease-out;
    }
    
    .loading-state {
      padding: 20px 0;
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

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
