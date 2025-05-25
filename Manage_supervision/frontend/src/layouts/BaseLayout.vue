<template>
  <div class="app-wrapper" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 左侧菜单 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo-container">
        <img src="../assets/logo.png" alt="Logo" class="logo-image" />
        <span class="logo-text" v-show="!isCollapsed">学生督导系统</span>
      </div>
      
      <el-scrollbar>
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapsed"
          :unique-opened="true"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <!-- 管理员菜单 -->
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/admin/dashboard">
              <el-icon><Monitor /></el-icon>
              <template #title>控制台</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/teachers">
              <el-icon><User /></el-icon>
              <template #title>教师管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/students">
              <el-icon><User /></el-icon>
              <template #title>学生管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/roles">
              <el-icon><Setting /></el-icon>
              <template #title>角色管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/courses">
              <el-icon><Document /></el-icon>
              <template #title>课程管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/course-categories">
              <el-icon><List /></el-icon>
              <template #title>课程类别管理</template>
            </el-menu-item>
          </template>
          
          <!-- 教师菜单 -->
          <template v-else-if="userStore.isSupervisor">
            <el-menu-item index="/supervisor/students">
              <el-icon><User /></el-icon>
              <template #title>学生管理</template>
            </el-menu-item>
            
            <el-menu-item index="/supervisor/courses">
              <el-icon><Document /></el-icon>
              <template #title>我的课程</template>
            </el-menu-item>
            
            <el-menu-item index="/supervisor/chat">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>
                <span>消息</span>
                <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
              </template>
            </el-menu-item>
          </template>
          
          <!-- 学生菜单 -->
          <template v-else>
            <el-menu-item index="/chat">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>
                <span>消息</span>
                <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
              </template>
            </el-menu-item>
            
            <el-menu-item index="/course/list">
              <el-icon><Document /></el-icon>
              <template #title>课程列表</template>
            </el-menu-item>
          </template>
          
          <!-- 公共菜单 -->
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
          
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>账号设置</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="navbar-left">
          <div class="hamburger" @click="toggleSidebar">
            <el-icon :size="24" v-if="isCollapsed"><Expand /></el-icon>
            <el-icon :size="24" v-else><Fold /></el-icon>
          </div>
          <breadcrumb />
        </div>
        
        <div class="navbar-right">
          <!-- 角色标识 -->
          <div class="role-indicator">
            <el-tag :type="roleTagType" effect="dark">
              {{ roleName }}
            </el-tag>
          </div>

          <el-dropdown trigger="click">
            <div class="avatar-container">
              <el-avatar :size="32" class="user-avatar" :src="userStore.user.avatar">
                {{ userStore.user.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.user.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 根据角色不同，跳转到不同的个人信息页面 -->
                <el-dropdown-item @click="navigateToProfile">
                  <el-icon><UserFilled /></el-icon>个人信息
                </el-dropdown-item>
                
                <!-- 管理员可以直接进入管理页面 -->
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin/dashboard')">
                  <el-icon><Setting /></el-icon>管理控制台
                </el-dropdown-item>
                
                <!-- 教师可以直接进入教师页面 -->
                <el-dropdown-item v-if="userStore.isSupervisor" @click="$router.push('/supervisor/students')">
                  <el-icon><Monitor /></el-icon>学生管理
                </el-dropdown-item>
                
                <el-dropdown-item @click="router.push('/settings')">
                  <el-icon><Setting /></el-icon>账号设置
                </el-dropdown-item>
                
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 主要内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import Breadcrumb from '../components/Breadcrumb.vue'
import { ElMessageBox } from 'element-plus'
import {
  Fold,
  Expand,
  UserFilled,
  Document,
  User,
  SwitchButton,
  CaretBottom,
  Setting,
  Monitor,
  ChatDotRound,
  List
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()
const isCollapsed = ref(false)

// 未读消息计数
const unreadCount = computed(() => chatStore.totalUnreadCount)

// 计算当前角色名称
const roleName = computed(() => {
  if (userStore.isAdmin) {
    return '管理员'
  } else if (userStore.isSupervisor) {
    return '教师'
  } else {
    return '学生'
  }
})

// 计算角色标签类型
const roleTagType = computed(() => {
  if (userStore.isAdmin) {
    return 'danger'
  } else if (userStore.isSupervisor) {
    return 'warning'
  } else {
    return 'success'
  }
})

// 根据角色导航到对应的个人信息页
const navigateToProfile = () => {
  if (userStore.isAdmin) {
    router.push('/profile')
  } else if (userStore.isSupervisor) {
    router.push('/supervisor/profile')
  } else {
    router.push('/profile')
  }
}

// 根据角色导航到对应的聊天页面
const navigateToChat = () => {
  if (userStore.isSupervisor) {
    router.push('/supervisor/chat')
  } else {
    router.push('/chat')
  }
}

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

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

// 初始化聊天服务
onMounted(async () => {
  if (userStore.isLoggedIn) {
    await chatStore.initChat()
  }
})

// 监听用户登录状态
watch(() => userStore.isLoggedIn, async (isLoggedIn) => {
  if (isLoggedIn) {
    await chatStore.initChat()
  } else {
    chatStore.clearChatData()
  }
})
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 210px;
  background: #304156;
  transition: width 0.3s;
  z-index: 1001;
  overflow: hidden;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.sidebar-container.is-collapsed {
  width: 64px;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  overflow: hidden;
  background: #2b2f3a;
}

.logo-image {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  opacity: 1;
  transition: opacity 0.3s;
}

.is-collapsed .logo-text {
  opacity: 0;
  display: none;
}

.main-container {
  position: relative;
  margin-left: 210px;
  min-height: 100%;
  background: #f0f2f5;
  transition: margin-left 0.3s;
}

.is-collapsed .main-container {
  margin-left: 64px;
}

.navbar {
  height: 60px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.notification-item {
  cursor: pointer;
  padding: 0 12px;
  margin-right: 8px;
}

.notification-item:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.role-indicator {
  margin-right: 16px;
}

.hamburger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  cursor: pointer;
  margin-right: 16px;
  transition: background-color 0.3s;
  border-radius: 50%;
}

.hamburger:hover {
  background-color: #f6f6f6;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.avatar-container:hover {
  background-color: #f6f6f6;
}

.user-avatar {
  background: #409eff;
  margin-right: 8px;
}

.username {
  font-size: 14px;
  margin-right: 4px;
}

.app-main {
  padding: 16px;
  min-height: calc(100vh - 60px);
}

/* 路由过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .sidebar-container {
    width: 64px;
  }
  
  .main-container {
    margin-left: 64px;
  }
  
  .is-collapsed .sidebar-container {
    width: 0;
  }
  
  .is-collapsed .main-container {
    margin-left: 0;
  }
}
</style> 