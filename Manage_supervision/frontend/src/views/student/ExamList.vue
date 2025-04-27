<template>
  <div class="exam-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的考试</span>
          <div class="right">
            <el-select v-model="statusFilter" placeholder="考试状态" class="m-2" @change="fetchExams">
              <el-option label="全部" value="" />
              <el-option label="未开始" value="NOT_STARTED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="已评分" value="GRADED" />
            </el-select>
            <!-- 调试按钮，仅在开发环境显示 -->
            <el-button v-if="isDevelopment" type="primary" size="small" @click="createTestData" style="margin-left: 10px;">
              创建测试数据
            </el-button>
            <el-button v-if="isDevelopment" type="warning" size="small" @click="fetchRawData" style="margin-left: 10px;">
              获取原始数据
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="exams" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="考试名称" min-width="180">
          <template #default="scope">
            <el-link :underline="false" type="primary" @click="viewExam(scope.row)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" min-width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="考试时长" min-width="80">
          <template #default="scope">
            {{ scope.row.duration }} 分钟
          </template>
        </el-table-column>
        <el-table-column prop="status" label="考试状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status, scope.row.studentStatus)">
              {{ getStatusText(scope.row.status, scope.row.studentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentScore" label="成绩" min-width="80">
          <template #default="scope">
            <span v-if="scope.row.studentStatus === 'GRADED'">{{ scope.row.studentScore }} / {{ scope.row.totalScore }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              v-if="canTakeExam(scope.row)"
              type="primary"
              size="small"
              @click="startExam(scope.row)"
            >
              {{ scope.row.studentStatus === 'IN_PROGRESS' ? '继续考试' : '开始考试' }}
            </el-button>
            <el-button
              v-else-if="scope.row.studentStatus === 'SUBMITTED' || scope.row.studentStatus === 'GRADED'"
              type="info"
              size="small"
              @click="viewExamResult(scope.row)"
            >
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="exams.length === 0 && !loading" class="empty-data">
        <el-empty description="暂无考试数据"></el-empty>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getStudentExams, createTestExamData, getStudentExamsRaw } from '@/api/exam';
import { formatDateTime } from '@/utils/formatter';

const router = useRouter();
const exams = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const statusFilter = ref('');

// 判断是否开发环境
const isDevelopment = computed(() => {
  return import.meta.env.MODE === 'development' || window.location.hostname === 'localhost';
});

// 获取考试列表
const fetchExams = async () => {
  loading.value = true;
  try {
    const response = await getStudentExams({
      page: currentPage.value - 1,
      size: pageSize.value,
      status: statusFilter.value
    });
    
    console.log('考试列表API响应:', response.data);
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data;
      console.log('解析响应数据:', data);
      
      if (data && Array.isArray(data.content)) {
        console.log('考试列表数据内容:', data.content);
        exams.value = data.content;
        total.value = data.totalElements || 0;
        console.log('解析后的数据:', exams.value.length, '条记录');
      } else if (data && data.content === null) {
        console.warn('考试列表内容为null');
        exams.value = [];
        total.value = data.totalElements || 0;
      } else if (data && typeof data === 'object' && !data.content) {
        // 处理可能直接返回Page对象的情况
        if (Array.isArray(data)) {
          console.log('返回的是数组数据');
          exams.value = data;
          total.value = data.length;
        } else if (data.empty !== undefined) {
          console.log('返回的是Page对象');
          exams.value = data.content || [];
          total.value = data.totalElements || 0;
        } else {
          console.warn('未知数据格式:', data);
          exams.value = [];
          total.value = 0;
        }
      } else {
        console.warn('API返回数据格式不符合预期:', data);
        exams.value = [];
        total.value = 0;
      }
    } else {
      console.error('API请求失败:', response.data);
      ElMessage.error(response.data?.message || '获取考试列表失败');
      exams.value = [];
      total.value = 0;
    }
    
    // 如果返回的数组为空，检查是否是因为没有数据
    if (exams.value.length === 0) {
      console.log('考试列表为空');
    }
  } catch (error) {
    console.error('获取考试列表失败', error);
    ElMessage.error('获取考试列表失败');
    exams.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 分页
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchExams();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchExams();
};

// 获取考试状态类型
const getStatusType = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'GRADED') {
    return 'success';
  }
  
  if (studentStatus === 'SUBMITTED') {
    return 'info';
  }
  
  if (studentStatus === 'IN_PROGRESS') {
    return 'warning';
  }
  
  // 检查考试整体状态
  switch (examStatus) {
    case 'PUBLISHED':
      return 'primary';
    case 'ONGOING':
      return 'warning';
    case 'FINISHED':
      return 'info';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (examStatus: string, studentStatus: string) => {
  if (studentStatus === 'GRADED') {
    return '已完成';
  }
  
  if (studentStatus === 'SUBMITTED') {
    return '已提交';
  }
  
  if (studentStatus === 'IN_PROGRESS') {
    return '进行中';
  }
  
  if (studentStatus === 'NOT_STARTED') {
    switch (examStatus) {
      case 'PUBLISHED':
      case 'ONGOING':
        return '待参加';
      case 'FINISHED':
        return '已结束';
      default:
        return '未开始';
    }
  }
  
  return '未知状态';
};

// 判断是否可以参加考试
const canTakeExam = (exam: any) => {
  // 如果已经提交或评分，则不能再次参加
  if (exam.studentStatus === 'SUBMITTED' || exam.studentStatus === 'GRADED') {
    return false;
  }
  
  // 如果考试状态是进行中或发布，且当前时间在考试时间范围内
  const now = new Date().getTime();
  const startTime = new Date(exam.startTime).getTime();
  const endTime = new Date(exam.endTime).getTime();
  
  return (exam.status === 'ONGOING' || exam.status === 'PUBLISHED') && 
         now >= startTime && now <= endTime;
};

// 查看考试详情
const viewExam = (exam: any) => {
  router.push(`/student/exams/${exam.id}`);
};

// 开始考试
const startExam = (exam: any) => {
  router.push(`/student/exams/${exam.id}/take`);
};

// 查看考试结果
const viewExamResult = (exam: any) => {
  router.push(`/student/exams/${exam.id}/result`);
};

// 创建测试数据
const createTestData = async () => {
  try {
    loading.value = true;
    const response = await createTestExamData();
    if (response.data && response.data.success) {
      ElMessage.success('测试数据创建成功');
      fetchExams(); // 刷新考试列表
    } else {
      ElMessage.error(response.data?.message || '创建测试数据失败');
    }
  } catch (error) {
    console.error('创建测试数据失败', error);
    ElMessage.error('创建测试数据失败');
  } finally {
    loading.value = false;
  }
};

// 获取原始数据
const fetchRawData = async () => {
  try {
    loading.value = true;
    await getStudentExamsRaw({
      page: currentPage.value - 1,
      size: pageSize.value,
      status: statusFilter.value
    });
    ElMessage.success('原始数据已在控制台输出');
  } catch (error) {
    console.error('获取原始数据失败', error);
    ElMessage.error('获取原始数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchExams();
});
</script>

<style scoped>
.exam-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-data {
  padding: 30px 0;
  text-align: center;
}
</style> 