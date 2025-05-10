<template>
  <div class="navigation-test">
    <h2>路由导航测试页面</h2>
    
    <div class="status-info">
      <h3>当前状态</h3>
      <pre>{{ userInfo }}</pre>
      
      <h3>当前路由</h3>
      <p>路径: {{ currentRoute.path }}</p>
      <p>完整路径: {{ currentRoute.fullPath }}</p>
      <p>名称: {{ currentRoute.name }}</p>
      <p>元信息:</p>
      <pre>{{ currentRouteMeta }}</pre>
    </div>
    
    <div class="navigation-buttons">
      <h3>导航测试</h3>
      <div class="button-grid">
        <el-button type="primary" @click="navigateTo('/chat')">聊天页面</el-button>
        <el-button type="primary" @click="navigateTo('/courses')">课程浏览</el-button>
        <el-button type="primary" @click="navigateTo('/orders')">我的订单</el-button>
        <el-button type="primary" @click="navigateTo('/after-sales')">售后管理</el-button>
        <el-button type="primary" @click="navigateTo('/profile')">个人信息</el-button>
        <el-button type="primary" @click="navigateTo('/settings')">账号设置</el-button>
      </div>
      
      <h3>管理员测试</h3>
      <div class="button-grid">
        <el-button type="danger" @click="navigateTo('/admin/dashboard')">管理员控制台</el-button>
      </div>
      
      <h3>教师测试</h3>
      <div class="button-grid">
        <el-button type="warning" @click="navigateTo('/supervisor/students')">教师-学生管理</el-button>
        <el-button type="warning" @click="navigateTo('/supervisor/course-students')">教师-课程学生</el-button>
      </div>
      
      <h3>用户操作</h3>
      <div class="button-grid">
        <el-button type="success" @click="refreshUserInfo">刷新用户信息</el-button>
        <el-button type="info" @click="logout">登出</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const navigateTo = (path) => {
  console.log('尝试导航到:', path)
  router.push(path).catch(err => {
    console.error('导航失败:', err)
  })
}

const refreshUserInfo = async () => {
  console.log('正在刷新用户信息...')
  try {
    await userStore.fetchUserInfo(true)
    console.log('用户信息刷新成功')
  } catch (error) {
    console.error('用户信息刷新失败:', error)
  }
}

const logout = () => {
  console.log('执行登出...')
  userStore.logout()
}

const userInfo = computed(() => {
  return {
    id: userStore.userId,
    username: userStore.user.username,
    roles: userStore.user.roles,
    isLoggedIn: userStore.isLoggedIn,
    isAdmin: userStore.isAdmin,
    isSupervisor: userStore.isSupervisor,
    isStudent: userStore.isStudent
  }
})

const currentRoute = computed(() => route)
const currentRouteMeta = computed(() => JSON.stringify(route.meta, null, 2))
</script>

<style scoped>
.navigation-test {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.status-info {
  margin-bottom: 30px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

pre {
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  max-height: 150px;
  overflow: auto;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  border-bottom: 1px solid #dcdfe6;
  padding-bottom: 8px;
}
</style> 