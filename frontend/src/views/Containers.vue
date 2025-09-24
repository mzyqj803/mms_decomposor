<template>
  <div class="containers">
    <div class="page-header">
      <h1 class="title">装箱单管理</h1>
      <p class="description">管理电梯装箱单和装箱组件</p>
    </div>
    
    <div class="table-container">
      <div class="table-header">
        <div class="header-left">
          <h3 class="header-title">装箱单列表</h3>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建装箱单
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
            <el-form-item label="装箱单号">
              <el-input
                v-model="searchForm.containerNo"
                placeholder="请输入装箱单号"
                clearable
                style="width: 200px;"
              />
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
          :data="containers"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="contractNo" label="合同号" width="150" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" />
          <el-table-column prop="containerNo" label="箱包号" width="150" />
          <el-table-column prop="name" label="箱包名称" min-width="200" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleEdit(row)">
                编辑箱包
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除箱包
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
    
    <!-- 新建装箱单对话框 -->
    <CreateContainerDialog
      v-model="showCreateDialog"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import CreateContainerDialog from '@/components/CreateContainerDialog.vue'
import containersApi from '@/api/containers'

const loading = ref(false)
const showCreateDialog = ref(false)

const searchForm = reactive({
  containerNo: '',
  contractNo: '',
  projectName: ''
})

const containers = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadContainers = async () => {
  loading.value = true
  try {
    const response = await containersApi.getContainers({
      containerNo: searchForm.containerNo,
      contractNo: searchForm.contractNo,
      projectName: searchForm.projectName,
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    })
    
    containers.value = response.content || []
    pagination.total = response.totalElements || 0
    
    // 处理数据，添加合同号和项目名称等信息
    containers.value = containers.value.map(container => ({
      ...container,
      contractNo: container.contract?.contractNo || '',
      projectName: container.contract?.projectName || '',
      componentCount: container.components?.length || 0
    }))
    
  } catch (error) {
    ElMessage.error('加载装箱单列表失败')
    console.error('Load containers error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadContainers()
}

const handleReset = () => {
  Object.assign(searchForm, {
    containerNo: '',
    contractNo: '',
    projectName: ''
  })
  handleSearch()
}

const handleCreate = () => {
  showCreateDialog.value = true
}

const handleView = (row) => {
  ElMessage.info(`查看装箱单: ${row.name}`)
}

const handleEdit = (row) => {
  // TODO: 实现编辑箱包功能
  ElMessage.info(`编辑箱包: ${row.name}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除箱包 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await containersApi.deleteContainer(row.id)
    
    if (response.success) {
      ElMessage.success('删除成功')
      loadContainers()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除箱包失败')
      console.error('Delete container error:', error)
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadContainers()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadContainers()
}

const handleCreateSuccess = (data) => {
  ElMessage.success('装箱单创建成功')
  loadContainers()
}

onMounted(() => {
  loadContainers()
})
</script>

<style lang="scss" scoped>
.containers {
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
</style>
