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
        path: 'chat/:conversationId',
        name: 'chat',
        component: () => import('../views/chat/ChatPage.vue'),
        meta: {
          title: '聊天',
          requiresAuth: true
        }
      },
      {
        path: 'friends',
        name: 'friends',
        component: () => import('../views/friend/FriendList.vue'),
        meta: {
          title: '好友管理',
          requiresAuth: true
        }
      },
      {
        path: 'running-record',
        name: 'RunningRecord',
        component: () => import('../views/running/RunningRecordForm.vue'),
        meta: {
          title: '跑步记录',
          requiresAuth: true
        }
      },
      {
        path: 'running-dashboard',
        name: 'RunningDashboard',
        component: () => import('../views/running/RunningDashboard.vue'),
        meta: {
          title: '跑步数据看板',
          requiresAuth: true
        }
      },
      {
        path: 'social',
        name: 'Social',
        component: () => import('../views/social/SocialTimeline.vue'),
        meta: {
          title: '朋友圈',
          requiresAuth: true
        }
      },
      {
        path: 'social/my-posts',
        name: 'MyPosts',
        component: () => import('../views/social/MyPosts.vue'),
        meta: {
          title: '我的发布',
          requiresAuth: true
        }
      },
      {
        path: 'social/post/:id',
        name: 'PostDetail',
        component: () => import('../views/social/PostDetail.vue'),
        meta: {
          title: '帖子详情',
          requiresAuth: true
        }
      },
      {
        path: 'social/create-post',
        name: 'CreatePost',
        component: () => import('../views/social/CreatePost.vue'),
        meta: {
          title: '发布动态',
          requiresAuth: true
        }
      },
      {
        path: 'user/:id/profile',
        name: 'UserProfile',
        component: () => import('../views/Profile.vue'),
        meta: {
          title: '用户主页',
          requiresAuth: true
        }
      },
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
      },
      {
        path: 'social/posts',
        name: 'SocialPostManagement',
        component: () => import('../views/admin/social/PostManagement.vue'),
        meta: { 
          title: '朋友圈管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'social/comments',
        name: 'SocialCommentManagement',
        component: () => import('../views/admin/social/CommentManagement.vue'),
        meta: { 
          title: '评论管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'social/post/:id',
        name: 'SocialPostDetail',
        component: () => import('../views/admin/social/PostDetail.vue'),
        meta: { 
          title: '帖子详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'running/report',
        name: 'RunningReport',
        component: () => import('../views/admin/running/RunningReport.vue'),
        meta: { 
          title: '跑步报表',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'running/ranking',
        name: 'RunningRanking',
        component: () => import('../views/admin/running/RunningRanking.vue'),
        meta: { 
          title: '跑步排名',
          requiresAuth: true,
          requiresAdmin: true
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
    if (to.meta.requiresAdmin) {
      console.log('页面需要特定角色权限，刷新用户信息...')
      try {
        // 尝试刷新用户信息，但不强制刷新以避免可能的循环
        // 只有当实际角色与预期角色不匹配时才强制刷新
        let needsForceRefresh = false
        
        if (to.meta.requiresAdmin && !userStore.isAdmin) {
          console.log('需要管理员权限但当前不是管理员，尝试强制刷新')
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
  }
  
  // 如果用户已登录且访问登录页，根据角色重定向到对应页面
  if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    if (userStore.isAdmin) {
      next('/admin/dashboard')
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