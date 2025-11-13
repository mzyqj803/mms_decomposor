<template>
  <div class="users">
    <div class="page-header">
      <h1 class="title">用户管理</h1>
      <p class="description">管理系统用户账号和权限</p>
    </div>
    
    <div class="table-container">
      <div class="table-header">
        <div class="header-left">
          <h3 class="header-title">用户列表</h3>
          <el-button 
            v-if="userStore.hasPermission('USER:CREATE')"
            type="primary" 
            @click="handleCreate" 
            class="create-button"
          >
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </div>
      
      <div class="table-content">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="用户名">
              <el-input
                v-model="searchForm.keyword"
                placeholder="请输入用户名或姓名"
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
          :data="users"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="name" label="姓名" width="150" />
          <el-table-column prop="email" label="邮箱" min-width="200" />
          <el-table-column prop="enabled" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'danger'">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="entryTs" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.entryTs) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="userStore.hasPermission('USER:VIEW')"
                type="primary" 
                size="small" 
                @click="handleView(row)"
              >
                查看
              </el-button>
              <el-button 
                v-if="userStore.hasPermission('USER:UPDATE')"
                type="success" 
                size="small" 
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="userStore.hasPermission('USER:DELETE')"
                type="danger" 
                size="small" 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 创建用户对话框 -->
    <CreateUserDialog
      v-model="createDialogVisible"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import CreateUserDialog from '@/components/CreateUserDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const users = ref([])
const createDialogVisible = ref(false)
const searchForm = ref({
  keyword: ''
})
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.get('/users', {
      params: {
        page: pagination.value.page - 1,
        size: pagination.value.size,
        keyword: searchForm.value.keyword || undefined
      }
    })
    
    if (response.success) {
      // 后端返回的data直接是数组
      users.value = Array.isArray(response.data) ? response.data : []
      pagination.value.total = response.total || 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('Load users error:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.value.keyword = ''
  pagination.value.page = 1
  loadUsers()
}

const handleCreate = () => {
  createDialogVisible.value = true
}

const handleCreateSuccess = () => {
  // 创建成功后刷新列表
  loadUsers()
}

const handleView = (row) => {
  ElMessage.info(`查看用户: ${row.username}`)
}

const handleEdit = (row) => {
  ElMessage.info(`编辑用户: ${row.username}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    try {
      const response = await api.delete(`/users/${row.id}`)
      if (response.success) {
        ElMessage.success('删除成功')
        loadUsers()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('Delete user error:', error)
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消删除
  }
}

const handleSizeChange = () => {
  loadUsers()
}

const handlePageChange = () => {
  loadUsers()
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.users {
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
  padding: 20px;
  
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .header-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
    }
  }
  
  .search-bar {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

