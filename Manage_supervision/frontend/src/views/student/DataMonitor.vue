<template>
  <div class="data-monitor-container">
    <el-card class="monitor-header">
      <div class="monitor-header-title">
        <h2>数据监控分析</h2>
        <div class="monitor-header-actions">
          <el-button type="primary" @click="exportReport">
            <el-icon><Download /></el-icon>导出报告
          </el-button>
          <el-button @click="refreshData">
            <el-icon><Refresh /></el-icon>刷新数据
          </el-button>
        </div>
      </div>
      <el-divider />
      
      <!-- 统计卡片 -->
      <div class="stat-cards">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>总数据量</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalRecords }}</div>
        </el-card>
        
        <el-card shadow="hover" class="stat-card anomaly">
          <template #header>
            <div class="stat-header">
              <el-icon><Warning /></el-icon>
              <span>异常数据</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.anomalyCount }}</div>
        </el-card>
        
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="stat-header">
              <el-icon><Calendar /></el-icon>
              <span>最近检测</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.lastUpdated }}</div>
        </el-card>
        
        <el-card shadow="hover" class="stat-card resolved">
          <template #header>
            <div class="stat-header">
              <el-icon><CircleCheck /></el-icon>
              <span>已处理异常</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.resolvedCount }}</div>
        </el-card>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card class="filter-section">
      <el-form :model="filterForm" label-width="100px" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="异常阈值">
          <el-slider
            v-model="filterForm.threshold"
            :min="1"
            :max="100"
            :step="1"
            show-input
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="仅显示异常">
          <el-switch v-model="filterForm.anomalyOnly" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="applyFilter">应用筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据展示表格 -->
    <el-card class="data-table-section">
      <el-table
        v-loading="loading"
        :data="monitorData"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="recordTime" label="记录时间" width="180" sortable />
        <el-table-column prop="dataType" label="数据类型" width="120" />
        <el-table-column prop="value" label="数值" width="120" sortable />
        <el-table-column prop="expected" label="预期范围" width="180" />
        <el-table-column prop="deviation" label="偏差率" width="120">
          <template #default="scope">
            <el-tag
              :type="getDeviationTagType(scope.row.deviation)"
              effect="dark"
            >
              {{ scope.row.deviation }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="scope.row.isAnomaly ? 'danger' : 'success'"
              effect="plain"
            >
              {{ scope.row.isAnomaly ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button
              v-if="scope.row.isAnomaly && !scope.row.resolved"
              type="primary"
              size="small"
              @click="markAsResolved(scope.row.id)"
            >
              标记为已处理
            </el-button>
            <el-tag v-else-if="scope.row.resolved" type="info">已处理</el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 图表展示 -->
    <el-card class="chart-section">
      <el-tabs v-model="activeChart">
        <el-tab-pane label="数据趋势" name="trend">
          <div ref="trendChartRef" class="chart-container"></div>
        </el-tab-pane>
        <el-tab-pane label="异常分布" name="anomaly">
          <div ref="anomalyChartRef" class="chart-container"></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, DataAnalysis, Warning, Calendar, CircleCheck } from '@element-plus/icons-vue'
import { getUserMonitorData, getAnomalyData, getMonitorStats, exportMonitorReport, markAnomalyAsResolved } from '@/api/dataMonitor'
import * as echarts from 'echarts'

// 图表引用
const trendChartRef = ref(null)
const anomalyChartRef = ref(null)
let trendChart: echarts.ECharts | null = null
let anomalyChart: echarts.ECharts | null = null

// 数据状态
const loading = ref(false)
const monitorData = ref<any[]>([])
const stats = reactive({
  totalRecords: 0,
  anomalyCount: 0,
  lastUpdated: '-',
  resolvedCount: 0
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选条件
const dateRange = ref<[string, string] | null>(null)
const activeChart = ref('trend')
const filterForm = reactive({
  startDate: '',
  endDate: '',
  threshold: 20,
  anomalyOnly: false
})

// 日期选择快捷选项
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

// 监听日期变化
watch(dateRange, (newVal) => {
  if (newVal) {
    filterForm.startDate = newVal[0]
    filterForm.endDate = newVal[1]
  } else {
    filterForm.startDate = ''
    filterForm.endDate = ''
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const response = await getUserMonitorData({
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      threshold: filterForm.threshold,
      anomalyOnly: filterForm.anomalyOnly
    })
    monitorData.value = response.data.records
    total.value = response.data.total
    loading.value = false
  } catch (error) {
    console.error('获取监控数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await getMonitorStats()
    Object.assign(stats, response.data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value as HTMLElement)
  }
  
  if (anomalyChartRef.value) {
    anomalyChart = echarts.init(anomalyChartRef.value as HTMLElement)
  }
  
  updateCharts()
}

// 更新图表数据
const updateCharts = () => {
  if (!monitorData.value.length) return
  
  // 趋势图
  if (trendChart) {
    const dates = monitorData.value.map(item => item.recordTime)
    const values = monitorData.value.map(item => item.value)
    const anomalies = monitorData.value.map(item => item.isAnomaly ? item.value : null)
    
    trendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['数值', '异常']
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '数值',
          type: 'line',
          data: values,
          smooth: true
        },
        {
          name: '异常',
          type: 'scatter',
          data: anomalies,
          symbolSize: 8,
          itemStyle: {
            color: '#F56C6C'
          }
        }
      ]
    })
  }
  
  // 异常分布图
  if (anomalyChart) {
    // 按数据类型统计异常
    const typeStats = {} as Record<string, number>
    monitorData.value.forEach(item => {
      if (item.isAnomaly) {
        typeStats[item.dataType] = (typeStats[item.dataType] || 0) + 1
      }
    })
    
    const types = Object.keys(typeStats)
    const counts = types.map(type => typeStats[type])
    
    anomalyChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        data: types
      },
      series: [
        {
          name: '异常分布',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
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
          data: types.map((type, index) => ({
            name: type,
            value: counts[index]
          }))
        }
      ]
    })
  }
}

// 应用筛选条件
const applyFilter = () => {
  currentPage.value = 1
  fetchData()
}

// 重置筛选条件
const resetFilter = () => {
  dateRange.value = null
  Object.assign(filterForm, {
    startDate: '',
    endDate: '',
    threshold: 20,
    anomalyOnly: false
  })
  currentPage.value = 1
  fetchData()
}

// 刷新数据
const refreshData = () => {
  fetchData()
  fetchStats()
  updateCharts()
}

// 导出报告
const exportReport = async () => {
  try {
    const response = await exportMonitorReport({
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      threshold: filterForm.threshold,
      anomalyOnly: filterForm.anomalyOnly
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `数据监控报告_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('导出报告失败:', error)
    ElMessage.error('导出报告失败，请稍后重试')
  }
}

// 标记异常为已处理
const markAsResolved = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定将此异常标记为已处理?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await markAnomalyAsResolved(id)
    ElMessage.success('操作成功')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记异常失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

// 分页操作
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

// 根据偏差率获取标签类型
const getDeviationTagType = (deviation: number) => {
  if (deviation > filterForm.threshold) return 'danger'
  if (deviation > filterForm.threshold / 2) return 'warning'
  return 'success'
}

// 组件挂载时获取数据并初始化图表
onMounted(() => {
  fetchData()
  fetchStats()
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', () => {
      trendChart?.resize()
      anomalyChart?.resize()
    })
  }, 300)
})
</script>

<style scoped>
.data-monitor-container {
  padding: 20px;
}

.monitor-header {
  margin-bottom: 20px;
}

.monitor-header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.monitor-header-actions {
  display: flex;
  gap: 10px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  text-align: center;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  color: #606266;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  padding: 10px 0;
}

.anomaly .stat-value {
  color: #F56C6C;
}

.resolved .stat-value {
  color: #67C23A;
}

.filter-section {
  margin-bottom: 20px;
}

.data-table-section {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}
</style> 