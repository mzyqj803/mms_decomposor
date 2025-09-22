<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <img src="/logo.svg" alt="MMS" v-if="!isCollapse" />
        <span v-if="!isCollapse">MMS制造管理系统</span>
        <img src="/logo-mini.svg" alt="MMS" v-else />
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        
        <el-menu-item index="/contracts">
          <el-icon><Document /></el-icon>
          <template #title>合同管理</template>
        </el-menu-item>
        
        <el-menu-item index="/components">
          <el-icon><Box /></el-icon>
          <template #title>零部件管理</template>
        </el-menu-item>
        
        <el-menu-item index="/containers">
          <el-icon><Collection /></el-icon>
          <template #title>装箱单管理</template>
        </el-menu-item>
        
        <el-menu-item index="/breakdown">
          <el-icon><Operation /></el-icon>
          <template #title>工艺分解</template>
        </el-menu-item>
        
        <el-menu-item index="/production-plan">
          <el-icon><Calendar /></el-icon>
          <template #title>生产计划</template>
        </el-menu-item>
        
        <el-menu-item index="/cost-estimation">
          <el-icon><Money /></el-icon>
          <template #title>成本估算</template>
        </el-menu-item>
        
        <el-menu-item index="/bidding">
          <el-icon><TrendCharts /></el-icon>
          <template #title>投标报价</template>
        </el-menu-item>
        
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <template #title>修改历史</template>
        </el-menu-item>
        
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleCollapse"
            class="collapse-btn"
          >
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}

// 监听路由变化，自动收起侧边栏（移动端）
watch(route, () => {
  if (window.innerWidth <= 768) {
    isCollapse.value = true
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    font-weight: 600;
    border-bottom: 1px solid #434a50;
    
    img {
      height: 32px;
      margin-right: 8px;
    }
  }
  
  .sidebar-menu {
    border: none;
    background: #304156;
    
    :deep(.el-menu-item) {
      color: #bfcbd9;
      
      &:hover {
        background-color: #263445;
        color: #fff;
      }
      
      &.is-active {
        background-color: #409eff;
        color: #fff;
      }
    }
  }
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      margin-right: 20px;
      font-size: 18px;
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin: 0 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.main-content {
  background: #f5f5f5;
  padding: 0;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }
  
  .header {
    padding: 0 10px;
    
    .header-left {
      .collapse-btn {
        margin-right: 10px;
      }
    }
  }
}
</style>
