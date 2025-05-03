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
    redirect: '/travel-recommendation',
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
        path: 'travel-recommendation',
        name: 'TravelRecommendation',
        component: () => import('../views/TravelRecommendation.vue'),
        meta: {
          title: '旅游推荐',
          requiresAuth: false
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
      }
    ]
  },
  {
    path: '/admin',
    component: BaseLayout,
    redirect: '/admin/regions',
    meta: { 
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'regions',
        name: 'RegionManagement',
        component: () => import('../views/admin/RegionManagement.vue'),
        meta: { 
          title: '地区管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'scenic-spots',
        name: 'ScenicSpotList',
        component: () => import('../views/ScenicSpot/ScenicSpotList.vue'),
        meta: { 
          title: '景区管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'scenic-spots/:id',
        name: 'ScenicSpotDetail',
        component: () => import('../views/ScenicSpot/ScenicSpotDetail.vue'),
        meta: { 
          title: '景区详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'hotels',
        name: 'HotelList',
        component: () => import('../views/Hotel/HotelList.vue'),
        meta: { 
          title: '酒店管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'hotels/:id',
        name: 'HotelDetail',
        component: () => import('../views/Hotel/HotelDetail.vue'),
        meta: { 
          title: '酒店详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/supervisor',
    component: BaseLayout,
    redirect: '/supervisor/profile',
    meta: { 
      requiresAuth: true,
      requiresSupervisor: true
    },
    children: [
      {
        path: 'profile',
        name: 'SupervisorProfile',
        component: () => import('../views/supervisor/Profile.vue'),
        meta: { 
          title: '安全分析师信息',
          requiresAuth: true,
          requiresSupervisor: true
        }
      }
    ]
  },
  {
    path: '/intrusion-detection',
    component: BaseLayout,
    meta: { 
      requiresAuth: true 
    },
    
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
  
  // 处理管理员访问首页的情况，直接重定向到管理页面
  if (to.path === '/' && userStore.isAdmin) {
    console.log('管理员访问首页，重定向到管理页面')
    next('/admin/regions')
    return
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
          console.log('需要教师权限但当前不是教师，尝试强制刷新')
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
      next('/profile')
      return
    }
    
    // 检查教师权限
    if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
      console.log('需要教师权限，但用户不是教师，重定向到首页')
      next('/profile')
      return
    }
  }
  
  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 旅游系统`
  
  // 放行路由
  console.log('路由检查通过，允许导航到:', to.path)
  next()
})

export default router