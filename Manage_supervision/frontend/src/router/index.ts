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
        path: 'question-banks',
        name: 'QuestionBankManagement',
        component: () => import('../views/supervisor/QuestionBankManagement.vue'),
        meta: { 
          title: '题库列表', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'questions',
        name: 'QuestionManagement',
        component: () => import('../views/supervisor/QuestionManagement.vue'),
        meta: { 
          title: '题目管理', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'questions/create',
        name: 'CreateQuestion',
        component: () => import('../views/supervisor/CreateQuestion.vue'),
        meta: { 
          title: '创建题目', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'questions/:id/edit',
        name: 'EditQuestion',
        component: () => import('../views/supervisor/CreateQuestion.vue'),
        meta: { 
          title: '编辑题目', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'exams',
        name: 'ExamManagement',
        component: () => import('../views/supervisor/ExamManagement.vue'),
        meta: { 
          title: '考试管理', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'exams/create',
        name: 'CreateExam',
        component: () => import('../views/supervisor/CreateExam.vue'),
        meta: { 
          title: '创建考试', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'exams/:id',
        name: 'ExamDetail',
        component: () => import('../views/supervisor/CreateExam.vue'),
        meta: { 
          title: '考试详情', 
          requiresAuth: true,
          requiresSupervisor: true
        }
      },
      {
        path: 'exams/:id/edit',
        name: 'EditExam',
        component: () => import('../views/supervisor/CreateExam.vue'),
        props: route => ({ edit: true }),
        meta: { 
          title: '编辑考试', 
          requiresAuth: true,
          requiresSupervisor: true
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
        path: 'tasks/:taskId',
        name: 'StudentTaskView',
        component: () => import('../views/student/TaskView.vue'),
        meta: { 
          title: '任务详情',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'exams',
        name: 'StudentExamList',
        component: () => import('../views/student/ExamList.vue'),
        meta: { 
          title: '考试列表',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'exams/:id',
        name: 'StudentExamDetail',
        component: () => import('../views/student/ExamDetail.vue'),
        meta: { 
          title: '考试详情',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'exams/:id/take',
        name: 'StudentExamTake',
        component: () => import('../views/student/ExamTake.vue'),
        meta: { 
          title: '参加考试',
          requiresAuth: true,
          requiresStudent: true
        }
      },
      {
        path: 'exams/:id/result',
        name: 'StudentExamResult',
        component: () => import('../views/student/ExamDetail.vue'),
        props: { showResult: true },
        meta: { 
          title: '考试结果',
          requiresAuth: true,
          requiresStudent: true
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
    if (to.meta.requiresAdmin || to.meta.requiresSupervisor || to.meta.requiresStudent) {
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
        } else if (to.meta.requiresStudent && !userStore.isStudent) {
          console.log('需要学生权限但当前不是学生，尝试强制刷新')
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
      console.log('需要教师权限，但用户不是教师，重定向到首页')
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