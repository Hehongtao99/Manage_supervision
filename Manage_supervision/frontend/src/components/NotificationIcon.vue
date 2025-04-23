<template>
  <div class="notification-icon" @click="showNotificationDrawer">
    <el-badge :value="unreadCount > 0 ? unreadCount : ''" :max="99" class="notification-badge">
      <el-icon :size="22"><Bell /></el-icon>
    </el-badge>
  </div>

  <el-drawer
    v-model="drawerVisible"
    title="我的通知"
    direction="rtl"
    size="350px"
  >
    <div class="notification-container">
      <div class="notification-header">
        <span>共 {{ notifications.length }} 条通知，{{ unreadCount }} 条未读</span>
        <el-button v-if="notifications.length > 0" type="primary" link @click="readAllNotifications">
          全部标记为已读
        </el-button>
      </div>
      
      <el-empty v-if="notifications.length === 0" description="暂无通知"></el-empty>
      
      <el-scrollbar height="calc(100vh - 180px)" v-else>
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ 'is-read': item.isRead }"
          @click="readNotification(item)"
        >
          <div class="notification-avatar">
            <el-avatar :size="40" :src="item.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
          </div>
          <div class="notification-content">
            <div class="notification-title">
              <span>{{ item.title }}</span>
              <div v-if="!item.isRead" class="unread-dot"></div>
            </div>
            <div class="notification-message">{{ item.content }}</div>
            <div class="notification-footer">
              <span class="sender">{{ item.senderName }}</span>
              <span class="time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import { getReceivedNotifications, markNotificationAsRead, getUnreadNotificationCount } from '@/api/notification';
import { ElMessage } from 'element-plus';
import type { NotificationResponse } from '@/api/notification';

const notifications = ref<NotificationResponse[]>([]);
const unreadCount = ref(0);
const drawerVisible = ref(false);
let pollingTimer: any = null;

// 加载通知数据
const loadNotifications = async () => {
  try {
    const res = await getReceivedNotifications();
    if (res.data.success) {
      notifications.value = res.data.data;
      countUnread();
    }
  } catch (error) {
    console.error('加载通知失败', error);
  }
};

// 加载未读通知数量
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadNotificationCount();
    if (res.data.success) {
      unreadCount.value = res.data.data.count;
    }
  } catch (error) {
    console.error('获取未读通知数量失败', error);
  }
};

// 统计未读通知数量
const countUnread = () => {
  unreadCount.value = notifications.value.filter(item => !item.isRead).length;
};

// 显示通知抽屉
const showNotificationDrawer = () => {
  drawerVisible.value = true;
  loadNotifications();
};

// 标记通知为已读
const readNotification = async (notification: NotificationResponse) => {
  if (notification.isRead) return;
  
  try {
    const res = await markNotificationAsRead(notification.id);
    if (res.data.success) {
      const index = notifications.value.findIndex(item => item.id === notification.id);
      if (index !== -1) {
        notifications.value[index].isRead = true;
        notifications.value[index].readTime = new Date().toISOString();
        countUnread();
      }
    }
  } catch (error) {
    console.error('标记通知已读失败', error);
  }
};

// 标记所有通知为已读
const readAllNotifications = async () => {
  const unreadNotifications = notifications.value.filter(item => !item.isRead);
  if (unreadNotifications.length === 0) return;
  
  try {
    for (const notification of unreadNotifications) {
      await markNotificationAsRead(notification.id);
    }
    
    notifications.value = notifications.value.map(item => {
      return { ...item, isRead: true, readTime: new Date().toISOString() };
    });
    
    unreadCount.value = 0;
    ElMessage.success('已全部标记为已读');
  } catch (error) {
    console.error('标记全部已读失败', error);
  }
};

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // 小于1小时，显示x分钟前
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000));
    return `${minutes}分钟前`;
  }
  
  // 小于24小时，显示x小时前
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000));
    return `${hours}小时前`;
  }
  
  // 小于7天，显示x天前
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000));
    return `${days}天前`;
  }
  
  // 否则显示完整日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 启动轮询
const startPolling = () => {
  pollingTimer = setInterval(() => {
    loadUnreadCount();
  }, 30000); // 每30秒轮询一次
};

onMounted(() => {
  loadUnreadCount();
  startPolling();
});

onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer);
  }
});
</script>

<style scoped>
.notification-icon {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 50%;
}

.notification-icon:hover {
  background-color: #f5f5f5;
}

.notification-badge {
  line-height: 1;
}

.notification-container {
  padding: 10px;
  height: 100%;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.notification-item {
  display: flex;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.is-read {
  opacity: 0.7;
}

.notification-avatar {
  margin-right: 12px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: bold;
  margin-bottom: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border-radius: 50%;
}

.notification-message {
  margin-bottom: 8px;
  color: #606266;
  word-break: break-all;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style> 