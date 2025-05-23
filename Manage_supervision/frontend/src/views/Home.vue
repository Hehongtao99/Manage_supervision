<template>
  <div class="dashboard-container">
    <div class="header">
      <h1>{{ getDashboardTitle }}</h1>
      <div class="user-actions">
        <span class="welcome-text">欢迎回来, {{ userStore.user.username }}</span>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 用户统计卡片 - 只对管理员显示 -->
      <el-col :span="24" v-if="userStore.isAdmin">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <h3>用户统计</h3>
            </div>
          </template>
          <div class="stat-content">
            <el-row>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value primary">{{ stats?.totalUsers || 0 }}</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value success">{{ adminCount }}</div>
                  <div class="stat-label">管理员数量</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>

      <!-- 学生信息卡片 - 只对普通学生显示 -->
      <el-col :span="24" v-if="userStore.isStudent">
        <el-card class="student-card">
          <template #header>
            <div class="card-header">
              <h3>个人概览</h3>
            </div>
          </template>
          <div class="student-info">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon><Calendar /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-value">0</div>
                    <div class="info-label">总任务数</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-value">0</div>
                    <div class="info-label">已完成任务</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户详情表格 - 只对管理员显示 -->
    <el-row style="margin-top: 20px" v-if="userStore.isAdmin">
      <el-col :span="24">
        <el-card class="user-list-card">
          <template #header>
            <div class="card-header">
              <h3>用户详情</h3>
            </div>
          </template>
          <el-table :data="stats?.userList || []" style="width: 100%" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="180" />
            <el-table-column label="角色" width="180">
              <template #default="scope">
                <el-tag
                  v-for="role in scope.row.roles"
                  :key="role"
                  :type="getRoleTagType(role)"
                  style="margin-right: 5px"
                >
                  {{ getRoleDisplayName(role) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理员入口 -->
    <div class="admin-actions" v-if="userStore.isAdmin">
      <el-button type="primary" @click="$router.push('/admin/users')">进入管理面板</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDashboardStats, type DashboardStats } from '../api/dashboard'
import { 
  Calendar, 
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const stats = ref<DashboardStats>()

// 根据用户角色获取仪表盘标题
const getDashboardTitle = computed(() => {
  if (userStore.isAdmin) {
    return '系统控制台'
  } else {
    return '用户首页'
  }
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    stats.value = await getDashboardStats()
  } catch (error: any) {
    console.error('Failed to get dashboard data:', error)
    if (error.response?.status === 403) {
      ElMessage.error('No permission to access this data')
    } else if (error.response?.status === 401) {
      ElMessage.error('Login expired, please login again')
      userStore.handleAuthError()
    } else {
      ElMessage.error(error.response?.data?.message || 'Failed to get dashboard data')
    }
  }
}

onMounted(() => {
  fetchDashboardData()
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('Logged out successfully')
  router.push('/login')
}

// 新增的函数
const getRoleTagType = (role: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    'ADMIN_END': 'danger',
    'MERCHANT_END': 'warning',
    'CITYIN_END': 'primary',
    'ENTERPRISE_END': 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleDisplayName = (role: string): string => {
  const roleMap: Record<string, string> = {
    'ADMIN_END': '管理员端',
    'MERCHANT_END': '商家端',
    'CITYIN_END': '城投端',
    'ENTERPRISE_END': '企业端'
  }
  return roleMap[role] || role
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 32px);
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-text {
  font-size: 16px;
}

.stat-card {
  height: 200px;
  display: flex;
  flex-direction: column;
}

.stat-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-value.success {
  color: #67C23A;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.chart-card, .user-list-card {
  margin-top: 20px;
}

/* 学生信息卡片样式 */
.student-card {
  margin-bottom: 20px;
}

.student-info {
  padding: 10px;
}

.info-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.info-icon {
  font-size: 30px;
  color: #409EFF;
  margin-right: 15px;
}

.info-content {
  flex: 1;
}

.info-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.info-label {
  color: #606266;
  font-size: 14px;
}

.admin-actions {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-card__body) {
  padding: 15px;
}
</style> 