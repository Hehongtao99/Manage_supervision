<template>
  <div class="parent-notifications">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的通知</h2>
          <div>
            <el-button type="primary" @click="markAllAsRead" :disabled="!hasUnread">全部标为已读</el-button>
          </div>
        </div>
      </template>

      <div class="table-container">
        <el-empty v-if="!notifications.length" description="暂无通知"></el-empty>

        <el-tabs v-else v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="全部通知" name="all">
            <notification-list :notifications="filteredNotifications" @view="viewNotification" @refresh="fetchNotifications" />
          </el-tab-pane>
          <el-tab-pane label="未读通知" name="unread">
            <notification-list :notifications="filteredNotifications" @view="viewNotification" @refresh="fetchNotifications" />
          </el-tab-pane>
          <el-tab-pane label="系统通知" name="system">
            <notification-list :notifications="filteredNotifications" @view="viewNotification" @refresh="fetchNotifications" />
          </el-tab-pane>
          <el-tab-pane label="教师通知" name="teacher">
            <notification-list :notifications="filteredNotifications" @view="viewNotification" @refresh="fetchNotifications" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="dialogVisible" :title="selectedNotification?.title || '通知详情'" width="50%">
      <div v-if="selectedNotification" class="notification-detail">
        <div class="notification-header">
          <h4>{{ selectedNotification.title }}</h4>
          <div class="notification-meta">
            <span>发送者: {{ selectedNotification.sender }}</span>
            <span>发送时间: {{ formatDate(selectedNotification.createdAt) }}</span>
          </div>
        </div>
        <div class="notification-content" v-html="selectedNotification.content"></div>
        <div class="notification-attachments" v-if="selectedNotification.attachments && selectedNotification.attachments.length">
          <h5>附件</h5>
          <ul>
            <li v-for="(attachment, index) in selectedNotification.attachments" :key="index">
              <a :href="attachment.url" target="_blank">{{ attachment.name }}</a>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 通知列表组件
const NotificationList = defineComponent({
  props: {
    notifications: Array
  },
  emits: ['view', 'refresh'],
  setup(props, { emit }) {
    const viewNotification = (notification) => {
      emit('view', notification)
    }
    
    const markAsRead = async (notification) => {
      // 这里调用实际的标记已读API
      ElMessage.info('标记已读功能尚未实现')
      emit('refresh')
    }
    
    return () => (
      <el-table data={props.notifications} style="width: 100%" border>
        <el-table-column width="50">
          {{
            default: ({ row }) => (
              <div class="read-status">
                {!row.isRead && <el-badge is-dot />}
              </div>
            )
          }}
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="sender" label="发送者" width="150" />
        <el-table-column prop="type" label="类型" width="120">
          {{
            default: ({ row }) => (
              <el-tag type={row.type === 'system' ? 'danger' : row.type === 'teacher' ? 'primary' : 'info'}>
                {row.type === 'system' ? '系统通知' : row.type === 'teacher' ? '教师通知' : '其他通知'}
              </el-tag>
            )
          }}
        </el-table-column>
        <el-table-column prop="createdAt" label="发送时间" width="180" />
        <el-table-column label="操作" width="180">
          {{
            default: ({ row }) => (
              <div>
                <el-button type="primary" link onClick={() => viewNotification(row)}>查看</el-button>
                {!row.isRead && (
                  <el-button type="success" link onClick={() => markAsRead(row)}>标为已读</el-button>
                )}
              </div>
            )
          }}
        </el-table-column>
      </el-table>
    )
  }
})

// 示例通知数据
const notifications = ref([
  // 示例数据
  /* 
  {
    id: 1,
    title: '系统维护通知',
    content: '系统将于2023年4月10日晚上22:00-24:00进行维护升级，期间将无法访问系统。',
    sender: '系统管理员',
    type: 'system',
    isRead: false,
    createdAt: '2023-04-08 10:00:00',
    attachments: []
  },
  {
    id: 2,
    title: '班级作业通知',
    content: '请通知您的孩子按时完成今天布置的家庭作业。',
    sender: '李老师',
    type: 'teacher',
    isRead: true,
    createdAt: '2023-04-07 15:30:00',
    attachments: [
      { name: '作业详情.pdf', url: '#' }
    ]
  }
  */
])

const activeTab = ref('all')
const dialogVisible = ref(false)
const selectedNotification = ref(null)

// 计算属性：根据当前选择的标签页筛选通知
const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  } else if (activeTab.value === 'unread') {
    return notifications.value.filter(n => !n.isRead)
  } else {
    return notifications.value.filter(n => n.type === activeTab.value)
  }
})

// 计算是否有未读通知
const hasUnread = computed(() => {
  return notifications.value.some(n => !n.isRead)
})

// 处理标签页点击
const handleTabClick = () => {
  console.log('当前标签页:', activeTab.value)
}

// 查看通知详情
const viewNotification = (notification) => {
  selectedNotification.value = notification
  dialogVisible.value = true
  
  // 如果通知未读，标记为已读
  if (notification && !notification.isRead) {
    // 调用标记已读API
    notification.isRead = true
  }
}

// 标记所有为已读
const markAllAsRead = async () => {
  // 这里调用实际的标记全部已读API
  ElMessage.info('标记全部已读功能尚未实现')
  
  // 模拟全部标记为已读
  notifications.value.forEach(n => n.isRead = true)
}

// 加载通知数据
const fetchNotifications = async () => {
  // 这里调用实际的通知API
  console.log('获取通知列表')
}

// 格式化日期
const formatDate = (dateString) => {
  return dateString
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.parent-notifications {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.table-container {
  margin-top: 10px;
}

.read-status {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.notification-detail {
  padding: 10px;
}

.notification-header {
  margin-bottom: 20px;
}

.notification-header h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
}

.notification-content {
  padding: 15px 0;
  line-height: 1.6;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.notification-attachments {
  margin-top: 15px;
}

.notification-attachments h5 {
  margin: 0 0 10px 0;
}

.notification-attachments ul {
  list-style-type: none;
  padding-left: 0;
}

.notification-attachments li {
  margin-bottom: 5px;
}
</style> 