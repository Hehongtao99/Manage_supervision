<template>
  <div class="friend-notification-wrapper">
    <!-- 通知图标，显示未读数量 -->
    <el-badge :value="unreadCount" :hidden="!unreadCount" class="notification-badge">
      <el-button 
        class="notification-icon" 
        :icon="Bell" 
        circle 
        @click="toggleNotifications" 
      />
    </el-badge>

    <!-- 通知弹出框 -->
    <el-popover
      v-model:visible="showNotifications"
      placement="bottom-end"
      :width="320"
      trigger="click"
      popper-class="friend-notification-popover"
    >
      <template #default>
        <div class="notification-container">
          <div class="notification-header">
            <h3>好友请求通知</h3>
            <el-button 
              v-if="unreadCount > 0" 
              type="text" 
              @click="markAllAsRead"
            >
              全部标为已读
            </el-button>
          </div>
          
          <el-divider />
          
          <div v-if="pendingRequests.length === 0" class="empty-notifications">
            暂无新的好友请求
          </div>
          
          <div v-else class="notification-list">
            <div 
              v-for="request in pendingRequests" 
              :key="request.id" 
              class="notification-item"
            >
              <div class="notification-avatar">
                <el-avatar :src="request.fromUser.avatar || defaultAvatar" />
              </div>
              <div class="notification-content">
                <div class="notification-title">
                  <span class="username">{{ request.fromUser.name || request.fromUser.username }}</span>
                  <span class="time">{{ formatTime(request.createdTime) }}</span>
                </div>
                <div class="notification-message">
                  {{ request.message || '请求添加您为好友' }}
                </div>
                <div class="notification-actions">
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="acceptRequest(request.id)"
                    :loading="processing === request.id"
                  >
                    接受
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small"
                    @click="rejectRequest(request.id)"
                    :loading="processing === request.id"
                  >
                    拒绝
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <el-divider v-if="pendingRequests.length > 0" />
          
          <div class="notification-footer">
            <el-button 
              type="primary" 
              plain 
              @click="goToFriendRequests"
            >
              查看所有请求
            </el-button>
          </div>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Bell } from '@element-plus/icons-vue';
import { useFriendStore, type FriendRequest } from '../../stores/friend';
import chatEvents from '../../services/eventBus';
import { format } from 'date-fns';

// 状态变量
const showNotifications = ref(false);
const defaultAvatar = ref('/images/default-avatar.png');
const processing = ref<number | null>(null);

// Store
const friendStore = useFriendStore();
const router = useRouter();

// 计算属性
const unreadCount = computed(() => friendStore.unreadRequestCount);
const pendingRequests = computed(() => friendStore.pendingReceivedRequests);

// 格式化时间
const formatTime = (timeString: string) => {
  try {
    const date = new Date(timeString);
    return format(date, 'yyyy-MM-dd HH:mm');
  } catch (error) {
    return timeString;
  }
};

// 切换通知显示状态
const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
};

// 标记所有请求为已读
const markAllAsRead = () => {
  friendStore.clearUnreadRequestCount();
};

// 接受好友请求
const acceptRequest = async (requestId: number) => {
  processing.value = requestId;
  try {
    await friendStore.processFriendRequest(requestId, true);
    showNotifications.value = false;
  } catch (error) {
    console.error('接受好友请求失败:', error);
  } finally {
    processing.value = null;
  }
};

// 拒绝好友请求
const rejectRequest = async (requestId: number) => {
  processing.value = requestId;
  try {
    await friendStore.processFriendRequest(requestId, false);
    showNotifications.value = false;
  } catch (error) {
    console.error('拒绝好友请求失败:', error);
  } finally {
    processing.value = null;
  }
};

// 前往好友请求页面
const goToFriendRequests = () => {
  router.push('/friends/requests');
  showNotifications.value = false;
};

// 监听WebSocket好友请求通知
const setupNotificationListener = () => {
  chatEvents.on('friendRequest', (data) => {
    console.log('收到好友请求WebSocket通知:', data);
    
    // 好友请求通知处理逻辑已经在friendStore中，这里不需要重复处理
    // 只需确保store已初始化
    if (!friendStore.initialized) {
      friendStore.initFriendSystem();
    }
  });
};

// 生命周期钩子
onMounted(() => {
  // 确保好友系统已初始化
  if (!friendStore.initialized) {
    friendStore.initFriendSystem();
  }
  
  // 设置通知监听
  setupNotificationListener();
});
</script>

<style scoped>
.friend-notification-wrapper {
  position: relative;
  display: inline-block;
}

.notification-badge {
  margin-right: 10px;
}

.notification-container {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
}

.empty-notifications {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-avatar {
  margin-right: 12px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.username {
  font-weight: bold;
}

.time {
  font-size: 12px;
  color: #909399;
}

.notification-message {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.notification-actions {
  display: flex;
  gap: 8px;
}

.notification-footer {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}
</style> 