<template>
  <div class="create-exam-container">
    <div class="header">
      <h2>{{ isEdit ? '编辑考试' : '创建考试' }}</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-form
      ref="examFormRef"
      :model="examForm"
      :rules="rules"
      label-width="120px"
      class="exam-form"
    >
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-form-item label="考试标题" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试标题" />
        </el-form-item>
        <el-form-item label="考试说明" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            placeholder="请输入考试说明"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="duration">
          <el-input-number
            v-model="examForm.duration"
            :min="10"
            :max="300"
            :step="5"
          />
        </el-form-item>
        <el-form-item label="及格分数" prop="passingScore">
          <el-input-number
            v-model="examForm.passingScore"
            :min="0"
            :precision="1"
            :step="5"
          />
        </el-form-item>
      </el-card>

      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span>考试题目</span>
            <el-button type="primary" @click="openQuestionSelectorDialog">
              添加题目
            </el-button>
          </div>
        </template>
        <div v-if="examForm.examQuestions.length === 0" class="empty-tip">
          请添加考试题目
        </div>
        <el-table
          v-else
          :data="examForm.examQuestions"
          style="width: 100%"
          border
        >
          <el-table-column type="index" width="50" />
          <el-table-column prop="questionTitle" label="题目标题" min-width="250" show-overflow-tooltip />
          <el-table-column prop="questionType" label="题目类型" width="100">
            <template #default="scope">
              {{ getQuestionTypeText(scope.row.questionType) }}
            </template>
          </el-table-column>
          <el-table-column prop="questionScore" label="分值" width="120">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.questionScore"
                :min="0.5"
                :max="100"
                :precision="1"
                :step="0.5"
                @change="calculateTotalScore"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                type="danger"
                size="small"
                @click="removeQuestion(scope.$index)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="examForm.examQuestions.length > 0" class="total-score">
          总分：{{ totalScore }}
        </div>
      </el-card>

      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <span>考试学生</span>
            <el-button type="primary" @click="openStudentSelectorDialog">
              添加学生
            </el-button>
          </div>
        </template>
        <div v-if="examForm.examStudents.length === 0" class="empty-tip">
          请添加参加考试的学生
        </div>
        <el-table
          v-else
          :data="examForm.examStudents"
          style="width: 100%"
          border
        >
          <el-table-column type="index" width="50" />
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="studentUsername" label="用户名" width="120" />
          <el-table-column prop="studentUserNumber" label="学号" min-width="150" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                type="danger"
                size="small"
                @click="removeStudent(scope.$index)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="saveExam">保存</el-button>
        <el-button v-if="!isEdit" type="success" @click="saveAndPublish">保存并发布</el-button>
      </div>
    </el-form>

    <!-- 选择题目对话框 -->
    <el-dialog
      v-model="questionDialogVisible"
      title="选择题目"
      width="75%"
      destroy-on-close
    >
      <el-tabs v-model="activeQuestionTab">
        <el-tab-pane label="题库选择" name="banks">
          <div class="dialog-filter">
            <el-select
              v-model="selectedQuestionBank"
              placeholder="请选择题库"
              @change="loadBankQuestions"
              style="width: 300px"
            >
              <el-option
                v-for="bank in questionBanks"
                :key="bank.id"
                :label="`${bank.name} (题目数: ${bank.questionCount || 0})`"
                :value="bank.id"
              />
            </el-select>
            <div v-if="questionBanks.length === 0" class="no-banks-tip">
              没有可用的题库，请先<router-link to="/supervisor/question-banks">创建题库</router-link>
            </div>
          </div>
          <div v-if="bankQuestionsLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在加载题目，请稍候...</span>
          </div>
          <div v-else-if="!selectedQuestionBank" class="empty-state">
            请选择一个题库以查看题目
          </div>
          <div v-else-if="bankQuestions.length === 0 && !bankQuestionsLoading" class="empty-state">
            该题库中没有可用的题目，请选择其他题库或创建新题目
          </div>
          <el-table
            v-else
            v-loading="bankQuestionsLoading"
            :data="bankQuestions"
            style="width: 100%"
            @selection-change="handleQuestionSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="title" label="题目标题" min-width="300" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                {{ getQuestionTypeText(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="scope">
                <el-rate
                  v-model="scope.row.difficulty"
                  disabled
                  :max="5"
                  :colors="difficultyColors"
                />
              </template>
            </el-table-column>
            <el-table-column prop="score" label="默认分值" width="100" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="questionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addSelectedQuestions">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 选择学生对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      title="选择学生"
      width="70%"
      destroy-on-close
    >
      <div class="dialog-filter">
        <el-input
          v-model="studentSearchKeyword"
          placeholder="搜索学生姓名/学号"
          clearable
          @input="searchStudents"
        >
          <template #prefix>
            <el-icon class="el-input__icon"><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-table
        v-loading="studentsLoading"
        :data="studentList"
        style="width: 100%"
        @selection-change="handleStudentSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="userNumber" label="学号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="studentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addSelectedStudents">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { createExam, getExam, updateExam, publishExam } from '@/api/exam'
import { getQuestionBanks } from '@/api/questionBank'
import { getQuestionsByBankIdPage } from '@/api/question'
import { getAssignedStudents } from '@/api/student'

const route = useRoute()
const router = useRouter()

// 判断是否为编辑模式
const isEdit = computed(() => {
  return route.query.edit === 'true' || !!examId.value
})

const examId = computed(() => {
  return route.params.id ? Number(route.params.id) : null
})

// 表单相关
const examFormRef = ref<FormInstance>()
const totalScore = ref(0)

// 表单数据
const examForm = reactive({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  duration: 60,
  passingScore: 60,
  examQuestions: [] as any[],
  examStudents: [] as any[]
})

// 表单验证规则
const rules = reactive({
  title: [
    { required: true, message: '请输入考试标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' }
  ]
})

// 题目选择对话框
const questionDialogVisible = ref(false)
const activeQuestionTab = ref('banks')
const questionBanks = ref<any[]>([])
const selectedQuestionBank = ref<number | null>(null)
const bankQuestions = ref<any[]>([])
const bankQuestionsLoading = ref(false)
const selectedQuestions = ref<any[]>([])

// 学生选择对话框
const studentDialogVisible = ref(false)
const studentList = ref<any[]>([])
const studentsLoading = ref(false)
const selectedStudents = ref<any[]>([])
const studentSearchKeyword = ref('')

// 难度颜色
const difficultyColors = ['#99A9BF', '#F7BA2A', '#F7BA2A', '#FF9900', '#FF4500']

// 页面初始化
onMounted(async () => {
  if (isEdit.value && examId.value) {
    await loadExamData(examId.value)
  }
  await loadQuestionBanks()
})

// 加载考试数据
const loadExamData = async (id: number) => {
  try {
    const response = await getExam(id)
    if (response.data && response.data.code === 200) {
      const examData = response.data.data
      
      // 设置基本信息
      examForm.title = examData.title
      examForm.description = examData.description
      examForm.startTime = examData.startTime
      examForm.endTime = examData.endTime
      examForm.duration = examData.duration
      examForm.passingScore = examData.passingScore
      
      // 获取题目和学生列表
      await Promise.all([
        loadExamQuestions(id),
        loadExamStudents(id)
      ])
      
      calculateTotalScore()
    }
  } catch (error) {
    console.error('加载考试数据失败:', error)
    ElMessage.error('加载考试数据失败')
  }
}

// 加载考试题目
const loadExamQuestions = async (examId: number) => {
  try {
    const response = await getExamQuestions(examId)
    if (response.data && response.data.code === 200) {
      examForm.examQuestions = response.data.data.map((item: any) => ({
        ...item,
        questionTitle: item.questionTitle || item.question.title,
        questionType: item.questionType || item.question.type
      }))
    }
  } catch (error) {
    console.error('加载考试题目失败:', error)
    ElMessage.error('加载考试题目失败')
  }
}

// 加载考试学生
const loadExamStudents = async (examId: number) => {
  try {
    const response = await getExamStudents(examId)
    if (response.data && response.data.code === 200) {
      examForm.examStudents = response.data.data
    }
  } catch (error) {
    console.error('加载考试学生失败:', error)
    ElMessage.error('加载考试学生失败')
  }
}

// 计算总分
const calculateTotalScore = () => {
  totalScore.value = examForm.examQuestions.reduce(
    (sum, question) => sum + Number(question.questionScore || 0),
    0
  )
}

// 加载题库列表
const loadQuestionBanks = async () => {
  try {
    // 使用常规题库列表API，添加status参数
    const response = await getQuestionBanks({
      status: 'active'
    })
    
    if (response.data && response.data.code === 200) {
      // 处理分页格式的返回数据
      const responseData = response.data.data;
      questionBanks.value = responseData.content || [];
      
      // 仅当有题库时才设置选中的题库并加载题目
      if (questionBanks.value.length > 0 && !selectedQuestionBank.value) {
        selectedQuestionBank.value = questionBanks.value[0].id;
        await loadBankQuestions();
      }
      // 如果没有题库，则显示提示
      if (questionBanks.value.length === 0) {
        ElMessage.warning('没有可用的题库，请先创建题库');
      }
    }
  } catch (error) {
    console.error('加载题库列表失败:', error);
    ElMessage.error('加载题库列表失败');
  }
}

// 加载题库下的题目
const loadBankQuestions = async () => {
  if (!selectedQuestionBank.value) {
    ElMessage.warning('请先选择题库');
    return;
  }
  
  bankQuestionsLoading.value = true;
  try {
    const params = {
      bankId: selectedQuestionBank.value,
      page: 0,
      size: 100,
      status: 'active' // 只获取激活状态的题目
    };
    const response = await getQuestionsByBankIdPage(params);
    if (response.data && response.data.code === 200) {
      // 过滤已添加的题目
      const existingQuestionIds = examForm.examQuestions.map((q) => q.questionId);
      bankQuestions.value = (response.data.data.content || [])
        .filter((q: any) => !existingQuestionIds.includes(q.id));
      
      if (bankQuestions.value.length === 0) {
        // 检查是否是因为所有题目都已添加
        if (response.data.data.content && response.data.data.content.length > 0) {
          ElMessage.warning('该题库中的所有题目已添加到考试中');
        } else {
          ElMessage.warning('该题库中没有可用的题目，请选择其他题库或创建新题目');
        }
      }
    } else {
      ElMessage.error(response.data?.message || '获取题目失败');
      bankQuestions.value = [];
    }
  } catch (error: any) {
    console.error('加载题目失败:', error);
    ElMessage.error('加载题目失败: ' + (error.response?.data?.message || '请检查网络连接或题库是否存在'));
    bankQuestions.value = [];
  } finally {
    bankQuestionsLoading.value = false;
  }
};

// 打开题目选择对话框
const openQuestionSelectorDialog = async () => {
  await loadQuestionBanks()
  
  if (questionBanks.value.length === 0) {
    ElMessage.warning('没有可用的题库，请先创建题库')
    return
  }
  
  questionDialogVisible.value = true
  selectedQuestions.value = []
  
  // 如果已有选中的题库，则加载该题库的题目
  if (selectedQuestionBank.value) {
    await loadBankQuestions()
  } 
  // 如果没有选中的题库但题库列表不为空，则自动选择第一个题库
  else if (questionBanks.value.length > 0) {
    selectedQuestionBank.value = questionBanks.value[0].id
    await loadBankQuestions()
  }
}

// 处理题目选择变化
const handleQuestionSelectionChange = (selection: any[]) => {
  selectedQuestions.value = selection
}

// 添加选中的题目
const addSelectedQuestions = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择题目')
    return
  }
  
  // 转换为考试题目格式
  const newQuestions = selectedQuestions.value.map((q) => ({
    questionId: q.id,
    questionTitle: q.title,
    questionType: q.type,
    questionScore: q.score || 5,
    question: q
  }))
  
  // 添加到考试题目列表
  examForm.examQuestions = [...examForm.examQuestions, ...newQuestions]
  
  // 关闭对话框
  questionDialogVisible.value = false
  
  // 重新计算总分
  calculateTotalScore()
}

// 移除题目
const removeQuestion = (index: number) => {
  examForm.examQuestions.splice(index, 1)
  calculateTotalScore()
}

// 打开学生选择对话框
const openStudentSelectorDialog = async () => {
  studentDialogVisible.value = true
  selectedStudents.value = []
  studentSearchKeyword.value = ''
  await loadStudents()
}

// 加载学生列表
const loadStudents = async () => {
  studentsLoading.value = true
  
  try {
    const response = await getAssignedStudents()
    if (response.data && response.data.code === 200) {
      // 过滤已添加的学生
      const existingStudentIds = examForm.examStudents.map((s) => s.studentId)
      studentList.value = (response.data.data || [])
        .filter((s: any) => !existingStudentIds.includes(s.id))
    } else {
      ElMessage.error('获取学生列表失败')
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

// 搜索学生
const searchStudents = () => {
  if (!studentSearchKeyword.value) {
    loadStudents()
    return
  }
  
  const keyword = studentSearchKeyword.value.toLowerCase()
  studentList.value = studentList.value.filter(
    (student) => student.realName?.toLowerCase().includes(keyword) || 
                 student.userNumber?.toLowerCase().includes(keyword)
  )
}

// 处理学生选择变化
const handleStudentSelectionChange = (selection: any[]) => {
  selectedStudents.value = selection
}

// 添加选中的学生
const addSelectedStudents = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择学生')
    return
  }
  
  // 转换为考试学生格式
  const newStudents = selectedStudents.value.map((s) => ({
    studentId: s.id,
    studentName: s.realName,
    studentUsername: s.username,
    studentUserNumber: s.userNumber
  }))
  
  // 添加到考试学生列表
  examForm.examStudents = [...examForm.examStudents, ...newStudents]
  
  // 关闭对话框
  studentDialogVisible.value = false
}

// 移除学生
const removeStudent = (index: number) => {
  examForm.examStudents.splice(index, 1)
}

// 保存考试
const saveExam = async () => {
  if (!examFormRef.value) return
  
  await examFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    // 检查必要的字段
    if (examForm.examQuestions.length === 0) {
      ElMessage.warning('请添加考试题目')
      return
    }
    
    if (examForm.examStudents.length === 0) {
      ElMessage.warning('请添加考试学生')
      return
    }
    
    try {
      // 自动生成开始和结束时间(现在的时间作为开始时间，结束时间为开始时间加上考试时长)
      const now = new Date()
      examForm.startTime = now.toISOString()
      
      const endTime = new Date(now.getTime())
      endTime.setMinutes(endTime.getMinutes() + examForm.duration)
      examForm.endTime = endTime.toISOString()
      
      const requestData = {
        ...examForm,
        totalScore: totalScore.value
      }
      
      let response
      if (isEdit.value && examId.value) {
        response = await updateExam(examId.value, requestData)
      } else {
        response = await createExam(requestData)
      }
      
      if (response.data && response.data.code === 200) {
        ElMessage.success(isEdit.value ? '考试更新成功' : '考试创建成功')
        goBack()
      } else {
        ElMessage.error(response.data?.message || (isEdit.value ? '考试更新失败' : '考试创建失败'))
      }
    } catch (error: any) {
      console.error(isEdit.value ? '更新考试失败:' : '创建考试失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '考试更新失败' : '考试创建失败'))
    }
  })
}

// 保存并发布考试
const saveAndPublish = async () => {
  if (!examFormRef.value) return
  
  await examFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    try {
      // 自动生成开始和结束时间(现在的时间作为开始时间，结束时间为开始时间加上考试时长)
      const now = new Date()
      examForm.startTime = now.toISOString()
      
      const endTime = new Date(now.getTime())
      endTime.setMinutes(endTime.getMinutes() + examForm.duration)
      examForm.endTime = endTime.toISOString()
      
      // 先保存考试
      const requestData = {
        ...examForm,
        totalScore: totalScore.value
      }
      
      const saveResponse = await createExam(requestData)
      
      if (saveResponse.data && saveResponse.data.code === 200) {
        const newExamId = saveResponse.data.data.id
        
        // 然后发布考试
        const publishResponse = await publishExam(newExamId)
        
        if (publishResponse.data && publishResponse.data.code === 200) {
          ElMessage.success('考试创建并发布成功')
          goBack()
        } else {
          ElMessage.warning('考试已创建，但发布失败')
          goBack()
        }
      } else {
        ElMessage.error(saveResponse.data?.message || '考试创建失败')
      }
    } catch (error: any) {
      console.error('创建并发布考试失败:', error)
      ElMessage.error(error.response?.data?.message || '创建并发布考试失败')
    }
  })
}

// 返回上一页
const goBack = () => {
  router.push('/supervisor/exams')
}

// 获取题目类型文本
const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  }
  return typeMap[type] || type
}
</script>

<style scoped>
.create-exam-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.total-score {
  margin-top: 15px;
  text-align: right;
  font-weight: bold;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

.dialog-filter {
  margin-bottom: 15px;
}

.no-banks-tip {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.loading-container {
  text-align: center;
  padding: 30px 0;
}

.empty-state {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}
</style> 