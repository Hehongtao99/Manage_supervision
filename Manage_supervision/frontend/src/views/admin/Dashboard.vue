<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from '../../utils/axios'
// 引入echarts相关组件
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent, TitleComponent, GridComponent } from 'echarts/components'
// 引入数字滚动动画组件
import CountTo from '../../components/CountTo.vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  GridComponent
])

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalRoles: 0,
  systemHealth: '正常',
  totalStudents: 0,
  totalTeachers: 0,
  totalClasses: 0,
  totalNotifications: 0,
  // 新增饼图数据
  userRoleDistribution: {},
  classDistribution: {}
})

const loading = ref(true)

// 定义卡片图标
const cardIcons = {
  totalUsers: 'User',
  activeUsers: 'View',
  totalRoles: 'Avatar',
  systemHealth: 'Monitor',
  totalStudents: 'Reading',
  totalTeachers: 'Opportunity',
  totalClasses: 'School',
  totalNotifications: 'Bell'
}

// 定义卡片颜色
const cardColors = {
  totalUsers: '#409EFF',
  activeUsers: '#67C23A',
  totalRoles: '#E6A23C',
  systemHealth: '#F56C6C',
  totalStudents: '#909399',
  totalTeachers: '#6366f1',
  totalClasses: '#ec4899',
  totalNotifications: '#14b8a6'
}

const fetchDashboardData = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/dashboard/admin/stats')
    console.log('管理员仪表盘数据:', response.data)
    
    if (response.data) {
      statistics.value = {
        totalUsers: response.data.totalUsers || 0,
        activeUsers: response.data.activeUsers || 0,
        totalRoles: response.data.totalRoles || 0,
        systemHealth: response.data.systemStatus || '正常',
        totalStudents: response.data.totalStudents || 0,
        totalTeachers: response.data.totalTeachers || 0,
        totalClasses: response.data.totalClasses || 0,
        totalNotifications: response.data.totalNotifications || 0,
        // 用户角色分布数据 - 如果后端没有提供，则使用模拟数据
        userRoleDistribution: response.data.roleDistribution || {
          '管理员': statistics.value.totalRoles || 4,
          '教师': statistics.value.totalTeachers || 0,
          '学生': statistics.value.totalStudents || 0,
          '家长': statistics.value.totalUsers - statistics.value.totalTeachers - statistics.value.totalRoles || 3
        },
        // 班级分布数据 - 如果后端没有提供，则使用模拟数据
        classDistribution: response.data.classDistribution || {
          '一年级': Math.round(Math.random() * 5) + 1,
          '二年级': Math.round(Math.random() * 5) + 1,
          '三年级': Math.round(Math.random() * 5) + 1
        }
      }
    }
  } catch (error) {
    console.error('获取管理员仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 用户角色分布图表配置
const userRoleChartOption = computed(() => ({
  title: {
    text: '用户角色分布',
    left: 'center',
    top: '10px',
    textStyle: {
      fontSize: 16,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle'
  },
  series: [
    {
      type: 'pie',
      radius: ['50%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '16',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: Object.entries(statistics.value.userRoleDistribution).map(([name, value]) => ({
        name,
        value
      }))
    }
  ]
}))

// 班级分布图表配置
const classChartOption = computed(() => ({
  title: {
    text: '班级分布',
    left: 'center',
    top: '10px',
    textStyle: {
      fontSize: 16,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: Object.entries(statistics.value.classDistribution).map(([name, value]) => ({
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

onMounted(async () => {
  await fetchDashboardData()
})
</script>

<template>
  <div class="admin-dashboard-container">
    <h1 class="dashboard-title">管理控制台</h1>
    
    <el-scrollbar class="dashboard-scrollbar" :always="true">
      <div class="admin-dashboard">
        <el-row v-loading="loading" :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#f0f5ff' }">
              <template #header>
                <div class="card-header">
                  <span>总用户数</span>
                  <el-icon :color="cardColors.totalUsers"><el-icon-user /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalUsers }">
                <count-to :start-val="0" :end-val="statistics.totalUsers" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#f0fff0' }">
              <template #header>
                <div class="card-header">
                  <span>活跃用户</span>
                  <el-icon :color="cardColors.activeUsers"><el-icon-view /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.activeUsers }">
                <count-to :start-val="0" :end-val="statistics.activeUsers" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#fffbf0' }">
              <template #header>
                <div class="card-header">
                  <span>角色数量</span>
                  <el-icon :color="cardColors.totalRoles"><el-icon-avatar /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalRoles }">
                <count-to :start-val="0" :end-val="statistics.totalRoles" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#fff0f0' }">
              <template #header>
                <div class="card-header">
                  <span>系统状态</span>
                  <el-icon :color="cardColors.systemHealth"><el-icon-monitor /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.systemHealth }">
                {{ statistics.systemHealth }}
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 新增的统计信息行 -->
        <el-row :gutter="20" class="second-row">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#f5f5f5' }">
              <template #header>
                <div class="card-header">
                  <span>学生总数</span>
                  <el-icon :color="cardColors.totalStudents"><el-icon-reading /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalStudents }">
                <count-to :start-val="0" :end-val="statistics.totalStudents" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#f0f0ff' }">
              <template #header>
                <div class="card-header">
                  <span>教师总数</span>
                  <el-icon :color="cardColors.totalTeachers"><el-icon-opportunity /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalTeachers }">
                <count-to :start-val="0" :end-val="statistics.totalTeachers" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#fff0f9' }">
              <template #header>
                <div class="card-header">
                  <span>班级总数</span>
                  <el-icon :color="cardColors.totalClasses"><el-icon-school /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalClasses }">
                <count-to :start-val="0" :end-val="statistics.totalClasses" :duration="2000" />
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card" :body-style="{ 'background-color': '#f0fffa' }">
              <template #header>
                <div class="card-header">
                  <span>通知总数</span>
                  <el-icon :color="cardColors.totalNotifications"><el-icon-bell /></el-icon>
                </div>
              </template>
              <div class="stat-value" :style="{ color: cardColors.totalNotifications }">
                <count-to :start-val="0" :end-val="statistics.totalNotifications" :duration="2000" />
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 新增图表行 -->
        <el-row :gutter="20" class="chart-row">
          <el-col :xs="24" :sm="12">
            <el-card class="chart-card">
              <v-chart class="chart" :option="userRoleChartOption" autoresize />
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12">
            <el-card class="chart-card">
              <v-chart class="chart" :option="classChartOption" autoresize />
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-scrollbar>
  </div>
</template>

<style scoped>
.admin-dashboard-container {
  height: 100vh;
  overflow: hidden;
  padding: 0;
  box-sizing: border-box;
  position: relative;
}

.admin-dashboard {
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
  position: relative;
}

.dashboard-scrollbar {
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.dashboard-title {
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
}

.stat-card {
  margin-bottom: 20px;
  height: 140px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.chart {
  height: 360px;
}

.stat-value {
  font-size: 42px;
  font-weight: 600;
  color: #409EFF;
  text-align: center;
  margin-top: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  height: 60px;
  line-height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 10px;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
  background: linear-gradient(135deg, #f5f7fa 0%, #ebf0f6 100%);
  border-radius: 0 0 8px 8px;
}

.second-row, .chart-row {
  margin-top: 10px;
}

/* 确保滑动条显示 */
:deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
  overflow-y: auto;
}

:deep(.el-scrollbar__bar.is-vertical) {
  width: 8px;
  right: 0;
  opacity: 1;
}

:deep(.el-scrollbar__bar.is-horizontal) {
  height: 8px;
  bottom: 0;
  opacity: 1;
}
</style> 