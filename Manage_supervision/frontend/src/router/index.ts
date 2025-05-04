import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'
import BaseLayout from '../layouts/BaseLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: BaseLayout,
    redirect: to => {
      const userStore = useUserStore()
      if (userStore.isAdmin) {
        return '/admin/dashboard'
      } else if (userStore.isSupervisor) {
        return '/companion/profile'
      } else {
        return '/chat'
      }
    },
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { 
          title: '个人信息',
          requiresAuth: true 
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: {
          title: '账号设置',
          requiresAuth: true
        }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      },
      {
        path: 'companion-market',
        name: 'CompanionMarket',
        component: () => import('../views/player/CompanionMarket.vue'),
        meta: {
          title: '陪玩大厅',
          requiresAuth: true
        }
      },
      {
        path: 'orders',
        name: 'PlayerOrders',
        component: () => import('../views/player/PlayerOrders.vue'),
        meta: {
          title: '我的订单',
          requiresAuth: true
        }
      },
      {
        path: 'reviews',
        name: 'PlayerReviews',
        component: () => import('../views/player/PlayerReviews.vue'),
        meta: {
          title: '我的评价',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: BaseLayout,
    redirect: '/admin/dashboard',
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { 
          title: '管理控制台',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { 
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'companions',
        name: 'CompanionManagement',
        component: () => import('../views/admin/CompanionManagement.vue'),
        meta: { 
          title: '陪玩管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'companion-services',
        name: 'AdminCompanionServiceManagement',
        component: () => import('../views/admin/CompanionServiceManagement.vue'),
        meta: { 
          title: '陪玩服务管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'players',
        name: 'AdminPlayerManagement',
        component: () => import('../views/admin/PlayerManagement.vue'),
        meta: { 
          title: '玩家管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('../views/admin/RoleManagement.vue'),
        meta: { 
          title: '角色管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'orders',
        name: 'AdminOrderManagement',
        component: () => import('../views/admin/OrderManagement.vue'),
        meta: { 
          title: '订单管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('../views/admin/SystemLogs.vue'),
        meta: { 
          title: '系统日志',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'reviews',
        name: 'AdminReviewManagement',
        component: () => import('../views/admin/ReviewManagement.vue'),
        meta: { 
          title: '评论审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/companion',
    component: BaseLayout,
    redirect: '/companion/profile',
    meta: { 
      requiresAuth: true,
      requiresSupervisor: true
    },
    children: [
      {
        path: 'profile',
        name: 'CompanionProfile',
        component: () => import('../views/companion/Profile.vue'),
        meta: { 
          title: '陪玩信息',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'chat',
        name: 'CompanionChat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'services',
        name: 'CompanionServiceManagement',
        component: () => import('../views/companion/ServiceManagement.vue'),
        meta: {
          title: '陪玩服务',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'orders',
        name: 'CompanionOrderManagement',
        component: () => import('../views/companion/OrderManagement.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'reviews',
        name: 'CompanionReviewManagement',
        component: () => import('../views/companion/ReviewManagement.vue'),
        meta: {
          title: '评价管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  console.log('路由导航开始，目标路径:', to.path)
  const userStore = useUserStore()
  
  // 初始化用户认证状态
  if (!userStore.initialized) {
    console.log('用户认证状态尚未初始化，执行初始化...')
    await userStore.initializeAuth()
  }
  
  // 如果需要身份验证
  if (to.meta.requiresAuth) {
    console.log('目标路径需要登录权限')
    
    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      console.log('用户未登录，重定向到登录页面')
      next('/login')
      return
    }
    
    // 检查特定角色要求之前，强制刷新用户信息以确保权限是最新的
    if (to.meta.requiresAdmin || to.meta.requiresSupervisor) {
      console.log('页面需要特定角色权限，刷新用户信息...')
      try {
        // 尝试刷新用户信息，但不强制刷新以避免可能的循环
        // 只有当实际角色与预期角色不匹配时才强制刷新
        let needsForceRefresh = false
        
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
          console.log('需要管理员权限但当前不是管理员，尝试强制刷新')
          needsForceRefresh = true
        } else if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
          console.log('需要陪玩权限但当前不是陪玩，尝试强制刷新')
          needsForceRefresh = true
        }
        
        if (needsForceRefresh) {
          await userStore.fetchUserInfo(true)
        }
      } catch (error) {
        console.error('刷新用户信息失败:', error)
      }
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      console.log('需要管理员权限，但用户不是管理员，重定向到首页')
      next('/chat')
      return
    }
    
    // 检查陪玩权限
    if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
      console.log('需要陪玩权限，但用户不是陪玩，重定向到首页')
      next('/chat')
      return
    }
  }
  
  // 如果用户已登录且访问登录页，根据角色重定向到对应页面
  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    if (userStore.isAdmin) {
      next('/admin/dashboard')
    } else if (userStore.isSupervisor) {
      next('/companion/profile')
    } else {
      next('/chat')
    }
    return
  }
  
  // 更新页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} | 陪玩系统`
  } else {
    document.title = '陪玩系统'
  }
  
  next()
})

export default router