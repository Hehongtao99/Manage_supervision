<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="$router.push('/home')">
            <img src="../assets/logo.png" alt="Run2gather" class="logo-img" />
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
                <el-dropdown-item @click="$router.push('/settings')">
                  <el-icon><Setting /></el-icon>账号设置
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

    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>Run2gather</h4>
            <p>让跑步更有趣，让运动成为生活方式</p>
          </div>
          <div class="footer-section">
            <h4>功能</h4>
            <ul>
              <li>跑步记录</li>
              <li>社交分享</li>
              <li>好友互动</li>
              <li>成就系统</li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <ul>
              <li>邮箱：contact@run2gather.com</li>
              <li>电话：400-888-0000</li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 Run2gather. All rights reserved.</p>
        </div>
      </div>
    </footer>
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
  Setting,
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
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
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
  min-height: calc(100vh - 64px - 200px);
}

.footer {
  background: #2c3e50;
  color: #fff;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 32px;
  margin-bottom: 32px;
}

.footer-section h4 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #1890ff;
}

.footer-section p {
  margin: 0;
  color: #bbb;
  line-height: 1.6;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin: 8px 0;
  color: #bbb;
}

.footer-bottom {
  border-top: 1px solid #3d4f5f;
  padding-top: 20px;
  text-align: center;
}

.footer-bottom p {
  margin: 0;
  color: #999;
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
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style> 