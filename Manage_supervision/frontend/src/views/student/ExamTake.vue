<template>
  <div class="exam-take-container">
    <el-card v-loading="loading" class="exam-card">
      <!-- 考试信息和倒计时 -->
      <div class="exam-header">
        <div class="exam-title">{{ exam ? exam.title : '加载中...' }}</div>
        <div class="exam-timer" v-if="exam && !submitted">
          <el-icon><Timer /></el-icon>
          <span class="remaining-time">剩余时间: {{ formatRemainingTime }}</span>
        </div>
      </div>
      
      <!-- 考试主体 -->
      <div v-if="exam && questions.length > 0 && !submitted" class="exam-body">
        <div class="questions-navigation">
          <div class="navigation-title">题目导航</div>
          <div class="question-buttons">
            <el-button
              v-for="(q, index) in questions"
              :key="q.id"
              :type="isQuestionAnswered(q.id) ? 'primary' : 'default'"
              size="small"
              @click="currentQuestionIndex = index"
              :class="{ 'current-question': currentQuestionIndex === index }"
            >
              {{ index + 1 }}
            </el-button>
          </div>
          <div class="navigation-bottom">
            <div class="question-status">
              <div class="status-item">
                <div class="status-indicator answered"></div>
                <span>已答题</span>
              </div>
              <div class="status-item">
                <div class="status-indicator unanswered"></div>
                <span>未答题</span>
              </div>
            </div>
            <div class="answered-count">
              已答: {{ Object.keys(answers).length }} / {{ questions.length }}
            </div>
          </div>
        </div>
        
        <div class="question-content">
          <div class="question-header">
            <div class="question-number">第 {{ currentQuestionIndex + 1 }} 题</div>
            <div class="question-type">
              [{{ getQuestionTypeText(currentQuestion.questionType) }}]
              <span class="question-score">{{ currentQuestion.questionScore }} 分</span>
            </div>
          </div>
          
          <div class="question-title">{{ currentQuestion.questionTitle }}</div>
          
          <!-- 单选题 -->
          <div v-if="currentQuestion.questionType === 'SINGLE_CHOICE'" class="question-options">
            <el-radio-group v-model="answers[currentQuestion.id]">
              <el-radio v-for="(option, optionKey) in parseOptions(currentQuestion)" :key="optionKey" :label="optionKey">
                {{ optionKey }}. {{ option }}
              </el-radio>
            </el-radio-group>
          </div>
          
          <!-- 多选题 -->
          <div v-else-if="currentQuestion.questionType === 'MULTIPLE_CHOICE'" class="question-options">
            <el-checkbox-group v-model="multipleChoiceAnswers[currentQuestion.id]" @change="updateMultipleChoiceAnswer">
              <el-checkbox v-for="(option, optionKey) in parseOptions(currentQuestion)" :key="optionKey" :label="optionKey">
                {{ optionKey }}. {{ option }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          
          <!-- 判断题 -->
          <div v-else-if="currentQuestion.questionType === 'JUDGMENT'" class="question-options">
            <el-radio-group v-model="answers[currentQuestion.id]">
              <el-radio label="T">正确</el-radio>
              <el-radio label="F">错误</el-radio>
            </el-radio-group>
          </div>
          
          <!-- 主观题 -->
          <div v-else-if="currentQuestion.questionType === 'ESSAY'" class="question-essay">
            <el-input
              v-model="answers[currentQuestion.id]"
              type="textarea"
              :rows="6"
              placeholder="请在此输入您的答案..."
            />
          </div>
          
          <div class="question-navigation">
            <el-button 
              :disabled="currentQuestionIndex === 0" 
              @click="currentQuestionIndex--"
              plain
            >
              上一题
            </el-button>
            <el-button 
              :disabled="currentQuestionIndex === questions.length - 1" 
              @click="currentQuestionIndex++"
              type="primary"
              plain
            >
              下一题
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 考试提交确认 -->
      <div class="exam-footer" v-if="exam && !submitted">
        <el-button type="danger" @click="showSubmitConfirm = true" :disabled="Object.keys(answers).length === 0">
          提交考试
        </el-button>
      </div>
      
      <!-- 提交成功 -->
      <div v-if="submitted" class="submit-success">
        <el-result
          icon="success"
          title="考试提交成功"
          sub-title="您的考试已成功提交，等待批阅"
        >
          <template #extra>
            <el-button type="primary" @click="goToExamList">返回考试列表</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
    
    <!-- 提交确认对话框 -->
    <el-dialog
      v-model="showSubmitConfirm"
      title="确认提交"
      width="30%"
      :close-on-click-modal="false"
    >
      <div class="submit-confirm-content">
        <p>您即将提交本次考试，提交后将无法再进行修改。</p>
        <p>您已回答 {{ Object.keys(answers).length }} 道题，共 {{ questions.length }} 道题。</p>
        <p v-if="Object.keys(answers).length < questions.length" class="warning-text">
          您还有 {{ questions.length - Object.keys(answers).length }} 道题未作答，确定要提交吗？
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSubmitConfirm = false">取消</el-button>
          <el-button type="primary" @click="submitExam" :loading="submitting">
            确认提交
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 时间到对话框 -->
    <el-dialog
      v-model="showTimeUpDialog"
      title="考试时间已到"
      width="30%"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="time-up-content">
        <p>考试时间已经结束，系统将自动提交您的答案。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="submitExam" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Timer } from '@element-plus/icons-vue';
import { getStudentExam, startExam, submitExam } from '@/api/exam';

const router = useRouter();
const route = useRoute();
const examId = route.params.id;

const loading = ref(false);
const submitting = ref(false);
const submitted = ref(false);
const exam = ref(null);
const questions = ref([]);
const currentQuestionIndex = ref(0);
const answers = ref({});
const multipleChoiceAnswers = ref({});
const remainingSeconds = ref(0);
const timer = ref(null);
const showSubmitConfirm = ref(false);
const showTimeUpDialog = ref(false);

// 计算当前题目
const currentQuestion = computed(() => {
  if (questions.value.length === 0) return null;
  return questions.value[currentQuestionIndex.value];
});

// 格式化剩余时间
const formatRemainingTime = computed(() => {
  const hours = Math.floor(remainingSeconds.value / 3600);
  const minutes = Math.floor((remainingSeconds.value % 3600) / 60);
  const seconds = remainingSeconds.value % 60;
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
});

// 获取考试详情并开始考试
const initExam = async () => {
  loading.value = true;
  try {
    // 获取考试详情
    const detailResponse = await getStudentExam(Number(examId));
    
    if (!detailResponse.data || !detailResponse.data.success) {
      ElMessage.error(detailResponse.data?.message || '获取考试详情失败');
      router.push('/student/exams');
      return;
    }
    
    const examData = detailResponse.data.data.exam;
    const studentStatus = detailResponse.data.data.studentStatus;
    
    // 检查考试是否可以参加
    const now = new Date().getTime();
    const startTime = new Date(examData.startTime).getTime();
    const endTime = new Date(examData.endTime).getTime();
    
    if (now < startTime) {
      ElMessage.error('考试尚未开始');
      router.push(`/student/exams/${examId}`);
      return;
    }
    
    if (now > endTime) {
      ElMessage.error('考试已经结束');
      router.push(`/student/exams/${examId}`);
      return;
    }
    
    if (studentStatus.status === 'SUBMITTED' || studentStatus.status === 'GRADED') {
      ElMessage.error('您已经提交过该考试');
      router.push(`/student/exams/${examId}`);
      return;
    }
    
    // 开始考试
    const startResponse = await startExam(Number(examId));
    
    if (!startResponse.data || !startResponse.data.success) {
      ElMessage.error(startResponse.data?.message || '开始考试失败');
      router.push('/student/exams');
      return;
    }
    
    const data = startResponse.data.data;
    exam.value = examData;
    questions.value = data.questions;
    
    // 初始化多选题答案
    questions.value.forEach(q => {
      if (q.questionType === 'MULTIPLE_CHOICE') {
        multipleChoiceAnswers.value[q.id] = [];
      }
    });
    
    // 计算剩余时间
    let elapsedMinutes = 0;
    
    if (studentStatus.status === 'IN_PROGRESS' && studentStatus.startTime) {
      const startTimeMs = new Date(studentStatus.startTime).getTime();
      const elapsedMs = now - startTimeMs;
      elapsedMinutes = Math.floor(elapsedMs / (1000 * 60));
    }
    
    const remainingMinutes = Math.max(0, examData.duration - elapsedMinutes);
    remainingSeconds.value = remainingMinutes * 60;
    
    // 开始倒计时
    startTimer();
    
  } catch (error) {
    console.error('初始化考试失败', error);
    ElMessage.error('初始化考试失败');
  } finally {
    loading.value = false;
  }
};

// 开始倒计时
const startTimer = () => {
  if (timer.value) {
    clearInterval(timer.value);
  }
  
  timer.value = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      clearInterval(timer.value);
      showTimeUpDialog.value = true;
      return;
    }
    remainingSeconds.value--;
  }, 1000);
};

// 解析选项
const parseOptions = (question) => {
  if (!question || !question.content) return {};
  
  try {
    const content = JSON.parse(question.content);
    return content.options || {};
  } catch (e) {
    return {};
  }
};

// 获取题目类型文本
const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'JUDGMENT': '判断题',
    'ESSAY': '简答题'
  };
  
  return typeMap[type] || type;
};

// 更新多选题答案
const updateMultipleChoiceAnswer = () => {
  if (!currentQuestion.value) return;
  
  const questionId = currentQuestion.value.id;
  if (multipleChoiceAnswers.value[questionId]) {
    // 将多选的选项合并为字符串，如 "A,B,C"
    answers.value[questionId] = multipleChoiceAnswers.value[questionId].sort().join(',');
  }
};

// 检查题目是否已答
const isQuestionAnswered = (questionId) => {
  return answers.value[questionId] !== undefined && answers.value[questionId] !== '';
};

// 提交考试
const submitExam = async () => {
  submitting.value = true;
  try {
    const response = await submitExam(Number(examId), answers.value);
    
    if (response.data && response.data.success) {
      submitted.value = true;
      showSubmitConfirm.value = false;
      showTimeUpDialog.value = false;
      clearInterval(timer.value);
      ElMessage.success('考试提交成功');
    } else {
      ElMessage.error(response.data?.message || '提交考试失败');
    }
  } catch (error) {
    console.error('提交考试失败', error);
    ElMessage.error('提交考试失败');
  } finally {
    submitting.value = false;
  }
};

// 返回考试列表
const goToExamList = () => {
  router.push('/student/exams');
};

// 离开页面前确认
const confirmLeave = (e) => {
  if (!submitted.value) {
    e.preventDefault();
    e.returnValue = '';
    return '';
  }
};

// 监听路由变化
watch(() => route.path, (newPath, oldPath) => {
  if (oldPath.includes('/take') && !submitted.value) {
    const confirmed = confirm('考试尚未提交，确定要离开吗？');
    if (!confirmed) {
      router.push(oldPath);
    }
  }
});

onMounted(() => {
  initExam();
  // 添加页面离开确认
  window.addEventListener('beforeunload', confirmLeave);
});

onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value);
  }
  window.removeEventListener('beforeunload', confirmLeave);
});
</script>

<style scoped>
.exam-take-container {
  padding: 20px;
}

.exam-card {
  margin-bottom: 20px;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.exam-title {
  font-size: 20px;
  font-weight: bold;
}

.exam-timer {
  display: flex;
  align-items: center;
  font-size: 18px;
  color: #f56c6c;
}

.exam-timer .el-icon {
  margin-right: 5px;
}

.exam-body {
  display: flex;
  min-height: 500px;
}

.questions-navigation {
  width: 200px;
  border-right: 1px solid #e4e7ed;
  padding-right: 20px;
}

.navigation-title {
  font-weight: bold;
  margin-bottom: 15px;
}

.question-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.question-buttons .el-button {
  margin: 0;
}

.current-question {
  border: 2px solid #409EFF !important;
}

.navigation-bottom {
  margin-top: 20px;
}

.question-status {
  margin-bottom: 10px;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.status-indicator {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  margin-right: 8px;
}

.status-indicator.answered {
  background-color: #409EFF;
}

.status-indicator.unanswered {
  background-color: #dcdfe6;
}

.answered-count {
  margin-top: 10px;
  font-weight: bold;
}

.question-content {
  flex: 1;
  padding-left: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
}

.question-type {
  color: #606266;
}

.question-score {
  color: #e6a23c;
  margin-left: 5px;
}

.question-title {
  font-size: 16px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.question-options {
  margin-bottom: 30px;
}

.question-options .el-radio, .question-options .el-checkbox {
  margin-bottom: 15px;
  display: block;
}

.question-essay {
  margin-bottom: 30px;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.exam-footer {
  margin-top: 30px;
  text-align: center;
}

.submit-confirm-content, .time-up-content {
  text-align: center;
}

.warning-text {
  color: #f56c6c;
}

.submit-success {
  padding: 40px 0;
}
</style> 