<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

onMounted(async () => {
  try {
    // 确保在应用启动时初始化认证
    const success = await userStore.initializeAuth()
    if (success) {
      console.log('认证初始化完成，用户编号:', userStore.user.userNumber)
    } else {
      console.log('认证初始化失败或用户未登录')
    }
  } catch (error) {
    console.error('认证初始化过程中发生错误:', error)
  }
})
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
