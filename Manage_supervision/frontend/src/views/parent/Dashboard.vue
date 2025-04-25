<template>
  <div class="parent-dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <h2>欢迎，{{ username }}</h2>
              <el-tag type="success">家长</el-tag>
            </div>
          </template>
          <div class="card-content">
            <p>欢迎访问家长控制台，您可以在这里查看孩子的学习进度、课程安排和成绩情况。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>子女信息</h3>
              <el-button type="primary" size="small" @click="navigateToChildManagement">管理</el-button>
            </div>
          </template>
          <div class="card-content">
            <el-empty description="暂无子女信息" v-if="!children.length"></el-empty>
            <div v-else class="child-list">
              <div v-for="child in children" :key="child.id" class="child-item">
                <el-avatar :size="40" :src="child.avatar || defaultAvatar">{{ child.name ? child.name.substring(0, 1) : 'U' }}</el-avatar>
                <div class="child-info">
                  <h4>{{ child.name }}</h4>
                  <div class="child-details">
                    <span v-if="child.className">班级：{{ child.className }}</span>
                    <span v-else>未分配班级</span>
                    <span>关系：{{ child.relationType }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>通知公告</h3>
              <el-button type="primary" size="small" @click="navigateToNotifications">更多</el-button>
            </div>
          </template>
          <div class="card-content">
            <el-empty description="暂无通知" v-if="!notifications.length"></el-empty>
            <div v-else class="notification-list">
              <div v-for="notification in notifications" :key="notification.id" class="notification-item">
                <div class="notification-title">
                  <el-icon><Bell /></el-icon>
                  <span>{{ notification.title }}</span>
                </div>
                <div class="notification-content">{{ notification.content }}</div>
                <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import axiosInstance from '../../utils/axios'

const userStore = useUserStore()
const router = useRouter()
const username = ref(userStore.user.realName || userStore.user.username || '家长用户')

// 数据状态
const children = ref([])
const notifications = ref([])
const defaultAvatar = ref('/assets/logo.png')

const navigateToChildManagement = () => {
  router.push('/parent/children')
}

const navigateToNotifications = () => {
  router.push('/parent/notifications')
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const fetchDashboardData = async () => {
  try {
    const response = await axiosInstance.get('/api/parent/dashboard')
    console.log('Dashboard API response:', response.data)
    
    if (response.data) {
      children.value = Array.isArray(response.data.children) ? response.data.children : []
      notifications.value = Array.isArray(response.data.notifications) ? response.data.notifications : []
      
      console.log('Children data:', children.value)
      console.log('Notifications data:', notifications.value)
    }
  } catch (error) {
    console.error('获取家长仪表盘数据失败:', error)
    children.value = []
    notifications.value = []
  }
}

onMounted(async () => {
  await fetchDashboardData()
})
</script>

<style scoped>
.parent-dashboard {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.dashboard-card {
  height: 350px;
  overflow: auto;
}

.card-content {
  min-height: 280px;
  display: flex;
  flex-direction: column;
}

.child-list, .notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.child-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.child-info {
  flex: 1;
}

.child-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.child-details {
  display: flex;
  flex-direction: column;
  font-size: 14px;
  color: #666;
}

.notification-item {
  padding: 12px;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  margin-bottom: 5px;
}

.notification-content {
  font-size: 14px;
  margin-bottom: 8px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style> 