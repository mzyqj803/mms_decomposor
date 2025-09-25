<template>
  <div class="fastener-warehouse">
    <div class="page-header">
      <h1 class="title">紧固件库管理</h1>
      <p class="description">管理紧固件基础信息、规格参数和库存数据</p>
    </div>
    
    <div class="table-container">
      <div class="table-header">
        <div class="header-left">
          <h3 class="header-title">紧固件列表</h3>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增紧固件
          </el-button>
        </div>
      </div>
      
      <div class="table-content">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :model="searchForm" inline>
            <el-form-item label="产品代码">
              <el-input
                v-model="searchForm.productCode"
                placeholder="请输入产品代码"
                clearable
                style="width: 180px;"
              />
            </el-form-item>
            <el-form-item label="ERP代码">
              <el-input
                v-model="searchForm.erpCode"
                placeholder="请输入ERP代码"
                clearable
                style="width: 180px;"
              />
            </el-form-item>
            <el-form-item label="名称">
              <el-input
                v-model="searchForm.name"
                placeholder="请输入名称"
                clearable
                style="width: 200px;"
              />
            </el-form-item>
            <el-form-item label="规格">
              <el-input
                v-model="searchForm.specs"
                placeholder="请输入规格"
                clearable
                style="width: 150px;"
              />
            </el-form-item>
            <el-form-item label="材料">
              <el-select
                v-model="searchForm.material"
                placeholder="请选择材料"
                clearable
                style="width: 150px;"
              >
                <el-option
                  v-for="material in materials"
                  :key="material"
                  :label="material"
                  :value="material"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="表面处理">
              <el-select
                v-model="searchForm.surfaceTreatment"
                placeholder="请选择表面处理"
                clearable
                style="width: 150px;"
              >
                <el-option
                  v-for="treatment in surfaceTreatments"
                  :key="treatment"
                  :label="treatment"
                  :value="treatment"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="等级">
              <el-select
                v-model="searchForm.level"
                placeholder="请选择等级"
                clearable
                style="width: 120px;"
              >
                <el-option
                  v-for="level in levels"
                  :key="level"
                  :label="level"
                  :value="level"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="默认">
              <el-select
                v-model="searchForm.defaultFlag"
                placeholder="请选择"
                clearable
                style="width: 100px;"
              >
                <el-option label="是" :value="true" />
                <el-option label="否" :value="false" />
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
          :data="fasteners"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="productCode" label="产品代码" width="150" />
          <el-table-column prop="erpCode" label="ERP代码" width="150" />
          <el-table-column prop="name" label="名称" min-width="200" />
          <el-table-column prop="specs" label="规格" width="120" />
          <el-table-column prop="level" label="等级" width="100" />
          <el-table-column prop="material" label="材料" width="120" />
          <el-table-column prop="surfaceTreatment" label="表面处理" width="120" />
          <el-table-column prop="defaultFlag" label="默认" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.defaultFlag ? 'success' : 'info'">
                {{ row.defaultFlag ? '是' : '否' }}
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
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button 
                v-if="!row.defaultFlag" 
                type="warning" 
                size="small" 
                @click="handleSetDefault(row)"
              >
                设默认
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品代码" prop="productCode">
              <el-input v-model="form.productCode" placeholder="请输入产品代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ERP代码" prop="erpCode">
              <el-input v-model="form.erpCode" placeholder="请输入ERP代码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="specs">
              <el-input v-model="form.specs" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-input v-model="form.level" placeholder="请输入等级" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="材料" prop="material">
              <el-input v-model="form.material" placeholder="请输入材料" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="表面处理" prop="surfaceTreatment">
              <el-input v-model="form.surfaceTreatment" placeholder="请输入表面处理" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认标志">
              <el-switch v-model="form.defaultFlag" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="紧固件详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品代码">{{ viewData.productCode }}</el-descriptions-item>
        <el-descriptions-item label="ERP代码">{{ viewData.erpCode }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ viewData.specs }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ viewData.level }}</el-descriptions-item>
        <el-descriptions-item label="材料">{{ viewData.material }}</el-descriptions-item>
        <el-descriptions-item label="表面处理">{{ viewData.surfaceTreatment }}</el-descriptions-item>
        <el-descriptions-item label="默认标志">
          <el-tag :type="viewData.defaultFlag ? 'success' : 'info'">
            {{ viewData.defaultFlag ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(viewData.entryTs) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(viewData.lastUpdateTs) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { fastenerWarehouseApi } from '@/api/fastenerWarehouse'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({
  productCode: '',
  erpCode: '',
  name: '',
  specs: '',
  material: '',
  surfaceTreatment: '',
  level: '',
  defaultFlag: null
})

const form = reactive({
  productCode: '',
  erpCode: '',
  name: '',
  specs: '',
  level: '',
  material: '',
  surfaceTreatment: '',
  remark: '',
  defaultFlag: false
})

const viewData = ref({})

const fasteners = ref([])
const materials = ref([])
const surfaceTreatments = ref([])
const levels = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const rules = {
  productCode: [
    { required: true, message: '请输入产品代码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ]
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadFasteners = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1, // 后端页码从0开始
      size: pagination.pageSize,
      productCode: searchForm.productCode || undefined,
      erpCode: searchForm.erpCode || undefined,
      name: searchForm.name || undefined,
      specs: searchForm.specs || undefined,
      material: searchForm.material || undefined,
      surfaceTreatment: searchForm.surfaceTreatment || undefined,
      level: searchForm.level || undefined,
      defaultFlag: searchForm.defaultFlag
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const response = await fastenerWarehouseApi.getFasteners(params)
    
    if (response && response.content) {
      fasteners.value = response.content
      pagination.total = response.totalElements || 0
    } else {
      fasteners.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载紧固件列表失败:', error)
    ElMessage.error('加载紧固件列表失败')
    fasteners.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [materialsRes, treatmentsRes, levelsRes] = await Promise.all([
      fastenerWarehouseApi.getMaterials(),
      fastenerWarehouseApi.getSurfaceTreatments(),
      fastenerWarehouseApi.getLevels()
    ])
    
    materials.value = materialsRes || []
    surfaceTreatments.value = treatmentsRes || []
    levels.value = levelsRes || []
  } catch (error) {
    console.error('加载选项数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadFasteners()
}

const handleReset = () => {
  Object.assign(searchForm, {
    productCode: '',
    erpCode: '',
    name: '',
    specs: '',
    material: '',
    surfaceTreatment: '',
    level: '',
    defaultFlag: null
  })
  handleSearch()
}

const handleCreate = () => {
  dialogTitle.value = '新增紧固件'
  Object.assign(form, {
    productCode: '',
    erpCode: '',
    name: '',
    specs: '',
    level: '',
    material: '',
    surfaceTreatment: '',
    remark: '',
    defaultFlag: false
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑紧固件'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (dialogTitle.value === '新增紧固件') {
      await fastenerWarehouseApi.createFastener(form)
      ElMessage.success('新增成功')
    } else {
      await fastenerWarehouseApi.updateFastener(form.id, form)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadFasteners()
  } catch (error) {
    if (error !== false) { // 不是表单验证失败
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleSetDefault = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 "${row.name}" 设置为默认紧固件吗？`,
      '确认设置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await fastenerWarehouseApi.setDefaultFastener(row.id)
    ElMessage.success('设置成功')
    loadFasteners()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置默认紧固件失败:', error)
      ElMessage.error('设置失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除紧固件 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await fastenerWarehouseApi.deleteFastener(row.id)
    ElMessage.success('删除成功')
    loadFasteners()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除紧固件失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadFasteners()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadFasteners()
}

onMounted(() => {
  loadFasteners()
  loadOptions()
})
</script>

<style lang="scss" scoped>
.fastener-warehouse {
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

.dialog-footer {
  text-align: right;
}
</style>
