<template>
  <div class="components">
    <div class="page-header">
      <h1 class="title">零部件管理</h1>
      <p class="description">管理电梯零部件基础信息和规格参数</p>
    </div>
    
    <div class="table-container">
      <div class="table-header">
        <div class="header-left">
          <h3 class="header-title">零部件列表</h3>
          <el-button type="primary" @click="handleCreate" class="create-button">
            <el-icon><Plus /></el-icon>
            新增零部件
          </el-button>
        </div>
      </div>
      
      <div class="table-content">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="零部件代号">
              <el-input
                v-model="searchForm.componentCode"
                placeholder="请输入零部件代号"
                clearable
                style="width: 200px;"
              />
            </el-form-item>
            <el-form-item label="零部件名称">
              <el-input
                v-model="searchForm.name"
                placeholder="请输入零部件名称"
                clearable
                style="width: 200px;"
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select
                v-model="searchForm.categoryCode"
                placeholder="请选择分类"
                clearable
                style="width: 150px;"
              >
                <el-option label="电梯主机" value="ELEVATOR" />
                <el-option label="门机" value="DOOR" />
                <el-option label="轿厢" value="CABIN" />
                <el-option label="导轨" value="RAIL" />
                <el-option label="电缆" value="CABLE" />
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
          :data="components"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="categoryCode" label="分类代码" width="120" />
          <el-table-column prop="componentCode" label="零部件代号" width="150" />
          <el-table-column prop="name" label="零部件名称" min-width="200" />
          <el-table-column prop="procurementFlag" label="采购标识" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.procurementFlag ? 'success' : 'info'">
                {{ row.procurementFlag ? '采购' : '自制' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="commonPartsFlag" label="通用件" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.commonPartsFlag === 1 ? 'warning' : (row.commonPartsFlag === 2 ? 'success' : 'info')">
                {{ row.commonPartsFlag === 1 ? '装箱紧固件' : (row.commonPartsFlag === 2 ? '装配紧固件' : '非紧固件') }}
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
              <el-button type="success" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
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
    
    <!-- 对话框组件 -->
    <CreateComponentDialog
      v-model="showCreateDialog"
      @success="handleCreateSuccess"
    />
    
    <ViewComponentDialog
      v-model="showViewDialog"
      :component-data="selectedComponent"
      @edit="handleViewEdit"
    />
    
    <EditComponentDialog
      v-model="showEditDialog"
      :component-data="selectedComponent"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { componentsApi } from '@/api/components'
import CreateComponentDialog from '@/components/CreateComponentDialog.vue'
import ViewComponentDialog from '@/components/ViewComponentDialog.vue'
import EditComponentDialog from '@/components/EditComponentDialog.vue'

const loading = ref(false)

const searchForm = reactive({
  componentCode: '',
  name: '',
  categoryCode: ''
})

const components = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 对话框状态
const showCreateDialog = ref(false)
const showViewDialog = ref(false)
const showEditDialog = ref(false)
const selectedComponent = ref({})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadComponents = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1, // 后端页码从0开始
      size: pagination.pageSize,
      componentCode: searchForm.componentCode || undefined,
      name: searchForm.name || undefined,
      categoryCode: searchForm.categoryCode || undefined
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const response = await componentsApi.getComponents(params)
    
    if (response && response.content) {
      components.value = response.content
      pagination.total = response.totalElements || 0
    } else {
      components.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载零部件列表失败:', error)
    ElMessage.error('加载零部件列表失败')
    components.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadComponents()
}

const handleReset = () => {
  Object.assign(searchForm, {
    componentCode: '',
    name: '',
    categoryCode: ''
  })
  handleSearch()
}

const handleCreate = () => {
  showCreateDialog.value = true
}

const handleView = (row) => {
  selectedComponent.value = row
  showViewDialog.value = true
}

const handleEdit = (row) => {
  selectedComponent.value = row
  showEditDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除零部件 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await componentsApi.deleteComponent(row.id)
    ElMessage.success('删除成功')
    loadComponents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除零部件失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadComponents()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadComponents()
}

onMounted(() => {
  loadComponents()
})

// 对话框事件处理
const handleCreateSuccess = () => {
  loadComponents()
}

const handleEditSuccess = () => {
  loadComponents()
}

const handleViewEdit = (componentData) => {
  selectedComponent.value = componentData
  showEditDialog.value = true
}
</script>

<style lang="scss" scoped>
.components {
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
    
    .header-left {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .header-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
      
      .create-button {
        align-self: flex-start;
      }
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
