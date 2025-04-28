<template>
  <div class="teacher-dashboard page-container">
    <div class="dashboard-header">
      <h1>教师工作台</h1>
      <span class="subtitle">欢迎回来，今天是 {{ currentDate }}</span>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <!-- 统计卡片区域 -->
      <el-col :span="12">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#409EFF"><User /></el-icon>
            <div class="card-details">
              <div class="card-value">{{ stats.studentCount }}</div>
              <div class="card-label">管理学生数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#67C23A"><Document /></el-icon>
            <div class="card-details">
              <div class="card-value">{{ stats.examCount }}</div>
              <div class="card-label">已发布考试数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <!-- 学生成绩分布图表 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>学生成绩分布</span>
              <el-select v-model="selectedExam" placeholder="选择考试" size="small">
                <el-option
                  v-for="exam in examOptions"
                  :key="exam.value"
                  :label="exam.label"
                  :value="exam.value"
                />
              </el-select>
            </div>
          </template>
          <v-chart class="chart" :option="gradeDistributionOption" autoresize />
        </el-card>
      </el-col>
      
      <!-- 考试参与情况饼图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>考试参与情况</span>
            </div>
          </template>
          <v-chart class="chart" :option="examParticipationOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <!-- 考试成绩趋势 -->
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>考试成绩趋势</span>
              <el-radio-group v-model="trendTimeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="semester">本学期</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart class="chart trend-chart" :option="gradeTrendOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { User, Document } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { 
  LegendComponent, 
  TooltipComponent, 
  GridComponent, 
  DatasetComponent,
  TitleComponent,
  DataZoomComponent
} from 'echarts/components'
import axios from '../../utils/axios'
import { ElMessage, ElEmpty } from 'element-plus'

// 注册必要的 echarts 组件
use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  LegendComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TitleComponent,
  DataZoomComponent
])

// Define interface for the backend response
interface TeacherStats {
  studentCount: number;
  examCount: number;
}

// Define interface for exam options
interface ExamOption {
  value: number; // Assuming exam ID is number
  label: string;
}

// Define interface for grade distribution response
interface GradeDistribution {
  [key: string]: number; // e.g., "不及格": 2, "60-70分": 3
}

// Define interface for participation stats response
interface ParticipationStats {
  [key: string]: number; // e.g., "未开始": 1, "进行中": 2
}

// Define interface for grade trend response
interface GradeTrendData {
  dates: string[];
  averageScores: number[];
  highestScores: number[];
}

const userStore = useUserStore()
const router = useRouter()

// 基础数据
const stats = reactive<TeacherStats>({
  studentCount: 0,
  examCount: 0,
})
const selectedExam = ref<number | null>(null);
const trendTimeRange = ref('month')
const gradeDistributionData = ref<number[]>([]) // Store fetched grade distribution data
const isLoadingGradeDistribution = ref(false) // Loading state for the chart
const examParticipationData = ref<{ value: number, name: string }[]>([]) // Store pie chart data
const isLoadingParticipation = ref(false) // Loading state for pie chart
const gradeTrendData = ref<GradeTrendData>({ dates: [], averageScores: [], highestScores: [] }); // Store trend data
const isLoadingGradeTrend = ref(false); // Loading state for trend chart

// 格式化当前日期
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

// 考试选项 - Will be fetched from API
const examOptions = ref<ExamOption[]>([]) // Initialize as empty array

// 获取成绩分布数据
const fetchGradeDistribution = async (examId: number | null) => {
  if (typeof examId !== 'number' || examId <= 0) { 
    gradeDistributionData.value = []
    console.log('Invalid or no examId provided for grade distribution fetch.');
    return
  }
  isLoadingGradeDistribution.value = true
  try {
    const response = await axios.get<GradeDistribution>(`/api/dashboard/teacher/exam/${examId}/grades`)
    const orderedData = [
      response.data["不及格"] || 0,
      response.data["60-70分"] || 0,
      response.data["70-80分"] || 0,
      response.data["80-90分"] || 0,
      response.data["90-100分"] || 0,
    ]
    gradeDistributionData.value = orderedData
  } catch (error) {
    console.error(`获取考试 ${examId} 成绩分布失败:`, error)
    // Only show error message if it's not a 400 likely caused by invalid ID selection initially
    if ((error as any).response?.status !== 400) {
        ElMessage.error('加载成绩分布数据失败')
    }
    gradeDistributionData.value = []
  } finally {
    isLoadingGradeDistribution.value = false
  }
}

// 获取考试参与情况统计
const fetchExamParticipationStats = async () => {
  isLoadingParticipation.value = true;
  try {
    const response = await axios.get<ParticipationStats>('/api/dashboard/teacher/participation-stats');
    if (response.data) {
      examParticipationData.value = Object.entries(response.data)
        .map(([name, value]) => ({ name, value }))
        .filter(item => item.value > 0); // Only show categories with count > 0
    }
  } catch (error) {
    console.error('获取考试参与情况失败:', error);
    ElMessage.error('加载考试参与情况数据失败');
    examParticipationData.value = []; // Clear data on error
  } finally {
    isLoadingParticipation.value = false;
  }
};

// 获取成绩趋势数据
const fetchGradeTrend = async (range: string) => {
  isLoadingGradeTrend.value = true;
  try {
    const response = await axios.get<GradeTrendData>('/api/dashboard/teacher/grade-trend', {
      params: { range: range }
    });
    gradeTrendData.value = response.data;
  } catch (error) {
    console.error(`获取成绩趋势 (${range}) 失败:`, error);
    ElMessage.error('加载成绩趋势数据失败');
    gradeTrendData.value = { dates: [], averageScores: [], highestScores: [] }; // Clear data on error
  } finally {
    isLoadingGradeTrend.value = false;
  }
};

// 学生成绩分布图表配置
const gradeDistributionOption = computed(() => {
  const selectedExamLabel = selectedExam.value !== null && Array.isArray(examOptions.value)
    ? examOptions.value.find(o => o.value === selectedExam.value)?.label || '未知考试'
    : null;

  return {
    title: {
      text: selectedExamLabel ? `${selectedExamLabel} 成绩分布` : '请选择考试查看成绩分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['不及格', '60-70分', '70-80分', '80-90分', '90-100分']
    },
    yAxis: { type: 'value', name: '学生人数' },
    series: [
      {
        name: '学生人数',
        type: 'bar',
        data: isLoadingGradeDistribution.value ? [] : gradeDistributionData.value,
        itemStyle: {
          color: function(params: any) {
            const colors = ['#F56C6C', '#E6A23C', '#67C23A', '#409EFF', '#9B59B6'];
            return colors[params.dataIndex];
          }
        }
      }
    ]
  }
})

// 考试参与情况饼图配置
const examParticipationOption = computed(() => {
  return {
    title: {
      text: '考试参与情况',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 'bottom'
    },
    series: [
      {
        name: '考试情况',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: isLoadingParticipation.value ? [] : examParticipationData.value, // Use fetched data
        // Optional: Define colors for specific statuses if needed
        color: ['#909399', '#E6A23C', '#F56C6C', '#67C23A'] // Example: 未开始, 进行中, 待批阅, 已完成
      }
    ]
  }
})

// 考试成绩趋势图配置
const gradeTrendOption = computed(() => {
  return {
    title: {
      text: '考试成绩趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均分', '最高分'], bottom: 'bottom' },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: isLoadingGradeTrend.value ? [] : gradeTrendData.value.dates // Use fetched dates
    },
    yAxis: { type: 'value', name: '分数', min: 0, max: 100 }, // Start y-axis from 0
    series: [
      {
        name: '平均分',
        type: 'line',
        data: isLoadingGradeTrend.value ? [] : gradeTrendData.value.averageScores, // Use fetched avg scores
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '最高分',
        type: 'line',
        data: isLoadingGradeTrend.value ? [] : gradeTrendData.value.highestScores, // Use fetched max scores
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})

// 获取考试选项列表
const fetchExamOptions = async () => {
  try {
    // Adjust expected response type to match the actual wrapped structure
    const response = await axios.get<{ code: number; data: ExamOption[]; message: string }>('/api/supervisor/exams/options')
    console.log("API Response /api/supervisor/exams/options:", response.data); 
    // Check if the outer response has data and the inner data is an array
    if (response.data && Array.isArray(response.data.data)) {
      examOptions.value = response.data.data // Access the inner data array
      if (examOptions.value.length > 0 && selectedExam.value === null) {
         selectedExam.value = examOptions.value[0].value 
      }
       else if (examOptions.value.length === 0){
          selectedExam.value = null
          gradeDistributionData.value = []
       }
    } else {
      console.error('获取考试选项失败: 返回的数据结构不符合预期或内部数据不是数组', response.data);
      ElMessage.error('加载考试列表失败: 数据格式错误');
      examOptions.value = [];
      selectedExam.value = null;
      gradeDistributionData.value = [];
    }
  } catch (error) {
    console.error('获取考试选项失败:', error);
    ElMessage.error('加载考试列表失败');
    examOptions.value = [];
    selectedExam.value = null;
    gradeDistributionData.value = [];
  }
}

// 获取仪表盘统计数据
const fetchDashboardData = async () => {
  isLoadingGradeDistribution.value = true; 
  isLoadingParticipation.value = true;
  isLoadingGradeTrend.value = true; 
  try {
    // Use Promise.allSettled to allow partial loading even if one API fails
    const results = await Promise.allSettled([
      axios.get<TeacherStats>('/api/dashboard/teacher/stats'),
      fetchExamOptions(), // fetchExamOptions handles its own error display
      fetchExamParticipationStats(),
      fetchGradeTrend(trendTimeRange.value),
    ]);

    // Process successful results
    if (results[0].status === 'fulfilled') {
        stats.studentCount = results[0].value.data.studentCount;
        stats.examCount = results[0].value.data.examCount;
    } else {
        console.error('获取基础统计数据失败:', results[0].reason);
        ElMessage.error('加载基础统计数据失败');
    }
    // Other fetches handle their own errors/loading states

  } catch (error) { // Catch errors from Promise.allSettled itself if any
    console.error('获取仪表盘数据时发生意外错误:', error);
    ElMessage.error('加载仪表盘数据时发生意外错误');
    isLoadingGradeDistribution.value = false;
    isLoadingParticipation.value = false;
    isLoadingGradeTrend.value = false;
  }
  // Note: loading states for charts are reset within their respective fetch functions
};

// 页面加载时获取数据
onMounted(() => {
  fetchDashboardData()
})

// Watch for changes in selectedExam
watch(selectedExam, (newExamId, oldExamId) => {
  // Prevent initial fetch if oldExamId is also null
  if (newExamId !== oldExamId) { 
    fetchGradeDistribution(newExamId);
  }
});

// Watch for changes in trendTimeRange
watch(trendTimeRange, (newRange) => {
  fetchGradeTrend(newRange);
});
</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
  /* Add height and overflow for scrolling */
  height: calc(100vh - 50px); /* Adjust 50px based on header height */
  overflow-y: auto;
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.stat-cards {
  margin-bottom: 20px;
}

.summary-card .card-content {
  display: flex;
  align-items: center;
}

.summary-card .card-icon {
  font-size: 40px;
  margin-right: 20px;
}

.summary-card .card-details {
  display: flex;
  flex-direction: column;
}

.summary-card .card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.summary-card .card-label {
  font-size: 14px;
  color: #606266;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 300px;
}

.trend-chart {
  height: 350px;
}
</style> 