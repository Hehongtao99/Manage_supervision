<template>
  <div class="exam-grading-container">
    <div class="page-header">
      <div class="left">
        <h2>批阅试卷</h2>
        <el-button @click="goBack" plain>返回</el-button>
      </div>
      <div class="right">
        <el-button type="primary" @click="saveGrading" :loading="saving">保存评分</el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="exam-grading-card">
      <div class="grading-scroll-container">
        <template v-if="student && exam">
          <div class="exam-info">
            <div class="exam-title">{{ exam.title }}</div>
            <div class="exam-meta">
              <div class="meta-item">
                <span class="label">学生姓名:</span>
                <span class="value">{{ student.fullName }}</span>
              </div>
              <div class="meta-item">
                <span class="label">学号:</span>
                <span class="value">{{ student.userNumber }}</span>
              </div>
              <div class="meta-item">
                <span class="label">提交时间:</span>
                <span class="value">{{ formatDateTime(examStudent.submitTime) }}</span>
              </div>
              <div class="meta-item">
                <span class="label">总分:</span>
                <span class="value">
                  <span :class="{'score-highlight': allGraded}">{{ totalScore }}</span> / {{ exam.totalScore }}
                </span>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="question-list">
            <div v-for="(qa, index) in questionAnswers" :key="index" class="question-item">
              <div class="question-header">
                <div class="question-title">
                  {{ index + 1 }}. {{ qa.question.questionTitle }}
                  <span class="question-type">{{ getQuestionTypeText(qa.question.questionType) }}</span>
                  <span class="question-score">{{ qa.question.questionScore }} 分</span>
                </div>
              </div>

              <p v-if="qa.question.questionType === 'JUDGMENT'" class="question-content-display mb-3">{{ formatJudgmentContent(qa.question.content) }}</p>
              <p v-if="!['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT'].includes(qa.question.questionType) && qa.question.content" class="question-content-display mb-3">{{ qa.question.content }}</p>
              <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(qa.question.questionType) && qa.question.options" class="mb-3 options-list">
                <div v-for="option in qa.question.options" :key="option.key" class="option-item">
                  <el-tag size="small" type="info" class="mr-2 option-key-tag">{{ option.key }}</el-tag> {{ option.value }}
                </div>
              </div>

              <div class="answer-section">
                <div class="answer-label">学生答案:</div>
                <div class="answer-content">
                  <el-input
                    type="textarea"
                    :value="formatStudentAnswer(qa.answer?.answer, qa.question.questionType, qa.question.content)"
                    :autosize="{ minRows: 2, maxRows: 6 }"
                    readonly
                    :class="getAnswerClass(qa.answer?.isCorrect, qa.question.questionType)"
                  />
                </div>
                <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT'].includes(qa.question.questionType)" class="correct-answer-section">
                  <div class="answer-label">正确答案:</div>
                  <el-input
                    type="textarea"
                    :value="formatCorrectAnswer(qa.question.answer, qa.question.questionType, qa.question.content)"
                    :autosize="{ minRows: 1, maxRows: 4 }"
                    readonly
                  />
                </div>
              </div>

              <div class="grading-section" v-if="qa.answer">
                <template v-if="qa.question.questionType === 'ESSAY'">
                  <div class="grading-controls">
                    <div class="score-input">
                      <span class="label">得分:</span>
                      <el-input-number 
                        v-model="qa.answer.score" 
                        :min="0" 
                        :max="qa.question.questionScore" 
                        :step="0.5" 
                        :precision="1"
                        @change="updateTotalScore"
                      />
                    </div>
                    <div class="correct-toggle">
                      <span class="label">是否正确:</span>
                      <el-switch
                        v-model="qa.answer.isCorrect"
                        :active-value="true"
                        :inactive-value="false"
                      />
                    </div>
                  </div>
                </template>
                <template v-else>
                  <div class="auto-graded-info">
                    <el-tag :type="qa.answer.isCorrect ? 'success' : 'danger'">
                      {{ qa.answer.isCorrect ? '正确' : '错误' }}
                    </el-tag>
                    <span class="auto-score">得分: {{ qa.answer.score }} / {{ qa.question.questionScore }}</span>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElCard, ElInput, ElInputNumber, ElSwitch, ElTag } from 'element-plus';
import { getStudentExamAnswers, gradeStudentExam } from '../../api/exam';
import { formatDateTime } from '../../utils/dateUtil';

const route = useRoute();
const router = useRouter();
const examId = ref(Number(route.params.examId));
const studentId = ref(Number(route.params.studentId));

const loading = ref(true);
const saving = ref(false);
const exam = ref<any>(null);
const student = ref<any>(null);
const examStudent = ref<any>(null);
const questionAnswers = ref<any[]>([]);

// 计算总分
const totalScore = computed(() => {
  if (!questionAnswers.value || questionAnswers.value.length === 0) return 0;
  
  return questionAnswers.value
    .filter(qa => qa.answer && qa.answer.score != null)
    .reduce((sum, qa) => sum + Number(qa.answer.score), 0)
    .toFixed(1);
});

// 是否所有题目都已评分
const allGraded = computed(() => {
  if (!questionAnswers.value || questionAnswers.value.length === 0) return false;
  
  return questionAnswers.value
    .filter(qa => qa.answer)
    .every(qa => qa.answer.score != null);
});

// 加载学生答案
const loadStudentAnswers = async () => {
  loading.value = true;
  try {
    const response = await getStudentExamAnswers(examId.value, studentId.value);
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data;
      exam.value = data.exam;
      student.value = data.student;
      examStudent.value = data.examStudent;
      questionAnswers.value = data.questionAnswers.map((qa: any) => ({
        ...qa,
        question: {
          ...qa.question,
          options: parseOptions(qa.question.options)
        }
      }));
    } else {
      ElMessage.error(response.data?.message || '获取学生答案失败');
    }
  } catch (error: any) {
    console.error('获取学生答案失败:', error);
    ElMessage.error('获取学生答案失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    loading.value = false;
  }
};

// 保存评分
const saveGrading = async () => {
  if (!allGraded.value) {
    ElMessage.warning('请完成所有题目的评分');
    return;
  }
  
  saving.value = true;
  try {
    // 准备评分数据
    const answers = questionAnswers.value
      .filter(qa => qa.answer)
      .map(qa => ({
        id: qa.answer.id,
        score: qa.answer.score,
        isCorrect: qa.answer.isCorrect
      }));
    
    const response = await gradeStudentExam(examId.value, studentId.value, { answers });
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评分保存成功');
      // 更新考试学生信息
      examStudent.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '保存评分失败');
    }
  } catch (error: any) {
    console.error('保存评分失败:', error);
    ElMessage.error('保存评分失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    saving.value = false;
  }
};

// 更新总分
const updateTotalScore = () => {
  // 总分会自动通过计算属性更新
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 解析选项 JSON 字符串为数组
const parseOptions = (optionsString: string | null): any[] | null => {
  if (!optionsString) return null;
  try {
    const parsed = JSON.parse(optionsString);
    if (Array.isArray(parsed)) {
      return parsed.filter(opt => typeof opt === 'object' && opt !== null && 'key' in opt && 'value' in opt);
    }
    return null;
  } catch (e) {
    console.error('Failed to parse question options:', optionsString, e);
    return null;
  }
};

// 新增：格式化判断题内容显示
const formatJudgmentContent = (contentString: string | null): string => {
  if (!contentString) return '-';
  try {
    const options = JSON.parse(contentString);
    if (Array.isArray(options) && options.length > 0) {
      return options.map(opt => `${opt.key}: ${opt.value}`).join(', ');
    }
    return contentString; // 如果不是预期的 JSON 格式，直接返回
  } catch (e) {
    return contentString; // 解析失败，直接返回原始字符串
  }
};

// 修改：格式化学生答案显示，增加对判断题的处理
const formatStudentAnswer = (answer: string | null | undefined, type: string, contentString?: string | null): string => {
  if (answer == null || answer === '') return '学生未作答';
  if (type === 'JUDGMENT' && contentString) {
    try {
      const options = JSON.parse(contentString);
      if (Array.isArray(options)) {
        const selectedOption = options.find(opt => opt.key === answer);
        return selectedOption ? `${answer} (${selectedOption.value})` : answer; // 例如显示 "A (正确)"
      }
    } catch (e) { /* 忽略解析错误 */ }
  }
  return answer;
};

// 修改：格式化正确答案显示，增加对判断题的处理
const formatCorrectAnswer = (answer: string | null | undefined, type: string, contentString?: string | null): string => {
  if (answer == null || answer === '') return '-';
  if (type === 'JUDGMENT' && contentString) {
     try {
      const options = JSON.parse(contentString);
      if (Array.isArray(options)) {
        const correctOption = options.find(opt => opt.key === answer);
        return correctOption ? `${answer} (${correctOption.value})` : answer; // 例如显示 "A (正确)"
      }
    } catch (e) { /* 忽略解析错误 */ }
  }
  // 对于多选题，如果答案是逗号分隔的，可以考虑加空格美化
  if (type === 'MULTIPLE_CHOICE' && answer.includes(',')) {
    return answer.split(',').join(', ');
  }

  return answer;
};

// 获取学生答案输入框样式
const getAnswerClass = (isCorrect: boolean | null | undefined, type: string) => {
  if (type === 'ESSAY') return '';
  if (isCorrect === true) return 'correct-answer';
  if (isCorrect === false) return 'wrong-answer';
  return '';
};

// 获取题目类型文本
const getQuestionTypeText = (type: string): string => {
  const typeMap: Record<string, string> = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'JUDGMENT': '判断题',
    'ESSAY': '主观题'
  };
  return typeMap[type] || type;
};

// 页面加载时获取数据
onMounted(() => {
  loadStudentAnswers();
});
</script>

<style scoped>
.exam-grading-container {
  padding: 20px;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.exam-grading-card {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.el-card__body) {
  height: 100%;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.grading-scroll-container {
  flex-grow: 1;
  overflow-y: auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.page-header .left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-header .left h2 {
  margin: 0;
}

.exam-info-card {
  margin-bottom: 20px;
}

.exam-info {
  padding: 15px 0;
}

.exam-title {
  font-size: 1.2em;
  font-weight: bold;
  margin-bottom: 10px;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 0.9em;
  color: #666;
}

.meta-item .label {
  font-weight: 500;
  margin-right: 5px;
  color: #333;
}

.score-highlight {
  color: #409eff;
  font-weight: bold;
}

.divider {
  height: 1px;
  background-color: #eee;
  margin: 15px 0;
}

.question-list {
  /* Removed fixed height and overflow */
}

.question-item {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.question-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.question-header {
  margin-bottom: 10px;
}

.question-title {
  font-size: 1.1em;
  font-weight: 500;
}

.question-type,
.question-score {
  margin-left: 10px;
  font-size: 0.9em;
  color: #999;
}

.question-content-display {
  background-color: #f9fafb;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 1rem;
  white-space: pre-wrap;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background-color: #f9fafb;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.option-item {
  font-size: 0.9rem;
}
.option-key-tag {
  width: 25px;
  text-align: center;
}

.answer-section {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.answer-label {
  font-weight: 500;
  margin-bottom: 5px;
  color: #555;
  font-size: 0.9em;
}

.answer-content {
  /* Styles for the answer display itself */
}

:deep(.el-textarea.is-readonly .el-textarea__inner) {
  cursor: default;
  color: #303133;
}

:deep(.correct-answer .el-textarea__inner) {
  border-color: #67c23a;
  background-color: #f0f9eb;
}
:deep(.wrong-answer .el-textarea__inner) {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.essay-answer {
  white-space: pre-wrap;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f7fa;
  min-height: 60px;
}

.correct-answer-section {
  margin-top: 10px;
}

.grading-section {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

.grading-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.score-input .label,
.correct-toggle .label {
  margin-right: 8px;
  font-size: 0.9em;
  color: #555;
}

.auto-graded-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auto-score {
  font-size: 0.9em;
  color: #666;
}
</style> 