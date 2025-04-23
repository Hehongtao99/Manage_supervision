<template>
  <div class="notifications-container">
    <div class="page-header">
      <h2>我的通知</h2>
    </div>

    <el-card class="notification-list">
      <template #header>
        <div class="card-header">
          <span>通知列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索通知"
              clearable
              prefix-icon="Search"
              style="width: 220px"
            />
            <el-select v-model="filterType" placeholder="类型" style="width: 120px; margin-left: 10px;">
              <el-option label="全部" value="" />
              <el-option label="全体通知" value="ALL" />
              <el-option label="班级通知" value="CLASS" />
            </el-select>
          </div>
        </div>
      </template>

      <el-empty v-if="filteredNotifications.length === 0" description="暂无通知" />

      <div v-else>
        <el-table :data="filteredNotifications" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <span class="notification-title" @click="viewNotificationDetail(row)">
                {{ row.title }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="senderName" label="发送人" width="120" />
          
          <el-table-column prop="recipientType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.recipientType === 'ALL' ? 'danger' : 'primary'" size="small">
                {{ row.recipientType === 'ALL' ? '全体' : '班级' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="viewNotificationDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="通知详情"
      width="500px"
    >
      <div v-if="selectedNotification" class="notification-detail">
        <h3 class="detail-title">{{ selectedNotification.title }}</h3>
        
        <div class="detail-meta">
          <span class="sender">发送人: {{ selectedNotification.senderName }}</span>
          <span class="recipient">
            <el-tag :type="selectedNotification.recipientType === 'ALL' ? 'danger' : 'primary'" size="small">
              {{ selectedNotification.recipientType === 'ALL' ? '全体通知' : '班级通知' }}
            </el-tag>
          </span>
          <span class="time">发送时间: {{ formatTime(selectedNotification.createTime) }}</span>
        </div>
        
        <div class="detail-content">
          {{ selectedNotification.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getReceivedNotifications, NotificationResponse } from '@/api/notification';

// 数据列表
const notifications = ref<NotificationResponse[]>([]);
const selectedNotification = ref<NotificationResponse | null>(null);

// 过滤和搜索
const searchKeyword = ref('');
const filterType = ref('');

// 对话框控制
const detailDialogVisible = ref(false);

// 过滤通知列表
const filteredNotifications = computed(() => {
  let result = notifications.value;
  
  // 按类型筛选
  if (filterType.value) {
    result = result.filter(item => item.recipientType === filterType.value);
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      item.content.toLowerCase().includes(keyword) ||
      item.senderName.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

// 初始化数据
onMounted(async () => {
  await loadNotifications();
});

// 加载通知列表
const loadNotifications = async () => {
  try {
    const res = await getReceivedNotifications();
    if (res.data.success) {
      notifications.value = res.data.data;
    }
  } catch (error) {
    console.error('获取通知列表失败', error);
    ElMessage.error('获取通知列表失败');
  }
};

// 查看通知详情
const viewNotificationDetail = (notification: NotificationResponse) => {
  selectedNotification.value = notification;
  detailDialogVisible.value = true;
};

// 格式化时间显示
const formatTime = (timestamp: string) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
.notifications-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
}

.notification-title {
  cursor: pointer;
}

.notification-detail {
  padding: 10px;
  
  .detail-title {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 18px;
  }
  
  .detail-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    color: #666;
    font-size: 14px;
    margin-bottom: 20px;
  }
  
  .detail-content {
    background-color: #f9f9f9;
    padding: 15px;
    border-radius: 4px;
    white-space: pre-wrap;
    line-height: 1.6;
  }
}
</style> 