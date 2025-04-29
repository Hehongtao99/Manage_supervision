<template>
  <div class="score-management-container p-4">
    <h1 class="text-2xl font-bold mb-4">分数管理</h1>

    <el-card>
      <div class="flex items-center mb-4">
        <label class="mr-2">选择考试:</label>
        <el-select 
          v-model="selectedExamId" 
          placeholder="请选择考试"
          @change="handleExamChange"
          filterable
          clearable
          style="width: 300px;"
        >
          <el-option
            v-for="exam in examList"
            :key="exam.value"
            :label="exam.label"
            :value="exam.value"
          >
          </el-option>
        </el-select>
      </div>

      <el-table 
        :data="scores"
        v-loading="loading"
        stripe
        @sort-change="handleSortChange"
        :default-sort="{ prop: 'score', order: 'descending' }"
      >
        <el-table-column prop="studentName" label="学生姓名" min-width="150"></el-table-column>
        <el-table-column prop="studentUserNumber" label="学号/工号" width="180"></el-table-column>
        <el-table-column prop="score" label="分数" width="120" sortable="custom" align="right">
           <template #default="{ row }">
             <el-tag :type="getScoreTagType(row.score, row.passingScore)">
               {{ row.score?.toFixed(1) ?? 'N/A' }}
              </el-tag>
           </template>
        </el-table-column>
        <template #empty>
          <el-empty v-if="!selectedExamId" description="请先选择一个考试"></el-empty>
          <el-empty v-else description="暂无已评分的学生成绩"></el-empty>
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
import { ref, onMounted, computed, watch } from 'vue';
import { ElTable, ElTableColumn, ElSelect, ElOption, ElCard, ElTag, ElEmpty, ElMessage, ElPagination } from 'element-plus';
import { getExamScores, getExam, getExamOptions } from '@/api/exam';

interface ExamOption {
  value: number;
  label: string;
}

interface StudentScore {
  studentId: number;
  studentName: string;
  studentUserNumber: string;
  score: number | null;
}

// --- State ---
const examList = ref<ExamOption[]>([]);
const selectedExamId = ref<number | null>(null);
const selectedExamPassingScore = ref<number | null>(null);
const scores = ref<StudentScore[]>([]);
const loading = ref(false);
const total = ref(0);
const pagination = ref({
  page: 0, // 后端是从 0 开始
  size: 10,
});
const sortParams = ref({
  prop: 'score',
  order: 'descending'
});

// --- Computed ---
const currentSortString = computed(() => {
  if (!sortParams.value.prop) return 'score,desc'; // 默认排序
  const direction = sortParams.value.order === 'ascending' ? 'asc' : 'desc';
  return `${sortParams.value.prop},${direction}`;
});

// --- Methods ---
// 获取考试列表 (用于下拉选择)
const fetchExamList = async () => {
  try {
    // 使用新的 API 获取选项列表
    const res = await getExamOptions(); 
    if (res.data.code === 200 && Array.isArray(res.data.data)) {
      // 直接使用返回的数据，因为格式已经是 {value, label}
      examList.value = res.data.data;
    } else {
      examList.value = []; // 清空列表以防万一
      ElMessage.error(res.data.message || '获取考试选项列表失败');
    }
  } catch (error) {
    examList.value = []; // 清空列表
    console.error('获取考试选项列表失败:', error);
    ElMessage.error('获取考试选项列表失败');
  }
};

// 获取选中考试的及格分
const fetchExamPassingScore = async (examId: number) => {
  try {
    const res = await getExam(examId);
    if (res.data.code === 200 && res.data.data) {
      selectedExamPassingScore.value = res.data.data.passingScore;
    } else {
      selectedExamPassingScore.value = null; // 获取失败则清空
      console.warn('获取考试及格分失败:', res.data.message);
    }
  } catch (error) {
     selectedExamPassingScore.value = null;
     console.error('获取考试及格分异常:', error);
  }
}

// 获取学生分数
const fetchScores = async () => {
  if (!selectedExamId.value) {
    scores.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      sort: currentSortString.value
    };
    const res = await getExamScores(selectedExamId.value, params);
    if (res.data.code === 200 && res.data.data) {
      scores.value = res.data.data.content.map((s: any) => ({ ...s, passingScore: selectedExamPassingScore.value }));
      total.value = res.data.data.totalElements;
    } else {
      scores.value = [];
      total.value = 0;
      ElMessage.error(res.data.message || '获取分数列表失败');
    }
  } catch (error) {
    scores.value = [];
    total.value = 0;
    console.error('获取分数列表失败:', error);
    ElMessage.error('获取分数列表失败');
  } finally {
    loading.value = false;
  }
};

// 考试选择变化
const handleExamChange = (examId: number | null) => {
  pagination.value.page = 0; // 重置页码
  if (examId) {
    fetchExamPassingScore(examId).then(() => {
       fetchScores();
    });
  } else {
    selectedExamPassingScore.value = null;
    scores.value = [];
    total.value = 0;
  }
};

// 排序变化
const handleSortChange = ({ prop, order }: { prop: string, order: 'ascending' | 'descending' | null }) => {
  sortParams.value.prop = prop;
  sortParams.value.order = order || 'descending'; // 如果 order 为 null，则默认为降序
  pagination.value.page = 0; // 重置页码
  fetchScores();
};

// 分页大小变化
const handleSizeChange = (newSize: number) => {
  pagination.value.size = newSize;
  pagination.value.page = 0; // 重置页码
  fetchScores();
};

// 当前页变化
const handleCurrentChange = (newPage: number) => {
  pagination.value.page = newPage - 1; // Element Plus 的 current-page 是从 1 开始
  fetchScores();
};

// 根据分数和及格分获取标签类型
const getScoreTagType = (score: number | null, passingScore: number | null) => {
  if (score === null || score === undefined) return 'info';
  if (passingScore === null || passingScore === undefined) return 'primary'; // 没有及格分标准
  return score >= passingScore ? 'success' : 'danger';
};

// --- Lifecycle Hooks ---
onMounted(() => {
  fetchExamList();
});

</script>

<style scoped>
.score-management-container {
  max-width: 1200px;
  margin: 0 auto;
}
.pagination-container {
  padding-top: 1rem; /* Add some space above pagination */
}
</style> 