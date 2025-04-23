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
    redirect: '/chat',
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
    path: '/student',
    component: BaseLayout,
    redirect: '/student/tasks',
    meta: { 
      requiresAuth: true,
      requiresStudent: true
    },
    children: [
      {
        path: 'tasks',
        name: 'StudentTaskList',
        component: () => import('../views/student/TaskList.vue'),
        meta: { 
          title: '任务列表',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'task/:id',
        name: 'StudentTaskView',
        component: () => import('../views/student/TaskView.vue'),
        meta: { 
          title: '任务详情',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'teachers-classes',
        name: 'MyTeachersAndClasses',
        component: () => import('../views/student/MyTeachersAndClasses.vue'),
        meta: { 
          title: '我的班级和老师',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'timetable',
        name: 'StudentTimetable',
        component: () => import('../views/student/Timetable.vue'),
        meta: { 
          title: '班级课表',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'notifications',
        name: 'StudentNotifications',
        component: () => import('../views/student/Notifications.vue'),
        meta: { 
          title: '我的通知',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'bills',
        name: 'StudentBills',
        component: () => import('../views/student/StudentBills.vue'),
        meta: { 
          title: '我的账单',
          requiresAuth: true,
          requiresStudent: true
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
        path: 'classes',
        name: 'ClassManagement',
        component: () => import('../views/admin/ClassManagement.vue'),
        meta: { 
          title: '班级管理',
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
        path: 'notifications',
        name: 'AdminNotificationManagement',
        component: () => import('../views/admin/NotificationManagement.vue'),
        meta: { 
          title: '通知管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'timetables',
        name: 'TimetableManagement',
        component: () => import('../views/admin/TimetableManagement.vue'),
        meta: { 
          title: '课程表管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'timetables/:id',
        name: 'TimetableDetail',
        component: () => import('../views/admin/TimetableDetail.vue'),
        meta: { 
          title: '课程表详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'fee-standards',
        name: 'FeeStandardManagement',
        component: () => import('../views/admin/FeeStandardList.vue'),
        meta: { 
          title: '费用标准管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'bills',
        name: 'BillManagement',
        component: () => import('../views/admin/BillManagement.vue'),
        meta: { 
          title: '账单管理',
          requiresAuth: true,
          requiresAdmin: true
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
        name: 'SupervisorStudentManagement',
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
          title: '个人信息',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'classes',
        name: 'SupervisorClassManagement',
        component: () => import('../views/supervisor/ClassManagement.vue'),
        meta: { 
          title: '班级管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'timetable',
        name: 'SupervisorTimetable',
        component: () => import('../views/supervisor/Timetable.vue'),
        meta: { 
          title: '班级课表',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'timetable/:id',
        name: 'SupervisorTimetableDetail',
        component: () => import('../views/supervisor/TimetableDetail.vue'),
        meta: { 
          title: '课程表详情',
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
        path: 'projects',
        name: 'ProjectManagement',
        component: () => import('../views/supervisor/ProjectManagement.vue'),
        meta: { 
          title: '项目管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'notifications',
        name: 'SupervisorNotificationManagement',
        component: () => import('../views/supervisor/NotificationManagement.vue'),
        meta: { 
          title: '通知管理',
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'bills',
        name: 'SupervisorBillManagement',
        component: () => import('../views/supervisor/BillManagement.vue'),
        meta: { 
          title: '账单管理',
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
      next('/chat')
      return
    }
    
    // 检查教师权限
    if (to.meta.requiresSupervisor && !userStore.isSupervisor) {
      console.log('需要教师权限，但用户不是教师，角色:', userStore.user.roles)
      console.log('isSupervisor返回值:', userStore.isSupervisor)
      
      // 尝试从localStorage获取角色信息进行二次验证
      try {
        const rolesStr = localStorage.getItem('userRoles')
        if (rolesStr) {
          const roles = JSON.parse(rolesStr)
          const hasSupervisorRole = roles.some((role: string) => 
            role === 'SUPERVISOR' || role === 'supervisor' || 
            role === 'TEACHER' || role === 'teacher'
          )
          console.log('从localStorage检查教师角色:', roles, '结果:', hasSupervisorRole)
          
          if (hasSupervisorRole) {
            console.log('localStorage中存在教师角色，允许访问')
            next()
            return
          }
        }
      } catch (e) {
        console.error('解析localStorage中的角色信息失败:', e)
      }
      
      console.log('权限验证失败，重定向到聊天页面')
      next('/chat')
      return
    }
    
    // 检查学生权限
    if (to.meta.requiresStudent && !userStore.isStudent) {
      console.log('需要学生权限，但用户不是学生，重定向到首页')
      next('/chat')
      return
    }
  }
  
  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 毕业设计督导系统`
  
  // 放行路由
  console.log('路由检查通过，允许导航到:', to.path)
  next()
})

export default router