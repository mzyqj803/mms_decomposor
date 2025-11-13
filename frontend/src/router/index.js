import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/contracts'
      },
      {
        path: '/contracts',
        name: 'Contracts',
        component: () => import('@/views/Contracts.vue'),
        meta: { title: '合同管理', requiresPermission: 'CONTRACT:VIEW' }
      },
      {
        path: '/contracts/:id',
        name: 'ContractDetail',
        component: () => import('@/views/ContractDetail.vue'),
        meta: { title: '合同详情', requiresPermission: 'CONTRACT:VIEW' }
      },
      {
        path: '/components',
        name: 'Components',
        component: () => import('@/views/Components.vue'),
        meta: { title: '零部件管理', requiresPermission: 'COMPONENT:VIEW' }
      },
      {
        path: '/fastener-warehouse',
        name: 'FastenerWarehouse',
        component: () => import('@/views/FastenerWarehouse.vue'),
        meta: { title: '紧固件库管理', requiresPermission: 'FASTENER:VIEW' }
      },
      {
        path: '/containers',
        name: 'Containers',
        component: () => import('@/views/Containers.vue'),
        meta: { title: '装箱单管理', requiresPermission: 'CONTAINER:VIEW' }
      },
      {
        path: '/breakdown',
        name: 'Breakdown',
        component: () => import('@/views/Breakdown.vue'),
        meta: { title: '工艺分解', requiresPermission: 'BREAKDOWN:VIEW' }
      },
      {
        path: '/production-plan',
        name: 'ProductionPlan',
        component: () => import('@/views/ProductionPlan.vue'),
        meta: { title: '生产计划', requiresPermission: 'PRODUCTION:VIEW' }
      },
      {
        path: '/cost-estimation',
        name: 'CostEstimation',
        component: () => import('@/views/CostEstimation.vue'),
        meta: { title: '成本估算', requiresPermission: 'COST:VIEW' }
      },
      {
        path: '/bidding',
        name: 'Bidding',
        component: () => import('@/views/Bidding.vue'),
        meta: { title: '投标报价', requiresPermission: 'BIDDING:VIEW' }
      },
      {
        path: '/history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: { title: '修改历史', requiresPermission: 'HISTORY:VIEW' }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置' },
        redirect: '/settings/users',
        children: [
          {
            path: 'users',
            name: 'Users',
            component: () => import('@/views/Users.vue'),
            meta: { title: '用户管理', requiresPermission: 'USER:VIEW' }
          },
          {
            path: 'roles',
            name: 'Roles',
            component: () => import('@/views/Roles.vue'),
            meta: { title: '角色管理', requiresPermission: 'ROLE:VIEW' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 如果访问登录页
  if (to.path === '/login') {
    // 如果已登录，验证token有效性
    if (userStore.isLoggedIn) {
      const isValid = await userStore.checkAuth()
      if (isValid) {
        // Token有效，跳转到首页
        next('/')
        return
      }
    }
    // 未登录或token无效，允许访问登录页
    next()
    return
  }
  
  // 如果需要认证的路由
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存原始路径，登录后可以跳转回来
      })
      return
    }
    
    // 已登录，验证token有效性（只在首次或token变化时验证）
    if (!userStore.authChecked) {
      const isValid = await userStore.checkAuth()
      if (!isValid) {
        // Token无效，跳转到登录页
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }
    }
    
    // 检查路由权限要求
    if (to.meta.requiresPermission) {
      if (!userStore.hasPermission(to.meta.requiresPermission)) {
        // 没有权限，跳转到首页或显示无权限提示
        ElMessage.error('您没有权限访问该页面')
        next('/')
        return
      }
    }
  }
  
  next()
})

export default router
