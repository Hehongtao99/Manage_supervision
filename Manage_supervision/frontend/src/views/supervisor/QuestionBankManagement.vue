<template>
  <div class="question-bank-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">题库管理</span>
          <div class="right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题库"
              class="search-input"
              @input="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleCreateBank">
              <el-icon><Plus /></el-icon>
              创建题库
            </el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="活跃题库" name="active"></el-tab-pane>
        <el-tab-pane label="已归档题库" name="archived"></el-tab-pane>
      </el-tabs>
      
      <el-empty v-if="questionBanks.length === 0" description="暂无题库"></el-empty>
      
      <div class="bank-list" v-else>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="bank in questionBanks" :key="bank.id">
            <el-card shadow="hover" class="bank-card">
              <div class="bank-header">
                <h3 class="bank-title">{{ bank.name }}</h3>
                <el-dropdown trigger="click" @command="handleCommand($event, bank)">
                  <el-button type="primary" text>
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑题库</el-dropdown-item>
                      <el-dropdown-item command="view">查看题目</el-dropdown-item>
                      <el-dropdown-item command="add">添加题目</el-dropdown-item>
                      <el-dropdown-item v-if="activeTab === 'active'" command="archive" divided>归档题库</el-dropdown-item>
                      <el-dropdown-item v-else command="activate" divided>激活题库</el-dropdown-item>
                      <el-dropdown-item command="delete" divided danger>删除题库</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div class="bank-content">
                <p class="bank-description">{{ bank.description || '暂无描述' }}</p>
                <div class="bank-stats">
                  <p><el-icon><Collection /></el-icon> 题目数量: {{ bank.questionCount }}</p>
                  <p><el-icon><Timer /></el-icon> 创建时间: {{ formatDate(bank.createTime) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
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
    
    <!-- 创建/编辑题库对话框 -->
    <el-dialog
      v-model="bankDialogVisible"
      :title="isEdit ? '编辑题库' : '创建题库'"
      width="500px"
      @close="resetBankForm"
    >
      <el-form :model="bankForm" ref="bankFormRef" :rules="bankRules" label-width="100px">
        <el-form-item label="题库名称" prop="name">
          <el-input v-model="bankForm.name" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="题库描述" prop="description">
          <el-input
            v-model="bankForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题库描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bankDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBankForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <p>确定要删除题库 "{{ currentBank?.name }}" 吗？此操作不可恢复，题库中的所有题目关联也将被删除。</p>
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
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus, More, Collection, Timer } from '@element-plus/icons-vue';
import QuestionBankService from '../../api/questionBank';
import { formatDateTime } from '../../utils/dateUtils';

export default {
  name: 'QuestionBankManagement',
  components: {
    Search,
    Plus,
    More,
    Collection,
    Timer
  },
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    const bankFormRef = ref(null);
    
    const currentUser = computed(() => userStore.user);
    
    const activeTab = ref('active');
    const searchKeyword = ref('');
    const questionBanks = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    const bankDialogVisible = ref(false);
    const deleteDialogVisible = ref(false);
    const isEdit = ref(false);
    const currentBank = ref(null);
    
    const bankForm = reactive({
      name: '',
      description: '',
      creatorId: null
    });
    
    const bankRules = {
      name: [
        { required: true, message: '请输入题库名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ]
    };
    
    // 初始化加载数据
    onMounted(() => {
      loadQuestionBanks();
    });
    
    // 加载题库列表
    const loadQuestionBanks = async () => {
      if (!userStore.isLoggedIn || !currentUser.value || !currentUser.value.id) {
        ElMessage.error('用户未登录');
        return;
      }
      
      try {
        const status = activeTab.value.toUpperCase();
        const response = searchKeyword.value
          ? await QuestionBankService.searchQuestionBanks(
              searchKeyword.value,
              currentUser.value.id,
              status,
              currentPage.value - 1,
              pageSize.value
            )
          : await QuestionBankService.getQuestionBanksByCreator(
              currentUser.value.id,
              status,
              currentPage.value - 1,
              pageSize.value
            );
        
        questionBanks.value = response.data.content;
        totalItems.value = response.data.totalItems;
      } catch (error) {
        console.error('获取题库列表失败', error);
        ElMessage.error('获取题库列表失败');
      }
    };
    
    // 切换选项卡
    const handleTabClick = () => {
      currentPage.value = 1;
      loadQuestionBanks();
    };
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadQuestionBanks();
    };
    
    // 页码变化
    const handlePageChange = (page) => {
      currentPage.value = page;
      loadQuestionBanks();
    };
    
    // 创建题库
    const handleCreateBank = () => {
      isEdit.value = false;
      bankForm.creatorId = currentUser.value.id;
      bankDialogVisible.value = true;
    };
    
    // 编辑题库
    const handleEditBank = (bank) => {
      isEdit.value = true;
      currentBank.value = bank;
      bankForm.name = bank.name;
      bankForm.description = bank.description;
      bankForm.creatorId = currentUser.value.id;
      bankDialogVisible.value = true;
    };
    
    // 提交题库表单
    const submitBankForm = async () => {
      if (!bankFormRef.value) return;
      
      await bankFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              await QuestionBankService.updateQuestionBank(currentBank.value.id, bankForm);
              ElMessage.success('题库更新成功');
            } else {
              await QuestionBankService.createQuestionBank(bankForm);
              ElMessage.success('题库创建成功');
            }
            bankDialogVisible.value = false;
            loadQuestionBanks();
          } catch (error) {
            console.error('保存题库失败', error);
            ElMessage.error('保存题库失败');
          }
        }
      });
    };
    
    // 重置表单
    const resetBankForm = () => {
      if (bankFormRef.value) {
        bankFormRef.value.resetFields();
      }
      bankForm.name = '';
      bankForm.description = '';
      currentBank.value = null;
    };
    
    // 处理下拉菜单命令
    const handleCommand = (command, bank) => {
      currentBank.value = bank;
      
      switch (command) {
        case 'edit':
          handleEditBank(bank);
          break;
        case 'view':
          router.push({ 
            name: 'QuestionManagement', 
            query: { bankId: bank.id, bankName: bank.name } 
          });
          break;
        case 'add':
          router.push({ 
            name: 'CreateQuestion', 
            query: { bankId: bank.id, bankName: bank.name } 
          });
          break;
        case 'archive':
          archiveBank(bank.id);
          break;
        case 'activate':
          activateBank(bank.id);
          break;
        case 'delete':
          deleteDialogVisible.value = true;
          break;
      }
    };
    
    // 归档题库
    const archiveBank = async (bankId) => {
      try {
        await QuestionBankService.archiveQuestionBank(bankId);
        ElMessage.success('题库归档成功');
        loadQuestionBanks();
      } catch (error) {
        console.error('题库归档失败', error);
        ElMessage.error('题库归档失败');
      }
    };
    
    // 激活题库
    const activateBank = async (bankId) => {
      try {
        await QuestionBankService.activateQuestionBank(bankId);
        ElMessage.success('题库激活成功');
        loadQuestionBanks();
      } catch (error) {
        console.error('题库激活失败', error);
        ElMessage.error('题库激活失败');
      }
    };
    
    // 确认删除题库
    const confirmDelete = async () => {
      if (!currentBank.value) return;
      
      try {
        await QuestionBankService.deleteQuestionBank(currentBank.value.id);
        ElMessage.success('题库删除成功');
        deleteDialogVisible.value = false;
        loadQuestionBanks();
      } catch (error) {
        console.error('题库删除失败', error);
        ElMessage.error('题库删除失败');
      }
    };
    
    // 格式化日期
    const formatDate = (date) => {
      return formatDateTime(date);
    };
    
    return {
      activeTab,
      searchKeyword,
      questionBanks,
      currentPage,
      pageSize,
      totalItems,
      bankDialogVisible,
      deleteDialogVisible,
      isEdit,
      currentBank,
      bankForm,
      bankRules,
      bankFormRef,
      handleTabClick,
      handleSearch,
      handlePageChange,
      handleCreateBank,
      handleCommand,
      submitBankForm,
      resetBankForm,
      confirmDelete,
      formatDate
    };
  }
};
</script>

<style scoped>
.question-bank-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 250px;
  margin-right: 10px;
}

.bank-list {
  margin-top: 20px;
}

.bank-card {
  height: 180px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.bank-title {
  margin: 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.bank-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bank-description {
  flex: 1;
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 10px;
}

.bank-stats {
  font-size: 12px;
  color: #909399;
}

.bank-stats p {
  margin: 5px 0;
  display: flex;
  align-items: center;
}

.bank-stats .el-icon {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 