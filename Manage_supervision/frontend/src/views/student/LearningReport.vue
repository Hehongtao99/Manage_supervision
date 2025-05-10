<template>
  <div class="learning-report">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学习记录报表</span>
          <div class="actions">
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>刷新数据
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="24">
          <div class="summary-cards">
            <el-card shadow="hover" class="summary-card">
              <template #header>
                <div class="summary-header">
                  <el-icon><Collection /></el-icon>
                  <span>课程总数</span>
                </div>
              </template>
              <div class="summary-value">{{ learningRecords.totalCourses }}</div>
            </el-card>

            <el-card shadow="hover" class="summary-card">
              <template #header>
                <div class="summary-header">
                  <el-icon><Timer /></el-icon>
                  <span>总学习时长</span>
                </div>
              </template>
              <div class="summary-value">{{ learningRecords.totalHours }} 小时</div>
            </el-card>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :sm="24" :md="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>课程学习时长分布</span>
              </div>
            </template>
            <div class="chart-container" v-loading="loading">
              <v-chart class="chart" :option="pieChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>月度学习时长趋势</span>
              </div>
            </template>
            <div class="chart-container" v-loading="loading">
              <v-chart class="chart" :option="lineChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="table-row">
        <el-col :span="24">
          <el-card shadow="hover" class="table-card">
            <template #header>
              <div class="table-header">
                <span>课程学习详情</span>
              </div>
            </template>
            <el-table :data="courseTableData" style="width: 100%" v-loading="loading">
              <el-table-column prop="courseName" label="课程名称" />
              <el-table-column prop="hours" label="学习时长(小时)" />
              <el-table-column prop="percentage" label="占比">
                <template #default="scope">
                  <div class="percentage-cell">
                    <el-progress 
                      :percentage="scope.row.percentage" 
                      :color="getRandomColor(scope.row.courseName)" 
                      :stroke-width="10"
                      :format="(percentage) => `${percentage}%`"
                    />
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getStudentLearningRecords, getStudentLearningTrend } from '../../api/dashboard'
import type { StudentLearningRecords, StudentLearningTrend } from '../../api/dashboard'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
])

// 状态变量
const loading = ref(false)
const learningRecords = reactive<StudentLearningRecords>({
  courseNames: [],
  courseHours: [],
  totalCourses: 0,
  totalHours: 0
})
const learningTrend = reactive<StudentLearningTrend>({
  months: [],
  hours: []
})

// 颜色映射缓存
const colorCache = new Map<string, string>()

// 生成随机颜色
const getRandomColor = (key: string) => {
  if (colorCache.has(key)) {
    return colorCache.get(key)
  }
  
  const colors = [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#6e7074'
  ]
  
  const index = Math.abs(key.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)) % colors.length
  const color = colors[index]
  colorCache.set(key, color)
  return color
}

// 计算课程表格数据
const courseTableData = computed(() => {
  const totalHours = learningRecords.totalHours || 1 // 避免除以0
  
  return learningRecords.courseNames.map((name, index) => {
    const hours = learningRecords.courseHours[index]
    const percentage = Math.round((hours / totalHours) * 100)
    
    return {
      courseName: name,
      hours: hours,
      percentage: percentage
    }
  })
})

// 饼图配置
const pieChartOption = computed(() => {
  const data = learningRecords.courseNames.map((name, index) => ({
    name,
    value: learningRecords.courseHours[index]
  }))
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}小时 ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: learningRecords.courseNames
    },
    series: [
      {
        name: '学习时长',
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
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
})

// 折线图配置
const lineChartOption = computed(() => {
  return {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}小时'
    },
    xAxis: {
      type: 'category',
      data: learningTrend.months,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '学习时长(小时)'
    },
    series: [
      {
        name: '学习时长',
        type: 'line',
        data: learningTrend.hours,
        smooth: true,
        showSymbol: true,
        symbolSize: 8,
        lineStyle: {
          width: 3,
          shadowColor: 'rgba(0,0,0,0.3)',
          shadowBlur: 10,
          shadowOffsetY: 8
        },
        itemStyle: {
          color: '#5470c6'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(84,112,198,0.5)' },
              { offset: 1, color: 'rgba(84,112,198,0.1)' }
            ]
          }
        }
      }
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    }
  }
})

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    // 获取课程学习记录数据
    const recordsData = await getStudentLearningRecords()
    Object.assign(learningRecords, recordsData)
    
    // 获取学习趋势数据
    const trendData = await getStudentLearningTrend()
    learningTrend.months = trendData.months
    learningTrend.hours = trendData.hours
  } catch (error) {
    console.error('加载学习记录数据失败:', error)
    ElMessage.error('加载学习记录数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  loadData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.learning-report {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.summary-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.summary-card {
  flex: 1;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.summary-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  padding: 20px 0;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
}

.chart-header {
  font-size: 16px;
  font-weight: bold;
}

.chart-container {
  height: 320px;
}

.chart {
  height: 100%;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  font-size: 16px;
  font-weight: bold;
}

.percentage-cell {
  width: 100%;
  padding-right: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .summary-cards {
    flex-direction: column;
  }
  
  .chart-card {
    height: 350px;
  }
  
  .chart-container {
    height: 270px;
  }
}
</style> 