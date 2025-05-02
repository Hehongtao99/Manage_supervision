<template>
  <div class="running-dashboard-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="title-container">
          <h2>跑步数据看板</h2>
        </div>
      </el-col>
    </el-row>

    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">总跑量</div>
            <div class="stat-value">{{ stats.totalDistance || 0 }} 公里</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">本周跑量</div>
            <div class="stat-value">{{ stats.currentWeekDistance || 0 }} 公里</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">平均配速</div>
            <div class="stat-value">{{ stats.averagePace || '0:00' }} 分/公里</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">最佳配速</div>
            <div class="stat-value">{{ stats.bestPace || '0:00' }} 分/公里</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 成就卡片 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <AchievementCard />
      </el-col>
    </el-row>

    <!-- 图表展示区域 -->
    <el-row :gutter="20" class="chart-container">
      <!-- 周维度跑量堆积图 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>周维度跑量堆积图</span>
            </div>
          </template>
          <div class="chart-wrapper" v-loading="loading">
            <div id="weeklyDistanceChart" class="chart"></div>
            <div v-if="!hasWeeklyData" class="no-data-container">
              <el-empty description="暂无周跑量数据"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 月维度配速波动折线图 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>月维度配速波动折线图</span>
              <div class="header-notes">
                <small>注：配速越低越好</small>
              </div>
            </div>
          </template>
          <div class="chart-wrapper" v-loading="loading">
            <div id="monthlyPaceChart" class="chart"></div>
            <div v-if="!hasMonthlyData" class="no-data-container">
              <el-empty description="暂无配速数据"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 训练建议 -->
    <el-row :gutter="20" class="advice-container">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="advice-header">
              <span>训练建议</span>
            </div>
          </template>
          <div class="advice-content">
            <p v-if="stats.totalRunCount && stats.totalRunCount > 0">
              根据您的跑步数据分析，建议：
              <template v-if="stats.currentWeekDistance < 10">增加每周跑量，循序渐进地达到每周至少10公里的跑量</template>
              <template v-else-if="stats.currentWeekDistance < 20">维持当前跑量，可以适当增加高强度间歇训练</template>
              <template v-else>注意恢复，保持良好的训练状态，可以尝试参加一些短距离比赛</template>
            </p>
            <p v-else>
              开始记录您的跑步数据，我们将为您提供个性化的训练建议。
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getRunningStats } from '@/api/running'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  LegendComponent,
  ToolboxComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import AchievementCard from '@/components/AchievementCard.vue'

// 注册 ECharts 必需的组件
echarts.use([
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  LegendComponent,
  ToolboxComponent,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])

// 状态变量
const loading = ref(false)
const stats = reactive<any>({
  totalDistance: 0,
  totalRunCount: 0,
  currentWeekDistance: 0,
  currentMonthDistance: 0,
  averagePace: '0:00',
  bestPace: '0:00',
  weeklyDistanceStats: {},
  monthlyPaceStats: []
})

// 图表实例
let weeklyDistanceChart: echarts.ECharts | null = null
let monthlyPaceChart: echarts.ECharts | null = null

// 计算属性判断是否有数据
const hasWeeklyData = computed(() => {
  return stats.weeklyDistanceStats && Object.keys(stats.weeklyDistanceStats).length > 0
})

const hasMonthlyData = computed(() => {
  return stats.monthlyPaceStats && stats.monthlyPaceStats.length > 0
})

// 初始化周维度跑量堆积图
const initWeeklyDistanceChart = () => {
  if (!hasWeeklyData.value) return
  
  const chartDom = document.getElementById('weeklyDistanceChart')
  if (!chartDom) return
  
  weeklyDistanceChart = echarts.init(chartDom)
  
  const weeks = Object.keys(stats.weeklyDistanceStats)
  const distances = Object.values(stats.weeklyDistanceStats)
  
  const option = {
    title: {
      text: '周跑量统计',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params: any) {
        const week = params[0].name
        const distance = params[0].value
        return `${week}周: ${distance} 公里`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: weeks,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '跑量(公里)'
    },
    series: [
      {
        name: '跑量',
        type: 'bar',
        stack: 'total',
        label: {
          show: true,
          position: 'top',
          formatter: '{c} 公里'
        },
        data: distances,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  
  weeklyDistanceChart.setOption(option)
}

// 初始化月维度配速波动折线图
const initMonthlyPaceChart = () => {
  if (!hasMonthlyData.value) return
  
  const chartDom = document.getElementById('monthlyPaceChart')
  if (!chartDom) return
  
  monthlyPaceChart = echarts.init(chartDom)
  
  // 处理数据
  const dates: string[] = []
  const paces: number[] = []
  
  stats.monthlyPaceStats.forEach((item: any) => {
    dates.push(item.date)
    // 配速是按秒计算的，转换为分钟展示
    paces.push(item.pace / 60)
  })
  
  const option = {
    title: {
      text: '30天配速趋势',
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params: any) {
        const date = params[0].name
        // 转换回配速格式
        const seconds = params[0].value * 60
        const minutes = Math.floor(seconds / 60)
        const remainingSeconds = Math.round(seconds % 60)
        const paceStr = `${minutes}:${remainingSeconds < 10 ? '0' + remainingSeconds : remainingSeconds}`
        return `${date}: ${paceStr} 分/公里`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '配速(分/公里)',
      inverse: true, // 倒置坐标轴，使得配速越低(更好)显示在上方
      axisLabel: {
        formatter: function (value: number) {
          const minutes = Math.floor(value)
          const seconds = Math.round((value - minutes) * 60)
          return `${minutes}:${seconds < 10 ? '0' + seconds : seconds}`
        }
      }
    },
    series: [
      {
        name: '配速',
        type: 'line',
        smooth: true,
        data: paces,
        markPoint: {
          data: [
            { 
              type: 'min', 
              name: '最佳配速',
              label: {
                formatter: function (params: any) {
                  const seconds = params.value * 60
                  const minutes = Math.floor(seconds / 60)
                  const remainingSeconds = Math.round(seconds % 60)
                  return `${minutes}:${remainingSeconds < 10 ? '0' + remainingSeconds : remainingSeconds}`
                }
              }
            }
          ]
        },
        lineStyle: {
          width: 3,
          color: '#5470C6'
        },
        itemStyle: {
          color: '#5470C6'
        },
        symbolSize: 8
      }
    ]
  }
  
  monthlyPaceChart.setOption(option)
}

// 获取跑步统计数据
const fetchRunningStats = async () => {
  try {
    loading.value = true
    const { data } = await getRunningStats()
    
    if (data) {
      Object.assign(stats, data)
      
      // 初始化图表
      setTimeout(() => {
        initWeeklyDistanceChart()
        initMonthlyPaceChart()
      }, 0)
    }
  } catch (error) {
    console.error('获取跑步统计数据失败:', error)
    ElMessage.error('获取跑步统计数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 监听窗口大小变化，重置图表大小
const handleResize = () => {
  if (weeklyDistanceChart) {
    weeklyDistanceChart.resize()
  }
  if (monthlyPaceChart) {
    monthlyPaceChart.resize()
  }
}

// 生命周期钩子
onMounted(() => {
  fetchRunningStats()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除监听
const onBeforeUnmount = () => {
  window.removeEventListener('resize', handleResize)
  if (weeklyDistanceChart) {
    weeklyDistanceChart.dispose()
  }
  if (monthlyPaceChart) {
    monthlyPaceChart.dispose()
  }
}
</script>

<style scoped>
.running-dashboard-container {
  padding: 20px;
}

.title-container {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-notes {
  color: #909399;
}

.chart-wrapper {
  width: 100%;
  height: 320px;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}

.no-data-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.advice-container {
  margin-bottom: 20px;
}

.advice-header {
  font-weight: bold;
}

.advice-content {
  padding: 10px 0;
  line-height: 1.6;
}
</style> 