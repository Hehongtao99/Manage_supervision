<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getDashboardStats, getUserList } from '../../api/dashboard'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import { 
  LegendComponent, 
  TooltipComponent, 
  TitleComponent, 
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import axios from '../../utils/axios'
import { ElLoading, ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  BarChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  GridComponent,
  DataZoomComponent
])

const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalRoles: 0,
  systemHealth: '正常'
})

const isLoading = ref(true)
const roleDistribution = ref<Record<string, number>>({})
const userTrend = ref<{dates: string[], counts: number[]}>({
  dates: [],
  counts: []
})

// 角色分布图表配置
const roleChartOption = computed(() => {
  return {
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
      data: Object.keys(roleDistribution.value)
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
        data: Object.entries(roleDistribution.value).map(([name, value]) => ({
          name,
          value
        }))
      }
    ]
  }
})

// 用户增长趋势图表配置
const userTrendOption = computed(() => {
  return {
    title: {
      text: '用户增长趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: userTrend.value.dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: userTrend.value.counts,
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: {
          width: 3,
          shadowColor: 'rgba(0,0,0,0.3)',
          shadowBlur: 10,
          shadowOffsetY: 8
        },
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
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.6)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.1)'
              }
            ]
          }
        }
      }
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100
      }
    ]
  }
})

// 根据用户创建时间生成用户增长趋势数据
const processUserTrendData = (userList: Array<{id: number, username: string, createTime: string}>) => {
  // 创建日期映射表
  const dateMap = new Map<string, number>()
  
  // 处理每个用户的创建时间，按日期分组
  userList.forEach(user => {
    if (!user.createTime) return; // 跳过没有创建时间的用户
    
    // 将时间字符串转换为Date对象，提取日期部分
    const createDate = new Date(user.createTime).toISOString().split('T')[0]
    
    // 将用户添加到对应日期
    if (dateMap.has(createDate)) {
      dateMap.set(createDate, dateMap.get(createDate)! + 1)
    } else {
      dateMap.set(createDate, 1)
    }
  })
  
  // 没有任何数据时直接返回空数据
  if (dateMap.size === 0) {
    return { dates: [], counts: [] }
  }
  
  // 将日期映射表转换为有序数组（按日期排序）
  const sortedEntries = Array.from(dateMap.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  
  // 累计用户数量，计算每天的总用户数
  let totalUsers = 0
  const result = sortedEntries.map(([date, count]) => {
    totalUsers += count
    return { date, totalUsers }
  })
  
  // 提取日期和累计数量作为两个数组
  const dates = result.map(item => {
    // 格式化日期为 MM-DD 格式
    const parts = item.date.split('-')
    return `${parseInt(parts[1])}-${parseInt(parts[2])}`
  })
  const counts = result.map(item => item.totalUsers)
  
  return { dates, counts }
}

// 获取用户增长趋势数据
const updateUserTrend = (userInfo: Array<{id: number, username: string, createTime: string}>) => {
  if (!userInfo || userInfo.length === 0) {
    console.error('没有可用的用户数据来生成趋势')
    userTrend.value = { dates: [], counts: [] }
    return
  }
  
  try {
    const trendData = processUserTrendData(userInfo)
    userTrend.value = trendData
  } catch (error) {
    console.error('处理用户趋势数据失败:', error)
    userTrend.value = { dates: [], counts: [] }
  }
}

// 获取仪表盘数据
const getDashboardData = async () => {
  isLoading.value = true
  
  const loading = ElLoading.service({
    lock: true,
    text: '加载中...',
    background: 'rgba(255, 255, 255, 0.7)'
  })
  
  try {
    // 从API获取数据
    const dashboardStats = await getDashboardStats()
    
    // 更新统计数据
    statistics.value.totalUsers = dashboardStats.totalUsers || 0
    statistics.value.activeUsers = dashboardStats.activeUsers || Math.floor(dashboardStats.totalUsers * 0.7)
    
    // 更新角色分布
    if (dashboardStats.roleDistribution) {
      roleDistribution.value = dashboardStats.roleDistribution
    } else {
      roleDistribution.value = {
        'ADMIN': 3,
        'SUPERVISOR': 15,
        'USER': 82
      }
    }
    
    // 计算角色总数
    statistics.value.totalRoles = Object.keys(roleDistribution.value).length
    
    // 获取用户趋势数据
    updateUserTrend(dashboardStats.userList || [])
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.close()
    isLoading.value = false
  }
}

onMounted(async () => {
  // 加载仪表盘数据
  await getDashboardData()
})
</script>

<template>
  <div class="admin-dashboard">
    <h1 class="dashboard-title">管理控制台</h1>
    
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
        <div class="stat-value">{{ statistics.systemHealth }}</div>
      </div>
    </div>

    <div class="dashboard-charts">
      <div class="chart-container">
        <v-chart class="chart" :option="roleChartOption" autoresize />
      </div>
      
      <div class="chart-container">
        <div v-if="isLoading" class="chart-loading">
          <el-loading />
          <p>加载用户趋势数据...</p>
        </div>
        <div v-else-if="userTrend.dates.length === 0" class="chart-error">
          <el-icon><WarningFilled /></el-icon>
          <p>无法获取用户增长趋势数据</p>
          <el-button size="small" type="primary" @click="getDashboardData">重试</el-button>
        </div>
        <v-chart v-else class="chart" :option="userTrendOption" autoresize />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  padding: 24px;
  height: calc(100vh - 60px);
  overflow-y: auto;
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
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
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

.dashboard-charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.chart-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 350px;
}

.chart {
  height: 100%;
  width: 100%;
}

.chart-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: #909399;
  text-align: center;
}

.chart-error i {
  font-size: 32px;
  color: #E6A23C;
}

.chart-error p {
  margin: 0;
  font-size: 14px;
}

.chart-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .statistics-grid,
  .dashboard-charts {
    grid-template-columns: 1fr;
  }
}
</style> 