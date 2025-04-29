<template>
  <div class="question-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="title-container">
            <span class="title">题目管理</span>
            <span v-if="bankName" class="bank-name">({{ bankName }})</span>
          </div>
          <div class="right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目"
              class="search-input"
              @input="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleCreateQuestion">
              <el-icon><Plus /></el-icon>
              创建题目
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="filter-container">
        <div class="filter-left">
          <el-radio-group v-model="questionType" @change="handleTypeChange">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="SINGLE_CHOICE">单选题</el-radio-button>
            <el-radio-button label="MULTIPLE_CHOICE">多选题</el-radio-button>
            <el-radio-button label="JUDGMENT">判断题</el-radio-button>
            <el-radio-button label="ESSAY">问答题</el-radio-button>
          </el-radio-group>
          
          <div class="bank-selector-container">
            <el-select 
              v-model="selectedBankId" 
              placeholder="选择题库" 
              filterable
              clearable
              @change="handleBankSearch"
            >
              <el-option 
                v-for="bank in questionBanks" 
                :key="bank.id" 
                :label="bank.name" 
                :value="bank.id" 
              />
            </el-select>
            <el-button type="primary" @click="handleBankSearch" :loading="loadingQuestions">搜索</el-button>
            <el-button @click="resetBankFilter" :disabled="loadingQuestions">重置</el-button>
          </div>
        </div>
        
        <div class="filter-right">
          <el-button v-if="bankId" @click="goBack" size="small">
            <el-icon><Back /></el-icon> 返回题库
          </el-button>
        </div>
      </div>
      
      <el-empty v-if="questions.length === 0" description="暂无题目"></el-empty>
      
      <div class="question-list" v-else>
        <el-table
          :data="questions"
          style="width: 100%"
          border
          @row-click="viewQuestionDetail"
        >
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column prop="title" label="题目标题" min-width="200">
            <template #default="scope">
              <div class="question-title">{{ scope.row.title }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.type === 'SINGLE_CHOICE'" type="primary">单选题</el-tag>
              <el-tag v-else-if="scope.row.type === 'MULTIPLE_CHOICE'" type="success">多选题</el-tag>
              <el-tag v-else-if="scope.row.type === 'JUDGMENT'" type="warning">判断题</el-tag>
              <el-tag v-else-if="scope.row.type === 'ESSAY'" type="info">问答题</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100" align="center">
            <template #default="scope">
              <el-rate
                v-model="scope.row.difficulty"
                disabled
                show-score
                text-color="#ff9900"
                :colors="difficultyColors"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button type="primary" text @click.stop="handleEditQuestion(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="danger" text @click.stop="handleDeleteQuestion(scope.row)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-container" v-if="totalItems > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="totalItems"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="questionDetailVisible"
      title="题目详情"
      width="700px"
    >
      <div v-if="currentQuestion" class="question-detail">
        <div class="detail-header">
          <div class="detail-title">{{ currentQuestion.title }}</div>
          <div class="detail-meta">
            <el-tag v-if="currentQuestion.type === 'SINGLE_CHOICE'" type="primary">单选题</el-tag>
            <el-tag v-else-if="currentQuestion.type === 'MULTIPLE_CHOICE'" type="success">多选题</el-tag>
            <el-tag v-else-if="currentQuestion.type === 'JUDGMENT'" type="warning">判断题</el-tag>
            <el-tag v-else-if="currentQuestion.type === 'ESSAY'" type="info">问答题</el-tag>
            <span class="difficulty">难度：</span>
            <el-rate
              v-model="currentQuestion.difficulty"
              disabled
              show-score
              text-color="#ff9900"
              :colors="difficultyColors"
            />
          </div>
        </div>
        
        <div class="detail-content">
          <p class="question-content">{{ currentQuestion.content }}</p>
          
          <!-- 选项 -->
          <div v-if="['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(currentQuestion.type)">
            <div v-for="(option, index) in currentQuestion.options" :key="index" class="option-item">
              <span class="option-key">{{ option.key }}.</span>
              <span class="option-value">{{ option.value }}</span>
            </div>
          </div>
          
          <!-- 判断题只有True/False选项 -->
          <div v-if="currentQuestion.type === 'JUDGMENT'" class="judgment-options">
            <div class="option-item">
              <span class="option-key">A.</span>
              <span class="option-value">正确</span>
            </div>
            <div class="option-item">
              <span class="option-key">B.</span>
              <span class="option-value">错误</span>
            </div>
          </div>
        </div>
        
        <div class="detail-answer">
          <h4>参考答案</h4>
          <p>{{ currentQuestion.answer }}</p>
        </div>
        
        <div class="detail-analysis" v-if="currentQuestion.analysis">
          <h4>答案解析</h4>
          <p>{{ currentQuestion.analysis }}</p>
        </div>
      </div>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <p>确定要删除题目 "{{ currentQuestion?.title }}" 吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue';
import { useUserStore } from '../../stores/user';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Plus, Edit, Delete, Back } from '@element-plus/icons-vue';
import QuestionService from '../../api/question';
import { formatDateTime } from '../../utils/dateUtils';
import QuestionBankService from '../../api/questionBank';

export default {
  name: 'QuestionManagement',
  components: {
    Search,
    Plus,
    Edit,
    Delete,
    Back
  },
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    const route = useRoute();
    
    const currentUser = computed(() => userStore.user);
    
    // 根据URL参数设置题库ID和名称
    const bankId = ref(route.query.bankId);
    const bankName = ref(route.query.bankName);
    
    // 题库选择
    const questionBanks = ref([]);
    const selectedBankId = ref(bankId.value || null);
    
    const questionType = ref('ALL');
    const searchKeyword = ref('');
    const questions = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    
    const questionDetailVisible = ref(false);
    const deleteDialogVisible = ref(false);
    const currentQuestion = ref(null);
    
    const difficultyColors = ['#99A9BF', '#F7BA2A', '#F7BA2A', '#FF9900', '#FF4949'];
    
    const loadingBanks = ref(false);
    const loadingQuestions = ref(false);
    
    // 初始化加载数据
    onMounted(async () => {
      await loadQuestionBanks();
      
      // 如果URL中有题库ID，则设置选中的题库
      if (route.query.bankId) {
        selectedBankId.value = route.query.bankId;
        bankId.value = route.query.bankId;
      }
      
      loadQuestions();
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
        
        // 检查返回结果是否成功以及数据是否存在
        if (response.data && response.data.code === 200 && response.data.data) {
          questionBanks.value = response.data.data.content; // 从 response.data.data.content 获取
        } else {
          console.error('获取题库列表失败:', response.data?.message || '未知错误');
          ElMessage.error(response.data?.message || '获取题库列表失败');
          questionBanks.value = []; // 清空列表以防万一
        }
      } catch (error) {
        console.error('获取题库列表失败', error);
        ElMessage.error('获取题库列表失败');
      }
    };
    
    // 题库变化处理
    const handleBankChange = (value) => {
      if (value) {
        bankId.value = value;
        const selectedBank = questionBanks.value.find(bank => bank.id === value);
        bankName.value = selectedBank ? selectedBank.name : '';
      } else {
        bankId.value = null;
        bankName.value = null;
      }
      currentPage.value = 1;
      loadQuestions();
    };
    
    // 加载题目列表
    const loadQuestions = async () => {
      if (!userStore.isLoggedIn || !currentUser.value || !currentUser.value.id) {
        ElMessage.error('用户未登录');
        return;
      }
      
      loadingQuestions.value = true;
      
      try {
        let response;
        if (selectedBankId.value) {
          // 加载题库中的题目
          if (questionType.value === 'ALL') {
            response = await QuestionService.getQuestionsByBank(
              selectedBankId.value,
              currentPage.value - 1,
              pageSize.value
            );
          } else {
            response = await QuestionService.getQuestionsByBankAndType(
              selectedBankId.value,
              questionType.value,
              currentPage.value - 1,
              pageSize.value
            );
          }
        } else {
          // 加载用户创建的所有题目
          if (questionType.value === 'ALL') {
            response = searchKeyword.value
              ? await QuestionService.searchQuestions(
                  searchKeyword.value,
                  currentUser.value.id,
                  currentPage.value - 1,
                  pageSize.value
                )
              : await QuestionService.getQuestionsByCreator(
                  currentUser.value.id,
                  currentPage.value - 1,
                  pageSize.value
                );
          } else {
            response = await QuestionService.getQuestionsByType(
              questionType.value,
              currentUser.value.id,
              currentPage.value - 1,
              pageSize.value
            );
          }
        }
        
        // 统一处理后端返回的数据结构，检查 Result 包装
        if (response.data && response.data.code === 200 && response.data.data) {
          questions.value = response.data.data.content;
          totalItems.value = response.data.data.totalItems;
        } else if (response.data && response.data.content) { 
          // 兼容直接返回 Map 的情况 (虽然理想情况是后端统一返回 Result)
          questions.value = response.data.content;
          totalItems.value = response.data.totalItems;
        } else {
          console.error('获取题目列表数据结构错误或失败:', response.data?.message || '未知错误');
          ElMessage.error(response.data?.message || '获取题目列表失败');
          questions.value = [];
          totalItems.value = 0;
        }

      } catch (error) {
        console.error('获取题目列表失败', error);
        ElMessage.error('获取题目列表失败');
      } finally {
        loadingQuestions.value = false;
      }
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadQuestions();
    };
    
    // 类型变化
    const handleTypeChange = () => {
      currentPage.value = 1;
      loadQuestions();
    };
    
    // 页码变化
    const handlePageChange = (page) => {
      currentPage.value = page;
      loadQuestions();
    };
    
    // 创建题目
    const handleCreateQuestion = () => {
      router.push({
        name: 'CreateQuestion',
        query: selectedBankId.value ? { 
          bankId: selectedBankId.value, 
          bankName: questionBanks.value.find(bank => bank.id === selectedBankId.value)?.name || ''
        } : {}
      });
    };
    
    // 编辑题目
    const handleEditQuestion = (question) => {
      router.push({
        name: 'EditQuestion',
        params: { id: question.id },
        query: selectedBankId.value ? { 
          bankId: selectedBankId.value, 
          bankName: questionBanks.value.find(bank => bank.id === selectedBankId.value)?.name || ''
        } : {}
      });
    };
    
    // 删除题目
    const handleDeleteQuestion = (question) => {
      currentQuestion.value = question;
      deleteDialogVisible.value = true;
    };
    
    // 确认删除
    const confirmDelete = async () => {
      if (!currentQuestion.value) return;
      
      try {
        await QuestionService.deleteQuestion(currentQuestion.value.id);
        ElMessage.success('题目删除成功');
        deleteDialogVisible.value = false;
        loadQuestions();
      } catch (error) {
        console.error('题目删除失败', error);
        ElMessage.error('题目删除失败');
      }
    };
    
    // 查看题目详情
    const viewQuestionDetail = (row) => {
      currentQuestion.value = row;
      questionDetailVisible.value = true;
    };
    
    // 返回题库页面
    const goBack = () => {
      router.push({ name: 'QuestionBankManagement' });
    };
    
    // 格式化日期
    const formatDate = (date) => {
      return formatDateTime(date);
    };
    
    // 搜索题库
    const handleBankSearch = () => {
      currentPage.value = 1;
      loadQuestions();
    };
    
    // 重置题库过滤器
    const resetBankFilter = () => {
      selectedBankId.value = null;
      currentPage.value = 1;
      loadQuestions();
    };
    
    // 搜索题库
    const searchQuestionBanks = async (query) => {
      if (query) {
        loadingBanks.value = true;
        try {
          const response = await QuestionBankService.searchQuestionBanks(
            query,
            currentUser.value.id,
            'ACTIVE',
            0,
            10
          );
          questionBanks.value = response.data.content;
        } catch (error) {
          console.error('搜索题库失败', error);
        } finally {
          loadingBanks.value = false;
        }
      } else {
        loadQuestionBanks();
      }
    };
    
    return {
      bankId,
      bankName,
      questionType,
      searchKeyword,
      questions,
      currentPage,
      pageSize,
      totalItems,
      questionDetailVisible,
      deleteDialogVisible,
      currentQuestion,
      difficultyColors,
      handleSearch,
      handleTypeChange,
      handlePageChange,
      handleCreateQuestion,
      handleEditQuestion,
      handleDeleteQuestion,
      confirmDelete,
      viewQuestionDetail,
      goBack,
      formatDate,
      questionBanks,
      selectedBankId,
      handleBankSearch,
      resetBankFilter,
      loadingBanks,
      loadingQuestions,
      searchQuestionBanks
    };
  }
};
</script>

<style scoped>
.question-management {
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

.right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 250px;
  margin-right: 10px;
}

.filter-container {
  margin: 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
}

.bank-selector-container {
  display: flex;
  align-items: center;
  margin: 0 15px;
}

.bank-selector-container .el-select {
  min-width: 200px;
  margin-right: 10px;
}

.filter-right {
  display: flex;
  align-items: center;
}

.question-list {
  margin-top: 20px;
}

.question-title {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 详情样式 */
.question-detail {
  padding: 10px;
}

.detail-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.detail-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.detail-meta {
  display: flex;
  align-items: center;
}

.difficulty {
  margin: 0 10px;
}

.detail-content {
  margin: 20px 0;
}

.question-content {
  font-size: 16px;
  margin-bottom: 15px;
}

.option-item {
  margin: 10px 0;
  display: flex;
}

.option-key {
  width: 30px;
  font-weight: bold;
}

.detail-answer, .detail-analysis {
  margin-top: 20px;
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
}

.detail-answer h4, .detail-analysis h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409eff;
}
</style> 