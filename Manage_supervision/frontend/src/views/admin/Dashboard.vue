<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { getUserCreationTrend, getCourseCount, getCourseCategoryDistribution } from '../../api/dashboard'
import type { CourseDistribution } from '../../api/dashboard'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const loading = ref(true)
const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalRoles: 0,
  systemHealth: '正常'
})

// 课程统计数据
const courseStatistics = ref({
  totalCourses: 0
})

// 用户角色分布数据
const userRoleDistribution = ref([
  { value: 0, name: '管理员' },
  { value: 0, name: '教师' },
  { value: 0, name: '学生' },
  { value: 0, name: '普通用户' }
])

// 课程分类分布数据
const courseCategoryDistribution = ref<CourseDistribution[]>([])

// 近7天用户创建趋势数据
const userCreationData = ref({
  dates: [],
  counts: []
})

// 角色分布饼图配置
const pieChartOption = computed(() => ({
  title: {
    text: '用户角色分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: userRoleDistribution.value.map(item => item.name)
  },
  series: [
    {
      name: '角色分布',
      type: 'pie',
      radius: ['40%', '70%'],
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
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: userRoleDistribution.value
    }
  ]
}))

// 课程分类分布饼图配置
const coursePieChartOption = computed(() => ({
  title: {
    text: '课程分类分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: courseCategoryDistribution.value.map(item => item.name)
  },
  series: [
    {
      name: '课程分类',
      type: 'pie',
      radius: ['40%', '70%'],
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
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: courseCategoryDistribution.value
    }
  ]
}))

// 用户创建趋势折线图配置
const lineChartOption = computed(() => ({
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
    minInterval: 1,
    axisLabel: {
      formatter: '{value}'
    }
  },
  series: [
    {
      name: '创建用户数',
      type: 'line',
      smooth: true,
      data: userCreationData.value.counts,
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ]
        }
      }
    }
  ]
}))

const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 1. 获取总体统计数据
    const statsResponse = await axios.get('/api/admin/statistics')
    statistics.value = statsResponse.data

    // 2. 获取角色分布数据
    const roleDistResponse = await axios.get('/api/admin/statistics/role-distribution')
    userRoleDistribution.value = roleDistResponse.data

    // 3. 获取近7天用户创建趋势数据
    try {
      const creationTrendData = await getUserCreationTrend()
      userCreationData.value = creationTrendData
    } catch (error) {
      console.error('获取用户创建趋势数据失败:', error)
      // 显示错误提示而不是使用模拟数据
      ElMessage.error('获取用户创建趋势数据失败，请稍后再试')
      userCreationData.value = {
        dates: [],
        counts: []
      }
    }
    
    // 4. 获取课程统计数据
    try {
      const totalCourses = await getCourseCount()
      courseStatistics.value.totalCourses = totalCourses
      
      const categoryDist = await getCourseCategoryDistribution()
      courseCategoryDistribution.value = categoryDist
    } catch (error) {
      console.error('获取课程统计数据失败:', error)
      ElMessage.error('获取课程统计数据失败，请稍后再试')
    }
  } catch (error) {
    console.error('获取控制台数据失败:', error)
    ElMessage.error('获取控制台数据失败，请稍后再试')
    loading.value = false
  } finally {
    loading.value = false
  }
}

// 修改模拟数据方法，保留必要的统计数据初始化但移除创建趋势的模拟
const mockDashboardData = () => {
  statistics.value = {
    totalUsers: 0,
    activeUsers: 0,
    totalRoles: 0,
    systemHealth: '正常'
  }
  
  userRoleDistribution.value = []
  
  // 不再模拟用户创建趋势数据
  userCreationData.value = {
    dates: [],
    counts: []
  }
}

onMounted(async () => {
  fetchDashboardData()
})
</script>

<template>
  <div class="admin-dashboard">
    <h1 class="dashboard-title">管理控制台</h1>
    
    <el-row :gutter="20" v-loading="loading">
      <!-- 用户统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalUsers }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.activeUsers }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总角色数</span>
            </div>
          </template>
          <div class="stat-value">{{ statistics.totalRoles }}</div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总课程数</span>
            </div>
          </template>
          <div class="stat-value">{{ courseStatistics.totalCourses }}</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <!-- 用户角色分布图 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover" class="chart-card">
          <v-chart class="chart" :option="pieChartOption" autoresize />
        </el-card>
      </el-col>
      
      <!-- 课程分类分布图 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover" class="chart-card">
          <v-chart class="chart" :option="coursePieChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row>
      <!-- 用户创建趋势图 -->
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card line-chart-card">
          <v-chart class="chart" :option="lineChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row>
      <el-col :span="24">
        <el-card shadow="hover" class="system-info-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
              <el-tag type="success" v-if="statistics.systemHealth === '正常'">{{ statistics.systemHealth }}</el-tag>
              <el-tag type="warning" v-else-if="statistics.systemHealth === '警告'">{{ statistics.systemHealth }}</el-tag>
              <el-tag type="danger" v-else>{{ statistics.systemHealth }}</el-tag>
            </div>
          </template>
          <div class="system-info">
            <p>上次刷新时间: {{ new Date().toLocaleString() }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.dashboard-title {
  margin-bottom: 20px;
  font-weight: 500;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.line-chart-card {
  height: 400px;
}

.chart {
  height: 300px;
}

.system-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-info {
  color: #606266;
}
</style> 