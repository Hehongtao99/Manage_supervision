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
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Home.vue'),
        meta: { 
          title: '仪表盘',
          requiresAuth: true 
        }
      },
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
        path: 'task-list',
        name: 'TaskList',
        component: () => import('../views/student/TaskList.vue'),
        meta: {
          title: '我的任务',
          requiresAuth: true
        }
      },
      {
        path: 'task-view',
        name: 'TaskView',
        component: () => import('../views/student/TaskView.vue'),
        meta: {
          title: '我的任务',
          requiresAuth: true
        }
      },
      {
        path: 'project-view',
        name: 'ProjectView',
        component: () => import('../views/student/ProjectView.vue'),
        meta: {
          title: '我的课题',
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
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('../views/admin/SystemLogs.vue'),
        meta: { 
          title: '系统日志',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  {
    path: '/supervisor',
    component: BaseLayout,
    redirect: '/supervisor/dashboard',
    meta: { 
      requiresAuth: true,
      requiresSupervisor: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'SupervisorDashboard',
        component: () => import('../views/supervisor/Dashboard.vue'),
        meta: { 
          title: '督导控制台',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'students',
        name: 'StudentManagement',
        component: () => import('../views/supervisor/StudentManagement.vue'),
        meta: { 
          title: '学生管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'tasks',
        name: 'SupervisorTasks',
        component: () => import('../views/supervisor/ProjectManagement.vue'),
        meta: {
          title: '课题管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'assignments',
        name: 'SupervisorAssignments',
        component: () => import('../views/supervisor/TaskManagement.vue'),
        meta: {
          title: '任务管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'profile',
        name: 'SupervisorProfile',
        component: () => import('../views/supervisor/Profile.vue'),
        meta: { 
          title: '督导信息',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'chat',
        name: 'SupervisorChat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
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
          console.log('需要督导员权限但当前不是督导员，尝试强制刷新')
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
      next('/')
      return
    }
    
    // 检查督导员权限
    if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
      console.log('需要督导员权限，但用户不是督导员，重定向到首页')
      next('/')
      return
    }
  }
  
  // 特殊处理：督导员访问/dashboard时重定向到/supervisor/dashboard
  if (to.path === '/dashboard' && userStore.isSupervisor) {
    console.log('督导员访问/dashboard，重定向到督导控制台')
    next('/supervisor/dashboard')
    return
  }
  
  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 毕业设计督导系统`
  
  // 放行路由
  console.log('路由检查通过，允许导航到:', to.path)
  next()
})

export default router