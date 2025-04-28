<template>
  <div class="exam-detail-container">
    <div class="back-link">
      <el-link :underline="false" @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回考试列表
      </el-link>
    </div>
    
    <el-card v-loading="loading" class="exam-card">
      <div v-if="exam" class="exam-info">
        <h2 class="exam-title">{{ exam.title }}</h2>
        
        <div class="exam-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>开始时间：{{ formatDateTime(exam.startTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>结束时间：{{ formatDateTime(exam.endTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Timer /></el-icon>
            <span>考试时长：{{ exam.duration }} 分钟</span>
          </div>
          <div class="meta-item">
            <el-icon><Reading /></el-icon>
            <span>总分：{{ exam.totalScore }} 分</span>
          </div>
          <div class="meta-item">
            <el-icon><Pointer /></el-icon>
            <span>及格分数：{{ exam.passingScore }} 分</span>
          </div>
          <div class="meta-item">
            <el-icon><List /></el-icon>
            <span>题目数量：{{ exam.examQuestions ? exam.examQuestions.length : 0 }}</span>
          </div>
        </div>
        
        <div class="exam-status">
          <div class="status-label">考试状态：</div>
          <el-tag :type="getStatusType(exam.status, studentStatus ? studentStatus.status : '')">
            {{ getStatusText(exam.status, studentStatus ? studentStatus.status : '') }}
          </el-tag>
        </div>
        
        <div v-if="studentStatus && (studentStatus.status === 'SUBMITTED' || studentStatus.status === 'GRADED')" class="exam-result">
          <div class="result-item">
            <div class="result-label">开始时间：</div>
            <div class="result-value">{{ formatDateTime(studentStatus.startTime) }}</div>
          </div>
          <div class="result-item">
            <div class="result-label">提交时间：</div>
            <div class="result-value">{{ formatDateTime(studentStatus.submitTime) }}</div>
          </div>
          <div class="result-item">
            <div class="result-label">得分：</div>
            <div class="result-value">
              <strong>{{ studentStatus.score }}</strong> / {{ exam.totalScore }}
              <el-tag v-if="studentStatus.isPassed" type="success" style="margin-left: 10px">及格</el-tag>
              <el-tag v-else type="danger" style="margin-left: 10px">不及格</el-tag>
            </div>
          </div>
        </div>
        
        <div class="exam-description">
          <div class="description-title">考试说明：</div>
          <div class="description-content">{{ exam.description || '无' }}</div>
        </div>
        
        <div class="exam-actions">
          <el-button
            v-if="canTakeExam()"
            type="primary"
            size="large"
            :icon="studentStatus && studentStatus.status === 'IN_PROGRESS' ? 'el-icon-video-play' : 'el-icon-video-play'"
            @click="showStartConfirm"
            :disabled="!canTakeExam()"
            class="start-exam-btn"
          >
            <el-icon class="start-icon"><VideoPlay /></el-icon>
            {{ studentStatus && studentStatus.status === 'IN_PROGRESS' ? '继续考试' : '开始考试' }}
          </el-button>
          
          <el-button
            v-if="studentStatus && (studentStatus.status === 'SUBMITTED' || studentStatus.status === 'GRADED')"
            type="info"
            @click="viewResult"
          >
            查看结果
          </el-button>
          
          <el-button type="default" @click="goBack">返回</el-button>
        </div>
        
        <div v-if="!canTakeExam() && (!studentStatus || studentStatus.status === 'NOT_STARTED')" class="exam-hint">
          <el-alert
            v-if="isBeforeExam()"
            title="考试尚未开始"
            type="info"
            description="请在考试开始时间后再来参加考试。"
            show-icon
          />
          <el-alert
            v-else-if="isAfterExam()"
            title="考试已结束"
            type="warning"
            description="很遗憾，考试时间已过，无法参加考试。"
            show-icon
          />
        </div>
      </div>
    </el-card>
    
    <!-- 开始考试确认对话框 -->
    <el-dialog
      v-model="startConfirmVisible"
      title="开始考试确认"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="start-confirm-content">
        <p class="confirm-title">您即将开始 <strong>{{ exam?.title }}</strong> 考试</p>
        <ul class="confirm-info">
          <li>考试时长：{{ exam?.duration }} 分钟</li>
          <li>考试题目数量：{{ exam?.examQuestions ? exam?.examQuestions.length : 0 }} 题</li>
          <li>总分：{{ exam?.totalScore }} 分</li>
        </ul>
        <p class="confirm-notice">
          <el-icon><Warning /></el-icon> 
          开始考试后，计时将立即开始。请确保您已准备好并有充足的时间完成考试。
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="startConfirmVisible = false">取消</el-button>
          <el-button type="primary" @click="startExam" :loading="startLoading">
            确认开始
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Calendar, Timer, Reading, Pointer, List, ArrowLeft, VideoPlay, Warning } from '@element-plus/icons-vue';
import { getStudentExam, startExam as startExamApi } from '@/api/exam';
import { formatDateTime } from '@/utils/formatter';

const router = useRouter();
const route = useRoute();
const examId = route.params.id;

const loading = ref(false);
const startLoading = ref(false);
const exam = ref(null);
const studentStatus = ref(null);
const startConfirmVisible = ref(false);

// 获取考试详情
const fetchExamDetail = async () => {
  loading.value = true;
  try {
    const response = await getStudentExam(Number(examId));
    
    if (response.data && response.data.success) {
      const data = response.data.data;
      exam.value = data.exam;
      studentStatus.value = data.studentStatus;
    } else {
      ElMessage.error(response.data.message || '获取考试详情失败');
    }
  } catch (error) {
    console.error('获取考试详情失败', error);
    ElMessage.error('获取考试详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取考试状态样式
const getStatusType = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'GRADED') {
    return 'success';
  }
  
  if (studentStatus === 'SUBMITTED') {
    return 'info';
  }
  
  if (studentStatus === 'IN_PROGRESS') {
    return 'warning';
  }
  
  // 检查考试整体状态
  switch (examStatus) {
    case 'PUBLISHED':
      return 'primary';
    case 'ONGOING':
      return 'warning';
    case 'FINISHED':
      return 'info';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'GRADED') {
    return '已完成';
  }
  
  if (studentStatus === 'SUBMITTED') {
    return '已提交';
  }
  
  if (studentStatus === 'IN_PROGRESS') {
    return '进行中';
  }
  
  if (studentStatus === 'NOT_STARTED') {
    switch (examStatus) {
      case 'PUBLISHED':
      case 'ONGOING':
        const now = new Date().getTime();
        const startTime = exam.value ? new Date(exam.value.startTime).getTime() : 0;
        const endTime = exam.value ? new Date(exam.value.endTime).getTime() : 0;
        
        if (now < startTime) {
          return '未开始';
        } else if (now > endTime) {
          return '已结束';
        } else {
          return '待参加';
        }
      case 'FINISHED':
        return '已结束';
      default:
        return '未开始';
    }
  }
  
  return '未知状态';
};

// 判断是否可以参加考试
const canTakeExam = () => {
  if (!exam.value) return false;
  
  // 如果已经提交或评分，则不能再次参加
  if (studentStatus.value && (studentStatus.value.status === 'SUBMITTED' || studentStatus.value.status === 'GRADED')) {
    return false;
  }
  
  // 如果考试状态是进行中或发布，且当前时间在考试时间范围内
  const now = new Date().getTime();
  const startTime = new Date(exam.value.startTime).getTime();
  const endTime = new Date(exam.value.endTime).getTime();
  
  return (exam.value.status === 'ONGOING' || exam.value.status === 'PUBLISHED') && 
         now >= startTime && now <= endTime;
};

// 判断是否在考试开始前
const isBeforeExam = () => {
  if (!exam.value) return false;
  
  const now = new Date().getTime();
  const startTime = new Date(exam.value.startTime).getTime();
  
  return now < startTime;
};

// 判断是否在考试结束后
const isAfterExam = () => {
  if (!exam.value) return false;
  
  const now = new Date().getTime();
  const endTime = new Date(exam.value.endTime).getTime();
  
  return now > endTime;
};

// 显示开始考试确认对话框
const showStartConfirm = () => {
  startConfirmVisible.value = true;
};

// 开始考试
const startExam = async () => {
  startLoading.value = true;
  console.log('开始考试函数被调用，考试ID:', examId);
  
  try {
    // 提前保存考试状态到localStorage，以便在API调用期间路由守卫可以正确处理
    localStorage.setItem('current_exam_id', examId.toString());
    localStorage.setItem('exam_start_time', new Date().toString());
    
    // 调用后端API真正开始考试
    console.log('正在调用开始考试API...');
    const response = await startExamApi(Number(examId));
    console.log('开始考试API响应:', response);
    
    if (response.data && response.data.success) {
      console.log('开始考试成功，准备跳转到考试页面');
      ElMessage.success('正在进入考试...');
      startConfirmVisible.value = false;
      
      // 延迟跳转，确保消息显示
      setTimeout(() => {
        // 使用replace而不是push，避免用户可以返回回来
        console.log('跳转到考试页面:', `/student/exams/${examId}/take`);
        
        // 添加时间戳防止缓存并使用replace模式
        router.replace({
          path: `/student/exams/${examId}/take`, 
          query: { t: Date.now().toString() }
        });
      }, 500);
    } else {
      console.error('开始考试API返回失败:', response.data);
      ElMessage.error(response.data?.message || '开始考试失败');
      
      // 清除localStorage中的考试状态
      localStorage.removeItem('current_exam_id');
      localStorage.removeItem('exam_start_time');
    }
  } catch (error) {
    console.error('开始考试请求异常:', error);
    ElMessage.error('开始考试失败，请重试');
    
    // 清除localStorage中的考试状态
    localStorage.removeItem('current_exam_id');
    localStorage.removeItem('exam_start_time');
  } finally {
    startLoading.value = false;
  }
};

// 查看结果
const viewResult = () => {
  router.push(`/student/exams/${examId}/result`);
};

// 返回考试列表
const goBack = () => {
  router.push('/student/exams');
};

onMounted(() => {
  fetchExamDetail();
});
</script>

<style scoped>
.exam-detail-container {
  padding: 20px;
}

.back-link {
  margin-bottom: 15px;
}

.exam-card {
  margin-bottom: 20px;
}

.exam-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.exam-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.exam-status {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.status-label {
  margin-right: 10px;
  font-weight: bold;
}

.exam-result {
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.result-item {
  display: flex;
  margin-bottom: 10px;
}

.result-label {
  font-weight: bold;
  width: 100px;
}

.exam-description {
  margin-bottom: 25px;
}

.description-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.description-content {
  line-height: 1.6;
  white-space: pre-wrap;
}

.exam-actions {
  margin-top: 30px;
}

.exam-hint {
  margin-top: 25px;
}

/* 新增样式 */
.start-exam-btn {
  padding: 12px 24px;
  font-size: 16px;
  transition: all 0.3s;
}

.start-exam-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.start-icon {
  margin-right: 8px;
  font-size: 18px;
}

.start-confirm-content {
  text-align: center;
  padding: 0 20px;
}

.confirm-title {
  font-size: 18px;
  margin-bottom: 20px;
  color: #303133;
}

.confirm-info {
  text-align: left;
  margin: 0 auto 20px;
  width: fit-content;
  list-style-type: none;
  padding: 0;
}

.confirm-info li {
  margin-bottom: 8px;
  color: #606266;
}

.confirm-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 10px;
  border-radius: 4px;
}

.confirm-notice .el-icon {
  margin-right: 8px;
  font-size: 18px;
}
</style> 