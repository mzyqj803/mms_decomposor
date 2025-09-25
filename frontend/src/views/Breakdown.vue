<template>
  <div class="breakdown">
    <div class="page-header">
      <h1 class="title">工艺分解</h1>
      <p class="description">电梯零部件工艺分解和合并分解表生成</p>
    </div>
    
    <!-- 合同搜索 -->
    <div class="search-section">
      <el-card class="search-card">
        <template #header>
          <div class="card-header">
            <span>合同搜索</span>
          </div>
        </template>
        <el-form :model="searchForm" :inline="true" @submit.prevent="searchContracts">
          <el-form-item label="合同号/项目号">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入合同号或项目号"
              clearable
              style="width: 300px"
              @keyup.enter="searchContracts"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchContracts" :loading="searchLoading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <!-- 合同列表 -->
    <div class="contracts-section" v-if="contracts.length > 0">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>合同列表</span>
          </div>
        </template>
        <el-table :data="contracts" stripe>
          <el-table-column prop="contractNo" label="合同号" width="200" />
          <el-table-column prop="projectName" label="项目名称" />
          <el-table-column prop="clientName" label="客户名称" width="200" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="selectContract(row)">
                选择合同
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 箱包选择 -->
    <div class="containers-section" v-if="selectedContract">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>箱包选择 - {{ selectedContract.contractNo }}</span>
            <div class="header-buttons">
              <el-button type="success" @click="mergeBreakdownTables" :loading="mergeLoading" :disabled="selectedContainers.length === 0">
                合并分解表
              </el-button>
              <el-button type="primary" @click="breakdownAllContainers" :loading="breakdownLoading">
                全部分解
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="containers" stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" :selectable="isSelectable" />
          <el-table-column prop="containerNo" label="箱包号" width="200" />
          <el-table-column prop="name" label="箱包名称" />
          <el-table-column prop="containerSize" label="尺寸" width="120" />
          <el-table-column prop="containerWeight" label="重量" width="120" />
          <el-table-column label="操作" width="320">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 0"
                type="success" 
                size="small" 
                @click="breakdownContainer(row)"
                :loading="breakdownLoading && breakdownLoadingContainerId === row.id"
              >
                工艺分解
              </el-button>
              <template v-else>
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="breakdownContainer(row)"
                  :loading="breakdownLoading && breakdownLoadingContainerId === row.id"
                >
                  重新分解
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewBreakdownTable(row)"
                  style="margin-left: 8px"
                >
                  查看分解表
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteBreakdown(row)"
                  style="margin-left: 8px"
                >
                  删除分解表
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 分解结果 -->
    <div class="results-section" v-if="breakdownResults">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>分解结果</span>
            <el-button type="success" @click="exportBreakdown" :loading="exportLoading">
              导出汇总表
            </el-button>
          </div>
        </template>
        
        <!-- 问题部件提醒 -->
        <el-alert
          v-if="breakdownResults.allProblemComponents && breakdownResults.allProblemComponents.length > 0"
          title="发现问题部件"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <div>
              <p>以下部件在components表中找不到匹配项：</p>
              <ul>
                <li v-for="problem in breakdownResults.allProblemComponents" :key="problem">
                  {{ problem }}
                </li>
              </ul>
            </div>
          </template>
        </el-alert>
        
        <!-- 汇总统计 -->
        <div class="summary-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总箱包数" :value="breakdownResults.totalContainers" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="处理部件数" :value="breakdownResults.totalProcessedComponents" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="问题部件数" :value="breakdownResults.allProblemComponents ? breakdownResults.allProblemComponents.length : 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="分解状态" value="完成" />
            </el-col>
          </el-row>
        </div>
        
        <!-- 箱包分解详情 -->
        <el-collapse v-model="activeCollapse" style="margin-top: 20px">
          <el-collapse-item
            v-for="containerResult in breakdownResults.containerResults"
            :key="containerResult.containerId"
            :title="`${containerResult.containerNo} - ${containerResult.containerName}`"
            :name="containerResult.containerId"
          >
            <div class="container-detail">
              <p><strong>总组件数：</strong>{{ containerResult.totalComponents }}</p>
              <p v-if="containerResult.problemComponents && containerResult.problemComponents.length > 0">
                <strong>问题部件：</strong>
                <span style="color: #f56c6c">{{ containerResult.problemComponents.join(', ') }}</span>
              </p>
              
              <!-- 部件分解详情 -->
              <el-table :data="containerResult.allComponents" style="margin-top: 15px">
                <el-table-column prop="componentCode" label="部件编号" width="150" />
                <el-table-column prop="name" label="部件名称" />
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="procurementFlag" label="是否外购" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.procurementFlag ? 'success' : 'info'">
                      {{ row.procurementFlag ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="commonPartsFlag" label="是否通用件" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.commonPartsFlag ? 'warning' : 'info'">
                      {{ row.commonPartsFlag ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>

    <!-- 分解表弹窗 -->
    <el-dialog
      v-model="showBreakdownDialog"
      title="分解表详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="currentBreakdownData">
        <div class="breakdown-dialog-header">
          <h3>{{ currentBreakdownData.containerNo }} - {{ currentBreakdownData.containerName }}</h3>
          <p><strong>总组件数：</strong>{{ currentBreakdownData.totalComponents }}</p>
          <p v-if="currentBreakdownData.problemComponents && currentBreakdownData.problemComponents.length > 0">
            <strong>问题部件：</strong>
            <span style="color: #f56c6c">{{ currentBreakdownData.problemComponents.join(', ') }}</span>
          </p>
        </div>
        
        <el-table 
          :data="currentBreakdownData.allComponents" 
          stripe 
          style="margin-top: 20px"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="componentCode" label="部件编号" width="150" />
          <el-table-column prop="name" label="部件名称" min-width="200" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="procurementFlag" label="是否外购" width="100">
            <template #default="{ row }">
              <el-tag :type="row.procurementFlag ? 'success' : 'info'">
                {{ row.procurementFlag ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="commonPartsFlag" label="是否通用件" width="100">
            <template #default="{ row }">
              <el-tag :type="row.commonPartsFlag ? 'warning' : 'info'">
                {{ row.commonPartsFlag ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" width="120">
            <template #default="{ row }">
              <span v-if="row.remark" class="problem-component-text">{{ row.remark }}</span>
              <span v-else class="normal-component-text">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="showBreakdownDialog = false">关闭</el-button>
        <el-button type="primary" @click="exportSingleBreakdown">导出此箱包分解表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { contractsApi } from '@/api/contracts'
import { breakdownApi } from '@/api/breakdown'
import '@/styles/problem-components.css'

// 响应式数据
const searchForm = reactive({
  keyword: ''
})

const contracts = ref([])
const selectedContract = ref(null)
const containers = ref([])
const breakdownResults = ref(null)
const activeCollapse = ref([])
const showBreakdownDialog = ref(false) // 控制分解表弹窗显示
const currentBreakdownData = ref(null) // 当前要显示的分解数据

// 加载状态
const searchLoading = ref(false)
const breakdownLoading = ref(false)
const exportLoading = ref(false)
const mergeLoading = ref(false)
const breakdownLoadingContainerId = ref(null) // 当前正在分解的箱包ID

// 合并分解表相关
const selectedContainers = ref([]) // 选中的箱包

// 获取表格行样式类名
const getRowClassName = ({ row }) => {
  return row.remark ? 'problem-component-row' : 'normal-component-row'
}

// 搜索合同
const searchContracts = async () => {
  if (!searchForm.keyword.trim()) {
    ElMessage.warning('请输入合同号或项目号')
    return
  }
  
  searchLoading.value = true
  try {
    const response = await contractsApi.searchContracts(searchForm.keyword, 0, 10)
    contracts.value = response.content || []
    
    if (contracts.value.length === 0) {
      ElMessage.info('未找到匹配的合同')
    }
  } catch (error) {
    console.error('搜索合同失败:', error)
    ElMessage.error('搜索合同失败')
  } finally {
    searchLoading.value = false
  }
}

// 选择合同
const selectContract = async (contract) => {
  selectedContract.value = contract
  try {
    const response = await contractsApi.getContractContainers(contract.id)
    containers.value = response || []
    
    if (containers.value.length === 0) {
      ElMessage.info('该合同暂无箱包数据')
    }
    // 移除自动检查分解状态的逻辑，改为点击时再查询
  } catch (error) {
    console.error('获取箱包列表失败:', error)
    ElMessage.error('获取箱包列表失败')
  }
}

// 移除checkContainerBreakdownStatus函数，改为点击时再查询

// 分解单个箱包
const breakdownContainer = async (container) => {
  try {
    await ElMessageBox.confirm(
      `确定要对箱包 ${container.containerNo} 进行工艺分解吗？`,
      '确认分解',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    breakdownLoading.value = true
    breakdownLoadingContainerId.value = container.id
    
    const response = await breakdownApi.breakdownContainer(container.id)
    
    ElMessage.success('箱包工艺分解完成')
    
    // 更新container状态
    container.status = 1
    
    // 显示分解结果
    breakdownResults.value = {
      containerResults: [response],
      totalContainers: 1,
      totalProcessedComponents: response.totalComponents,
      allProblemComponents: response.problemComponents
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('箱包分解失败:', error)
      ElMessage.error('箱包分解失败')
    }
  } finally {
    breakdownLoading.value = false
    breakdownLoadingContainerId.value = null
  }
}

// 分解所有箱包
const breakdownAllContainers = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要对合同 ${selectedContract.value.contractNo} 的所有箱包进行工艺分解吗？`,
      '确认分解',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    breakdownLoading.value = true
    const response = await breakdownApi.breakdownContract(selectedContract.value.id)
    
    ElMessage.success('合同工艺分解完成')
    breakdownResults.value = response
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('合同分解失败:', error)
      ElMessage.error('合同分解失败')
    }
  } finally {
    breakdownLoading.value = false
  }
}

// 查看分解表
const viewBreakdownTable = async (container) => {
  try {
    // 点击时才查询分解数据
    const response = await breakdownApi.getContainerBreakdown(container.id)
    
    if (response.hasBreakdown) {
      currentBreakdownData.value = {
        containerId: container.id,
        containerNo: container.containerNo,
        containerName: container.name,
        totalComponents: response.totalComponents,
        problemComponents: response.problemComponents,
        allComponents: response.allComponents,
        breakdownTime: response.breakdownTime || '未知时间'
      }
      showBreakdownDialog.value = true
    } else {
      ElMessage.warning('该箱包尚未进行工艺分解')
    }
  } catch (error) {
    console.error('获取分解表失败:', error)
    ElMessage.error('获取分解表失败')
  }
}

// 删除分解表
const deleteBreakdown = async (container) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除箱包 ${container.containerNo} 的分解表吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await breakdownApi.deleteContainerBreakdown(container.id)
    ElMessage.success('分解表已删除')
    
    // 更新container状态
    container.status = 0
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分解表失败:', error)
      ElMessage.error('删除分解表失败')
    }
  }
}

// 导出单个箱包分解表
const exportSingleBreakdown = async () => {
  if (!currentBreakdownData.value) {
    ElMessage.warning('没有可导出的分解数据')
    return
  }
  
  try {
    // 创建Excel数据
    const breakdownData = currentBreakdownData.value
    const wsData = [
      ['箱包号', '部件编号', '部件名称', '数量', '是否外购', '是否通用件', '备注']
    ]
    
    breakdownData.allComponents.forEach(component => {
      wsData.push([
        breakdownData.containerNo,
        component.componentCode,
        component.name,
        component.quantity,
        component.procurementFlag ? '是' : '否',
        component.commonPartsFlag ? '是' : '否',
        component.remark || ''
      ])
    })
    
    // 创建CSV内容
    const csvContent = wsData.map(row => row.join(',')).join('\n')
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `分解表_${breakdownData.containerNo}.csv`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出分解表
const exportBreakdown = async () => {
  if (!selectedContract.value) {
    ElMessage.warning('请先选择合同')
    return
  }
  
  exportLoading.value = true
  try {
    const response = await breakdownApi.exportBreakdown(selectedContract.value.id, 'excel')
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `工艺分解表_${selectedContract.value.contractNo}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'PROCESSING': 'warning',
    'COMPLETED': 'success',
    'ERROR': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PROCESSING': '处理中',
    'COMPLETED': '完成',
    'ERROR': '错误'
  }
  return statusMap[status] || status
}

// 检查行是否可选择（只有已分解的箱包才能选择）
const isSelectable = (row) => {
  return row.status === 1
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedContainers.value = selection
}

// 合并分解表
const mergeBreakdownTables = async () => {
  if (selectedContainers.value.length === 0) {
    ElMessage.warning('请选择要合并的箱包')
    return
  }
  
  try {
    mergeLoading.value = true
    
    const containerIds = selectedContainers.value.map(container => container.id)
    const response = await breakdownApi.mergeBreakdownTables(containerIds)
    
    if (response.success) {
      ElMessage.success('合并分解表成功')
      
      // 生成下载链接
      const downloadUrl = response.downloadUrl
      if (downloadUrl) {
        // 创建下载链接
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = `合并分解表_${selectedContract.value.contractNo}_${new Date().toISOString().slice(0, 10)}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        ElMessage.success('PDF文件已开始下载')
      }
    } else {
      ElMessage.error(response.message || '合并分解表失败')
    }
  } catch (error) {
    console.error('合并分解表失败:', error)
    ElMessage.error('合并分解表失败')
  } finally {
    mergeLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.breakdown {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-buttons {
    display: flex;
    gap: 10px;
  }
}

.info-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  
  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }
}

.breakdown-dialog-header {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  
  h3 {
    margin: 0 0 10px 0;
    color: #303133;
    font-size: 18px;
  }
  
  p {
    margin: 5px 0;
    color: #606266;
    font-size: 14px;
  }
}


.sub-component-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  
  &:last-child {
    margin-bottom: 0;
  }
}
</style>
