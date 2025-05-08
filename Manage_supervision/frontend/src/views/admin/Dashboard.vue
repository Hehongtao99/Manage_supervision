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
import { getUserCreationTrend } from '../../api/dashboard'

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

// 用户角色分布数据
const userRoleDistribution = ref([
  { value: 0, name: '管理员' },
  { value: 0, name: '教师' },
  { value: 0, name: '学生' },
  { value: 0, name: '普通用户' }
])

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
    
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-container">
          <el-skeleton-item variant="rect" style="width: 100%; height: 100px; margin-bottom: 20px;" />
          <el-skeleton-item variant="rect" style="width: 100%; height: 400px" />
        </div>
      </template>
      
      <template #default>
        <div class="statistics-grid">
          <div class="stat-card">
            <h3>总用户数</h3>
            <div class="stat-value">{{ statistics.totalUsers }}</div>
          </div>
          
          <div class="stat-card">
            <h3>活跃用户</h3>
            <div class="stat-value">{{ statistics.activeUsers }}</div>
          </div>
          
          <div class="stat-card">
            <h3>角色数量</h3>
            <div class="stat-value">{{ statistics.totalRoles }}</div>
          </div>
          
          <div class="stat-card">
            <h3>系统状态</h3>
            <div class="stat-value">
              <el-tag type="success" v-if="statistics.systemHealth === '正常'">{{ statistics.systemHealth }}</el-tag>
              <el-tag type="warning" v-else-if="statistics.systemHealth === '警告'">{{ statistics.systemHealth }}</el-tag>
              <el-tag type="danger" v-else>{{ statistics.systemHealth }}</el-tag>
            </div>
          </div>
        </div>

        <div class="charts-container">
          <div class="chart-card">
            <v-chart class="chart" :option="pieChartOption" autoresize />
          </div>
          
          <div class="chart-card">
            <v-chart class="chart" :option="lineChartOption" autoresize />
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.admin-dashboard {
  padding: 24px;
}

.dashboard-title {
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart {
  height: 350px;
  width: 100%;
}

.skeleton-container {
  width: 100%;
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}
</style> 