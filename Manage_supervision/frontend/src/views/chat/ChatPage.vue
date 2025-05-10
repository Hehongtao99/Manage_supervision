<template>
  <div class="chat-page-wrapper">
    <!-- 完全独立于BaseLayout的聊天页面 -->
    <div class="chat-page">
      <div class="chat-container">
        <div class="chat-sidebar">
          <div class="sidebar-header">
            <h2>消息</h2>
          </div>
          <ChatList 
            :loading="loadingConversations"
            @select="handleSelectConversation"
          />
        </div>
        <div class="chat-main">
          <ChatWindow 
            :conversation-id="activeConversationId"
            @message-sent="handleMessageSent"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '../../stores/user';
import { useChatStore } from '../../stores/chat';
import ChatList from '../../components/chat/ChatList.vue';
import ChatWindow from '../../components/chat/ChatWindow.vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const userStore = useUserStore();
const chatStore = useChatStore();

// 状态
const activeConversationId = ref<number | null>(null);
const loadingConversations = computed(() => chatStore.loadingConversations);

// 选择会话
const handleSelectConversation = (conversationId: number) => {
  activeConversationId.value = conversationId;
};

// 发送消息后处理
const handleMessageSent = () => {
  // 这里可以添加任何消息发送后的逻辑
};

// 初始化
onMounted(async () => {
  // 初始化聊天服务
  await chatStore.initChat();
  
  // 加载会话列表
  await chatStore.loadConversations();
  
  // 检查URL中是否有会话ID参数
  const route = router.currentRoute.value;
  const conversationIdParam = route.query.conversationId;
  
  if (conversationIdParam) {
    // 如果URL中有会话ID，则选择该会话
    const conversationId = parseInt(conversationIdParam as string, 10);
    if (!isNaN(conversationId)) {
      activeConversationId.value = conversationId;
      
      // 检查该会话是否已加载到聊天存储中
      if (!chatStore.conversations.some(c => c.id === conversationId)) {
        // 如果会话不在列表中，尝试加载该特定会话
        try {
          await chatStore.loadConversationById(conversationId);
        } catch (error) {
          console.error('Failed to load conversation by ID:', error);
        }
      }
    }
  } else if (chatStore.conversations.length > 0) {
    // 如果没有指定会话ID且有会话，选择第一个会话
    activeConversationId.value = chatStore.conversations[0].id;
  }
});
</script>

<style scoped>
.chat-page-wrapper {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.chat-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-container {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: hidden;
}

.chat-sidebar {
  width: 280px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e0e0e0;
}

.sidebar-header h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.chat-main {
  flex: 1;
  height: 100%;
  background-color: #f5f7fa;
  overflow: hidden;
}

/* 媒体查询 - 小屏幕设备 */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 100%;
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 2;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .chat-sidebar.active {
    transform: translateX(0);
  }
  
  .chat-main {
    width: 100%;
  }
}
</style> 