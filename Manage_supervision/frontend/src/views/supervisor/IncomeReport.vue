<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage, ElDatePicker } from 'element-plus'
import { 
  getTeacherIncomeTrend, 
  getTeacherCourseIncomeDistribution,
  getTeacherIncomeStats,
  TeacherIncomeTrend,
  TeacherCourseIncome,
  TeacherIncomeStats
} from '../../api/dashboard'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent
])

// 加载状态
const loading = ref(true)

// 选择的时间周期
const selectedPeriod = ref<'week' | 'month' | 'year'>('month')

// 收入统计数据
const incomeStats = ref<TeacherIncomeStats>({
  totalIncome: 0,
  thisMonthIncome: 0,
  lastMonthIncome: 0,
  courseIncomeDistribution: []
})

// 收入趋势数据
const incomeTrendData = ref<TeacherIncomeTrend>({
  dates: [],
  amounts: []
})

// 课程收入分布数据
const courseIncomeData = ref<TeacherCourseIncome[]>([])

// 时间周期选项
const periodOptions = [
  { label: '近7天', value: 'week' },
  { label: '近30天', value: 'month' },
  { label: '近12个月', value: 'year' }
]

// 收入趋势折线图配置
const lineChartOption = computed(() => ({
  title: {
    text: '收入趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}<br />{a}: ¥{c}'
  },
  toolbox: {
    feature: {
      saveAsImage: { title: '保存为图片' }
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
    data: incomeTrendData.value.dates
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '¥{value}'
    }
  },
  series: [
    {
      name: '收入',
      type: 'line',
      smooth: true,
      data: incomeTrendData.value.amounts,
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

// 课程收入饼图配置
const pieChartOption = computed(() => ({
  title: {
    text: '课程收入分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: ¥{c} ({d}%)'
  },
  toolbox: {
    feature: {
      saveAsImage: { title: '保存为图片' }
    }
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle',
    data: courseIncomeData.value.map(item => item.courseName)
  },
  series: [
    {
      name: '课程收入',
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
      data: courseIncomeData.value.map(item => ({
        name: item.courseName,
        value: item.amount
      }))
    }
  ]
}))

// 获取收入趋势数据
const fetchIncomeTrend = async () => {
  try {
    const data = await getTeacherIncomeTrend(selectedPeriod.value)
    incomeTrendData.value = data
  } catch (error) {
    console.error('获取收入趋势数据失败:', error)
    ElMessage.error('获取收入趋势数据失败，请稍后再试')
  }
}

// 获取课程收入分布数据
const fetchCourseIncomeDistribution = async () => {
  try {
    let period: 'month' | 'year' | 'all' = 'month'
    
    // 根据选择的趋势周期选择合适的分布统计周期
    if (selectedPeriod.value === 'week' || selectedPeriod.value === 'month') {
      period = 'month'
    } else if (selectedPeriod.value === 'year') {
      period = 'year'
    }
    
    const data = await getTeacherCourseIncomeDistribution(period)
    courseIncomeData.value = data
  } catch (error) {
    console.error('获取课程收入分布数据失败:', error)
    ElMessage.error('获取课程收入分布数据失败，请稍后再试')
  }
}

// 获取收入统计数据
const fetchIncomeStats = async () => {
  try {
    const data = await getTeacherIncomeStats()
    incomeStats.value = data
  } catch (error) {
    console.error('获取收入统计数据失败:', error)
    ElMessage.error('获取收入统计数据失败，请稍后再试')
  }
}

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchIncomeTrend(),
      fetchCourseIncomeDistribution(),
      fetchIncomeStats()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理时间周期变化
const handlePeriodChange = (period: 'week' | 'month' | 'year') => {
  selectedPeriod.value = period
  loadAllData()
}

// 计算收入环比
const incomeGrowthRate = computed(() => {
  if (!incomeStats.value.lastMonthIncome) return 0
  return ((incomeStats.value.thisMonthIncome - incomeStats.value.lastMonthIncome) / incomeStats.value.lastMonthIncome * 100).toFixed(2)
})

// 页面加载时获取数据
onMounted(loadAllData)
</script>

<template>
  <div class="income-report">
    <h1 class="page-title">收入报表</h1>
    
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-container">
          <el-skeleton-item variant="rect" style="width: 100%; height: 100px; margin-bottom: 20px;" />
          <el-skeleton-item variant="rect" style="width: 100%; height: 400px" />
        </div>
      </template>
      
      <template #default>
        <!-- 收入统计卡片 -->
        <div class="statistics-grid">
          <div class="stat-card">
            <h3>总收入</h3>
            <div class="stat-value">¥{{ incomeStats.totalIncome.toLocaleString() }}</div>
          </div>
          
          <div class="stat-card">
            <h3>本月收入</h3>
            <div class="stat-value">¥{{ incomeStats.thisMonthIncome.toLocaleString() }}</div>
          </div>
          
          <div class="stat-card">
            <h3>上月收入</h3>
            <div class="stat-value">¥{{ incomeStats.lastMonthIncome.toLocaleString() }}</div>
          </div>
          
          <div class="stat-card">
            <h3>环比增长</h3>
            <div class="stat-value">
              <el-tag :type="Number(incomeGrowthRate) >= 0 ? 'success' : 'danger'">
                {{ Number(incomeGrowthRate) >= 0 ? '+' : '' }}{{ incomeGrowthRate }}%
              </el-tag>
            </div>
          </div>
        </div>
        
        <!-- 时间周期选择器 -->
        <div class="period-selector">
          <el-radio-group v-model="selectedPeriod" @change="handlePeriodChange">
            <el-radio-button v-for="option in periodOptions" :key="option.value" :label="option.value">
              {{ option.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        
        <!-- 图表区域 -->
        <div class="charts-container">
          <div class="chart-card">
            <v-chart class="chart" :option="lineChartOption" autoresize />
          </div>
          
          <div class="chart-card">
            <v-chart class="chart" :option="pieChartOption" autoresize />
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.income-report {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  font-size: 16px;
  color: #606266;
  margin: 0 0 15px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.period-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 400px;
}

.chart {
  height: 100%;
  width: 100%;
}

@media (max-width: 768px) {
  .statistics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
}
</style> 