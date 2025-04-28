<template>
  <div class="my-grades-container p-4">
    <h1 class="text-2xl font-bold mb-4">我的成绩</h1>

    <el-card>
      <el-table 
        :data="gradedExams"
        v-loading="loading"
        stripe
      >
        <el-table-column prop="title" label="考试名称" min-width="200"></el-table-column>
        <el-table-column prop="passingScore" label="及格分数" width="100" align="center">
          <template #default="{ row }">
            {{ row.passingScore?.toFixed(1) ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentScore" label="我的得分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreTagType(row.studentScore, row.passingScore)">
              {{ row.studentScore?.toFixed(1) ?? '未评分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否通过" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.studentScore !== null && row.passingScore !== null"
                    :type="row.studentScore >= row.passingScore ? 'success' : 'danger'">
              {{ row.studentScore >= row.passingScore ? '是' : '否' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
         <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.studentSubmitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="viewResultDetails(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无已评分的考试成绩"></el-empty>
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElTable, ElTableColumn, ElCard, ElTag, ElEmpty, ElMessage, ElPagination, ElButton } from 'element-plus';
import { getStudentExams } from '@/api/exam';
import { formatDateTime } from '@/utils/dateUtil';

interface GradedExam {
  id: number;
  title: string;
  passingScore: number | null;
  studentScore: number | null;
  studentSubmitTime: string | Date | null;
  studentStatus: string;
}

// --- Router ---
const router = useRouter();

// --- State ---
const allExams = ref<GradedExam[]>([]); // 保存所有获取到的考试记录
const loading = ref(false);
const total = ref(0);
const pagination = ref({
  page: 0, // 后端页码从 0 开始
  size: 10,
});

// --- Computed ---
// 筛选出已发布的考试用于显示
const gradedExams = computed(() => {
    return allExams.value.filter(exam => exam.studentStatus === 'PUBLISHED');
});

// --- Methods ---
const fetchGrades = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      // 不需要传 status，获取所有记录，然后在前端过滤
    };
    const res = await getStudentExams(params);
    if (res.data.code === 200 && res.data.data) {
      // 注意：API 返回的 ExamDTO 结构可能需要适配
      allExams.value = res.data.data.content.map((exam: any) => ({
        id: exam.id,
        title: exam.title,
        passingScore: exam.passingScore,
        studentScore: exam.studentScore,
        studentSubmitTime: exam.studentSubmitTime,
        studentStatus: exam.studentStatus
      }));
      total.value = res.data.data.totalElements; // 使用总数进行分页
    } else {
      allExams.value = [];
      total.value = 0;
      ElMessage.error(res.data.message || '获取成绩列表失败');
    }
  } catch (error) {
    allExams.value = [];
    total.value = 0;
    console.error('获取成绩列表失败:', error);
    ElMessage.error('获取成绩列表失败');
  } finally {
    loading.value = false;
  }
};

// 分页大小变化
const handleSizeChange = (newSize: number) => {
  pagination.value.size = newSize;
  pagination.value.page = 0; // 重置页码
  fetchGrades();
};

// 当前页变化
const handleCurrentChange = (newPage: number) => {
  pagination.value.page = newPage - 1; // Element Plus 的 current-page 是从 1 开始
  fetchGrades();
};

// 根据分数和及格分获取标签类型
const getScoreTagType = (score: number | null, passingScore: number | null) => {
  if (score === null || score === undefined) return 'info';
  if (passingScore === null || passingScore === undefined) return 'primary';
  return score >= passingScore ? 'success' : 'danger';
};

// 查看结果详情
const viewResultDetails = (examId: number) => {
  router.push(`/student/exams/${examId}/result`);
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchGrades();
});

</script>

<style scoped>
.my-grades-container {
  max-width: 1000px;
  margin: 0 auto;
}
.pagination-container {
  padding-top: 1rem;
}
</style> 