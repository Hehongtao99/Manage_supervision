<template>
  <div class="class-management">
    <h1 class="page-title">班级管理</h1>
    
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的班级</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索班级名称"
            class="search-input"
            clearable
            @clear="searchClasses"
            @input="searchClasses"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="classes"
        style="width: 100%"
        border
        @row-click="handleClassClick"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click.stop="viewStudents(scope.row)">查看学生</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog
      v-model="studentsDialogVisible"
      title="班级学生"
      width="70%"
    >
      <el-table
        v-loading="studentsLoading"
        :data="students"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="学号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || '/avatar-placeholder.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { getTeacherClasses, getClassStudents } from '@/api/supervisor';

// 班级数据
const classes = ref<any[]>([]);
const loading = ref(false);
const searchKeyword = ref('');

// 学生数据
const students = ref<any[]>([]);
const studentsLoading = ref(false);
const studentsDialogVisible = ref(false);
const selectedClass = ref<any>(null);

// 获取班级列表
const fetchClasses = async () => {
  loading.value = true;
  try {
    const data = await getTeacherClasses();
    classes.value = data;
  } catch (error: any) {
    ElMessage.error('获取班级失败：' + (error.response?.data || error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 搜索班级
const searchClasses = () => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  if (!keyword) {
    fetchClasses();
    return;
  }
  
  // 本地过滤
  classes.value = classes.value.filter(cls => 
    cls.className.toLowerCase().includes(keyword) ||
    cls.grade?.toLowerCase().includes(keyword) ||
    cls.description?.toLowerCase().includes(keyword)
  );
};

// 处理班级点击
const handleClassClick = (row: any) => {
  viewStudents(row);
};

// 查看班级学生
const viewStudents = async (classData: any) => {
  selectedClass.value = classData;
  studentsDialogVisible.value = true;
  studentsLoading.value = true;
  
  try {
    const data = await getClassStudents(classData.id);
    students.value = data;
  } catch (error: any) {
    ElMessage.error('获取学生列表失败：' + (error.response?.data || error.message || '未知错误'));
  } finally {
    studentsLoading.value = false;
  }
};

onMounted(() => {
  fetchClasses();
});
</script>

<style scoped>
.class-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-input {
  width: 300px;
}

.box-card {
  margin-bottom: 20px;
}

.el-table {
  cursor: pointer;
}
</style> 