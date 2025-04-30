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
            @clear="loadClasses"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <el-empty v-else-if="filteredClasses.length === 0" description="暂无班级数据"></el-empty>
      
      <el-table
        v-else
        :data="filteredClasses"
        style="width: 100%"
        border
        @row-click="handleClassClick"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button size="small" @click.stop="viewStudents(scope.row)">查看学生</el-button>
            <el-button size="small" type="primary" @click.stop="viewTimetable(scope.row)">查看课表</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 班级学生对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      :title="selectedClass ? `${selectedClass.className}学生列表` : '班级学生'"
      width="70%"
    >
      <div v-if="studentsLoading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <el-empty v-else-if="students.length === 0" description="该班级暂无学生"></el-empty>
      
      <el-table
        v-else
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

    <!-- 班级课表对话框 -->
    <el-dialog
      v-model="timetableDialogVisible"
      :title="selectedClass ? `${selectedClass.className}课表` : '班级课表'"
      width="80%"
    >
      <div v-if="timetableLoading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <el-empty v-else-if="timetables.length === 0" description="该班级暂无课表"></el-empty>
      
      <div v-else>
        <div class="filter-container">
          <el-select v-model="selectedWeekNumber" placeholder="选择周次" clearable style="width: 200px;" @change="filterTimetableByWeek">
            <el-option 
              v-for="week in weekOptions" 
              :key="week.value" 
              :label="`第${week.value}周`" 
              :value="week.value" 
            />
          </el-select>
        </div>
        
        <el-table
          :data="filteredTimetables"
          style="width: 100%"
          border
        >
          <el-table-column prop="className" label="班级" />
          <el-table-column prop="weekNumber" label="周次">
            <template #default="scope">
              第{{ scope.row.weekNumber }}周
            </template>
          </el-table-column>
          <el-table-column prop="name" label="课程表名称" />
          <el-table-column prop="createTime" label="创建时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" @click="viewTimetableDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { getTeacherClasses } from '@/api/class';
import { getTimetablesByClassId } from '@/api/timetable';
import { useRouter } from 'vue-router';

const router = useRouter();

// 班级数据
const classes = ref<any[]>([]);
const loading = ref(false);
const searchKeyword = ref('');

// 学生数据
const students = ref<any[]>([]);
const studentsLoading = ref(false);
const studentsDialogVisible = ref(false);
const selectedClass = ref<any>(null);

// 课表数据
const timetables = ref<any[]>([]);
const filteredTimetables = ref<any[]>([]);
const timetableLoading = ref(false);
const timetableDialogVisible = ref(false);
const selectedWeekNumber = ref<number | null>(null);
const weekOptions = computed(() => {
  const weeks = new Set(timetables.value.map(t => t.weekNumber));
  return Array.from(weeks).map(week => ({ value: week }));
});

// 计算过滤后的班级列表
const filteredClasses = computed(() => {
  if (!searchKeyword.value) return classes.value;
  
  const keyword = searchKeyword.value.toLowerCase().trim();
  return classes.value.filter(cls => 
    (cls.className && cls.className.toLowerCase().includes(keyword)) ||
    (cls.grade && cls.grade.toLowerCase().includes(keyword)) ||
    (cls.description && cls.description.toLowerCase().includes(keyword))
  );
});

// 获取班级列表
const loadClasses = async () => {
  loading.value = true;
  try {
    const result = await getTeacherClasses();
    console.log('获取到班级数据:', result);
    
    // 检查返回数据格式并处理
    if (result && Array.isArray(result)) {
      classes.value = result;
    } else if (result && result.data && Array.isArray(result.data)) {
      classes.value = result.data;
    } else if (result?.data?.data && Array.isArray(result.data.data)) {
      classes.value = result.data.data;
    } else {
      console.warn('返回的班级数据格式不是预期格式:', result);
      classes.value = [];
      ElMessage.warning('获取班级数据格式不正确，请联系管理员');
    }
  } catch (error: any) {
    console.error('获取班级失败:', error);
    classes.value = [];
    ElMessage.error('获取班级失败：' + (error.response?.data?.message || error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 延迟搜索以提高性能
const handleSearch = () => {
  if (searchKeyword.value.trim() === '') {
    loadClasses();
  }
};

// 处理班级点击
const handleClassClick = (row: any) => {
  viewStudents(row);
};

// 查看班级学生
const viewStudents = async (classInfo: any) => {
  selectedClass.value = classInfo;
  studentsDialogVisible.value = true;
  studentsLoading.value = true;
  
  try {
    const response = await fetch(`/api/classes/${classInfo.id}/students`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    students.value = Array.isArray(data) ? data : [];
    
    if (students.value.length === 0) {
      ElMessage.info('该班级暂无学生');
    }
  } catch (error: any) {
    console.error('获取班级学生失败:', error);
    students.value = [];
    ElMessage.error('获取学生列表失败：' + error.message);
  } finally {
    studentsLoading.value = false;
  }
};

// 查看班级课表
const viewTimetable = async (classInfo: any) => {
  selectedClass.value = classInfo;
  timetableDialogVisible.value = true;
  timetableLoading.value = true;
  selectedWeekNumber.value = null;
  
  try {
    const result = await getTimetablesByClassId(classInfo.id);
    console.log('获取到课表数据:', result);
    
    if (Array.isArray(result)) {
      timetables.value = result;
      filteredTimetables.value = result;
    } else {
      console.warn('返回的课表数据格式不是数组:', result);
      timetables.value = [];
      filteredTimetables.value = [];
    }
    
    if (timetables.value.length === 0) {
      ElMessage.info('该班级暂无课表');
    }
  } catch (error: any) {
    console.error('获取班级课表失败:', error);
    timetables.value = [];
    filteredTimetables.value = [];
    ElMessage.error('获取课表失败：' + (error.response?.data?.message || error.message || '未知错误'));
  } finally {
    timetableLoading.value = false;
  }
};

// 按周次筛选课表
const filterTimetableByWeek = () => {
  if (selectedWeekNumber.value === null) {
    filteredTimetables.value = timetables.value;
    return;
  }
  
  filteredTimetables.value = timetables.value.filter(
    t => t.weekNumber === selectedWeekNumber.value
  );
};

// 查看课表详情
const viewTimetableDetail = (timetable: any) => {
  // 跳转到课表详情页面
  router.push({
    name: 'SupervisorTimetableDetail',
    params: { id: timetable.id }
  });
};

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString();
};

// 页面加载时获取班级数据
onMounted(() => {
  loadClasses();
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

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-input {
  width: 300px;
}

.loading-container {
  padding: 20px 0;
}

.el-table {
  margin-top: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

/* 表格行悬停效果 */
:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover > td) {
  background-color: #f0f9ff !important;
}
</style> 