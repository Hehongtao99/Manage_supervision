<template>
  <div class="create-question">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="title-container">
            <span class="title">{{ isEdit ? '编辑题目' : '创建题目' }}</span>
            <span v-if="bankName" class="bank-name">({{ bankName }})</span>
          </div>
          <div class="right">
            <el-button @click="goBack">
              <el-icon><Back /></el-icon> 返回
            </el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="questionForm" ref="questionFormRef" :rules="questionRules" label-width="100px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="请选择题目类型" @change="handleTypeChange">
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="JUDGMENT" />
            <el-option label="问答题" value="ESSAY" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="题目标题" prop="title">
          <el-input 
            v-model="questionForm.title" 
            type="text" 
            placeholder="请输入题目标题"
          />
        </el-form-item>
        
        <el-form-item label="题目内容" prop="content">
          <el-input 
            v-model="questionForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入题目内容"
          />
        </el-form-item>
        
        <!-- 单选题和多选题的选项 -->
        <template v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)">
          <div class="options-header">
            <h3>选项</h3>
            <el-button type="primary" @click="addOption" size="small">
              <el-icon><Plus /></el-icon> 添加选项
            </el-button>
          </div>
          
          <div v-for="(option, index) in options" :key="index" class="option-item-edit">
            <el-row :gutter="10">
              <el-col :span="4">
                <el-input v-model="option.key" placeholder="选项标识" maxlength="1" />
              </el-col>
              <el-col :span="18">
                <el-input v-model="option.value" placeholder="选项内容" />
              </el-col>
              <el-col :span="2">
                <el-button type="danger" icon="Delete" circle @click="removeOption(index)" />
              </el-col>
            </el-row>
          </div>
        </template>
        
        <!-- 单选题答案 -->
        <el-form-item v-if="questionForm.type === 'SINGLE_CHOICE'" label="正确答案" prop="answer">
          <el-select v-model="questionForm.answer" placeholder="请选择正确答案">
            <el-option 
              v-for="option in options" 
              :key="option.key" 
              :label="option.key" 
              :value="option.key" 
            />
          </el-select>
        </el-form-item>
        
        <!-- 多选题答案 -->
        <el-form-item v-if="questionForm.type === 'MULTIPLE_CHOICE'" label="正确答案" prop="answer">
          <el-select v-model="multipleAnswer" multiple placeholder="请选择正确答案">
            <el-option 
              v-for="option in options" 
              :key="option.key" 
              :label="option.key" 
              :value="option.key" 
            />
          </el-select>
        </el-form-item>
        
        <!-- 判断题答案 -->
        <el-form-item v-if="questionForm.type === 'JUDGMENT'" label="正确答案" prop="answer">
          <el-radio-group v-model="questionForm.answer">
            <el-radio label="A">正确</el-radio>
            <el-radio label="B">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 问答题答案 -->
        <el-form-item v-if="questionForm.type === 'ESSAY'" label="参考答案" prop="answer">
          <el-input 
            v-model="questionForm.answer" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入参考答案"
          />
        </el-form-item>
        
        <el-form-item label="答案解析" prop="analysis">
          <el-input 
            v-model="questionForm.analysis" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入答案解析（可选）"
          />
        </el-form-item>
        
        <el-form-item label="难度等级" prop="difficulty">
          <el-rate 
            v-model="questionForm.difficulty" 
            :colors="difficultyColors"
            show-score
          />
        </el-form-item>
        
        <el-form-item label="所属题库" prop="questionBankIds">
          <el-select 
            v-model="questionForm.questionBankIds" 
            multiple 
            placeholder="请选择所属题库" 
            style="width: 100%"
          >
            <el-option 
              v-for="bank in questionBanks" 
              :key="bank.id" 
              :label="bank.name" 
              :value="bank.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useUserStore } from '../../stores/user';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus, Back, Delete } from '@element-plus/icons-vue';
import QuestionService from '../../api/question';
import QuestionBankService from '../../api/questionBank';

export default {
  name: 'CreateQuestion',
  components: {
    Plus,
    Back,
    Delete
  },
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    const route = useRoute();
    const questionFormRef = ref(null);
    
    const currentUser = computed(() => userStore.user);
    const isEdit = computed(() => route.name === 'EditQuestion');
    
    // 题库信息
    const bankId = ref(route.query.bankId);
    const bankName = ref(route.query.bankName);
    const questionBanks = ref([]);
    
    // 问题表单
    const questionForm = reactive({
      id: null,
      title: '',
      content: '',
      type: 'SINGLE_CHOICE',
      options: null,
      answer: '',
      analysis: '',
      difficulty: 3,
      creatorId: currentUser.value?.id,
      questionBankIds: bankId.value ? [bankId.value] : []
    });
    
    // 多选题的答案需要特殊处理
    const multipleAnswer = ref([]);
    
    // 选项数组（用于表单显示和编辑）
    const options = ref([
      { key: 'A', value: '' },
      { key: 'B', value: '' },
      { key: 'C', value: '' },
      { key: 'D', value: '' }
    ]);
    
    // 难度等级颜色
    const difficultyColors = ['#99A9BF', '#F7BA2A', '#F7BA2A', '#FF9900', '#FF4949'];
    
    // 表单验证规则
    const questionRules = {
      title: [
        { required: true, message: '请输入题目标题', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入题目内容', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择题目类型', trigger: 'change' }
      ],
      answer: [
        { required: true, message: '请提供正确答案', trigger: 'blur' }
      ],
      difficulty: [
        { required: true, message: '请选择难度等级', trigger: 'change' }
      ]
    };
    
    // 监听多选题答案变化，自动转换为逗号分隔的字符串
    watch(multipleAnswer, (newVal) => {
      if (questionForm.type === 'MULTIPLE_CHOICE') {
        questionForm.answer = newVal.join(',');
      }
    });
    
    // 初始化
    onMounted(async () => {
      await loadQuestionBanks();
      
      if (isEdit.value && route.params.id) {
        await loadQuestion(route.params.id);
      }
    });
    
    // 加载题库列表
    const loadQuestionBanks = async () => {
      if (!userStore.isLoggedIn || !currentUser.value || !currentUser.value.id) {
        ElMessage.error('用户未登录');
        return;
      }
      
      try {
        const response = await QuestionBankService.getQuestionBanksByCreator(
          currentUser.value.id,
          'ACTIVE',
          0,
          100 // 获取足够多的题库
        );
        
        questionBanks.value = response.data.content;
      } catch (error) {
        console.error('获取题库列表失败', error);
        ElMessage.error('获取题库列表失败');
      }
    };
    
    // 加载题目详情（编辑模式）
    const loadQuestion = async (id) => {
      try {
        const response = await QuestionService.getQuestion(id);
        const question = response.data;
        
        // 填充表单
        questionForm.id = question.id;
        questionForm.title = question.title;
        questionForm.content = question.content;
        questionForm.type = question.type;
        questionForm.answer = question.answer;
        questionForm.analysis = question.analysis;
        questionForm.difficulty = question.difficulty;
        questionForm.creatorId = question.creatorId;
        questionForm.questionBankIds = question.questionBankIds || [];
        
        // 处理选项
        if (question.options && ['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(question.type)) {
          options.value = question.options;
        }
        
        // 处理多选题答案
        if (question.type === 'MULTIPLE_CHOICE' && question.answer) {
          multipleAnswer.value = question.answer.split(',');
        }
      } catch (error) {
        console.error('获取题目详情失败', error);
        ElMessage.error('获取题目详情失败');
      }
    };
    
    // 题目类型变化处理
    const handleTypeChange = (type) => {
      // 切换到判断题时，设置默认选项
      if (type === 'JUDGMENT') {
        options.value = [
          { key: 'A', value: '正确' },
          { key: 'B', value: '错误' }
        ];
      } else if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(type) && options.value.length < 2) {
        // 确保选择题至少有两个选项
        options.value = [
          { key: 'A', value: '' },
          { key: 'B', value: '' }
        ];
      }
      
      // 重置答案
      questionForm.answer = '';
      multipleAnswer.value = [];
    };
    
    // 添加选项
    const addOption = () => {
      const keys = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
      const nextKey = keys.charAt(options.value.length % keys.length);
      options.value.push({ key: nextKey, value: '' });
    };
    
    // 移除选项
    const removeOption = (index) => {
      if (options.value.length <= 2) {
        ElMessage.warning('至少需要保留两个选项');
        return;
      }
      options.value.splice(index, 1);
      
      // 更新选项的key
      const keys = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
      options.value.forEach((option, idx) => {
        option.key = keys.charAt(idx % keys.length);
      });
    };
    
    // 提交表单
    const submitForm = async () => {
      if (!questionFormRef.value) return;
      
      await questionFormRef.value.validate(async (valid) => {
        if (valid) {
          // 处理选项
          if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)) {
            // 验证选项是否填写完整
            for (const option of options.value) {
              if (!option.value.trim()) {
                ElMessage.warning('请填写完整的选项内容');
                return;
              }
            }
            questionForm.options = options.value;
          } else if (questionForm.type === 'JUDGMENT') {
            questionForm.options = [
              { key: 'A', value: '正确' },
              { key: 'B', value: '错误' }
            ];
          } else {
            questionForm.options = null;
          }
          
          try {
            if (isEdit.value) {
              await QuestionService.updateQuestion(questionForm.id, questionForm);
              ElMessage.success('题目更新成功');
            } else {
              await QuestionService.createQuestion(questionForm);
              ElMessage.success('题目创建成功');
            }
            
            // 返回上一页
            goBack();
          } catch (error) {
            console.error('保存题目失败', error);
            ElMessage.error('保存题目失败');
          }
        }
      });
    };
    
    // 重置表单
    const resetForm = () => {
      if (questionFormRef.value) {
        questionFormRef.value.resetFields();
      }
      
      if (!isEdit.value) {
        // 在创建模式下重置所有字段
        questionForm.title = '';
        questionForm.content = '';
        questionForm.type = 'SINGLE_CHOICE';
        questionForm.answer = '';
        questionForm.analysis = '';
        questionForm.difficulty = 3;
        questionForm.questionBankIds = bankId.value ? [bankId.value] : [];
        
        options.value = [
          { key: 'A', value: '' },
          { key: 'B', value: '' },
          { key: 'C', value: '' },
          { key: 'D', value: '' }
        ];
        
        multipleAnswer.value = [];
      }
    };
    
    // 返回上一页
    const goBack = () => {
      if (bankId.value) {
        router.push({ 
          name: 'QuestionManagement', 
          query: { bankId: bankId.value, bankName: bankName.value } 
        });
      } else {
        router.push({ name: 'QuestionManagement' });
      }
    };
    
    return {
      isEdit,
      bankId,
      bankName,
      questionBanks,
      questionForm,
      questionFormRef,
      questionRules,
      options,
      multipleAnswer,
      difficultyColors,
      handleTypeChange,
      addOption,
      removeOption,
      submitForm,
      resetForm,
      goBack
    };
  }
};
</script>

<style scoped>
.create-question {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-container {
  display: flex;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.bank-name {
  margin-left: 10px;
  color: #909399;
}

.options-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0 10px 100px;
}

.options-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #606266;
}

.option-item-edit {
  margin-bottom: 10px;
  padding-left: 100px;
}
</style> 