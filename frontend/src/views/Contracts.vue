<template>
  <div class="contracts">
    <div class="page-header">
      <h1 class="title">合同管理</h1>
      <p class="description">管理电梯生产合同和装箱单</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <div class="table-container">
      <div class="table-header">
        <div class="header-left">
          <h3 class="header-title">合同列表</h3>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建合同
          </el-button>
        </div>
      </div>
      
      <div class="table-content">
        <!-- 搜索栏 -->
        <div class="search-bar">
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
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
                style="width: 150px;"
              >
                <el-option label="草稿" value="DRAFT" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="错误" value="ERROR" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 表格 -->
        <el-table
          :data="contracts"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="contractNo" label="合同号" width="150" />
          <el-table-column prop="clientName" label="客户名称" width="200" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" />
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="entryTs" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.entryTs) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleAction(row)">
                {{ getActionText(row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 新建/编辑合同对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="contractFormRef"
            :model="contractForm"
            :rules="contractRules"
            label-width="100px"
          >
            <el-form-item label="合同号" prop="contractNo">
              <el-input v-model="contractForm.contractNo" placeholder="请输入合同号" />
            </el-form-item>
            <el-form-item label="客户名称" prop="clientName">
              <el-input v-model="contractForm.clientName" placeholder="请输入客户名称" />
            </el-form-item>
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="contractForm.projectName" placeholder="请输入项目名称" />
            </el-form-item>
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="contractForm.quantity" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 合同参数 -->
        <el-tab-pane label="合同参数" name="parameters">
          <div class="parameters-section">
            <div class="section-header">
              <h4>合同参数设置</h4>
              <el-button type="primary" size="small" @click="addParameter">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </div>
            
            <el-table :data="contractForm.parameters" style="width: 100%" border>
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
                    :disabled="contractForm.parameters.length <= 1"
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
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractsApi } from '@/api/contracts'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const contractFormRef = ref()
const activeTab = ref('basic')

const searchForm = reactive({
  contractNo: '',
  projectName: '',
  status: ''
})

const contractForm = reactive({
  contractNo: '',
  clientName: '',
  projectName: '',
  quantity: 1,
  parameters: [
    { paramName: '', paramValue: '' }
  ]
})

const contracts = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const contractRules = {
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

const dialogTitle = computed(() => {
  return contractForm.id ? '编辑合同' : '新建合同'
})

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

const getActionText = (row) => {
  if (!row.containers || row.containers.length === 0) {
    return '生成装箱单'
  } else if (row.status === 'DRAFT') {
    return '开始分解'
  } else if (row.status === 'PROCESSING') {
    return '查看进度'
  } else if (row.status === 'COMPLETED') {
    return '查看结果'
  } else if (row.status === 'ERROR') {
    return '查看错误'
  }
  return '操作'
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadContracts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1, // Spring Data JPA 使用0基索引
      size: pagination.pageSize
    }
    
    // 只添加非空的搜索参数
    if (searchForm.contractNo && searchForm.contractNo.trim()) {
      params.contractNo = searchForm.contractNo.trim()
    }
    if (searchForm.projectName && searchForm.projectName.trim()) {
      params.projectName = searchForm.projectName.trim()
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    const response = await contractsApi.getContracts(params)
    // 由于响应拦截器已经返回了response.data，所以response就是后端返回的数据
    contracts.value = response.content || []
    pagination.total = response.totalElements || 0
  } catch (error) {
    console.error('加载合同列表失败:', error)
    ElMessage.error('加载合同列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadContracts()
}

const handleReset = () => {
  Object.assign(searchForm, {
    contractNo: '',
    projectName: '',
    status: ''
  })
  handleSearch()
}

const handleCreate = () => {
  Object.assign(contractForm, {
    contractNo: '',
    clientName: '',
    projectName: '',
    quantity: 1,
    parameters: [
      { paramName: '', paramValue: '' }
    ]
  })
  activeTab.value = 'basic'
  dialogVisible.value = true
}

// 合同参数相关方法
const addParameter = () => {
  contractForm.parameters.push({ paramName: '', paramValue: '' })
}

const removeParameter = (index) => {
  if (contractForm.parameters.length > 1) {
    contractForm.parameters.splice(index, 1)
  }
}

const validateParameterName = (index) => {
  const currentName = contractForm.parameters[index].paramName
  if (!currentName) return
  
  const duplicateIndex = contractForm.parameters.findIndex((param, i) => 
    i !== index && param.paramName === currentName
  )
  
  if (duplicateIndex !== -1) {
    ElMessage.warning('参数名称不能重复')
    contractForm.parameters[index].paramName = ''
  }
}

const handleView = (row) => {
  router.push(`/contracts/${row.id}`)
}

const handleAction = async (row) => {
  if (!row.containers || row.containers.length === 0) {
    // 生成装箱单
    try {
      await contractsApi.generateContainer(row.id)
      ElMessage.success('装箱单生成成功')
      loadContracts()
    } catch (error) {
      ElMessage.error('生成装箱单失败')
    }
  } else if (row.status === 'DRAFT') {
    // 开始工艺分解
    try {
      await contractsApi.startBreakdown(row.id)
      ElMessage.success('工艺分解已开始')
      loadContracts()
    } catch (error) {
      ElMessage.error('开始工艺分解失败')
    }
  } else {
    // 查看详情
    router.push(`/contracts/${row.id}`)
  }
}

const handleSubmit = async () => {
  if (!contractFormRef.value) return
  
  await contractFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 过滤掉空的参数
        const validParameters = contractForm.parameters.filter(param => 
          param.paramName.trim() && param.paramValue.trim()
        )
        
        // 准备合同数据
        const contractData = {
          contractNo: contractForm.contractNo,
          clientName: contractForm.clientName,
          projectName: contractForm.projectName,
          quantity: contractForm.quantity,
          parameters: validParameters
        }
        
        if (contractForm.id) {
          await contractsApi.updateContract(contractForm.id, contractData)
          ElMessage.success('合同更新成功')
        } else {
          const response = await contractsApi.createContract(contractData)
          contractForm.id = response.id
          ElMessage.success('合同创建成功')
        }
        
        dialogVisible.value = false
        loadContracts()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  contractFormRef.value?.resetFields()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadContracts()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadContracts()
}

onMounted(() => {
  loadContracts()
})
</script>

<style lang="scss" scoped>
.contracts {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .description {
    color: #606266;
    font-size: 14px;
  }
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  
  .table-header {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .table-content {
    padding: 20px;
    
    .search-bar {
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #ebeef5;
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
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
</style>
