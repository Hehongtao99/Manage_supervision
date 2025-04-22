<template>
  <div class="timetable-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>班级课表</h2>
        </div>
      </template>

      <div class="filter-container">
        <el-select v-model="selectedClassId" placeholder="选择班级" clearable style="width: 200px;" @change="loadTimetables">
          <el-option v-for="cls in classes" :key="cls.id" :label="cls.className" :value="cls.id" />
        </el-select>
      </div>

      <el-empty v-if="!selectedClassId" description="请选择班级查看课表"></el-empty>

      <el-table v-else :data="timetables" stripe style="width: 100%" v-loading="loading">
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
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="scope">
            <el-button size="small" @click="viewTimetable(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="selectedClassId && timetables.length === 0 && !loading" class="empty-text">
        暂无课程表数据
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { getTimetablesByClassId } from '@/api/timetable';
import { getTeacherClasses } from '@/api/class';
import type { TimetableDTO } from '@/types/timetable';
import type { ClassDTO } from '@/types/class';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// 加载状态
const loading = ref(false);

// 班级列表
const classes = ref<ClassDTO[]>([]);

// 课程表列表
const timetables = ref<TimetableDTO[]>([]);

// 筛选条件
const selectedClassId = ref<number | null>(null);

// 加载教师的班级
const loadTeacherClasses = async () => {
  loading.value = true;
  try {
    // 获取当前用户ID
    const userId = userStore.userId;
    console.log('从用户Store获取用户ID:', userId);
    
    // 记录当前token状态
    console.log('当前token是否存在:', !!userStore.token);
    console.log('当前用户角色:', userStore.user.roles);
    
    // 使用用户ID获取班级列表
    const response = await getTeacherClasses(userId);
    classes.value = response;
    
    // 默认不选择任何班级，让用户自己选择
    loading.value = false;
  } catch (error) {
    ElMessage.error('加载班级列表失败');
    console.error(error);
    loading.value = false;
  }
};

// 加载课程表
const loadTimetables = async () => {
  if (!selectedClassId.value && selectedClassId.value !== 0) {
    timetables.value = [];
    return;
  }
  
  loading.value = true;
  try {
    // 加载指定班级的所有课程表
    timetables.value = await getTimetablesByClassId(selectedClassId.value);
  } catch (error) {
    ElMessage.error('加载课程表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 查看课程表详情
const viewTimetable = (timetable: TimetableDTO) => {
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

// 页面加载时获取班级列表
onMounted(() => {
  loadTeacherClasses();
});
</script>

<style scoped>
.timetable-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.empty-text {
  text-align: center;
  padding: 30px;
  color: #909399;
}
</style> 