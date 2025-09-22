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
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleCreate">
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
          <el-table-column prop="commonPartsFlag" label="通用件" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.commonPartsFlag ? 'warning' : 'info'">
                {{ row.commonPartsFlag ? '是' : '否' }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)

const searchForm = reactive({
  componentCode: '',
  name: '',
  categoryCode: ''
})

const components = ref([
  {
    id: 1,
    categoryCode: 'ELEVATOR',
    componentCode: 'ELE-001',
    name: '电梯主机',
    comment: '电梯核心动力设备',
    procurementFlag: false,
    commonPartsFlag: false,
    entryTs: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    categoryCode: 'DOOR',
    componentCode: 'DOOR-001',
    name: '电梯门机',
    comment: '电梯门开关控制设备',
    procurementFlag: false,
    commonPartsFlag: true,
    entryTs: '2024-01-15 10:00:00'
  },
  {
    id: 3,
    categoryCode: 'CABIN',
    componentCode: 'CABIN-001',
    name: '电梯轿厢',
    comment: '电梯载客厢体',
    procurementFlag: false,
    commonPartsFlag: false,
    entryTs: '2024-01-15 10:00:00'
  }
])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 3
})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadComponents = async () => {
  loading.value = true
  try {
    // TODO: 调用API加载零部件数据
    console.log('Loading components...')
  } catch (error) {
    ElMessage.error('加载零部件列表失败')
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
  ElMessage.info('新增零部件功能开发中...')
}

const handleView = (row) => {
  ElMessage.info(`查看零部件: ${row.name}`)
}

const handleEdit = (row) => {
  ElMessage.info(`编辑零部件: ${row.name}`)
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
    
    // TODO: 调用API删除零部件
    ElMessage.success('删除成功')
    loadComponents()
  } catch {
    // 用户取消删除
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
