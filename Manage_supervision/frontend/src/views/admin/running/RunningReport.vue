<template>
  <div class="running-report">
    <div class="page-header">
      <h2>跑步报表</h2>
      <div class="date-filter">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="handleDateChange"
        ></el-date-picker>
        <el-button type="primary" @click="fetchReportData">查询</el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="10" animated />

    <div v-else>
      <!-- 总体统计数据卡片 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">总跑步人数</div>
              <div class="stat-value">{{ report.totalRunners || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">总跑步次数</div>
              <div class="stat-value">{{ report.totalRunCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">总跑步距离</div>
              <div class="stat-value">{{ formatDecimal(report.totalDistance) || 0 }} 公里</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">平均跑步距离</div>
              <div class="stat-value">{{ formatDecimal(report.avgDistance) || 0 }} 公里</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">人均跑步次数</div>
              <div class="stat-value">{{ formatDecimal(report.avgRunCountPerPerson) || 0 }} 次</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-title">日均跑步人数</div>
              <div class="stat-value">{{ formatDecimal(report.avgDailyRunners) || 0 }} 人</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover" class="chart-card">
            <div class="card-header">
              <h3>每日跑步人数</h3>
            </div>
            <div class="chart-container">
              <v-chart class="chart" :option="dailyRunnerChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="hover" class="chart-card">
            <div class="card-header">
              <h3>距离分布</h3>
            </div>
            <div class="chart-container">
              <v-chart class="chart" :option="distanceDistributionChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 排行榜预览 -->
      <el-row :gutter="20" class="ranking-preview">
        <el-col :xs="24">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>跑步距离排行榜（前5名）</h3>
                <el-button type="text" @click="$router.push('/admin/running/ranking')">查看更多</el-button>
              </div>
            </template>
            <el-table :data="report.distanceRanking ? report.distanceRanking.slice(0, 5) : []" style="width: 100%">
              <el-table-column type="index" label="排名" width="80" />
              <el-table-column label="用户" min-width="180">
                <template #default="scope">
                  <div class="user-info">
                    <el-avatar :size="32" :src="scope.row.avatar">{{ scope.row.username?.charAt(0) }}</el-avatar>
                    <span class="username">{{ scope.row.realName || scope.row.username }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="totalDistance" label="跑步距离(公里)" width="150" />
              <el-table-column prop="runCount" label="跑步次数" width="150" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRunningReport } from '@/api/adminRunning'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()
const loading = ref(true)
const report = ref<any>({})
const dateRange = ref<[string, string]>(['', ''])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 每日跑步人数图表配置
const dailyRunnerChartOption = computed(() => {
  if (!report.value || !report.value.dailyRunnerCount) {
    return {
      title: { text: '每日跑步人数' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ data: [], type: 'line' }]
    }
  }

  const dailyData = report.value.dailyRunnerCount
  const dates = Object.keys(dailyData).sort()
  const values = dates.map(date => dailyData[date])

  return {
    title: { text: '每日跑步人数' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '跑步人数',
        data: values,
        type: 'line',
        smooth: true,
        lineStyle: { width: 3 },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.7)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        }
      }
    ]
  }
})

// 距离分布图表配置
const distanceDistributionChartOption = computed(() => {
  if (!report.value || !report.value.distanceDistribution) {
    return {
      title: { text: '跑步距离分布' },
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: '55%', data: [] }]
    }
  }

  const distData = report.value.distanceDistribution
  const data = Object.keys(distData).map(key => ({
    name: key,
    value: distData[key]
  }))

  return {
    title: { text: '跑步距离分布' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { 
      orient: 'vertical', 
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '距离分布',
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
            fontSize: '16',
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

// 处理日期变化
const handleDateChange = (val: [string, string]) => {
  if (val) {
    dateRange.value = val
  }
}

// 格式化小数
const formatDecimal = (value: any) => {
  if (!value) return '0.00'
  return Number(value).toFixed(2)
}

// 获取报表数据
const fetchReportData = async () => {
  loading.value = true
  
  try {
    // 设置日期范围，如果未选择则使用最近一个月
    let startDate = dateRange.value[0]
    let endDate = dateRange.value[1]
    
    if (!startDate || !endDate) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      
      startDate = start.toISOString().split('T')[0]
      endDate = end.toISOString().split('T')[0]
      
      dateRange.value = [startDate, endDate]
    }
    
    const response = await getRunningReport(startDate, endDate)
    report.value = response.data.data
  } catch (error) {
    console.error('获取跑步报表数据失败:', error)
    ElMessage.error('获取跑步报表数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReportData()
})
</script>

<style scoped>
.running-report {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.date-filter {
  display: flex;
  gap: 10px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.chart-container {
  height: 300px;
}

.chart {
  height: 100%;
  width: 100%;
}

.ranking-preview {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  margin-left: 10px;
  font-weight: 500;
}
</style>