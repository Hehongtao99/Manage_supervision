<template>
  <div class="exam-result-container p-4">
    <div class="back-link mb-4">
      <el-link :underline="false" @click="goBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-link>
    </div>

    <el-card v-loading="loading">
      <template #header>
        <div class="text-xl font-bold">考试结果 - {{ examInfo.title }}</div>
      </template>

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
        <div v-for="(item, index) in questionAnswers" :key="item.question.id" class="mb-6 pb-4 border-b last:border-b-0">
           <h3 class="text-lg font-semibold mb-2">{{ index + 1 }}. {{ item.question.questionTitle }} ({{ item.question.questionScore }}分)</h3>
           <p class="text-sm text-gray-500 mb-2">类型: {{ getQuestionTypeText(item.question.questionType) }}</p>
           
           <!-- 题目选项 (选择题) -->
           <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(item.question.questionType) && item.question.content" class="mb-3 options-list">
             <div v-for="(option, key) in parseOptions(item.question.content)" :key="key" class="option-item">
                <el-tag size="small" type="info" class="mr-2">{{ key }}</el-tag> {{ option }}
             </div>
           </div>

           <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
             <div>
               <label class="block text-sm font-medium text-gray-700 mb-1">你的答案:</label>
               <el-input
                 type="textarea"
                 :value="item.answer ? item.answer.answer : '未作答'"
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
                    :value="item.question.answer || '-'" 
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
            <div v-if="item.answer" class="mt-2 flex items-center">
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

interface Question {
  id: number;
  questionId: number;
  questionTitle: string;
  questionType: string;
  content: string | null; // 选择题选项 JSON string
  answer: string | null;  // 正确答案
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
  // 不再需要 studentId.value 检查，后端会从token获取
  loading.value = true;
  try {
    // 调用新的 API 函数
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
      questionAnswers.value = data.questionAnswers;
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

// 解析选项字符串为对象
const parseOptions = (content: string | null): Record<string, string> => {
  if (!content) return {};
  try {
    return JSON.parse(content);
  } catch (e) {
    console.error('Failed to parse question options:', content, e);
    return {};
  }
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
}

.back-link {
  cursor: pointer;
  color: #409eff;
  display: inline-flex;
  align-items: center;
}

.options-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.option-item {
    font-size: 0.9rem;
}

/* 自定义答案输入框样式 */
:deep(.el-textarea.is-readonly .el-textarea__inner) {
  cursor: default;
  background-color: #f5f7fa; /* 默认只读背景色 */
}

:deep(.el-textarea.correct-answer .el-textarea__inner) {
  background-color: #f0f9eb; /* 绿色背景表示正确 */
  border-color: #e1f3d8;
}

:deep(.el-textarea.wrong-answer .el-textarea__inner) {
  background-color: #fef0f0; /* 红色背景表示错误 */
  border-color: #fde2e2;
}
</style> 