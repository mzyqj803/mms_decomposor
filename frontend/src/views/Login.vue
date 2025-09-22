<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="/logo.svg" alt="MMS" class="logo" />
        <h1>MMS制造管理系统</h1>
        <p>Manufacturing Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2024 MMS制造管理系统. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm.username, loginForm.password)
        if (success) {
          ElMessage.success('登录成功')
          router.push('/')
        } else {
          ElMessage.error('用户名或密码错误')
        }
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  
  .login-header {
    text-align: center;
    margin-bottom: 40px;
    
    .logo {
      height: 60px;
      margin-bottom: 20px;
    }
    
    h1 {
      font-size: 28px;
      color: #303133;
      margin-bottom: 8px;
      font-weight: 600;
    }
    
    p {
      color: #909399;
      font-size: 14px;
    }
  }
  
  .login-form {
    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .login-footer {
    text-align: center;
    margin-top: 30px;
    
    p {
      color: #c0c4cc;
      font-size: 12px;
    }
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
    
    .login-header {
      h1 {
        font-size: 24px;
      }
    }
  }
}
</style>
