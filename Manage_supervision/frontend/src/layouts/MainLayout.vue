<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-image" />
        <span class="logo-text">管理系统</span>
      </div>
      <el-menu
        mode="horizontal"
        :router="true"
        class="nav-menu"
        :default-active="$route.path"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/profile">个人信息</el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin">
          管理控制台
        </el-menu-item>
      </el-menu>
      <div class="user-info">
        <span>{{ userStore.user?.username }}</span>
        <el-button type="text" @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-image {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.nav-menu {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
}
</style> 