<template>
  <div class="exam-management-container">
    <div class="page-header">
      <h2>考试管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索考试名称"
          clearable
          style="width: 200px; margin-right: 10px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 130px; margin-right: 10px"
          @change="loadExams"
        >
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="FINISHED" />
        </el-select>
        <el-button type="primary" @click="goToCreateExam">
          创建考试
        </el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="exam-list">
      <template v-if="!exams || exams.length === 0">
        <div class="empty-exams">
          <el-empty description="暂无考试数据" />
          <el-button type="primary" @click="goToCreateExam">创建考试</el-button>
        </div>
      </template>
      <el-table v-else :data="exams" style="width: 100%">
        <el-table-column prop="title" label="考试名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.displayStatus)" size="small">
              {{ getStatusText(scope.row.displayStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope">
            {{ scope.row.duration }} 分钟
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200">
          <template #default="scope">
            <div style="line-height: 1.5;">
              <div>总人数: {{ scope.row.totalStudents || 0 }}</div>
              <div v-if="scope.row.submittedCount > 0">
                 <el-tag type="warning" size="small" effect="plain">待批阅: {{ scope.row.submittedCount }}</el-tag>
              </div>
              <div v-if="scope.row.pendingPublishCount > 0">
                 <el-tag type="primary" size="small" effect="plain">待发布: {{ scope.row.pendingPublishCount }}</el-tag>
              </div>
               <div v-if="scope.row.publishedCount > 0">
                 <el-tag type="success" size="small" effect="plain">已发布: {{ scope.row.publishedCount }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              @click="viewExam(scope.row)"
            >
              查看
            </el-button>
            <el-button
              type="primary"
              size="small"
              :disabled="scope.row.status !== 'DRAFT'"
              @click="editExam(scope.row)"
            >
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleCommand(command, scope.row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="scope.row.status === 'DRAFT'"
                    command="publish"
                  >
                    发布
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="hasPendingGrading(scope.row)"
                    command="gradeExams"
                  >
                    批阅试卷
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="viewStudents"
                  >
                    查看学生
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="scope.row.status !== 'ONGOING'"
                    command="delete"
                    divided
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, ArrowDown } from '@element-plus/icons-vue';
import { getExams, deleteExam, publishExam } from '../../api/exam';
import { formatDateTime } from '../../utils/dateUtil';

const router = useRouter();

// 考试列表数据
const exams = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const statusFilter = ref('');

// 页面加载时获取考试列表
onMounted(() => {
  loadExams();
});

// 加载考试列表
const loadExams = async () => {
  loading.value = true;
  
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    };
    
    const response = await getExams(params);
    
    if (response.data && response.data.code === 200) {
      const responseData = response.data.data;
      exams.value = responseData.content || [];
      total.value = responseData.totalElements || 0;
    } else {
      ElMessage.error(response.data?.message || '获取考试列表失败');
    }
  } catch (error: any) {
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    loading.value = false;
  }
};

// 搜索考试
const handleSearch = () => {
  currentPage.value = 1;
  loadExams();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  loadExams();
};

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
  loadExams();
};

// 跳转到创建考试页面
const goToCreateExam = () => {
  router.push('/supervisor/exams/create');
};

// 查看考试详情
const viewExam = (exam: any) => {
  router.push(`/supervisor/exams/${exam.id}`);
};

// 查看考试学生列表
const viewExamStudents = (exam: any) => {
  router.push(`/supervisor/exams/${exam.id}/students`);
};

// 编辑考试
const editExam = (exam: any) => {
  router.push(`/supervisor/exams/${exam.id}/edit`);
};

// 处理更多下拉菜单的命令
const handleCommand = (command: string, exam: any) => {
  if (command === 'publish') {
    publishExamAction(exam);
  } else if (command === 'delete') {
    confirmDeleteExam(exam);
  } else if (command === 'gradeExams') {
    showGradeExamDialog(exam);
  } else if (command === 'viewStudents') {
    viewExamStudents(exam);
  }
};

// 发布考试
const publishExamAction = async (exam: any) => {
  try {
    const response = await publishExam(exam.id);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('考试发布成功');
      loadExams(); // 重新加载列表
    } else {
      ElMessage.error(response.data?.message || '发布考试失败');
    }
  } catch (error: any) {
    console.error('发布考试失败:', error);
    ElMessage.error('发布考试失败: ' + (error.response?.data?.message || '请检查网络连接'));
  }
};

// 确认删除考试
const confirmDeleteExam = (exam: any) => {
  ElMessageBox.confirm(
    '确定要删除该考试吗？此操作不可撤销。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      deleteExamAction(exam.id);
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 删除考试
const deleteExamAction = async (examId: number) => {
  try {
    const response = await deleteExam(examId);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('考试删除成功');
      loadExams(); // 重新加载列表
    } else {
      ElMessage.error(response.data?.message || '删除考试失败');
    }
  } catch (error: any) {
    console.error('删除考试失败:', error);
    ElMessage.error('删除考试失败: ' + (error.response?.data?.message || '请检查网络连接'));
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ONGOING: '进行中',
    FINISHED: '已结束',
    COMPLETED: '已完成',
    PENDING: '待批阅',
    '草稿': '草稿',
    '已发布': '已发布',
    '进行中': '进行中',
    '已完成': '已完成',
    '已结束': '已结束'
  };
  return statusMap[status] || status;
};

// 获取状态标签类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    PUBLISHED: 'primary',
    ONGOING: 'warning',
    FINISHED: 'info',
    COMPLETED: 'success',
    '草稿': 'info',
    '已发布': 'primary',
    '进行中': 'warning',
    '已完成': 'success',
    '已结束': 'info'
  };
  return typeMap[status] || 'info';
};

// 判断是否有待批阅的学生
const hasPendingGrading = (exam: any) => {
  return exam.submittedCount > 0;
};

// 显示批阅考试对话框
const showGradeExamDialog = async (exam: any) => {
  // 直接跳转到学生列表页面
  router.push(`/supervisor/exams/${exam.id}/students`);
};
</script>

<style scoped>
.exam-management-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.exam-list {
  margin-bottom: 20px;
}

.empty-exams {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.empty-exams .el-button {
  margin-top: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 