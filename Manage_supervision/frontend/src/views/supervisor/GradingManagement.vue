<template>
  <div class="grading-management-container p-4">
    <h1 class="text-2xl font-bold mb-4">批阅管理</h1>

    <el-card>
      <!-- 筛选区域 -->
      <div class="flex justify-between items-center mb-4">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="pending">待批阅</el-radio-button>
          <el-radio-button label="graded">待发布</el-radio-button>
          <el-radio-button label="published">已发布</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="examListData" v-loading="loading" stripe>
        <el-table-column prop="title" label="考试名称" min-width="200"></el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="批阅状态" width="200">
          <template #default="{ row }">
            <div style="line-height: 1.5;">
              <div>总人数: {{ row.totalStudents || 0 }}</div>
              <div v-if="row.submittedCount > 0">
                 <el-tag type="warning" size="small" effect="plain">待批阅: {{ row.submittedCount }}</el-tag>
              </div>
              <div v-if="row.pendingPublishCount > 0">
                 <el-tag type="primary" size="small" effect="plain">待发布: {{ row.pendingPublishCount }}</el-tag>
              </div>
              <div v-if="row.publishedCount > 0">
                 <el-tag type="success" size="small" effect="plain">已发布: {{ row.publishedCount }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="状态标签" width="120">
           <template #default="{ row }">
              <el-tag v-if="row.submittedCount === 0 && row.pendingPublishCount === 0 && row.publishedCount === 0" type="info">无提交</el-tag>
              <el-tag v-else-if="row.submittedCount > 0" type="warning">待批阅</el-tag>
              <el-tag v-else-if="row.pendingPublishCount > 0" type="primary">待发布</el-tag>
              <el-tag v-else-if="row.publishedCount > 0 && row.publishedCount === row.totalStudents" type="success">已完成</el-tag>
              <el-tag v-else-if="row.publishedCount > 0" type="success">部分完成</el-tag>
              <el-tag v-else type="info">未知状态</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToGrade(row.id)" :disabled="row.totalStudents === 0">查看/批阅</el-button>
            <el-button 
              type="success" 
              link 
              v-if="row.pendingPublishCount > 0 && row.submittedCount === 0"
              @click="publishAllGrades(row)"
              style="margin-left: 8px;"
             >
              一键发布
             </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无考试数据"></el-empty>
        </template>
      </el-table>

       <!-- 分页 -->
      <div class="pagination-container mt-4 flex justify-end" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          :current-page="pagination.page + 1"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElTable, ElTableColumn, ElButton, ElCard, ElTag, ElEmpty, ElMessage, ElPagination, ElRadioGroup, ElRadioButton, ElMessageBox } from 'element-plus';
import { getGradingOverviewExams, publishStudentGrades } from '@/api/exam';
import { formatDateTime } from '@/utils/dateUtil';

interface GradingExamInfo {
  id: number;
  title: string;
  startTime: string | Date;
  endTime: string | Date;
  submittedCount: number | null;
  pendingPublishCount: number | null; // 待发布数量，取代原来的gradedCount
  publishedCount: number | null; // 已发布数量
  totalStudents: number | null;
  status: string;
}

const router = useRouter();
const examListData = ref<GradingExamInfo[]>([]);
const loading = ref(false);
const filterStatus = ref('');
const total = ref(0);
const pagination = ref({
  page: 0,
  size: 10,
});

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      gradingStatus: filterStatus.value || undefined,
      sort: 'startTime,desc'
    };
    const res = await getGradingOverviewExams(params);
    if (res.data.code === 200 && res.data.data) {
      examListData.value = res.data.data.content;
      total.value = res.data.data.totalElements;
    } else {
      examListData.value = [];
      total.value = 0;
      ElMessage.error(res.data.message || '获取考试列表失败');
    }
  } catch (error) {
    examListData.value = [];
    total.value = 0;
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败');
  } finally {
    loading.value = false;
  }
};

const handleFilterChange = () => {
  pagination.value.page = 0;
  fetchData();
};

const handleSizeChange = (newSize: number) => {
  pagination.value.size = newSize;
  pagination.value.page = 0;
  fetchData();
};

const handleCurrentChange = (newPage: number) => {
  pagination.value.page = newPage - 1;
  fetchData();
};

const goToGrade = (examId: number) => {
  router.push(`/supervisor/exams/${examId}/students`);
};

// 一键发布所有待发布成绩
const publishAllGrades = async (exam: GradingExamInfo) => {
  ElMessageBox.confirm(
    `确定要发布考试【${exam.title}】的所有待发布成绩吗？`, 
    '确认发布', 
    {
      confirmButtonText: '确定发布',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    loading.value = true;
    try {
      // 调用 API，不传递 studentId
      const response = await publishStudentGrades(exam.id);
      if (response.data && response.data.code === 200) {
        ElMessage.success('成绩发布成功！');
        fetchData(); // 重新加载列表
      } else {
        ElMessage.error(response.data?.message || '发布成绩失败');
      }
    } catch (error: any) {
      console.error('发布成绩失败:', error);
      ElMessage.error('发布成绩失败: ' + (error.response?.data?.message || '网络错误'));
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消
    ElMessage.info('已取消发布');
  });
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.grading-management-container {
  max-width: 1200px;
  margin: 0 auto;
}
.pagination-container {
  padding-top: 1rem;
}
</style> 