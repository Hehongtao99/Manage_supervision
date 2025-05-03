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
        <el-table-column label="姓名" width="120">
          <template #default="scope">
            {{ scope.row.realName || scope.row.name || scope.row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="userNumber" label="学号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
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
      class="student-detail-dialog"
    >
      <div v-if="currentStudent" class="student-info-container">
        <div class="student-header">
          <div class="avatar-container">
            <el-avatar :size="80" :src="currentStudent.avatar">
              {{ (currentStudent.realName || currentStudent.name || currentStudent.username || '').substring(0, 1) }}
            </el-avatar>
          </div>
          <div class="student-main-info">
            <h2>{{ currentStudent.realName || currentStudent.name || currentStudent.username }}</h2>
            <div class="student-id">学号: {{ currentStudent.userNumber }}</div>
          </div>
        </div>

        <el-divider />
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentStudent.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentStudent.realName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentStudent.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentStudent.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentStudent.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentStudent.status === 'active' ? 'success' : 'warning'">
              {{ currentStudent.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentStudent.createTime || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="2">
            <el-tag 
              v-for="(role, index) in currentStudent.roles" 
              :key="index" 
              type="info" 
              class="mr-2"
            >
              {{ role }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentStudent.bio || '暂无个人简介' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
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
import { getStudents, getStudentDetail, updateStudentStatus, deleteStudent, getAssignedStudents } from '../../api/student';
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
    const response = await getAssignedStudents();
    console.log('获取的学生数据:', JSON.stringify(response));

    // Check if the response structure is { code: ..., message: ..., data: [...] }
    if (response && typeof response === 'object' && Array.isArray(response.data)) {
      console.log('第一条学生数据:', response.data.length > 0 ? JSON.stringify(response.data[0]) : '无数据');
      students.value = response.data;
      totalStudents.value = response.data.length;
      if (response.data.length > 0) {
        ElMessage.success('成功获取学生数据');
      } else {
        ElMessage.info('暂无分配的学生');
      }
    }
    // Keep the check for paginated data structure { content: [...] } just in case
    else if (response && typeof response === 'object' && Array.isArray(response.content)) {
      console.log('分页学生数据第一条:', response.content.length > 0 ? JSON.stringify(response.content[0]) : '无数据');
      students.value = response.content;
      totalStudents.value = response.totalElements || response.content.length;
      if (response.content.length > 0) {
        ElMessage.success('成功获取学生数据');
      } else {
        ElMessage.info('暂无分配的学生');
      }
    } 
    // Handle cases where data might be directly an array (less likely now but good to keep)
    else if (Array.isArray(response)) {
        console.log('直接获取到学生数组:', response.length > 0 ? JSON.stringify(response[0]) : '无数据');
        students.value = response;
        totalStudents.value = response.length;
        if (response.length > 0) {
          ElMessage.success('成功获取学生数据');
        } else {
          ElMessage.info('暂无分配的学生');
        }
    }
    else {
      console.error('返回的学生数据格式不正确:', response);
      ElMessage.warning('获取的学生数据格式不正确');
      students.value = [];
      totalStudents.value = 0;
    }
  } catch (error) {
    console.error('获取学生数据失败:', error);
    ElMessage.error('获取学生数据失败，请稍后重试');
    students.value = [];
    totalStudents.value = 0;
  } finally {
    loading.value = false;
  }
};

// 根据搜索过滤学生
const filteredStudents = computed(() => {
  if (!Array.isArray(students.value)) {
    console.warn('学生数据不是数组:', students.value);
    return [];
  }
  
  if (!searchQuery.value) return students.value;
  
  const query = searchQuery.value.toLowerCase();
  return students.value.filter(student => 
    (student.realName && student.realName.toLowerCase().includes(query)) || 
    (student.name && student.name.toLowerCase().includes(query)) || 
    (student.username && student.username.toLowerCase().includes(query)) || 
    (student.userNumber && student.userNumber.toLowerCase().includes(query)) || 
    (student.email && student.email.toLowerCase().includes(query))
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
  fetchStudents();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  fetchStudents();
};

const viewStudentDetails = async (student: Student) => {
  try {
    loading.value = true;
    console.log('查看学生详情, ID:', student.id);
    
    try {
      // 获取学生详细信息
      const detailedStudent = await getStudentDetail(student.id);
      console.log('获取到的详细学生信息:', detailedStudent);
      
      // 确保所有必要的字段都存在
      const processedStudent = {
        ...student,
        ...detailedStudent,
        realName: detailedStudent.realName || student.realName || student.name || '',
        username: detailedStudent.username || student.username || '',
        userNumber: detailedStudent.userNumber || student.userNumber || '',
        email: detailedStudent.email || student.email || '',
        phone: detailedStudent.phone || student.phone || '',
        status: detailedStudent.status || student.status || 'inactive',
        createTime: detailedStudent.createTime || student.createTime || '',
        roles: Array.isArray(detailedStudent.roles) ? detailedStudent.roles : 
              Array.isArray(student.roles) ? student.roles : ['USER'],
        bio: detailedStudent.bio || ''
      };
      
      currentStudent.value = processedStudent;
      detailsDialogVisible.value = true;
    } catch (error) {
      // 如果getStudentDetail失败，直接使用表格中的学生数据显示详情
      console.log('使用当前学生数据作为详情:', student);
      currentStudent.value = {
        ...student,
        realName: student.realName || student.name || '',
        roles: Array.isArray(student.roles) ? student.roles : ['USER'],
        bio: ''
      };
      detailsDialogVisible.value = true;
    }
  } catch (error) {
    console.error('获取学生详情失败:', error);
    ElMessage.error('获取学生详情失败');
  } finally {
    loading.value = false;
  }
};

const toggleStudentStatus = async (student: Student) => {
  try {
    // 确认对话框
    await ElMessageBox.confirm(
      `确定要${student.status === 'active' ? '禁用' : '启用'}学生"${student.realName || student.name || student.username}"吗？`,
      '操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    loading.value = true;
    const newStatus = student.status === 'active' ? 'inactive' : 'active';
    console.log(`尝试将学生 ${student.id} 状态更改为 ${newStatus}`);
    
    const success = await updateStudentStatus(student.id, newStatus);
    
    if (success) {
      ElMessage.success(`学生"${student.realName || student.name || student.username}"已${newStatus === 'active' ? '启用' : '禁用'}`);
      // 更新本地数据
      const index = students.value.findIndex(s => s.id === student.id);
      if (index !== -1) {
        students.value[index].status = newStatus;
      }
    } else {
      ElMessage.error('操作失败，请稍后重试');
    }
  } catch (error: any) {
    // 判断是否为用户取消操作
    if (error === 'cancel' || error.toString().includes('cancel')) {
      console.log('用户取消了操作');
      return;
    }
    
    // 处理权限错误
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足，无法执行此操作');
      console.error('权限错误:', error);
    } else if (error.message && error.message.includes('权限')) {
      ElMessage.error(error.message);
      console.error('权限错误:', error);
    } else {
      console.error('更新学生状态失败:', error);
      ElMessage.error('禁用学生失败');
    }
  } finally {
    loading.value = false;
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

.mr-2 {
  margin-right: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.student-detail-dialog {
  /* Add your styles here */
}

.student-info-container {
  padding: 0 20px;
}

.student-header {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.avatar-container {
  margin-right: 24px;
}

.student-main-info {
  flex: 1;
}

.student-main-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.student-id {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.student-roles {
  margin-top: 12px;
}
</style> 