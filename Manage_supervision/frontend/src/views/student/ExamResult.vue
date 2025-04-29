<template>
  <div class="exam-result-container p-4">
    <div class="back-link mb-4">
      <el-link :underline="false" @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-link>
    </div>

    <el-card v-loading="loading" class="exam-result-card">
      <template #header>
        <div class="text-xl font-bold">考试结果 - {{ examInfo.title }}</div>
      </template>

      <!-- 添加一个容器用于滚动 -->
      <div class="result-scroll-container">
        <div v-if="examInfo && studentInfo">
          <!-- 考试和学生基本信息 -->
          <el-descriptions :column="2" border class="mb-6">
            <el-descriptions-item label="考试名称">{{ examInfo.title }}</el-descriptions-item>
            <el-descriptions-item label="学生姓名">{{ studentInfo.studentName }} ({{ studentInfo.studentUserNumber }})</el-descriptions-item>
            <el-descriptions-item label="总分/及格分">{{ examInfo.totalScore }} / {{ examInfo.passingScore }}</el-descriptions-item>
            <el-descriptions-item label="最终得分">
              <el-tag :type="studentInfo.score >= examInfo.passingScore ? 'success' : 'danger'" size="large">
                {{ studentInfo.score?.toFixed(1) ?? 'N/A' }}
              </el-tag>
            </el-descriptions-item>
             <el-descriptions-item label="开始时间">{{ formatDateTime(studentInfo.startTime) }}</el-descriptions-item>
             <el-descriptions-item label="提交时间">{{ formatDateTime(studentInfo.submitTime) }}</el-descriptions-item>
          </el-descriptions>

          <!-- 题目和答案详情 -->
          <div v-for="(item, index) in questionAnswers" :key="item.question.id" class="mb-6 pb-4 border-b last:border-b-0 question-answer-item">
             <h3 class="text-lg font-semibold mb-2">{{ index + 1 }}. {{ item.question.questionTitle }} ({{ item.question.questionScore }}分)</h3>
             <p class="text-sm text-gray-500 mb-2">类型: {{ getQuestionTypeText(item.question.questionType) }}</p>

             <!-- Display content ONLY if it's NOT choice/judgment type AND content exists -->
             <p v-if="!['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT'].includes(item.question.questionType) && item.question.content" class="mb-3 question-content-display">
               {{ item.question.content }}
             </p>

             <!-- Display options list ONLY for SINGLE/MULTIPLE choice AND if parsedOptions exist -->
             <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(item.question.questionType) && item.parsedOptions" class="mb-3 options-list">
               <div v-for="option in item.parsedOptions" :key="option.key" class="option-item">
                  <el-tag size="small" type="info" class="mr-2 option-key-tag">{{ option.key }}</el-tag> {{ option.value }}
               </div>
             </div>

             <div class="grid grid-cols-1 md:grid-cols-2 gap-4 answer-section">
               <div>
                 <label class="block text-sm font-medium text-gray-700 mb-1">你的答案:</label>
                 <el-input
                   type="textarea"
                   :value="formatStudentAnswer(item.answer?.answer, item.question.questionType)"
                   :autosize="{ minRows: 2, maxRows: 6 }"
                   readonly
                   :class="getAnswerClass(item.answer?.isCorrect)"
                 />
               </div>
               <div>
                  <!-- 客观题显示正确答案 -->
                  <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT'].includes(item.question.questionType)">
                    <label class="block text-sm font-medium text-gray-700 mb-1">正确答案:</label>
                    <el-input
                      type="textarea"
                      :value="formatCorrectAnswer(item.question.answer, item.question.questionType)"
                      :autosize="{ minRows: 2, maxRows: 6 }"
                      readonly
                    />
                  </div>
                  <!-- 主观题显示得分 -->
                   <div v-else>
                     <label class="block text-sm font-medium text-gray-700 mb-1">得分:</label>
                      <p class="text-lg font-semibold">{{ item.answer?.score?.toFixed(1) ?? '-' }} / {{ item.question.questionScore }}</p>
                   </div>
               </div>
             </div>
              <div v-if="item.answer" class="mt-2 flex items-center result-summary">
                   <label class="text-sm font-medium text-gray-700 mr-2">结果:</label>
                   <el-tag :type="item.answer.isCorrect === true ? 'success' : (item.answer.isCorrect === false ? 'danger' : 'info')" size="small">
                       {{ item.answer.isCorrect === true ? '正确' : (item.answer.isCorrect === false ? '错误' : '待评分/主观题') }}
                   </el-tag>
                   <span v-if="item.answer.isCorrect === true || item.answer.isCorrect === false" class="ml-4 text-sm text-gray-600">
                      得分: {{ item.answer.score?.toFixed(1) ?? 0 }} / {{ item.question.questionScore }}
                   </span>
               </div>
          </div>

        </div>
        <el-empty v-else description="无法加载考试结果数据"></el-empty>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElCard, ElDescriptions, ElDescriptionsItem, ElTag, ElEmpty, ElMessage, ElLink, ElIcon, ElInput } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { getStudentExamResultDetails } from '@/api/exam';
import { formatDateTime } from '@/utils/dateUtil';
import { useUserStore } from '@/stores/user';

interface QuestionOption {
  key: string;
  value: string;
}

interface Question {
  id: number;
  questionId: number;
  questionTitle: string;
  questionType: string;
  content: string | null; // May contain text (Essay) or JSON string (Choice/Judgment)
  answer: string | null;  // Correct answer (Key or text)
  questionScore: number;
}

interface Answer {
  id: number;
  answer: string;
  isCorrect: boolean | null;
  score: number | null;
}

interface QuestionAnswerItem {
  question: Question;
  answer: Answer | null;
  parsedOptions: QuestionOption[] | null; // Store parsed options here
}

interface ExamInfo {
    id: number;
    title: string;
    totalScore: number;
    passingScore: number;
    // Add other exam fields if needed
}

interface StudentInfo {
    studentId: number;
    studentName: string;
    studentUserNumber: string;
    score: number | null;
    startTime: string | Date | null;
    submitTime: string | Date | null;
    // Add other student status fields if needed
}

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const examId = Number(route.params.id);
// !! 注意：我们需要 studentId。如果API是学生专用的，可以从token获取。
// !! 如果复用教师API，需要想办法获取当前 studentId。
// !! 暂时从 userStore 获取，假设 userStore.user.id 就是 studentId。
const studentId = computed(() => userStore.user.id);

const loading = ref(false);
const examInfo = ref<Partial<ExamInfo>>({}); // Use Partial for initial empty object
const studentInfo = ref<Partial<StudentInfo>>({});
const questionAnswers = ref<QuestionAnswerItem[]>([]);

const fetchExamResult = async () => {
  loading.value = true;
  try {
    const res = await getStudentExamResultDetails(examId);
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data;
      examInfo.value = {
          id: data.exam.id,
          title: data.exam.title,
          totalScore: data.exam.totalScore,
          passingScore: data.exam.passingScore
      };
      studentInfo.value = {
          studentId: data.student.id, // 确认后端返回的 student 包含 id, realName, userNumber
          studentName: data.student.realName || data.student.username,
          studentUserNumber: data.student.userNumber,
          score: data.examStudent.score,
          startTime: data.examStudent.startTime,
          submitTime: data.examStudent.submitTime
      };
      questionAnswers.value = data.questionAnswers.map((qa: any) => {
        let parsedOptions: QuestionOption[] | null = null;
        // Try parsing options ONLY for choice/judgment types from their content field
        if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'JUDGMENT'].includes(qa.question.questionType)) {
            parsedOptions = parseOptionsFromJson(qa.question.content);
        }
        return {
          question: qa.question, // Keep original question object
          answer: qa.answer,
          parsedOptions: parsedOptions // Add the parsed options field
        };
      });
    } else {
      ElMessage.error(res.data.message || '加载考试结果失败');
    }
  } catch (error) {
    console.error('加载考试结果失败:', error);
    ElMessage.error('加载考试结果失败');
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  // 可以考虑返回到 /student/grades 或 /student/exams
  router.push('/student/exams');
};

const getQuestionTypeText = (type: string) => {
    const map: Record<string, string> = {
        SINGLE_CHOICE: '单选题',
        MULTIPLE_CHOICE: '多选题',
        JUDGMENT: '判断题',
        ESSAY: '简答题',
    };
    return map[type] || type;
};

// Renamed from parseOptions to avoid confusion with a potential 'options' field
const parseOptionsFromJson = (jsonString: string | null): QuestionOption[] | null => {
  if (!jsonString) return null;
  try {
    const parsed = JSON.parse(jsonString);
    if (Array.isArray(parsed)) {
        // Validate structure minimally
        const filtered = parsed.filter(opt => typeof opt === 'object' && opt !== null && 'key' in opt && 'value' in opt);
        // Return null if parsing succeeded but structure is wrong (e.g., array of strings)
        return filtered.length === parsed.length ? filtered : null;
    }
    return null; // Parsed but not an array
  } catch (e) {
    // Parsing failed, likely plain text content. Return null.
    return null;
  }
};

// 格式化学生答案显示
const formatStudentAnswer = (answer: string | null, type: string): string => {
    if (!answer) return '未作答';
    // 对判断题做特殊处理，如果答案是A或B，转为True/False或中文
    if (type === 'JUDGMENT') {
        return answer === 'A' ? '正确' : (answer === 'B' ? '错误' : answer);
    }
    // 其他类型直接显示
    return answer;
};

// 格式化正确答案显示
const formatCorrectAnswer = (answer: string | null, type: string): string => {
    if (!answer) return '-';
     if (type === 'JUDGMENT') {
        return answer === 'A' ? '正确' : (answer === 'B' ? '错误' : answer);
    }
    return answer;
};

// 根据答案正确与否返回样式类
const getAnswerClass = (isCorrect: boolean | null) => {
    if (isCorrect === true) return 'correct-answer';
    if (isCorrect === false) return 'wrong-answer';
    return '';
}

onMounted(() => {
  fetchExamResult();
});

</script>

<style scoped>
.exam-result-container {
  max-width: 900px;
  margin: 0 auto;
  height: calc(100vh - 100px); /* 减去大致的 header 和 padding 高度 */
  display: flex;
  flex-direction: column;
}

.exam-result-card {
  flex-grow: 1; /* 让卡片充满剩余空间 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止内部元素溢出卡片 */
}

:deep(.el-card__body) {
  height: 100%; /* 让 card body 也能撑开 */
  padding: 0; /* 移除默认 padding，由滚动容器处理 */
  display: flex;
  flex-direction: column;
}

.result-scroll-container {
  flex-grow: 1;
  overflow-y: auto; /* 添加垂直滚动条 */
  padding: 20px; /* 把 padding 移到这里 */
}

.back-link {
  cursor: pointer;
  color: #409eff;
  display: inline-flex;
  align-items: center;
  margin-bottom: 1rem; /* 保持原来的间距 */
}

.question-answer-item {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}
.question-answer-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
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
    margin-top: 1rem;
}

.result-summary {
    margin-top: 0.5rem;
}

/* 自定义答案输入框样式 */
:deep(.el-textarea.is-readonly .el-textarea__inner) {
  cursor: default;
  color: #303133; /* 让只读文本更清晰 */
}

:deep(.correct-answer .el-textarea__inner) {
   border-color: #67c23a;
   background-color: #f0f9eb;
}
:deep(.wrong-answer .el-textarea__inner) {
   border-color: #f56c6c;
   background-color: #fef0f0;
}
</style> 