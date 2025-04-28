<template>
  <div class="exam-student-list-container">
    <div class="page-header">
      <div class="left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>{{ exam?.title }} - 学生列表</h2>
      </div>
    </div>
    
    <el-card class="student-list-card">
      <div class="filter-bar mb-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名或学号"
          prefix-icon="Search"
          clearable
          @change="applyFilter"
          class="search-input mr-3"
          style="width: 250px"
        />
        
        <el-radio-group v-model="statusFilter" @change="applyFilter">
          <el-radio-button label="">全部状态</el-radio-button>
          <el-radio-button label="NOT_STARTED">未开始</el-radio-button>
          <el-radio-button label="IN_PROGRESS">进行中</el-radio-button>
          <el-radio-button label="SUBMITTED">待批阅</el-radio-button>
          <el-radio-button label="PENDING_PUBLISH">待发布</el-radio-button>
          <el-radio-button label="PUBLISHED">已发布</el-radio-button>
        </el-radio-group>
      </div>
      
      <el-table :data="filteredStudents" v-loading="loading" border stripe>
        <el-table-column prop="studentUserNumber" label="学号" width="150"></el-table-column>
        <el-table-column prop="studentName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="scope">
            {{ scope.row.startTime ? formatDateTime(scope.row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="scope">
            {{ scope.row.submitTime ? formatDateTime(scope.row.submitTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="100">
          <template #default="scope">
            <template v-if="scope.row.status === 'PENDING_PUBLISH' || scope.row.status === 'PUBLISHED'">
              {{ scope.row.score }}
            </template>
            <template v-else>-</template>
          </template>
        </el-table-column>
        <el-table-column prop="isPassed" label="是否及格" width="100">
          <template #default="scope">
            <template v-if="scope.row.status === 'PENDING_PUBLISH' || scope.row.status === 'PUBLISHED'">
              <el-tag type="success" v-if="scope.row.isPassed">及格</el-tag>
              <el-tag type="danger" v-else>不及格</el-tag>
            </template>
            <template v-else>-</template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              :disabled="scope.row.status !== 'SUBMITTED'"
              @click="gradeExam(scope.row)"
            >
              批阅
            </el-button>
            <el-button 
              type="success"
              size="small" 
              v-if="scope.row.status === 'PENDING_PUBLISH'"
              @click="publishGrade(scope.row)"
            >
              发布
            </el-button>
            <el-button 
              size="small" 
              v-if="scope.row.status === 'PENDING_PUBLISH' || scope.row.status === 'PUBLISHED'"
              @click="gradeExam(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { getExam, getExamStudents, publishStudentGrades } from '../../api/exam';
import { formatDateTime } from '../../utils/dateUtil';

const route = useRoute();
const router = useRouter();
const examId = Number(route.params.id);

const loading = ref(true);
const exam = ref<any>(null);
const students = ref<any[]>([]);
const searchKeyword = ref('');
const statusFilter = ref('');

// 根据搜索关键词和状态过滤学生列表
const filteredStudents = computed(() => {
  let result = students.value;
  
  // 按状态过滤
  if (statusFilter.value) {
    result = result.filter(student => student.status === statusFilter.value);
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(student => 
      (student.studentName && student.studentName.toLowerCase().includes(keyword)) ||
      (student.studentUserNumber && student.studentUserNumber.toLowerCase().includes(keyword))
    );
  }
  
  return result;
});

// 加载考试详情
const loadExamDetail = async () => {
  loading.value = true;
  try {
    const response = await getExam(examId);
    
    if (response.data && response.data.code === 200) {
      exam.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '获取考试详情失败');
    }
  } catch (error: any) {
    console.error('获取考试详情失败:', error);
    ElMessage.error('获取考试详情失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    loading.value = false;
  }
};

// 加载考试学生列表
const loadExamStudents = async () => {
  loading.value = true;
  try {
    const response = await getExamStudents(examId);
    
    if (response.data && response.data.code === 200) {
      students.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '获取学生列表失败');
    }
  } catch (error: any) {
    console.error('获取学生列表失败:', error);
    ElMessage.error('获取学生列表失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    loading.value = false;
  }
};

// 应用过滤器
const applyFilter = () => {
  // 过滤器变更时自动通过计算属性更新
};

// 跳转到批阅页面
const gradeExam = (student: any) => {
  router.push(`/supervisor/exams/${examId}/grade/${student.studentId}`);
};

// 发布学生成绩
const publishGrade = async (student: any) => {
  try {
    loading.value = true;
    const response = await publishStudentGrades(examId, student.studentId);
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('成绩发布成功');
      // 重新加载学生列表
      await loadExamStudents();
    } else {
      ElMessage.error(response.data?.message || '发布成绩失败');
    }
  } catch (error: any) {
    console.error('发布成绩失败:', error);
    ElMessage.error('发布成绩失败: ' + (error.response?.data?.message || '请检查网络连接'));
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'SUBMITTED': 'danger',
    'PENDING_PUBLISH': 'warning',
    'PUBLISHED': 'success'
  };
  return typeMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'SUBMITTED': '待批阅',
    'PENDING_PUBLISH': '待发布',
    'PUBLISHED': '已发布'
  };
  return textMap[status] || status;
};

// 页面加载时获取数据
onMounted(() => {
  loadExamDetail();
  loadExamStudents();
});
</script>

<style scoped>
.exam-student-list-container {
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

.student-list-card {
  margin-bottom: 20px;
}
</style> 