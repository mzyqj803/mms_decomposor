import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: '/contracts',
        name: 'Contracts',
        component: () => import('@/views/Contracts.vue'),
        meta: { title: '合同管理' }
      },
      {
        path: '/contracts/:id',
        name: 'ContractDetail',
        component: () => import('@/views/ContractDetail.vue'),
        meta: { title: '合同详情' }
      },
      {
        path: '/components',
        name: 'Components',
        component: () => import('@/views/Components.vue'),
        meta: { title: '零部件管理' }
      },
      {
        path: '/fastener-warehouse',
        name: 'FastenerWarehouse',
        component: () => import('@/views/FastenerWarehouse.vue'),
        meta: { title: '紧固件库管理' }
      },
      {
        path: '/containers',
        name: 'Containers',
        component: () => import('@/views/Containers.vue'),
        meta: { title: '装箱单管理' }
      },
      {
        path: '/breakdown',
        name: 'Breakdown',
        component: () => import('@/views/Breakdown.vue'),
        meta: { title: '工艺分解' }
      },
      {
        path: '/production-plan',
        name: 'ProductionPlan',
        component: () => import('@/views/ProductionPlan.vue'),
        meta: { title: '生产计划' }
      },
      {
        path: '/cost-estimation',
        name: 'CostEstimation',
        component: () => import('@/views/CostEstimation.vue'),
        meta: { title: '成本估算' }
      },
      {
        path: '/bidding',
        name: 'Bidding',
        component: () => import('@/views/Bidding.vue'),
        meta: { title: '投标报价' }
      },
      {
        path: '/history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: { title: '修改历史' }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置' }
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
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
