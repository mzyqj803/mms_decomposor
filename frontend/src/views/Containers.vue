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
            <el-form-item label="装箱单号">
              <el-input
                v-model="searchForm.containerNo"
                placeholder="请输入装箱单号"
                clearable
                style="width: 200px;"
              />
            </el-form-item>
            <el-form-item label="合同号">
              <el-input
                v-model="searchForm.contractNo"
                placeholder="请输入合同号"
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
          <el-table-column prop="containerNo" label="装箱单号" width="150" />
          <el-table-column prop="contractNo" label="合同号" width="150" />
          <el-table-column prop="name" label="装箱单名称" min-width="200" />
          <el-table-column prop="containerSize" label="尺寸" width="120" />
          <el-table-column prop="containerWeight" label="重量" width="120" />
          <el-table-column prop="componentCount" label="组件数量" width="100" align="center" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)

const searchForm = reactive({
  containerNo: '',
  contractNo: ''
})

const containers = ref([
  {
    id: 1,
    containerNo: 'PACK-001',
    contractNo: 'CT-2024-001',
    name: '某大厦电梯装箱单',
    containerSize: '2000x1000x1000',
    containerWeight: '500kg',
    componentCount: 25,
    entryTs: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    containerNo: 'PACK-002',
    contractNo: 'CT-2024-002',
    name: '商业中心电梯装箱单',
    containerSize: '2500x1200x1200',
    containerWeight: '600kg',
    componentCount: 30,
    entryTs: '2024-01-15 11:00:00'
  }
])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 2
})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadContainers = async () => {
  loading.value = true
  try {
    // TODO: 调用API加载装箱单数据
    console.log('Loading containers...')
  } catch (error) {
    ElMessage.error('加载装箱单列表失败')
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
    contractNo: ''
  })
  handleSearch()
}

const handleCreate = () => {
  ElMessage.info('新建装箱单功能开发中...')
}

const handleView = (row) => {
  ElMessage.info(`查看装箱单: ${row.name}`)
}

const handleEdit = (row) => {
  ElMessage.info(`编辑装箱单: ${row.name}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除装箱单 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用API删除装箱单
    ElMessage.success('删除成功')
    loadContainers()
  } catch {
    // 用户取消删除
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
