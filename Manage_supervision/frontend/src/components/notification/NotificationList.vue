<template>
  <el-card shadow="hover" class="notification-list-card">
    <template #header>
      <div class="card-header">
        <h3>{{ title }}</h3>
        <el-button v-if="notifications.length > 0" text type="primary" @click="refresh">刷新</el-button>
      </div>
    </template>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <el-empty v-else-if="notifications.length === 0" description="暂无通知" />
    
    <el-scrollbar max-height="500px" v-else>
      <div class="notification-list">
        <el-card 
          v-for="notification in notifications" 
          :key="notification.id"
          shadow="hover"
          class="notification-item"
          @click="showNotificationDetail(notification.id)"
        >
          <div class="notification-header">
            <h4 class="notification-title">{{ notification.title }}</h4>
            <el-tag size="small" effect="light">{{ notification.createTime }}</el-tag>
          </div>
          
          <div class="notification-content">
            {{ notification.content.length > 100 ? notification.content.substring(0, 100) + '...' : notification.content }}
          </div>
          
          <div class="notification-footer">
            <span class="notification-sender">发送者: {{ notification.senderName }}</span>
          </div>
        </el-card>
      </div>
    </el-scrollbar>
    
    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentNotification?.title || '通知详情'"
      width="50%"
      align-center
    >
      <div v-if="currentNotification" class="notification-detail">
        <div class="detail-header">
          <span class="detail-sender">发送者: {{ currentNotification.senderName }}</span>
          <span class="detail-time">时间: {{ currentNotification.createTime }}</span>
        </div>
        
        <div class="detail-content">
          {{ currentNotification.content }}
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, PropType } from 'vue';
import { useNotificationStore } from '../../stores/notification';
import type { NotificationDTO } from '../../types/notification';

// 组件属性
const props = defineProps({
  type: {
    type: String as PropType<'received' | 'sent'>,
    required: true
  }
});

// 通知存储
const notificationStore = useNotificationStore();

// 状态
const loading = ref(false);
const dialogVisible = ref(false);
const currentNotification = ref<NotificationDTO | null>(null);

// 计算属性
const title = computed(() => {
  return props.type === 'received' ? '收到的通知' : '发送的通知';
});

const notifications = computed(() => {
  return notificationStore.sortedNotifications;
});

// 加载通知
const loadNotifications = async () => {
  loading.value = true;
  try {
    if (props.type === 'received') {
      await notificationStore.loadReceivedNotifications();
    } else {
      await notificationStore.loadSentNotifications();
    }
  } catch (error) {
    console.error('加载通知失败:', error);
  } finally {
    loading.value = false;
  }
};

// 刷新通知列表
const refresh = () => {
  loadNotifications();
};

// 显示通知详情
const showNotificationDetail = async (id: number) => {
  try {
    const notification = await notificationStore.getNotificationDetail(id);
    if (notification) {
      currentNotification.value = notification;
      dialogVisible.value = true;
    }
  } catch (error) {
    console.error('获取通知详情失败:', error);
  }
};

// 页面加载时获取通知列表
onMounted(() => {
  loadNotifications();
});
</script>

<style scoped>
.notification-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.notification-content {
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
  font-size: 14px;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.notification-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}

.detail-content {
  color: #303133;
  line-height: 1.6;
  white-space: pre-line;
}

.dialog-footer {
  text-align: right;
}
</style> 