<template>
  <div class="timetable-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>班级课表</h2>
            <div v-if="currentClass" class="subtitle">
              {{ currentClass.className }} 
              <div class="week-selector">
                <el-button type="text" :disabled="currentWeek <= 1" @click="changeWeek(-1)">
                  <el-icon><arrow-left /></el-icon>
                </el-button>
                <span>第{{ currentWeek }}周</span>
                <el-button type="text" :disabled="currentWeek >= 20" @click="changeWeek(1)">
                  <el-icon><arrow-right /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!currentClass" description="您尚未被分配到班级"></el-empty>
        <el-empty v-else-if="!timetable" description="当前周次暂无课表"></el-empty>

        <div v-else class="timetable-container">
          <!-- 时间表格 -->
          <div class="timetable-grid">
            <div class="header-row">
              <div class="time-column">时间段</div>
              <div class="day-column" v-for="day in 7" :key="day">
                {{ dayLabels[day - 1] }}
              </div>
            </div>

            <!-- 上午课程 -->
            <div class="time-section">
              <div class="section-header">上午</div>
            </div>
            <div v-for="period in 4" :key="`morning-${period}`" class="timetable-row">
              <div class="time-column">
                <div class="period-number">第{{ period }}节</div>
                <div class="time-range">{{ getTimeRange('morning', period) }}</div>
              </div>
              <div 
                v-for="day in 7" 
                :key="`morning-${period}-${day}`" 
                class="day-column class-cell"
              >
                <div v-if="getCourseItem('morning', period, day)" class="class-item">
                  <div class="course-name">{{ getCourseItem('morning', period, day)?.courseName }}</div>
                  <div class="course-teacher">{{ getCourseItem('morning', period, day)?.teacherName || '未分配' }}</div>
                  <div class="course-room">{{ getCourseItem('morning', period, day)?.classroom || '未分配' }}</div>
                </div>
                <div v-else class="empty-cell">
                  <span>空</span>
                </div>
              </div>
            </div>

            <!-- 下午课程 -->
            <div class="time-section">
              <div class="section-header">下午</div>
            </div>
            <div v-for="period in 4" :key="`afternoon-${period}`" class="timetable-row">
              <div class="time-column">
                <div class="period-number">第{{ period }}节</div>
                <div class="time-range">{{ getTimeRange('afternoon', period) }}</div>
              </div>
              <div 
                v-for="day in 7" 
                :key="`afternoon-${period}-${day}`" 
                class="day-column class-cell"
              >
                <div v-if="getCourseItem('afternoon', period, day)" class="class-item">
                  <div class="course-name">{{ getCourseItem('afternoon', period, day)?.courseName }}</div>
                  <div class="course-teacher">{{ getCourseItem('afternoon', period, day)?.teacherName || '未分配' }}</div>
                  <div class="course-room">{{ getCourseItem('afternoon', period, day)?.classroom || '未分配' }}</div>
                </div>
                <div v-else class="empty-cell">
                  <span>空</span>
                </div>
              </div>
            </div>

            <!-- 晚上课程 -->
            <div class="time-section">
              <div class="section-header">晚上</div>
            </div>
            <div v-for="period in 4" :key="`evening-${period}`" class="timetable-row">
              <div class="time-column">
                <div class="period-number">第{{ period }}节</div>
                <div class="time-range">{{ getTimeRange('evening', period) }}</div>
              </div>
              <div 
                v-for="day in 7" 
                :key="`evening-${period}-${day}`" 
                class="day-column class-cell"
              >
                <div v-if="getCourseItem('evening', period, day)" class="class-item">
                  <div class="course-name">{{ getCourseItem('evening', period, day)?.courseName }}</div>
                  <div class="course-teacher">{{ getCourseItem('evening', period, day)?.teacherName || '未分配' }}</div>
                  <div class="course-room">{{ getCourseItem('evening', period, day)?.classroom || '未分配' }}</div>
                </div>
                <div v-else class="empty-cell">
                  <span>空</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { getCurrentUser } from '@/api/user';
import { getStudentClass } from '@/api/student';
import { getTimetablesByClassIdAndWeekNumber, getTimetableItems, calculateTimeSlot } from '@/api/timetable';
import type { TimetableDTO, TimetableItemDTO } from '@/types/timetable';
import type { ClassDTO } from '@/types/class';

// 星期标签
const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

// 加载状态
const loading = ref(false);

// 当前班级
const currentClass = ref<ClassDTO | null>(null);

// 课程表数据
const timetable = ref<TimetableDTO | null>(null);
const timetableItems = ref<TimetableItemDTO[]>([]);
const currentWeek = ref(1);

// 时间段映射
const timeRanges = ref<Record<string, Record<number, string>>>({
  morning: {},
  afternoon: {},
  evening: {}
});

// 加载学生所在班级
const loadStudentClass = async () => {
  loading.value = true;
  try {
    // 获取学生所在班级
    const classInfo = await getStudentClass();
    console.log('获取到的班级信息:', classInfo);
    
    if (classInfo && classInfo.hasClass && classInfo.classes && classInfo.classes.length > 0) {
      const firstClass = classInfo.classes[0];
      
      // 确保是普通对象而非Proxy
      currentClass.value = {
        id: Number(firstClass.id),
        className: firstClass.className,
        grade: firstClass.grade,
        description: firstClass.description,
        createTime: firstClass.createTime,
        updateTime: firstClass.updateTime,
        status: firstClass.status
      };
      
      console.log('设置当前班级:', currentClass.value);
      // 加载默认第一周的课表
      await loadTimetable(1);
    } else {
      console.error('班级信息不完整或为空:', classInfo);
      ElMessage.info('您尚未被分配到班级');
      loading.value = false;
    }
  } catch (error) {
    ElMessage.error('获取班级信息失败');
    console.error(error);
    loading.value = false;
  }
};

// 加载指定周次的课表
const loadTimetable = async (weekNumber: number) => {
  if (!currentClass.value) {
    console.error('班级信息为空');
    ElMessage.warning('班级信息获取失败，请联系管理员');
    loading.value = false;
    return;
  }
  
  if (currentClass.value.id === undefined || currentClass.value.id === null || isNaN(Number(currentClass.value.id))) {
    console.error('班级ID无效:', currentClass.value);
    ElMessage.warning('班级ID无效，请联系管理员');
    loading.value = false;
    return;
  }
  
  const classId = Number(currentClass.value.id);
  console.log('加载课表使用的班级ID:', classId);
  
  loading.value = true;
  try {
    const timetables = await getTimetablesByClassIdAndWeekNumber(
      classId,
      weekNumber
    );
    
    if (timetables.length > 0) {
      timetable.value = timetables[0];
      currentWeek.value = weekNumber;
      
      // 加载课表内容
      await loadTimetableItems();
      
      // 计算时间段
      await calculateAllTimeRanges();
    } else {
      timetable.value = null;
      timetableItems.value = [];
      currentWeek.value = weekNumber;
    }
  } catch (error) {
    ElMessage.error('加载课表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 加载课程表项目
const loadTimetableItems = async () => {
  if (!timetable.value || !timetable.value.id) return;
  
  try {
    timetableItems.value = await getTimetableItems(timetable.value.id);
  } catch (error) {
    ElMessage.error('加载课程项目失败');
    console.error(error);
  }
};

// 计算所有时间段
const calculateAllTimeRanges = async () => {
  const periodTypes = ['morning', 'afternoon', 'evening'];
  const periodNumbers = [1, 2, 3, 4];
  
  for (const type of periodTypes) {
    for (const num of periodNumbers) {
      try {
        const result = await calculateTimeSlot(type, num);
        if (result) {
          const startTime = new Date(`2000-01-01T${result.startTime}`).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          const endTime = new Date(`2000-01-01T${result.endTime}`).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          timeRanges.value[type] = timeRanges.value[type] || {};
          timeRanges.value[type][num] = `${startTime} - ${endTime}`;
        }
      } catch (error) {
        console.error(`计算时间段失败: ${type}, ${num}`, error);
      }
    }
  }
};

// 切换周次
const changeWeek = async (delta: number) => {
  const newWeek = currentWeek.value + delta;
  if (newWeek < 1 || newWeek > 20) return;
  
  await loadTimetable(newWeek);
};

// 获取时间范围
const getTimeRange = (periodType: string, periodNumber: number | undefined) => {
  if (!periodType || !periodNumber) return '';
  return timeRanges.value[periodType]?.[periodNumber] || '';
};

// 获取指定时间段和星期的课程
const getCourseItem = (periodType: string, periodNumber: number, dayOfWeek: number) => {
  return timetableItems.value.find(item => 
    item.periodType === periodType && 
    item.periodNumber === periodNumber && 
    item.dayOfWeek === dayOfWeek
  );
};

// 页面加载时获取学生班级信息和课表
onMounted(() => {
  loadStudentClass();
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

.subtitle {
  color: #606266;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.week-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 10px;
  background-color: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.timetable-container {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: auto;
}

.timetable-grid {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  min-width: 900px;
}

.header-row, .timetable-row {
  display: flex;
  width: 100%;
}

.header-row {
  background-color: #f5f7fa;
  font-weight: bold;
  position: sticky;
  top: 0;
  z-index: 2;
}

.time-column, .day-column {
  padding: 10px;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 40px;
}

.time-column {
  width: 120px;
  background-color: #f5f7fa;
  position: sticky;
  left: 0;
  z-index: 1;
}

.day-column {
  flex: 1;
  min-width: 120px;
}

.time-section {
  display: flex;
  width: 100%;
  background-color: #ecf5ff;
  border-bottom: 1px solid #ebeef5;
}

.section-header {
  width: 100%;
  padding: 8px;
  font-weight: bold;
  text-align: center;
}

.period-number {
  font-weight: bold;
  margin-bottom: 5px;
}

.time-range {
  font-size: 0.85em;
  color: #606266;
}

.class-cell {
  min-height: 80px;
  padding: 5px;
}

.class-item {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.course-teacher, .course-room {
  font-size: 0.85em;
  color: #606266;
  margin-bottom: 3px;
}

.empty-cell {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}
</style> 