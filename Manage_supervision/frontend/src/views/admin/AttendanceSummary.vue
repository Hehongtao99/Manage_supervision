<template>
  <div class="attendance-summary">
    <el-scrollbar height="calc(100vh - 80px)" class="scrollbar-container">
      <el-card class="attendance-card">
        <template #header>
          <div class="card-header">
            <span class="title">考勤汇总</span>
            <div class="filter-area">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                :default-time="defaultTime"
                :shortcuts="dateShortcuts"
              />
              <el-button type="primary" @click="loadSummary">查询</el-button>
            </div>
          </div>
        </template>
        
        <div v-loading="loading">
          <!-- 总体统计卡片 -->
          <div class="summary-cards" v-if="summaryData">
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">考勤总数</div>
              </template>
              <div class="stat-value">{{ summaryData.totalAttendance }}</div>
            </el-card>
            
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">总人数</div>
              </template>
              <div class="stat-value">{{ summaryData.totalUsers }}</div>
            </el-card>
            
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">总签到次数</div>
              </template>
              <div class="stat-value">{{ summaryData.totalCheckins }}</div>
            </el-card>
            
            <el-card class="stat-card">
              <template #header>
                <div class="stat-header">总体签到率</div>
              </template>
              <div class="stat-value rate-value">
                {{ (summaryData.overallCheckInRate || 0).toFixed(2) }}%
                <el-progress 
                  :percentage="summaryData.overallCheckInRate" 
                  :format="() => ''" 
                  :stroke-width="6">
                </el-progress>
              </div>
            </el-card>
          </div>
          
          <!-- 图表区域 -->
          <div class="chart-container" v-if="summaryData && summaryData.attendanceSummaries.length > 0">
            <el-row :gutter="20">
              <!-- 签到率饼图 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <div class="chart-header">签到状态统计</div>
                  </template>
                  <div class="chart-wrapper">
                    <v-chart class="chart" :option="pieChartOption" autoresize />
                  </div>
                </el-card>
              </el-col>
              
              <!-- 考勤折线图 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <div class="chart-header">考勤签到率趋势</div>
                  </template>
                  <div class="chart-wrapper">
                    <v-chart class="chart" :option="lineChartOption" autoresize />
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <!-- 详细考勤列表 -->
          <div class="attendance-details" v-if="summaryData && summaryData.attendanceSummaries.length > 0">
            <h3>考勤详情</h3>
            <el-table :data="summaryData.attendanceSummaries" border stripe>
              <el-table-column prop="title" label="考勤标题" min-width="150"></el-table-column>
              <el-table-column label="考勤时间" min-width="220">
                <template #default="{ row }">
                  {{ formatDateTime(row.startTime) }} ~ {{ formatDateTime(row.endTime) }}
                </template>
              </el-table-column>
              <el-table-column label="签到情况" width="200">
                <template #default="{ row }">
                  {{ row.checkedInCount }} / {{ row.totalUsers }}
                  <el-progress 
                    :percentage="row.checkInRate" 
                    :format="() => row.checkInRate.toFixed(2) + '%'" 
                    :stroke-width="6">
                  </el-progress>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="navigateToDetail(row.id)">
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 无数据提示 -->
          <el-empty 
            v-if="!loading && (!summaryData || summaryData.attendanceSummaries.length === 0)" 
            description="暂无考勤数据">
          </el-empty>
        </div>
      </el-card>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as attendanceApi from '@/api/attendance';
import { formatDateTime } from '@/utils/format';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { PieChart, LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';
import VChart from 'vue-echarts';

// 注册必须的组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
]);

const router = useRouter();
const loading = ref(false);
const summaryData = ref<any>(null);

// 日期范围
const dateRange = ref<[string, string] | null>(null);
const defaultTime = ['00:00:00', '23:59:59'];

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      return [start, end];
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      return [start, end];
    },
  },
];

// 饼图配置
const pieChartOption = computed(() => {
  if (!summaryData.value) return {};
  
  const totalAttendancePossible = summaryData.value.totalAttendance * summaryData.value.totalUsers;
  const checkedInCount = summaryData.value.totalCheckins;
  const uncheckedCount = totalAttendancePossible - checkedInCount;
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['已签到', '未签到']
    },
    series: [
      {
        name: '签到状态',
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
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: checkedInCount, name: '已签到', itemStyle: { color: '#67C23A' } },
          { value: uncheckedCount, name: '未签到', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  };
});

// 折线图配置
const lineChartOption = computed(() => {
  if (!summaryData.value || !summaryData.value.attendanceSummaries.length) return {};
  
  // 按日期排序考勤记录
  const sortedSummaries = [...summaryData.value.attendanceSummaries].sort((a, b) => {
    return new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
  });
  
  const titles = sortedSummaries.map((item: any) => item.title);
  const rates = sortedSummaries.map((item: any) => Number(item.checkInRate.toFixed(2)));
  
  return {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: titles,
      axisLabel: {
        interval: 0,
        rotate: 30,
        formatter: (value: string) => {
          if (value.length > 10) {
            return value.substring(0, 10) + '...';
          }
          return value;
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '签到率(%)',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '签到率',
        type: 'line',
        data: rates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  };
});

// 加载考勤汇总数据
const loadSummary = async () => {
  loading.value = true;
  try {
    let startDate, endDate;
    if (dateRange.value) {
      [startDate, endDate] = dateRange.value;
    }
    
    const data = await attendanceApi.getAttendanceSummary(startDate, endDate);
    summaryData.value = data;
  } catch (error) {
    console.error('加载考勤汇总失败', error);
    ElMessage.error('加载考勤汇总失败');
  } finally {
    loading.value = false;
  }
};

// 跳转到考勤详情
const navigateToDetail = (id: number) => {
  router.push(`/admin/attendance?id=${id}`);
};

// 初始加载
onMounted(() => {
  loadSummary();
});
</script>

<style scoped>
.attendance-summary {
  padding: 20px;
  height: 100vh;
  box-sizing: border-box;
  overflow: hidden;
}

.scrollbar-container {
  width: 100%;
}

.attendance-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.filter-area {
  display: flex;
  gap: 10px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
}

.stat-header, .chart-header {
  font-weight: bold;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  padding: 10px 0;
}

.rate-value {
  font-size: 24px;
}

.chart-container {
  margin: 30px 0;
}

.chart-wrapper {
  height: 300px;
}

.chart {
  height: 100%;
  width: 100%;
}

.attendance-details {
  margin-top: 30px;
}

h3 {
  margin-bottom: 15px;
}
</style> 