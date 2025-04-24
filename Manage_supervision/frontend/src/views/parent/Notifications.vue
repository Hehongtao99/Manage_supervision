<template>
  <div class="parent-notifications">
    <!-- 管理员发送通知按钮 -->
    <el-button 
      v-if="isAdmin" 
      type="primary" 
      @click="openSendDialog" 
      style="position: absolute; top: 20px; right: 20px;"
    >
      发送通知给所有家长
    </el-button>

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
            <notification-table 
              :notifications="filteredNotifications" 
              @view="viewNotification" 
              @refresh="fetchNotifications" 
            />
          </el-tab-pane>
          <el-tab-pane label="未读通知" name="unread">
            <notification-table 
              :notifications="filteredNotifications" 
              @view="viewNotification" 
              @refresh="fetchNotifications" 
            />
          </el-tab-pane>
          <el-tab-pane label="系统通知" name="system">
            <notification-table 
              :notifications="filteredNotifications" 
              @view="viewNotification" 
              @refresh="fetchNotifications" 
            />
          </el-tab-pane>
          <el-tab-pane label="教师通知" name="teacher">
            <notification-table 
              :notifications="filteredNotifications" 
              @view="viewNotification" 
              @refresh="fetchNotifications" 
            />
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

    <!-- 管理员发送通知对话框 -->
    <el-dialog v-if="isAdmin" v-model="sendDialogVisible" title="发送通知给所有家长" width="50%">
      <el-form :model="notificationForm" label-width="100px">
        <el-form-item label="通知标题" required>
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="notificationForm.type" placeholder="请选择通知类型">
            <el-option label="系统通知" value="system"></el-option>
            <el-option label="教师通知" value="teacher"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input v-model="notificationForm.content" type="textarea" rows="10" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            action="/api/upload"
            :on-remove="handleFileRemove"
            :on-success="handleFileSuccess"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持任意类型文件，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="sendDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="sendNotificationToAllParents">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { ElMessage, ElTable, ElTableColumn, ElTag, ElButton, ElBadge } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 使用普通函数组件替代JSX
const NotificationTable = defineComponent({
  name: 'NotificationTable',
  props: {
    notifications: {
      type: Array,
      default: () => []
    }
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
    
    return () => {
      return h(ElTable, { 
        data: props.notifications, 
        style: 'width: 100%',
        border: true 
      }, {
        default: () => [
          h(ElTableColumn, { width: 50 }, {
            default: ({ row }) => h('div', { class: 'read-status' }, [
              !row.isRead ? h(ElBadge, { isDot: true }) : null
            ])
          }),
          h(ElTableColumn, { prop: 'title', label: '标题' }),
          h(ElTableColumn, { prop: 'sender', label: '发送者', width: 150 }),
          h(ElTableColumn, { prop: 'type', label: '类型', width: 120 }, {
            default: ({ row }) => h(ElTag, { 
              type: row.type === 'system' ? 'danger' : row.type === 'teacher' ? 'primary' : 'info' 
            }, () => row.type === 'system' ? '系统通知' : row.type === 'teacher' ? '教师通知' : '其他通知')
          }),
          h(ElTableColumn, { prop: 'createdAt', label: '发送时间', width: 180 }),
          h(ElTableColumn, { label: '操作', width: 180 }, {
            default: ({ row }) => h('div', {}, [
              h(ElButton, { 
                type: 'primary', 
                link: true,
                onClick: () => viewNotification(row)
              }, () => '查看'),
              !row.isRead ? h(ElButton, { 
                type: 'success', 
                link: true,
                onClick: () => markAsRead(row)
              }, () => '标为已读') : null
            ])
          })
        ]
      })
    }
  }
})

// 用户store，用于获取当前用户角色
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'admin')

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

// 新增：发送通知对话框
const sendDialogVisible = ref(false)
const notificationForm = ref({
  title: '',
  content: '',
  type: 'system',
  attachments: []
})

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
  try {
    const response = await fetch('/api/notifications/mark-all-read', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'userId': userStore.userInfo.id
      }
    });
    
    const result = await response.json();
    if (result.success) {
      ElMessage.success('所有通知已标记为已读');
      // 刷新通知列表
      fetchNotifications();
    } else {
      ElMessage.error(result.message || '标记已读失败');
    }
  } catch (error) {
    console.error('标记已读出错:', error);
    ElMessage.error('标记已读失败，请稍后重试');
  }
}

// 加载通知数据
const fetchNotifications = async () => {
  try {
    // 根据用户角色调用不同的API端点
    let url = '/api/parent/notifications';
    
    if (isAdmin.value) {
      // 管理员可以查看所有通知
      url = '/api/notifications/all';
    }
    
    // 使用Axios发送请求时添加认证头
    const token = localStorage.getItem('token');
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'userId': localStorage.getItem('userId') || userStore.userInfo?.id?.toString() || ''
      }
    });
    
    const result = await response.json();
    if (result.success) {
      notifications.value = result.data || [];
    } else {
      ElMessage.warning(result.message || '获取通知失败');
      notifications.value = [];
    }
  } catch (error) {
    console.error('获取通知列表出错:', error);
    ElMessage.error('获取通知列表失败，请稍后重试');
    notifications.value = [];
  }
}

// 格式化日期
const formatDate = (dateString) => {
  return dateString
}

// 新增：管理员发送通知给所有家长
const openSendDialog = () => {
  sendDialogVisible.value = true
}

// 新增：发送通知给所有家长
const sendNotificationToAllParents = async () => {
  // 表单验证
  if (!notificationForm.value.title.trim()) {
    ElMessage.error('请输入通知标题');
    return;
  }
  
  if (!notificationForm.value.content.trim()) {
    ElMessage.error('请输入通知内容');
    return;
  }
  
  try {
    // 调用API发送通知
    const response = await fetch('/api/notifications/send-to-all-parents', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'userId': userStore.userInfo.id // 从用户store中获取ID
      },
      body: JSON.stringify({
        title: notificationForm.value.title,
        content: notificationForm.value.content,
        recipientType: notificationForm.value.type,
        attachments: notificationForm.value.attachments
      })
    });
    
    const result = await response.json();
    if (result.success) {
      ElMessage.success('通知已成功发送给所有家长');
      sendDialogVisible.value = false;
      // 重置表单
      notificationForm.value = {
        title: '',
        content: '',
        type: 'system',
        attachments: []
      };
      fileList.value = [];
      // 刷新通知列表
      fetchNotifications();
    } else {
      ElMessage.error(result.message || '发送通知失败');
    }
  } catch (error) {
    console.error('发送通知出错:', error);
    ElMessage.error('发送通知失败，请稍后重试');
  }
}

// 文件上传相关
const fileList = ref([])
const handleFileRemove = (file) => {
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
}

const handleFileSuccess = (response, file, fileList) => {
  notificationForm.value.attachments.push({
    name: file.name,
    url: response.url
  })
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