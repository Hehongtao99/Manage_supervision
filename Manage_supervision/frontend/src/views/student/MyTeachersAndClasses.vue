<template>
  <div class="my-teachers-classes-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <h1 class="page-title">我的班级和老师</h1>
      </el-col>
    </el-row>

    <!-- 班级信息 -->
    <el-row :gutter="20" class="section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h2>我的班级</h2>
            </div>
          </template>
          <div v-if="loading.classes" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <el-empty v-else-if="!hasClass" description="您当前未被分配到任何班级"></el-empty>
          <div v-else>
            <el-table :data="classes" style="width: 100%">
              <el-table-column prop="className" label="班级名称" />
              <el-table-column prop="grade" label="年级" />
              <el-table-column prop="studentCount" label="班级人数" />
              <el-table-column prop="description" label="班级描述" show-overflow-tooltip />
              <el-table-column label="分配时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.assignTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 教师信息 -->
    <el-row :gutter="20" class="section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h2>我的教师</h2>
            </div>
          </template>
          <div v-if="loading.teachers" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <el-empty v-else-if="!hasTeachers" description="您当前未被分配教师"></el-empty>
          <div v-else>
            <el-table :data="teachers" style="width: 100%">
              <el-table-column prop="realName" label="教师姓名" />
              <el-table-column prop="userNumber" label="教师工号" />
              <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
              <el-table-column prop="phone" label="电话" show-overflow-tooltip />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 班级中的老师信息 -->
    <el-row :gutter="20" class="section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h2>班级教师</h2>
            </div>
          </template>
          <div v-if="loading.classTeachers" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <el-empty v-else-if="!hasClassTeachers" description="您的班级当前未分配教师"></el-empty>
          <div v-else>
            <el-collapse accordion>
              <el-collapse-item v-for="(classInfo, index) in classTeachers" :key="index" :title="classInfo.className">
                <el-table :data="classInfo.teachers" style="width: 100%">
                  <el-table-column prop="teacherName" label="教师姓名" />
                  <el-table-column label="分配时间" width="180">
                    <template #default="scope">
                      {{ formatDateTime(scope.row.assignTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from '@/utils/axios';

export default {
  name: 'MyTeachersAndClasses',
  setup() {
    // 定义数据
    const classes = ref([]);
    const hasClass = ref(false);
    const teachers = ref([]);
    const hasTeachers = ref(false);
    const classTeachers = ref([]);
    const hasClassTeachers = ref(false);
    const loading = reactive({
      classes: true,
      teachers: true,
      classTeachers: true
    });

    // 获取班级信息
    const getMyClasses = async () => {
      try {
        loading.classes = true;
        const response = await axios.get('/api/student/my-class');
        hasClass.value = response.data.hasClass;
        if (hasClass.value) {
          classes.value = response.data.classes;
        }
      } catch (error) {
        console.error('获取班级信息失败:', error);
        ElMessage.error('获取班级信息失败：' + (error.response?.data?.message || error.message));
      } finally {
        loading.classes = false;
      }
    };

    // 获取教师信息
    const getMyTeachers = async () => {
      try {
        loading.teachers = true;
        const response = await axios.get('/api/student/my-teachers');
        hasTeachers.value = response.data.hasTeachers;
        if (hasTeachers.value) {
          teachers.value = response.data.teachers;
        }
      } catch (error) {
        console.error('获取教师信息失败:', error);
        ElMessage.error('获取教师信息失败：' + (error.response?.data?.message || error.message));
      } finally {
        loading.teachers = false;
      }
    };

    // 获取班级教师信息
    const getClassTeachers = async () => {
      try {
        loading.classTeachers = true;
        const response = await axios.get('/api/student/class-teachers');
        hasClassTeachers.value = response.data.hasClassTeachers;
        if (hasClassTeachers.value) {
          classTeachers.value = response.data.classTeachers;
        }
      } catch (error) {
        console.error('获取班级教师信息失败:', error);
        ElMessage.error('获取班级教师信息失败：' + (error.response?.data?.message || error.message));
      } finally {
        loading.classTeachers = false;
      }
    };

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '未知';
      
      // 如果是ISO日期字符串，转换为本地格式
      if (typeof dateTime === 'string') {
        const date = new Date(dateTime);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }).replace(/\//g, '-');
      }
      return dateTime;
    };

    // 页面加载时获取数据
    onMounted(() => {
      getMyClasses();
      getMyTeachers();
      getClassTeachers();
    });

    return {
      classes,
      hasClass,
      teachers,
      hasTeachers,
      classTeachers,
      hasClassTeachers,
      loading,
      formatDateTime
    };
  }
};
</script>

<style scoped>
.my-teachers-classes-container {
  padding: 20px;
}

.page-title {
  color: #303133;
  margin-bottom: 20px;
  font-weight: 600;
}

.section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.loading-container {
  padding: 10px;
}

.el-table {
  margin-top: 10px;
}

.el-collapse-item {
  margin-bottom: 10px;
}
</style> 