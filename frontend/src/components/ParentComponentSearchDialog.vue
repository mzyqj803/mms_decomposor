<template>
  <el-dialog
    v-model="visible"
    title="选择父工件"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 搜索表单 -->
    <el-form :model="searchForm" :inline="true" style="margin-bottom: 20px;">
      <el-form-item label="工件ID">
        <el-input
          v-model="searchForm.componentCode"
          placeholder="请输入工件ID"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item label="工件名称">
        <el-input
          v-model="searchForm.name"
          placeholder="请输入工件名称"
          clearable
          style="width: 200px;"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch" :loading="loading">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 搜索结果表格 -->
    <el-table
      :data="searchResults"
      v-loading="loading"
      @row-click="handleRowClick"
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column prop="componentCode" label="工件ID" width="150" />
      <el-table-column prop="name" label="工件名称" min-width="200" />
      <el-table-column prop="categoryCode" label="分类代码" width="120" />
      <el-table-column prop="procurementFlag" label="采购标识" width="100">
        <template #default="{ row }">
          <el-tag :type="row.procurementFlag ? 'success' : 'info'">
            {{ row.procurementFlag ? '采购' : '自制' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="commonPartsFlag" label="通用件类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getCommonPartsType(row.commonPartsFlag)">
            {{ getCommonPartsText(row.commonPartsFlag) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleSelect(row)">
            选择
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 20px; text-align: right;">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { componentsApi } from '@/api/components'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const searchResults = ref([])

const searchForm = reactive({
  componentCode: '',
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const handleSearch = async () => {
  try {
    loading.value = true
    pagination.page = 1
    
    const response = await componentsApi.getComponents(
      searchForm.componentCode,
      searchForm.name,
      null,
      {
        page: pagination.page - 1,
        size: pagination.size
      }
    )
    
    searchResults.value = response.content || []
    pagination.total = response.totalElements || 0
    
  } catch (error) {
    console.error('搜索父工件失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.componentCode = ''
  searchForm.name = ''
  searchResults.value = []
  pagination.page = 1
  pagination.total = 0
}

const handleRowClick = (row) => {
  // 点击行时高亮显示
}

const handleSelect = (row) => {
  emit('select', row)
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  handleSearch()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  handleSearch()
}

const handleClose = () => {
  handleReset()
  visible.value = false
}

const getCommonPartsType = (flag) => {
  switch (flag) {
    case 0: return 'info'
    case 1: return 'warning'
    case 2: return 'success'
    default: return 'info'
  }
}

const getCommonPartsText = (flag) => {
  switch (flag) {
    case 0: return '非紧固件'
    case 1: return '装箱紧固件'
    case 2: return '装配紧固件'
    default: return '未知'
  }
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal) {
    // 对话框打开时自动搜索
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: var(--el-color-primary-light-9);
}
</style>

