<template>
  <el-dialog
    v-model="visible"
    title="编辑用户"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          maxlength="50"
          show-word-limit
          :disabled="true"
        />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="留空则不修改密码"
          show-password
          maxlength="100"
        />
        <div class="form-item-tip">留空则不修改密码</div>
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="留空则不修改密码"
          show-password
          maxlength="100"
        />
      </el-form-item>
      
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入姓名"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          placeholder="请输入邮箱地址"
          maxlength="100"
          type="email"
        />
      </el-form-item>
      
      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="form.roleIds"
          multiple
          placeholder="请选择一个或多个角色"
          style="width: 100%"
          filterable
        >
          <el-option
            v-for="role in roles"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          >
            <span style="float: left">{{ role.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ role.code }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="状态" prop="enabled">
        <el-radio-group v-model="form.enabled">
          <el-radio :label="true">启用</el-radio>
          <el-radio :label="false">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userData: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref(null)
const submitting = ref(false)
const roles = ref([])

const form = reactive({
  id: null,
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  email: '',
  roleIds: [],
  enabled: true
})

const validateConfirmPassword = (rule, value, callback) => {
  // 如果密码为空，则不需要确认密码
  if (!form.password) {
    callback()
    return
  }
  
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 100, message: '姓名长度不能超过 100 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleIds: [
    { required: true, message: '请至少选择一个角色', trigger: 'change' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadRoles = async () => {
  try {
    const response = await api.get('/roles', {
      params: {
        page: 0,
        size: 1000 // 获取所有角色
      }
    })
    
    if (response.success) {
      // 后端返回的data直接是数组
      roles.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error) {
    console.error('Load roles error:', error)
    ElMessage.error('获取角色列表失败')
  }
}

const loadUserData = async () => {
  if (!props.userData || !props.userData.id) return
  
  try {
    // 总是从后端重新加载用户数据，确保获取最新的角色信息
    const response = await api.get(`/users/${props.userData.id}`)
    
    if (response.success) {
      const user = response.data
      form.id = user.id
      form.username = user.username || ''
      form.name = user.name || ''
      form.email = user.email || ''
      form.enabled = user.enabled !== undefined ? user.enabled : true
      form.password = ''
      form.confirmPassword = ''
      
      // 设置角色ID
      if (user.roles && Array.isArray(user.roles)) {
        form.roleIds = user.roles.map(role => role.id)
      } else {
        form.roleIds = []
      }
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('Load user data error:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const requestData = {
      username: form.username,
      name: form.name,
      email: form.email || null,
      enabled: form.enabled,
      roleIds: form.roleIds
    }
    
    // 只有在密码不为空时才包含密码字段
    if (form.password) {
      requestData.password = form.password
    }
    
    const response = await api.put(`/users/${form.id}`, requestData)
    
    if (response.success) {
      ElMessage.success('用户更新成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.message || '更新用户失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('更新用户失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  // 重置表单
  formRef.value?.resetFields()
  form.id = null
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.name = ''
  form.email = ''
  form.roleIds = []
  form.enabled = true
  
  visible.value = false
}

// 监听对话框打开，加载角色列表和用户数据
watch(visible, (newVal) => {
  if (newVal) {
    loadRoles()
    if (props.userData) {
      loadUserData()
    }
  }
})

// 监听 userData 变化
watch(() => props.userData, (newVal) => {
  if (newVal && visible.value) {
    loadUserData()
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

