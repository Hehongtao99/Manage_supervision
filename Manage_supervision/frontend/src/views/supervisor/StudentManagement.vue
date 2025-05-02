<template>
  <div class="student-management">
    <!-- 如果没有学生管理权限，显示提示信息 -->
    <el-empty 
      v-if="!hasPermission('STUDENT_MANAGEMENT')"
      description="您没有学生管理权限"
    >
      <template #image>
        <el-icon style="font-size: 48px;"><Lock /></el-icon>
      </template>
    </el-empty>

    <!-- 有权限才显示学生管理内容 -->
    <el-card class="box-card" v-if="hasPermission('STUDENT_MANAGEMENT')">
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
            <el-button 
              type="primary" 
              size="small" 
              @click="viewStudentDetails(scope.row)"
              v-permission="'USER_VIEW'"
            >
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click="toggleStudentStatus(scope.row)"
              v-permission="'USER_EDIT'"
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
        
        <!-- 学生基本信息部分 - 所有权限用户都可查看 -->
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
        
        <!-- 学生进度部分 - 需要权限才能显示 -->
        <div v-if="hasPermission('STUDENT_PROGRESS_VIEW')" class="student-progress mt-4">
          <h3>学习进度</h3>
          <el-divider />
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>总体学习进度</span>
                  </div>
                </template>
                <el-progress :percentage="75" :stroke-width="20" :format="percentFormat" :color="getProgressColor(75)"></el-progress>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>最近活跃度</span>
                  </div>
                </template>
                <el-progress :percentage="60" :stroke-width="20" :format="percentFormat" :color="getProgressColor(60)"></el-progress>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" class="mt-4">
            <el-col :span="24">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>近期活动</span>
                  </div>
                </template>
                <el-timeline>
                  <el-timeline-item
                    timestamp="2023-05-10 20:46"
                    type="success"
                  >
                    完成作业：《第三章 数据结构》
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-08 09:30"
                    type="primary"
                  >
                    参加在线答疑会议
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-05 18:12"
                    type="warning"
                  >
                    延迟提交作业：《第二章 算法基础》
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </el-col>
          </el-row>
        </div>
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
import { hasPermission } from '../../utils/permission';

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
    const data = await getAssignedStudents();
    console.log('获取的学生数据:', JSON.stringify(data));
    
    if (Array.isArray(data)) {
      console.log('第一条学生数据:', data.length > 0 ? JSON.stringify(data[0]) : '无数据');
      students.value = data;
      totalStudents.value = data.length;
      if (data.length > 0) {
        ElMessage.success('成功获取学生数据');
      } else {
        ElMessage.info('暂无分配的学生');
      }
    } else if (data && typeof data === 'object' && Array.isArray(data.content)) {
      console.log('分页学生数据第一条:', data.content.length > 0 ? JSON.stringify(data.content[0]) : '无数据');
      // 处理分页响应格式
      students.value = data.content;
      totalStudents.value = data.totalElements || data.content.length;
      if (data.content.length > 0) {
        ElMessage.success('成功获取学生数据');
      } else {
        ElMessage.info('暂无分配的学生');
      }
    } else {
      console.error('返回的学生数据格式不正确:', data);
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
  // 检查权限
  if (!hasPermission('USER_VIEW')) {
    ElMessage.error('您没有查看学生详情的权限');
    return;
  }
  
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
  // 检查权限
  if (!hasPermission('USER_EDIT')) {
    ElMessage.error('您没有修改学生状态的权限');
    return;
  }
  
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
  // 检查权限
  if (!hasPermission('USER_DELETE')) {
    ElMessage.error('您没有删除学生的权限');
    return;
  }
  
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