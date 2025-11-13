<template>
  <el-dialog
    v-model="visible"
    title="新增用户"
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
        />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          maxlength="100"
        />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
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
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  email: '',
  roleIds: [],
  enabled: true
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 100, message: '姓名长度不能超过 100 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  roleIds: [
    { required: true, message: '请至少选择一个角色', trigger: 'change' }
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
      // 后端返回的data直接是数组，不是{content: [...]}
      roles.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error) {
    console.error('Load roles error:', error)
    ElMessage.error('获取角色列表失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const requestData = {
      username: form.username,
      password: form.password,
      name: form.name,
      email: form.email || null,
      enabled: form.enabled,
      roleIds: form.roleIds
    }
    
    const response = await api.post('/users', requestData)
    
    if (response.success) {
      ElMessage.success('用户创建成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.message || '创建用户失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('创建用户失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  // 重置表单
  formRef.value?.resetFields()
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.name = ''
  form.email = ''
  form.roleIds = []
  form.enabled = true
  
  visible.value = false
}

// 监听对话框打开，加载角色列表
watch(visible, (newVal) => {
  if (newVal) {
    loadRoles()
  }
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

