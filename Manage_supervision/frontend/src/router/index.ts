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
        return '/supervisor/students'
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
        path: 'teachers',
        name: 'TeacherManagement',
        component: () => import('../views/admin/TeacherManagement.vue'),
        meta: { 
          title: '教师管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'students',
        name: 'AdminStudentManagement',
        component: () => import('../views/admin/StudentManagement.vue'),
        meta: { 
          title: '学生管理',
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
          requiresAdmin: true,
          permissions: ['role:view', 'ROLE_VIEW']
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
        path: 'courses',
        name: 'AdminCourseManagement',
        component: () => import('../views/course/CourseList.vue'),
        meta: { 
          title: '课程管理',
          requiresAuth: true,
          requiresAdmin: true,
          permissions: ['course:view', 'course:add', 'course:edit', 'course:delete']
        }
      },
      {
        path: 'course-categories',
        name: 'CourseCategoryManagement',
        component: () => import('../views/course/CategoryManagement.vue'),
        meta: { 
          title: '课程类别管理',
          requiresAuth: true,
          requiresAdmin: true,
          permissions: ['course:add', 'course:edit', 'course:delete']
        }
      }
    ]
  },
  {
    path: '/supervisor',
    component: BaseLayout,
    redirect: '/supervisor/students',
    meta: { 
      requiresAuth: true,
      requiresSupervisor: true
    },
    children: [
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
        path: 'profile',
        name: 'SupervisorProfile',
        component: () => import('../views/supervisor/Profile.vue'),
        meta: { 
          title: '教师信息',
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
      },
      {
        path: 'courses',
        name: 'SupervisorCourseManagement',
        component: () => import('../views/course/CourseList.vue'),
        meta: { 
          title: '我的课程',
          requiresAuth: true,
          requiresSupervisor: true,
          permissions: ['course:view']
        }
      }
    ]
  },
  {
    path: '/course',
    component: BaseLayout,
    redirect: '/course/list',
    meta: { 
      requiresAuth: true,
    },
    children: [
      {
        path: 'list',
        name: 'CourseList',
        component: () => import('../views/course/CourseList.vue'),
        meta: { 
          title: '课程列表',
          requiresAuth: true,
          permissions: ['course:view']
        }
      },
      {
        path: 'create',
        name: 'CourseCreate',
        component: () => import('../views/course/CourseEdit.vue'),
        meta: { 
          title: '创建课程',
          requiresAuth: true,
          permissions: ['course:add']
        }
      },
      {
        path: ':id',
        name: 'CourseDetail',
        component: () => import('../views/course/CourseDetail.vue'),
        meta: { 
          title: '课程详情',
          requiresAuth: true,
          permissions: ['course:view']
        }
      },
      {
        path: ':id/edit',
        name: 'CourseEdit',
        component: () => import('../views/course/CourseEdit.vue'),
        meta: { 
          title: '编辑课程',
          requiresAuth: true,
          permissions: ['course:edit']
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
    if (to.meta.requiresAdmin || to.meta.requiresSupervisor || to.meta.permissions) {
      console.log('页面需要特定权限，刷新用户信息...')
      try {
        // 尝试刷新用户信息和权限
        await userStore.fetchUserInfo(true)
        await userStore.fetchUserPermissions()
      } catch (error) {
        console.error('刷新用户权限失败:', error)
      }
    }
    
    // 检查管理员权限 - 修改为检查是否有对应的权限
    if (to.meta.requiresAdmin) {
      // 如果用户是管理员，直接允许访问
      if (userStore.isAdmin) {
        console.log('用户是管理员，允许访问')
      } 
      // 如果用户不是管理员，但路径有对应的权限要求
      else if (to.path.includes('/admin/users') && 
          (userStore.permissions.includes('user:view') || userStore.permissions.includes('USER_VIEW'))) {
        console.log('用户有用户管理权限，允许访问')
      }
      else if (to.path.includes('/admin/roles') && 
          (userStore.permissions.includes('role:view') || userStore.permissions.includes('ROLE_VIEW'))) {
        console.log('用户有角色管理权限，允许访问')
      }
      else if (to.path.includes('/admin/teachers') && 
          (userStore.permissions.includes('teacher:view') || userStore.permissions.includes('TEACHER_VIEW'))) {
        console.log('用户有教师管理权限，允许访问')
      }
      else if (to.path.includes('/admin/students') && 
          (userStore.permissions.includes('student:view') || userStore.permissions.includes('STUDENT_VIEW'))) {
        console.log('用户有学生管理权限，允许访问')
      }
      else if (to.path.includes('/admin/dashboard') && 
          (userStore.permissions.includes('dashboard') || userStore.permissions.includes('DASHBOARD'))) {
        console.log('用户有仪表盘权限，允许访问')
      }
      else {
        console.log('用户没有访问此管理页面的权限，重定向到首页')
        next('/chat')
        return
      }
    }
    
    // 检查教师权限
    if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
      // 如果用户有学生管理权限，允许访问
      if (to.path.includes('/supervisor/students') && 
          (userStore.permissions.includes('student:view') || userStore.permissions.includes('STUDENT_VIEW'))) {
        console.log('用户有学生管理权限，允许访问督导员页面')
      } else {
        console.log('需要教师权限，但用户不是教师，重定向到首页')
        next('/chat')
        return
      }
    }
    
    // 检查特定权限要求
    if (to.meta.permissions) {
      const requiredPermissions = to.meta.permissions as string[]
      const hasRequiredPermission = requiredPermissions.some(permission => 
        userStore.permissions.includes(permission)
      )
      
      // 管理员始终有权限
      if (!userStore.isAdmin && !hasRequiredPermission) {
        console.log('用户没有访问此页面的权限，重定向到首页')
        next('/chat')
        return
      }
    }
  }
  
  // 如果用户已登录且访问登录页，根据角色重定向到对应页面
  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    if (userStore.isAdmin) {
      next('/admin/dashboard')
    } else if (userStore.isSupervisor) {
      next('/supervisor/students')
    } else {
      next('/chat')
    }
    return
  }
  
  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 毕业设计督导系统`
  
  // 放行路由
  console.log('路由检查通过，允许导航到:', to.path)
  next()
})

export default router