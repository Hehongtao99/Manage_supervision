<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <h3>欢迎使用学生端</h3>
            </div>
          </template>
          <div class="welcome-content">
            <el-row :gutter="20" class="info-row">
              <el-col :span="16">
                <div class="welcome-message">
                  <h2>{{ greeting }}，{{ userStore.user.name || '同学' }}</h2>
                  <p>今天是 {{ today }}，祝您学习愉快！</p>
                  <p>本系统可以帮助您管理学习任务、查看课表、接收通知等。</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="user-avatar">
                  <el-avatar :size="100" :src="userStore.user.avatar || defaultAvatar"></el-avatar>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <h3>最近通知</h3>
              <el-button text @click="goToNotifications">所有通知</el-button>
            </div>
          </template>
          <div v-if="recentNotifications.length > 0" class="card-content">
            <div v-for="(notification, index) in recentNotifications" :key="index" class="notification-item">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无通知"></el-empty>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <h3>考勤签到</h3>
              <el-button text @click="goToAttendance">查看考勤</el-button>
            </div>
          </template>
          <div class="card-content attendance-summary">
            <div class="attendance-status">
              <div v-if="canCheckIn" class="attendance-action">
                <p class="attendance-tip">今日尚未签到，请点击下方按钮完成签到</p>
                <el-button type="primary" :loading="checkingIn" @click="checkIn">今日签到</el-button>
              </div>
              <div v-else class="attended-notice">
                <el-icon class="attendance-icon"><Check /></el-icon>
                <span class="attendance-text">今日已完成签到</span>
              </div>
            </div>
            <div v-if="loadingAttendance" class="loading-container">
              <el-skeleton :rows="2" animated />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { format } from 'date-fns'
import { ElMessage } from 'element-plus'
import { getReceivedNotifications, NotificationResponse } from '../../api/notification'
import axios from '../../utils/axios'
import { Check } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 设置默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 日期和欢迎语
const today = computed(() => format(new Date(), 'yyyy年MM月dd日'))
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 最近通知数据
const recentNotifications = ref<NotificationResponse[]>([])
const loading = ref(false)

// 考勤状态
const canCheckIn = ref(false)
const checkingIn = ref(false)
const loadingAttendance = ref(false)

// 页面跳转方法
const goToNotifications = () => {
  router.push('/student/notifications')
}

const goToAttendance = () => {
  router.push('/student/attendance')
}

// 获取最近通知
const fetchRecentNotifications = async () => {
  loading.value = true
  try {
    const response = await getReceivedNotifications()
    if (response.data && response.data.success) {
      // 只取最近3条通知
      recentNotifications.value = response.data.data.slice(0, 3)
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    ElMessage.error('获取通知数据失败')
  } finally {
    loading.value = false
  }
}

// 获取考勤状态
const fetchAttendanceStatus = async () => {
  loadingAttendance.value = true
  try {
    const response = await axios.get('/api/attendance/student-records')
    if (response.data && response.data.success) {
      // 只获取今日是否已签到的状态
      canCheckIn.value = !response.data.attendedToday
    }
  } catch (error) {
    console.error('获取考勤状态失败:', error)
    ElMessage.error('获取考勤状态失败')
  } finally {
    loadingAttendance.value = false
  }
}

// 签到方法
const checkIn = async () => {
  checkingIn.value = true
  try {
    // 获取第一个班级ID（实际情况可能需要选择）
    const classResponse = await axios.get('/api/student/my-class')
    if (!classResponse.data || !classResponse.data.classes || classResponse.data.classes.length === 0) {
      ElMessage.error('您没有关联的班级，无法签到')
      return
    }
    
    const classId = classResponse.data.classes[0].id
    
    // 执行签到请求
    const response = await axios.post('/api/attendance/check-in', { classId })
    
    if (response.data && response.data.success) {
      ElMessage.success('签到成功！')
      canCheckIn.value = false
    } else {
      ElMessage.error(response.data.message || '签到失败，请重试')
    }
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败，请重试')
  } finally {
    checkingIn.value = false
  }
}

// 格式化时间显示
const formatTime = (timestamp: string) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1小时，显示x分钟前
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return `${minutes}分钟前`
  }
  
  // 小于24小时，显示x小时前
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  
  // 否则显示年月日
  return format(date, 'yyyy-MM-dd')
}

onMounted(() => {
  fetchRecentNotifications()
  fetchAttendanceStatus()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  padding: 10px 0;
}

.info-row {
  display: flex;
  align-items: center;
}

.welcome-message h2 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.welcome-message p {
  margin: 5px 0;
  color: #606266;
}

.user-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.feature-card {
  height: 100%;
}

.card-content {
  min-height: 200px;
}

.notification-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.attendance-summary {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.attendance-status {
  width: 100%;
}

.attendance-tip {
  color: #606266;
  margin-bottom: 20px;
}

.attendance-action {
  margin-top: 20px;
}

.attended-notice {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.attendance-icon {
  font-size: 48px;
  color: #67C23A;
  margin-bottom: 16px;
}

.attendance-text {
  font-size: 18px;
  color: #67C23A;
  font-weight: bold;
}

.loading-container {
  padding: 20px;
}
</style> 