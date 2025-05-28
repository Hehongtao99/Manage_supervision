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

      <!-- 跑步爱好者信息卡片 - 只对普通用户显示 -->
      <el-col :span="24" v-if="userStore.isRunner">
        <el-card class="user-info-card">
          <div class="info-header">
            <el-avatar :size="60" :src="userStore.user.avatar" class="user-avatar">
              <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png" />
            </el-avatar>
            <div class="info-content">
              <h3>{{ userStore.user.name || userStore.user.username }}</h3>
              <p class="user-role">
                <el-tag type="success" size="small">
                  {{ userStore.user.roles.includes('ADMIN') ? '管理员' : userStore.user.roles.includes('USER') ? '跑步爱好者' : userStore.user.roles[0] }}
                </el-tag>
              </p>
              <p class="user-number">编号: {{ userStore.user.userNumber || '未设置' }}</p>
            </div>
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
                  :type="role === 'ADMIN' ? 'danger' : role === 'SUPERVISOR' ? 'warning' : 'success'"
                  style="margin-right: 5px"
                >
                  {{ role === 'USER' ? '跑步爱好者' : role === 'ADMIN' ? '管理员' : role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 角色分布图表 -->
    <el-row style="margin-top: 20px" v-if="userStore.isAdmin && hasRoleDistribution">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>角色分布</h3>
            </div>
          </template>
          <v-chart class="chart" :option="chartOption" autoresize />
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
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent, TitleComponent } from 'echarts/components'
import { 
  Calendar, 
  Document
} from '@element-plus/icons-vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent
])

const router = useRouter()
const userStore = useUserStore()
const stats = ref<DashboardStats>()

// 根据用户角色获取仪表盘标题
const getDashboardTitle = computed(() => {
  if (userStore.isAdmin) {
    return '系统控制台'
  } else {
    return '跑步爱好者首页'
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

// 图表配置
const chartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: Object.entries(stats.value?.roleDistribution || {}).map(([name, value]) => ({
        name,
        value
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

const hasRoleDistribution = computed(() => {
  return stats.value?.roleDistribution && Object.keys(stats.value.roleDistribution).length > 0
})

const adminCount = computed(() => {
  return stats.value?.userList?.filter(user => user.roles.includes('ADMIN')).length || 0
})

// 状态样式
const getStatusType = (status: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case '初稿': return 'primary'
    case '期中': return 'warning'
    case '终稿': return 'success'
    case '未开始': return 'primary'
    case '进行中': return 'warning'
    case '已完成': return 'success'
    case '已结项': return 'info'
    case '未审核': return 'warning'
    case '审核未通过': return 'danger'
    default: return 'primary'
  }
}

// 优先级样式
const getPriorityType = (priority: string): 'primary' | 'success' | 'warning' | 'danger' => {
  switch (priority) {
    case '高': return 'danger'
    case '中': return 'warning'
    case '低': return 'success'
    default: return 'warning'
  }
}

// 转换状态为中文
const getChineseStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'DELAYED': '已延期',
    'CANCELLED': '已取消'
  };
  return statusMap[status] || status;
}

onMounted(() => {
  fetchDashboardData()
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('Logged out successfully')
  router.push('/login')
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

/* 跑步爱好者信息卡片样式 */
.user-info-card {
  margin-bottom: 20px;
}

.info-header {
  display: flex;
  align-items: center;
  padding: 10px;
}

.user-avatar {
  margin-right: 10px;
}

.info-content {
  flex: 1;
}

.info-content h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.user-role {
  margin: 5px 0;
}

.user-number {
  margin: 5px 0;
  font-size: 14px;
  color: #909399;
}

/* 任务列表样式 */
.task-card {
  margin-bottom: 20px;
}

.task-list {
  padding: 10px;
}

.empty-tasks {
  padding: 20px;
  text-align: center;
}

.empty-tasks .sub-text {
  font-size: 12px;
  color: #909399;
}

.task-item {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-header h4 {
  margin: 0;
  font-size: 16px;
}

.task-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.task-time {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
}

.admin-actions {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-card__body) {
  padding: 15px;
}

.chart {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.card-actions {
  display: flex;
  gap: 8px;
}
</style> 