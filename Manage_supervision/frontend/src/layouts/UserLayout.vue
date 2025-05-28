<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="$router.push('/home')">
            <!-- 跑步主题logo -->
            <div class="logo-icon">
              <!-- SVG跑步图标 -->
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M13.5 5.5C13.5 6.88071 12.3807 8 11 8C9.61929 8 8.5 6.88071 8.5 5.5C8.5 4.11929 9.61929 3 11 3C12.3807 3 13.5 4.11929 13.5 5.5Z" fill="#1890ff"/>
                <path d="M9.5 8.5L8 10.5L10.5 12L12 11L14 13L16.5 12L18 14L19.5 13.5L18.5 15.5L16 16.5L13.5 15L11.5 16.5L9 15L7.5 16.5L6 15L4.5 16.5L3 15L4 13L6 14L7.5 12.5L9.5 8.5Z" fill="#1890ff"/>
                <path d="M6 18L8 17L10 18.5L12 17.5L14 19L16 18L18 19.5L20 18.5L21 20.5L19 21.5L17 20L15 21L13 19.5L11 21L9 19.5L7 21L5 19.5L4 21.5L6 18Z" fill="#1890ff"/>
              </svg>
            </div>
            <span class="logo-text">Run2gather</span>
          </div>
        </div>
        
        <div class="header-center">
          <nav class="nav-menu">
            <div class="nav-item" :class="{ active: $route.path === '/home' || $route.path === '/' }" @click="$router.push('/home')">主页</div>
            <div class="nav-item" :class="{ active: $route.path === '/running-dashboard' }" @click="$router.push('/running-dashboard')">跑步</div>
            <div class="nav-item" :class="{ active: $route.path.startsWith('/social') }" @click="$router.push('/social')">朋友圈</div>
            <div class="nav-item" :class="{ active: $route.path === '/friends' }" @click="$router.push('/friends')">好友</div>
            <div class="nav-item" :class="{ active: $route.path === '/chat' }" @click="$router.push('/chat')">消息</div>
          </nav>
        </div>
        
        <div class="header-right">
          <!-- 消息提醒 -->
          <div class="notification-icon" @click="$router.push('/chat')">
            <el-badge :value="unreadCount > 0 ? unreadCount : ''" :max="99" :hidden="unreadCount <= 0">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </div>
          
          <!-- 用户头像下拉菜单 -->
          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.user.avatar">
                {{ userStore.user.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.user.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin/dashboard')">
                  <el-icon><Monitor /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { ElMessageBox } from 'element-plus'
import {
  Bell,
  User,
  Monitor,
  SwitchButton,
  CaretBottom
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const unreadCount = computed(() => chatStore.totalUnreadCount)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  position: relative;
  overflow: hidden;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #1890ff, #722ed1, #fa8c16, #52c41a, #1890ff);
  background-size: 200% 100%;
  animation: headerGradient 4s ease-in-out infinite;
}

@keyframes headerGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 12px;
}

.logo:hover {
  background: rgba(24, 144, 255, 0.05);
  transform: scale(1.02);
}

.logo-icon {
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #1890ff, #722ed1);
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.logo-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.2), transparent);
  border-radius: 50%;
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.logo:hover .logo-icon::before {
  transform: translateX(100%);
}

.logo-icon svg {
  position: relative;
  z-index: 2;
  filter: brightness(0) invert(1);
  transition: all 0.3s ease;
}

.logo:hover .logo-icon {
  transform: rotate(10deg) scale(1.1);
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff, #722ed1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
}

.logo:hover .logo-text {
  text-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
  transform: translateX(2px);
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  display: flex;
  gap: 32px;
}

.nav-item {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
  user-select: none;
}

.nav-item:hover,
.nav-item.active {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-icon {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.notification-icon:hover {
  background: #f5f5f5;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-weight: 500;
  color: #333;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 64px);
  padding-bottom: 0;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .header-center {
    display: none;
  }
  
  .header-container {
    padding: 0 16px;
  }
  
  .nav-menu {
    gap: 16px;
  }
}
</style> 