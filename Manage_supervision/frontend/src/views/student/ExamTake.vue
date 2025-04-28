<template>
  <div class="exam-take-container">
    <el-card v-loading="loading" element-loading-text="正在准备考试，请稍候..." :element-loading-spinner="loadingSpinner" :element-loading-background="loadingBackground" class="exam-card">
      <!-- 考试信息和倒计时 -->
      <div class="exam-header">
        <div class="exam-title">{{ exam ? exam.title : '加载中...' }}</div>
        <div class="exam-timer" v-if="exam && !submitted">
          <el-icon><Timer /></el-icon>
          <span class="remaining-time">剩余时间: {{ formatRemainingTime }}</span>
        </div>
      </div>
      
      <!-- 欢迎提示 -->
      <div v-if="initSuccess && !loading && questions.length > 0 && !submitted" class="exam-welcome" v-show="showWelcome">
        <el-alert
          type="success"
          show-icon
          :closable="true"
          @close="showWelcome = false"
        >
          <template #title>
            <div class="welcome-title">考试已开始</div>
          </template>
          <div class="welcome-content">
            <p>欢迎开始《{{ exam.title }}》考试，请注意以下事项：</p>
            <ol>
              <li>考试时长 {{ exam.duration }} 分钟，计时已开始</li>
              <li>本次考试共 {{ questions.length }} 道题目</li>
              <li>请合理安排答题时间，确保能够完成所有题目</li>
              <li>时间结束前，请务必点击【提交考试】按钮</li>
            </ol>
          </div>
        </el-alert>
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
            <el-radio-group v-if="Object.keys(parseOptions(currentQuestion)).length > 0" v-model="answers[currentQuestion.id]">
              <el-radio v-for="(option, optionKey) in parseOptions(currentQuestion)" :key="optionKey" :label="optionKey">
                {{ optionKey }}. {{ option }}
              </el-radio>
            </el-radio-group>
            <el-alert v-else title="题目选项加载失败" type="error" description="无法解析题目选项，请联系管理员。" show-icon :closable="false" />
          </div>
          
          <!-- 多选题 -->
          <div v-else-if="currentQuestion.questionType === 'MULTIPLE_CHOICE'" class="question-options">
            <el-checkbox-group v-if="Object.keys(parseOptions(currentQuestion)).length > 0" v-model="multipleChoiceAnswers[currentQuestion.id]" @change="updateMultipleChoiceAnswer">
              <el-checkbox v-for="(option, optionKey) in parseOptions(currentQuestion)" :key="optionKey" :label="optionKey">
                {{ optionKey }}. {{ option }}
              </el-checkbox>
            </el-checkbox-group>
            <el-alert v-else title="题目选项加载失败" type="error" description="无法解析题目选项，请联系管理员。" show-icon :closable="false" />
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
          <el-button type="primary" @click="handleExamSubmit" :loading="submitting">
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
          <el-button type="primary" @click="handleExamSubmit" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Timer } from '@element-plus/icons-vue';
import { getStudentExam, startExam, submitExam as submitExamApi } from '@/api/exam';

const router = useRouter();
const route = useRoute();
const examId = route.params.id;

const loading = ref(true);
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
const initSuccess = ref(false);
const showWelcome = ref(true);
const loadingSpinner = 'el-icon-loading';
const loadingBackground = 'rgba(255, 255, 255, 0.9)';

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
  console.log('ExamTake初始化，考试ID:', examId);
  
  // 检查是否有从上一页面传来的exam_start_time
  const startTimeFromStorage = localStorage.getItem('exam_start_time');
  const examIdFromStorage = localStorage.getItem('current_exam_id');
  
  if (startTimeFromStorage && examIdFromStorage === examId.toString()) {
    console.log('检测到localStorage中有考试开始信息，时间:', startTimeFromStorage);
  }
  
  try {
    // 获取考试详情
    console.log('正在获取考试详情...');
    const detailResponse = await getStudentExam(Number(examId));
    console.log('获取考试详情响应:', detailResponse);
    
    if (!detailResponse.data || detailResponse.data.code !== 200) {
      console.error('获取考试详情失败:', detailResponse.data);
      ElMessage.error(detailResponse.data?.message || '获取考试详情失败');
      setTimeout(() => router.push('/student/exams'), 1000);
      return;
    }
    
    const examData = detailResponse.data.data.exam;
    const studentStatus = detailResponse.data.data.studentStatus;
    
    console.log('考试详情数据:', examData);
    console.log('学生状态数据:', studentStatus);
    
    // 检查考试是否可以参加
    const now = new Date().getTime();
    const startTime = new Date(examData.startTime).getTime();
    const endTime = new Date(examData.endTime).getTime();
    
    if (now < startTime) {
      console.error('考试尚未开始');
      ElMessage.error('考试尚未开始');
      setTimeout(() => router.push(`/student/exams/${examId}`), 1000);
      return;
    }
    
    if (now > endTime) {
      console.error('考试已经结束');
      ElMessage.error('考试已经结束');
      setTimeout(() => router.push(`/student/exams/${examId}`), 1000);
      return;
    }
    
    if (studentStatus.status === 'SUBMITTED' || studentStatus.status === 'GRADED') {
      console.error('考试已提交');
      ElMessage.error('您已经提交过该考试');
      setTimeout(() => router.push(`/student/exams/${examId}`), 1000);
      return;
    }
    
    // 如果考试还未开始，调用开始考试API
    if (studentStatus.status !== 'IN_PROGRESS') {
      console.log('需要开始考试，正在调用开始考试API...');
      // 开始考试
      const startResponse = await startExam(Number(examId));
      console.log('开始考试API响应:', startResponse);
      
      if (!startResponse.data || startResponse.data.code !== 200) {
        console.error('开始考试失败:', startResponse.data);
        ElMessage.error(startResponse.data?.message || '开始考试失败');
        setTimeout(() => router.push('/student/exams'), 1000);
        return;
      }
      
      const data = startResponse.data.data;
      exam.value = examData;
      questions.value = data.questions;
      
      console.log('成功获取到题目数量:', questions.value.length);
      
      // 显示成功消息
      ElMessage.success('考试已成功开始，请认真答题！');
    } else {
      console.log('考试进行中，正在恢复考试状态...');
      // 如果考试已经在进行中，直接恢复状态
      // 再次调用开始API获取题目列表
      const startResponse = await startExam(Number(examId));
      console.log('恢复考试状态API响应:', startResponse);
      
      if (!startResponse.data || startResponse.data.code !== 200) {
        console.error('恢复考试失败:', startResponse.data);
        ElMessage.error(startResponse.data?.message || '恢复考试失败');
        setTimeout(() => router.push('/student/exams'), 1000);
        return;
      }
      
      const data = startResponse.data.data;
      exam.value = examData;
      questions.value = data.questions;
      
      console.log('成功恢复题目数量:', questions.value.length);
      
      // 显示恢复消息
      ElMessage.info('已成功恢复考试，请继续答题！');
    }
    
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
      console.log('已经过时间(分钟):', elapsedMinutes);
    }
    
    const remainingMinutes = Math.max(0, examData.duration - elapsedMinutes);
    remainingSeconds.value = remainingMinutes * 60;
    console.log('剩余时间(秒):', remainingSeconds.value);
    
    // 开始倒计时
    startTimer();
    
    // 标记初始化成功
    initSuccess.value = true;
    
    // 清除localStorage中的考试开始信息
    localStorage.removeItem('exam_start_time');
    localStorage.removeItem('current_exam_id');
    
  } catch (error) {
    console.error('初始化考试失败', error);
    ElMessage.error('初始化考试失败，请刷新页面重试');
    // 5秒后自动返回考试列表
    setTimeout(() => {
      router.push('/student/exams');
    }, 5000);
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
  console.log(`Parsing options for question ID: ${question?.id}, Type: ${question?.questionType}`);
  const rawContent = question?.content;
  console.log('Raw question content:', rawContent);
  
  if (!rawContent) {
    console.warn('Question content is missing or empty');
    return {};
  }
  
  try {
    const parsedData = JSON.parse(rawContent);
    console.log('Parsed content data:', parsedData);
    
    // 检查解析后的数据是否为数组
    if (Array.isArray(parsedData)) {
      const optionsObject = {};
      let isValidArray = true;
      
      // 遍历数组，构建 optionsObject
      for (const item of parsedData) {
        if (item && typeof item === 'object' && item.key && item.value) {
          optionsObject[item.key] = item.value;
        } else {
          console.warn('Invalid item format in options array:', item);
          isValidArray = false;
          break; // 发现无效项，停止处理
        }
      }
      
      if (isValidArray && Object.keys(optionsObject).length > 0) {
        console.log('Successfully parsed options array into object:', optionsObject);
        return optionsObject;
      } else if (!isValidArray) {
         console.error('Options array contains invalid items.');
         return {};
      } else {
         console.warn('Parsed options array is empty or could not be transformed.');
         return {};
      }
      
    } else {
      // 如果不是数组，可能是旧格式或其他错误格式
      console.warn('Parsed content is not an array as expected. Attempting fallback (expecting {options: ...})');
      // 尝试按旧格式解析（作为后备），尽管我们知道当前格式是数组
      if (parsedData && typeof parsedData === 'object' && parsedData.options && typeof parsedData.options === 'object') {
          console.log('Fallback successful, returning options from object:', parsedData.options);
          return parsedData.options;
      }
      console.error('Parsed content is not a valid options array and fallback failed.');
      return {};
    }
  } catch (e) {
    console.error('Failed to parse question content JSON:', e);
    console.error('Invalid JSON string was:', rawContent);
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
    answers.value[questionId] = multipleChoiceAnswers.value[questionId].sort().join(',');
  }
};

// 检查题目是否已答
const isQuestionAnswered = (questionId) => {
  return answers.value[questionId] !== undefined && answers.value[questionId] !== '';
};

// 提交考试
const handleExamSubmit = async (isAutoSubmit = false) => {
  // 标记为提交中，防止重复提交
  if (submitting.value) return;
  submitting.value = true;
  
  // 如果是自动提交，显示提示
  if (isAutoSubmit) {
      ElMessage.info('正在自动提交您的答案...');
  }
  
  try {
    console.log('Submitting answers (Auto:' + isAutoSubmit + '):', answers.value);
    const response = await submitExamApi(Number(examId), answers.value);
    
    if (response.data && response.data.code === 200) { 
      submitted.value = true; // 标记为已提交
      showSubmitConfirm.value = false;
      showTimeUpDialog.value = false;
      if (timer.value) clearInterval(timer.value);
      
      if (!isAutoSubmit) {
          ElMessage.success('考试提交成功');
      } else {
          // 自动提交成功后可能需要不同的提示或不提示
          console.log('自动提交成功');
      }
      return true; // 返回成功状态
    } else {
      if (!isAutoSubmit) {
          ElMessage.error(response.data?.message || '提交考试失败'); 
      }
      console.error('提交失败，后端信息:', response.data?.message);
      return false; // 返回失败状态
    }
  } catch (error) {
    console.error('提交考试请求异常:', error);
    const errorMsg = error.response?.data?.message || error.message || '提交考试失败，请确保网络连接正常后重试';
    if (!isAutoSubmit) {
        ElMessage.error(errorMsg);
    }
    return false; // 返回失败状态
  } finally {
    // 只有在非自动提交时才重置 submitting 状态，允许后续路由跳转
    if (!isAutoSubmit) {
         submitting.value = false;
    }
    // 对于自动提交，我们希望在提交完成后能顺利离开，所以不重置 submitting
  }
};

// 返回考试列表
const goToExamList = () => {
  router.push('/student/exams');
};

// 添加路由离开守卫
onBeforeRouteLeave(async (to, from, next) => {
  // 如果考试已经提交或没有成功初始化，则直接离开
  if (submitted.value || !initSuccess.value) {
    next();
    return;
  }

  // 如果考试正在进行中，弹出确认框
  try {
    await ElMessageBox.confirm(
      '您正在离开考试页面，系统将自动提交您当前的答案。确定要离开吗？',
      '离开确认',
      {
        confirmButtonText: '确认离开并提交',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    // 用户点击了"确认离开并提交"
    console.log('用户确认离开，执行自动提交...');
    const submitSuccess = await handleExamSubmit(true); // 调用提交函数，标记为自动提交
    if (submitSuccess) {
        console.log('自动提交成功，允许离开');
        next(); // 允许路由跳转
    } else {
        console.error('自动提交失败，阻止离开');
         ElMessage.error('自动提交失败，无法离开页面，请尝试手动提交或联系管理员');
        next(false); // 阻止路由跳转
    }
  } catch (action) {
    // 用户点击了"取消"或者关闭了对话框
    if (action === 'cancel') {
      console.log('用户取消离开');
      next(false); // 阻止路由跳转
    } else {
      // 其他关闭情况（如按 ESC），也阻止离开
      console.log('用户关闭了确认框');
      next(false);
    }
  }
});

onMounted(() => {
  initExam();
});

onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value);
  }
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

/* 欢迎提示样式 */
.exam-welcome {
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 16px;
  font-weight: bold;
}

.welcome-content {
  margin-top: 10px;
}

.welcome-content p {
  margin-bottom: 10px;
}

.welcome-content ol {
  margin-left: 20px;
  line-height: 1.6;
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