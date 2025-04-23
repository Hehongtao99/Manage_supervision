<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted, provide } from 'vue'
import { useUserStore } from './stores/user'
import axios from 'axios'

// Token 刷新初始化
onMounted(async () => {
  // 设置全局axios默认值
  setupAxiosDefaults()
  
  // 初始化用户认证状态
  const userStore = useUserStore()
  try {
    await userStore.initializeAuth()
  } catch (error) {
    console.error('初始化认证状态失败:', error)
  }
})

// 设置axios全局默认值
const setupAxiosDefaults = () => {
  // 从localStorage获取token
  const token = localStorage.getItem('token')
  if (token) {
    console.log('App.vue: 初始化设置全局Authorization头')
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  } else {
    console.log('App.vue: 初始化时未找到token')
  }
}
</script>

<template>
  <RouterView />
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden; /* 防止整个页面滚动 */
  position: fixed; /* 确保页面不会滚动 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  width: 100%;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
}

/* 全局样式 */
.page-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.card-shadow {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.text-center {
  text-align: center;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}
</style>
