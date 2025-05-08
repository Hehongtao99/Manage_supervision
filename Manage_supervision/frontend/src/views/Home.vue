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
              <h3>个人学习概览</h3>
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
                    <div class="info-value">{{ studentStats.totalTasks }}</div>
                    <div class="info-label">任务总数</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-card">
                  <div class="info-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="info-content">
                    <div class="info-value">{{ studentStats.completedTasks }}</div>
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
                  :type="role === 'ADMIN' ? 'danger' : role === 'SUPERVISOR' ? 'warning' : 'success'"
                  style="margin-right: 5px"
                >
                  {{ role === 'USER' ? '学生' : role === 'ADMIN' ? '管理员' : '教师' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生任务概览 - 只对学生显示 -->
    <el-row style="margin-top: 20px" v-if="userStore.isStudent">
      <el-col :span="24">
        <el-card class="task-card">
          <template #header>
            <div class="card-header">
              <h3>我的任务</h3>
              <el-button type="primary" size="small" @click="$router.push('/task-list')">查看所有任务</el-button>
            </div>
          </template>
          <div class="task-list" v-loading="tasksLoading">
            <div v-if="myTasks.length === 0" class="empty-tasks">
              <el-empty description="没有分配给您的任务" :image-size="80">
                <template #description>
                  <p>暂无分配给您的任务</p>
                  <p class="sub-text">任务将由教师分配，请耐心等待</p>
                </template>
              </el-empty>
            </div>
            <div v-else>
              <div v-for="(task, index) in myTasks" :key="index" class="task-item">
                <div class="task-header">
                  <h4>{{ task.title }}</h4>
                  <el-tag :type="getStatusType(task.status)" size="small">{{ getChineseStatus(task.status) }}</el-tag>
                </div>
                <div class="task-info">
                  <span class="task-supervisor">教师: {{ task.supervisorName || '未分配' }}</span>
                  <span class="task-priority" v-if="task.category">
                    类别: 
                    <el-tag type="info" size="small">{{ task.category }}</el-tag>
                  </span>
                </div>
                <div class="task-time">
                  <span v-if="task.endTime">截止日期: {{ task.endTime }}</span>
                </div>
                <div class="task-actions">
                  <el-button type="primary" size="small" @click="$router.push('/task-list')">查看详情</el-button>
                </div>
              </div>
            </div>
          </div>
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

    <!-- 用户创建趋势图表 -->
    <el-row style="margin-top: 20px" v-if="userStore.isAdmin">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>近7天创建用户趋势</h3>
            </div>
          </template>
          <v-chart class="chart" :option="userCreationChartOption" autoresize />
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
import { getDashboardStats, getUserCreationTrend, type DashboardStats } from '../api/dashboard'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent, TitleComponent, GridComponent } from 'echarts/components'
import { 
  Calendar, 
  Document
} from '@element-plus/icons-vue'
import { getMyTasks } from '../api/task'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  GridComponent
])

const router = useRouter()
const userStore = useUserStore()
const stats = ref<DashboardStats>()

// 学生统计数据
const studentStats = ref({
  totalTasks: 0,
  completedTasks: 0
})

// 学生任务数据
const myTasks = ref([])
const tasksLoading = ref(false)

// 近7天用户创建趋势数据
const userCreationData = ref({
  dates: [],
  counts: []
})

// 根据用户角色获取仪表盘标题
const getDashboardTitle = computed(() => {
  if (userStore.isAdmin) {
    return '系统控制台'
  } else if (userStore.isSupervisor) {
    return '督导工作台'
  } else {
    return '学生首页'
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

// 获取学生任务数据
const fetchMyTasks = async () => {
  if (userStore.isStudent) {
    tasksLoading.value = true
    try {
      const tasks = await getMyTasks()
      myTasks.value = tasks.slice(0, 3) // 只显示前3个任务
      updateTaskStats(tasks) // 更新任务统计数据
    } catch (error) {
      console.error('Failed to get task data:', error)
    } finally {
      tasksLoading.value = false
    }
  }
}

// 更新任务统计数据
const updateTaskStats = (tasks: any[]) => {
  if (tasks && tasks.length > 0) {
    studentStats.value.totalTasks = tasks.length
    studentStats.value.completedTasks = tasks.filter(task => task.status === '已完成').length
  } else {
    // 如果没有任务数据，设置为0
    studentStats.value.totalTasks = 0
    studentStats.value.completedTasks = 0
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
    left: 'left',
    formatter: function(name) {
      // 将USER角色显示为STUDENT
      return name === 'USER' ? 'STUDENT' : name;
    }
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: Object.entries(stats.value?.roleDistribution || {}).map(([name, value]) => ({
        // 将USER角色显示为STUDENT
        name: name === 'USER' ? 'STUDENT' : name,
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

// 用户创建趋势图表配置
const userCreationChartOption = computed(() => ({
  title: {
    text: '近7天创建用户趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: userCreationData.value.dates
  },
  yAxis: {
    type: 'value',
    minInterval: 1, // 确保Y轴间隔至少为1
    axisLabel: {
      formatter: '{value}' // 格式化为整数
    }
  },
  series: [
    {
      name: '创建用户数',
      type: 'line',
      smooth: true,
      data: userCreationData.value.counts,
      itemStyle: {
        color: '#67C23A'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ]
        }
      }
    }
  ]
}))

// 获取近7天用户创建趋势数据
const fetchUserCreationData = async () => {
  if (userStore.isAdmin) {
    try {
      const data = await getUserCreationTrend()
      userCreationData.value = data
    } catch (error) {
      console.error('获取用户创建趋势数据失败:', error)
      // 显示错误状态而不是使用模拟数据
      ElMessage.error('获取用户创建趋势数据失败，请稍后再试')
      userCreationData.value = {
        dates: [],
        counts: []
      }
    }
  }
}

onMounted(() => {
  fetchDashboardData()
  fetchMyTasks()
  fetchUserCreationData() // 获取用户创建趋势数据
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