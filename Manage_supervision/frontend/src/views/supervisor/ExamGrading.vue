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

    <el-card v-loading="loading" class="exam-info-card">
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

            <div class="question-content" v-html="qa.question.content"></div>

            <div class="answer-section">
              <div class="answer-label">学生答案:</div>
              <div class="answer-content">
                <template v-if="qa.question.questionType === 'ESSAY'">
                  <div class="essay-answer" v-html="qa.answer?.answer || '未作答'"></div>
                </template>
                <template v-else-if="qa.question.questionType === 'SINGLE_CHOICE' || qa.question.questionType === 'JUDGMENT'">
                  {{ qa.answer?.answer || '未作答' }}
                </template>
                <template v-else-if="qa.question.questionType === 'MULTIPLE_CHOICE'">
                  {{ formatMultipleChoiceAnswer(qa.answer?.answer) || '未作答' }}
                </template>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
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
      questionAnswers.value = data.questionAnswers;
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

// 格式化多选题答案
const formatMultipleChoiceAnswer = (answer: string | null | undefined) => {
  if (!answer) return '';
  return answer.split(',').join(', ');
};

// 题目类型文本
const getQuestionTypeText = (type: string) => {
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
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header .left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-header h2 {
  margin: 0;
}

.exam-info-card {
  margin-bottom: 20px;
}

.exam-info {
  margin-bottom: 20px;
}

.exam-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 15px;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.meta-item {
  display: flex;
  gap: 5px;
}

.label {
  color: #606266;
  font-weight: 500;
}

.divider {
  height: 1px;
  background-color: #EBEEF5;
  margin: 20px 0;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.question-item {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 20px;
  background-color: #F8F9FA;
}

.question-header {
  margin-bottom: 15px;
}

.question-title {
  font-size: 1.1rem;
  font-weight: 500;
}

.question-type, .question-score {
  margin-left: 10px;
  color: #909399;
  font-size: 0.9rem;
}

.question-content {
  margin-bottom: 20px;
  color: #303133;
}

.answer-section {
  background-color: white;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.answer-label {
  font-weight: 500;
  margin-bottom: 10px;
  color: #606266;
}

.essay-answer {
  white-space: pre-wrap;
  line-height: 1.5;
}

.grading-section {
  background-color: #F0F9EB;
  border: 1px solid #E1F3D8;
  border-radius: 4px;
  padding: 15px;
}

.grading-controls {
  display: flex;
  gap: 30px;
  align-items: center;
}

.score-input, .correct-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
}

.auto-graded-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.auto-score {
  font-weight: 500;
}

.score-highlight {
  color: #409EFF;
  font-weight: bold;
}
</style> 