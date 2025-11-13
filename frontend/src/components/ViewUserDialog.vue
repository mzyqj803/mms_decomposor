<template>
  <el-dialog
    v-model="visible"
    title="查看用户"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="view-user-dialog">
      <el-descriptions :column="2" border v-if="userData.id">
        <el-descriptions-item label="用户名">
          {{ userData.username }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ userData.name }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ userData.email || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userData.enabled ? 'success' : 'danger'">
            {{ userData.enabled ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">
          <el-tag
            v-for="role in userData.roles"
            :key="role.id"
            style="margin-right: 8px;"
          >
            {{ role.name }}
          </el-tag>
          <span v-if="!userData.roles || userData.roles.length === 0" style="color: #909399;">
            未分配角色
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(userData.entryTs) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(userData.lastUpdateTs) }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button 
          v-if="userStore.hasPermission('USER:UPDATE')"
          type="primary" 
          @click="handleEdit"
        >
          编辑
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'edit'])

const userStore = useUserStore()
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const userData = ref({})

const loadUser = async () => {
  if (!props.userId) return
  
  loading.value = true
  try {
    const response = await api.get(`/users/${props.userId}`)
    
    if (response.success) {
      userData.value = response.data || {}
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('Load user error:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  emit('edit', userData.value)
  handleClose()
}

const handleClose = () => {
  userData.value = {}
  visible.value = false
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 监听对话框打开，加载用户数据
watch(visible, (newVal) => {
  if (newVal && props.userId) {
    loadUser()
  }
})
</script>

<style lang="scss" scoped>
.view-user-dialog {
  min-height: 200px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

