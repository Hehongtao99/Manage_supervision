<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'
import { useChatStore } from './stores/chat'
import ChatNotification from './components/chat/ChatNotification.vue'

const userStore = useUserStore()
const chatStore = useChatStore()
const router = useRouter()

// 处理通知点击，跳转到聊天页面
const handleNotificationClick = (senderId: number) => {
  // 根据用户角色决定跳转路径
  if (userStore.isPlayer) {
    router.push({ name: 'ChatInterface', params: { companionId: senderId } })
  } else if (userStore.isCompanion) {
    router.push({ name: 'CompanionChatList', query: { activeChat: senderId } })
  }
}

onMounted(async () => {
  try {
    // 确保在应用启动时初始化认证
    const success = await userStore.initializeAuth()
    if (success) {
      console.log('认证初始化完成，用户编号:', userStore.user.userNumber)
      
      // 用户认证成功后初始化聊天服务
      await chatStore.initChat()
      console.log('聊天服务初始化完成')
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
  <ChatNotification @click="handleNotificationClick" />
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

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 确保element-plus的滚动条使用自定义样式 */
.el-scrollbar__bar {
  opacity: 0.3;
}

.el-scrollbar__bar.is-vertical {
  width: 8px;
}

.el-scrollbar__bar.is-horizontal {
  height: 8px;
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
