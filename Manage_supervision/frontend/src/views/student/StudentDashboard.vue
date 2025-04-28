<template>
  <div class="student-dashboard p-4">
    <h1 class="text-2xl font-bold mb-4">学生仪表盘</h1>

    <!-- 考试统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon bg-blue-100 text-blue-500">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ examStats.total || 0 }}</div>
              <div class="stat-label">总考试数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon bg-green-100 text-green-500">
              <el-icon><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ examStats.completed || 0 }}</div>
              <div class="stat-label">已完成考试</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon bg-yellow-100 text-yellow-500">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ examStats.pending || 0 }}</div>
              <div class="stat-label">待参加/进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-4">
      <!-- 近期考试列表 -->
      <el-col :span="16">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
              <div class="flex justify-between items-center">
                <span>近期考试 <small>(最近5场)</small></span>
                <el-button type="primary" link @click="goToExamList">查看全部</el-button>
              </div>
          </template>
          <el-table :data="recentExams" v-loading="loading" height="280px">
              <el-table-column prop="title" label="考试名称" min-width="150" show-overflow-tooltip/>
              <el-table-column prop="startTime" label="开始时间" width="170">
                <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
              </el-table-column>
              <el-table-column prop="endTime" label="结束时间" width="170">
                <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
              </el-table-column>
              <el-table-column prop="studentStatus" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status, scope.row.studentStatus)">
                      {{ getStatusText(scope.row.status, scope.row.studentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无近期考试记录"></el-empty>
              </template>
          </el-table>
        </el-card>
      </el-col>

      <!-- 考试状态分布饼图 -->
      <el-col :span="8">
        <el-card shadow="never" class="dashboard-card">
           <template #header>
              <span>考试状态分布</span>
           </template>
           <div ref="pieChartRef" style="height: 280px;" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期成绩趋势折线图 -->
    <el-row>
       <el-col :span="24">
          <el-card shadow="never" class="dashboard-card">
             <template #header>
                <span>近期成绩趋势 <small>(最近5次已完成)</small></span>
             </template>
             <div ref="lineChartRef" style="height: 300px;" v-loading="loading"></div>
             <!-- 提示信息 - 只保留无数据提示 -->
             <el-alert
                v-if="!loading && lineChartData.scores.length === 0"
                title="暂无已完成的考试成绩数据"
                type="info"
                :closable="false"
                show-icon
                center
                style="margin-top: 10px;"
              />
          </el-card>
       </el-col>
    </el-row>

  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElCard, ElRow, ElCol, ElIcon, ElTable, ElTableColumn, ElTag, ElEmpty, ElButton, ElAlert } from 'element-plus';
import { Collection, Select, Clock } from '@element-plus/icons-vue';
import { getStudentExams } from '@/api/exam';
import { formatDateTime } from '@/utils/formatter';
import * as echarts from 'echarts/core';
import { PieChart, LineChart } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册 ECharts 组件
echarts.use([TitleComponent, TooltipComponent, LegendComponent, PieChart, LineChart, CanvasRenderer, GridComponent]);

const router = useRouter();
const allExams = ref<any[]>([]);
const loading = ref(false);
const pieChartRef = ref<HTMLElement | null>(null);
const lineChartRef = ref<HTMLElement | null>(null);
let pieChartInstance: echarts.ECharts | null = null;
let lineChartInstance: echarts.ECharts | null = null;

// --- 计算属性 ---
const examStats = computed(() => {
  const total = allExams.value.length;
  const completed = allExams.value.filter(e => ['PUBLISHED'].includes(e.studentStatus)).length;
  const pending = allExams.value.filter(e => ['NOT_STARTED', 'IN_PROGRESS'].includes(e.studentStatus)).length;
  const submittedOrPendingPublish = allExams.value.filter(e => ['SUBMITTED', 'PENDING_PUBLISH'].includes(e.studentStatus)).length;
  return { total, completed, pending: pending + submittedOrPendingPublish };
});

const recentExams = computed(() => {
  return [...allExams.value]
    .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime())
    .slice(0, 5);
});

const pieChartData = computed(() => {
  const statusMap: Record<string, string> = {
    'NOT_STARTED': '待参加',
    'IN_PROGRESS': '进行中',
    'SUBMITTED': '已提交',
    'PENDING_PUBLISH': '待发布',
    'PUBLISHED': '已完成'
  };
  const counts: Record<string, number> = {};

  allExams.value.forEach(exam => {
    const statusText = getStatusText(exam.status, exam.studentStatus);
    const displayStatus = statusMap[exam.studentStatus] || statusText;
    counts[displayStatus] = (counts[displayStatus] || 0) + 1;
  });

  return Object.entries(counts)
    .filter(([_, value]) => value > 0)
    .map(([name, value]) => ({ name, value }));
});

const lineChartData = computed(() => {
  const completedExamsWithScores = allExams.value
    .filter(exam => exam.studentStatus === 'PUBLISHED' && typeof exam.studentScore === 'number')
    .sort((a, b) => new Date(a.endTime).getTime() - new Date(b.endTime).getTime())
    .slice(-5);

  const examNames = completedExamsWithScores.map(exam => exam.title);
  const scores = completedExamsWithScores.map(exam => exam.studentScore);

  return { examNames, scores };
});

// --- 方法 ---
const fetchStudentExamData = async () => {
  loading.value = true;
  try {
    const response = await getStudentExams({ page: 0, size: 1000 });
    if (response.data && response.data.code === 200 && response.data.data) {
      allExams.value = response.data.data.content || [];

      await nextTick();
      initOrUpdateChart();
      initOrUpdateLineChart();
    } else {
      console.error('获取学生考试数据失败:', response.data?.message);
      allExams.value = [];
      await nextTick();
      initOrUpdateChart();
      initOrUpdateLineChart();
    }
  } catch (error) {
    console.error('获取学生考试数据异常:', error);
    allExams.value = [];
    await nextTick();
    initOrUpdateChart();
    initOrUpdateLineChart();
  } finally {
    loading.value = false;
  }
};

const goToExamList = () => {
  router.push('/student/exams');
};

const initOrUpdateChart = () => {
  if (!pieChartRef.value || pieChartData.value.length === 0) {
     pieChartInstance?.dispose();
     pieChartInstance = null;
     if(pieChartRef.value) pieChartRef.value.innerHTML = '<div style="text-align: center; color: #999; padding-top: 100px;">暂无考试状态数据</div>';
     return;
  }
   if (pieChartRef.value && pieChartRef.value.innerHTML.includes('暂无考试状态数据')) pieChartRef.value.innerHTML = '';

  if (!pieChartInstance) {
    pieChartInstance = echarts.init(pieChartRef.value);
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      left: 'center',
      bottom: 0
    },
    series: [
      {
        name: '考试状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'outside',
           formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
             show: true,
             fontSize: '14',
             fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
           show: true
        },
        data: pieChartData.value
      }
    ]
  };

  pieChartInstance.setOption(option);
};

const initOrUpdateLineChart = () => {
  if (!lineChartRef.value || lineChartData.value.scores.length === 0) {
     lineChartInstance?.dispose();
     lineChartInstance = null;
     if(lineChartRef.value && lineChartRef.value.firstChild) {
       lineChartRef.value.innerHTML = '';
     }
     return;
  }

  if (!lineChartInstance) {
    if(lineChartRef.value && lineChartRef.value.firstChild) {
       lineChartRef.value.innerHTML = '';
    }
    lineChartInstance = echarts.init(lineChartRef.value);
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: lineChartData.value.examNames,
       axisLabel: {
         interval: 0,
         rotate: 30,
         fontSize: 10
       }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '考试成绩',
        type: 'line',
        smooth: true,
        data: lineChartData.value.scores,
        itemStyle: {
           color: '#409EFF'
        },
        areaStyle: {
           color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0)' }
           ])
        },
         markPoint: {
           data: [
             { type: 'max', name: '最高分' },
             { type: 'min', name: '最低分' }
           ]
         },
         markLine: {
            data: [{ type: 'average', name: '平均分' }]
         }
      }
    ]
  };

  lineChartInstance.setOption(option);
};

const getStatusType = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'PUBLISHED') { return 'success'; }
  if (studentStatus === 'PENDING_PUBLISH') { return 'warning'; }
  if (studentStatus === 'SUBMITTED') { return 'info'; }
  if (studentStatus === 'IN_PROGRESS') { return 'warning'; }
  switch (examStatus) {
    case 'PUBLISHED':
    case 'ONGOING': return 'warning';
    case 'FINISHED': return 'info';
    default: return 'info';
  }
};

const getStatusText = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'PUBLISHED') { return '已完成'; }
  if (studentStatus === 'PENDING_PUBLISH') { return '待发布'; }
  if (studentStatus === 'SUBMITTED') { return '已提交'; }
  if (studentStatus === 'IN_PROGRESS') { return '进行中'; }
  if (studentStatus === 'NOT_STARTED') {
    switch (examStatus) {
      case 'PUBLISHED':
      case 'ONGOING': return '待参加';
      case 'FINISHED': return '已结束 (未参加)';
      default: return '未开始';
    }
  }
  if (examStatus === 'FINISHED' && !studentStatus) { return '已结束 (未参加)'; }
  return '未知状态';
};

onMounted(() => {
  fetchStudentExamData();

  const resizeChart = () => {
    pieChartInstance?.resize();
    lineChartInstance?.resize();
  };
  window.addEventListener('resize', resizeChart);

  onUnmounted(() => {
    window.removeEventListener('resize', resizeChart);
    pieChartInstance?.dispose();
    lineChartInstance?.dispose();
  });
});

</script>

<style scoped>
.dashboard-card {
    margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #606266;
}
.bg-blue-100 { background-color: #ecf5ff; }
.text-blue-500 { color: #409eff; }
.bg-green-100 { background-color: #f0f9eb; }
.text-green-500 { color: #67c23a; }
.bg-yellow-100 { background-color: #fdf6ec; }
.text-yellow-500 { color: #e6a23c; }

.el-table .el-tooltip__popper {
  max-width: 300px;
}

.el-alert {
  margin-bottom: 0;
}
</style> 