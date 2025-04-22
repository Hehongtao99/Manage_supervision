<template>
  <div class="student-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div class="search-container">
            <el-input
              v-model="searchQuery"
              placeholder="搜索学生..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredStudents"
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewStudentDetails(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click="toggleStudentStatus(scope.row)"
            >
              <el-icon><Lock /></el-icon>
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalStudents"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="学生详情"
      width="60%"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentStudent"
        :column="2"
        border
      >
        <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentStudent.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentStudent.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentStudent.status === 'active' ? 'success' : 'warning'">
            {{ currentStudent.status === 'active' ? '活跃' : '非活跃' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  View,
  Edit,
  Delete,
  Lock
} from '@element-plus/icons-vue';
import { getStudents, getStudentDetail, updateStudentStatus, deleteStudent } from '../../api/student';
import type { Student } from '../../api/user';
import { useRouter } from 'vue-router';

// 状态变量
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalStudents = ref<number>(0);
const students = ref<Student[]>([]);
const selectedStudents = ref<Student[]>([]);
const currentStudent = ref<Student | null>(null);
const detailsDialogVisible = ref<boolean>(false);
const router = useRouter();

// 初始化数据
onMounted(() => {
  fetchStudents();
});

// 获取学生数据
const fetchStudents = async () => {
  loading.value = true;
  try {
    const data = await getStudents();
    students.value = data;
    totalStudents.value = data.length;
    ElMessage.success('成功获取学生数据');
  } catch (error) {
    console.error('获取学生数据失败:', error);
    ElMessage.error('获取学生数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 根据搜索过滤学生
const filteredStudents = computed(() => {
  if (!searchQuery.value) return students.value;
  
  const query = searchQuery.value.toLowerCase();
  return students.value.filter(student => 
    student.name.toLowerCase().includes(query) || 
    student.studentId.toLowerCase().includes(query) || 
    student.email.toLowerCase().includes(query)
  );
});

// 处理函数
const handleSearch = () => {
  currentPage.value = 1;
};

const handleSelectionChange = (selection: Student[]) => {
  selectedStudents.value = selection;
};

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  currentPage.value = 1;
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
};

const viewStudentDetails = async (student: Student) => {
  try {
    loading.value = true;
    // 获取学生详细信息
    const detailedStudent = await getStudentDetail(student.id);
    currentStudent.value = detailedStudent;
    detailsDialogVisible.value = true;
  } catch (error) {
    console.error('获取学生详情失败:', error);
    ElMessage.error('获取学生详情失败');
  } finally {
    loading.value = false;
  }
};

const toggleStudentStatus = async (student: Student) => {
  try {
    const newStatus = student.status === 'active' ? 'inactive' : 'active';
    const success = await updateStudentStatus(student.id, newStatus);
    
    if (success) {
      ElMessage.success(`学生${student.name}状态已${newStatus === 'active' ? '启用' : '禁用'}`);
      // 更新本地数据
      const index = students.value.findIndex(s => s.id === student.id);
      if (index !== -1) {
        students.value[index].status = newStatus;
      }
    } else {
      ElMessage.error('更新学生状态失败');
    }
  } catch (error) {
    console.error('更新学生状态失败:', error);
    ElMessage.error('更新学生状态失败');
  }
};

const deleteStudentUser = async (student: Student) => {
  try {
    const success = await deleteStudent(student.id);
    
    if (success) {
      ElMessage.success(`学生${student.name}已删除`);
      // 从本地数据中移除
      students.value = students.value.filter(s => s.id !== student.id);
      totalStudents.value = students.value.length;
    } else {
      ElMessage.error('删除学生失败');
    }
  } catch (error) {
    console.error('删除学生失败:', error);
    ElMessage.error('删除学生失败');
  }
};

// 辅助函数
const percentFormat = (percentage: number): string => {
  return percentage ? `${percentage}%` : '0%';
};

const getProgressColor = (completion: number): string => {
  if (completion >= 80) return '#67C23A';
  if (completion >= 50) return '#409EFF';
  return '#E6A23C';
};

const getActivityType = (type: string): string => {
  switch (type) {
    case 'success': return 'success';
    case 'warning': return 'warning';
    case 'error': return 'danger';
    default: return 'primary';
  }
};
</script>

<style scoped>
.student-management {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.details-tabs {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 