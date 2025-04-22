<template>
  <div class="timetable-detail">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2>{{ timetable?.name || '课程表详情' }}</h2>
            <div v-if="timetable" class="subtitle">
              {{ timetable.className }} 
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
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="openAddItemDialog">添加课程</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="timetable-container">
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
              @click="openAddItemDialog('morning', period, day)"
            >
              <div v-if="getCourseItem('morning', period, day)" class="class-item">
                <div class="course-name">{{ getCourseItem('morning', period, day)?.courseName }}</div>
                <div class="course-teacher">{{ getCourseItem('morning', period, day)?.teacherName || '未分配' }}</div>
                <div class="course-room">{{ getCourseItem('morning', period, day)?.classroom || '未分配' }}</div>
                <div class="item-actions">
                  <el-button size="small" type="primary" @click.stop="editItem(getCourseItem('morning', period, day))">编辑</el-button>
                  <el-button size="small" type="danger" @click.stop="deleteItem(getCourseItem('morning', period, day)?.id)">删除</el-button>
                </div>
              </div>
              <div v-else class="empty-cell">
                <el-icon><plus /></el-icon>
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
              @click="openAddItemDialog('afternoon', period, day)"
            >
              <div v-if="getCourseItem('afternoon', period, day)" class="class-item">
                <div class="course-name">{{ getCourseItem('afternoon', period, day)?.courseName }}</div>
                <div class="course-teacher">{{ getCourseItem('afternoon', period, day)?.teacherName || '未分配' }}</div>
                <div class="course-room">{{ getCourseItem('afternoon', period, day)?.classroom || '未分配' }}</div>
                <div class="item-actions">
                  <el-button size="small" type="primary" @click.stop="editItem(getCourseItem('afternoon', period, day))">编辑</el-button>
                  <el-button size="small" type="danger" @click.stop="deleteItem(getCourseItem('afternoon', period, day)?.id)">删除</el-button>
                </div>
              </div>
              <div v-else class="empty-cell">
                <el-icon><plus /></el-icon>
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
              @click="openAddItemDialog('evening', period, day)"
            >
              <div v-if="getCourseItem('evening', period, day)" class="class-item">
                <div class="course-name">{{ getCourseItem('evening', period, day)?.courseName }}</div>
                <div class="course-teacher">{{ getCourseItem('evening', period, day)?.teacherName || '未分配' }}</div>
                <div class="course-room">{{ getCourseItem('evening', period, day)?.classroom || '未分配' }}</div>
                <div class="item-actions">
                  <el-button size="small" type="primary" @click.stop="editItem(getCourseItem('evening', period, day))">编辑</el-button>
                  <el-button size="small" type="danger" @click.stop="deleteItem(getCourseItem('evening', period, day)?.id)">删除</el-button>
                </div>
              </div>
              <div v-else class="empty-cell">
                <el-icon><plus /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 课程项目表单对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="isEdit ? '编辑课程' : '添加课程'" width="500px">
      <el-form :model="itemForm" label-width="100px" ref="itemFormRef" :rules="itemRules">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="itemForm.courseName" />
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="itemForm.teacherId" placeholder="选择教师" style="width: 100%;" clearable>
            <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.realName" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" prop="dayOfWeek">
          <el-select v-model="itemForm.dayOfWeek" placeholder="选择星期" style="width: 100%;">
            <el-option v-for="(day, index) in dayLabels" :key="index" :label="day" :value="index + 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="periodType">
          <el-select v-model="itemForm.periodType" placeholder="选择时间段" style="width: 100%;">
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
            <el-option label="晚上" value="evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" prop="periodNumber">
          <el-select v-model="itemForm.periodNumber" placeholder="选择节次" style="width: 100%;">
            <el-option v-for="num in 4" :key="num" :label="`第${num}节`" :value="num" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="itemForm.classroom" />
        </el-form-item>
        <el-form-item label="时间">
          <div class="time-range-display">
            {{ getTimeRange(itemForm.periodType, itemForm.periodNumber) }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitItem">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElForm } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import { Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import type { FormInstance, FormRules } from 'element-plus';
import { getTimetableById, getTimetableItems, createTimetableItem, updateTimetableItem, deleteTimetableItem, calculateTimeSlot, getTimetablesByClassIdAndWeekNumber, createTimetable } from '@/api/timetable';
import { getAllTeachers } from '@/api/user';
import type { TimetableDTO, TimetableItemDTO } from '@/types/timetable';
import type { UserDTO } from '@/types/user';

const router = useRouter();
const route = useRoute();

// 星期标签
const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

// 加载状态
const loading = ref(false);

// 课程表数据
const timetable = ref<TimetableDTO | null>(null);
const timetableItems = ref<TimetableItemDTO[]>([]);
const teachers = ref<UserDTO[]>([]);
const currentWeek = ref(1);
const baseId = ref<number | null>(null);

// 时间段映射
const timeRanges = ref<Record<string, Record<number, string>>>({
  morning: {},
  afternoon: {},
  evening: {}
});

// 课程项对话框
const itemFormRef = ref<FormInstance>();
const itemDialogVisible = ref(false);
const isEdit = ref(false);
const itemForm = reactive<TimetableItemDTO>({
  timetableId: 0,
  courseName: '',
  dayOfWeek: 1,
  periodType: 'morning',
  periodNumber: 1
});

// 表单校验规则
const itemRules = reactive<FormRules>({
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  periodType: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  periodNumber: [{ required: true, message: '请选择节次', trigger: 'change' }]
});

// 获取课程表详情
const loadTimetableDetails = async () => {
  const id = route.params.id as string;
  if (!id) {
    ElMessage.error('课程表ID不存在');
    router.push({ name: 'TimetableManagement' });
    return;
  }

  baseId.value = parseInt(id);
  loading.value = true;
  try {
    const timetableData = await getTimetableById(parseInt(id));
    timetable.value = timetableData;
    currentWeek.value = timetableData.weekNumber;
    
    await loadTimetableItems();
    
    // 计算所有时间段
    await calculateAllTimeRanges();
  } catch (error) {
    ElMessage.error('加载课程表详情失败');
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
  
  loading.value = true;
  try {
    if (timetable.value) {
      // 尝试查找指定班级和周次的课程表
      const timetables = await getTimetablesByClassIdAndWeekNumber(
        timetable.value.classId, 
        newWeek
      );
      
      if (timetables.length > 0) {
        // 存在该周的课程表，加载它
        timetable.value = timetables[0];
        await loadTimetableItems();
      } else {
        // 不存在该周的课程表，创建一个新的
        const newTimetable: TimetableDTO = {
          classId: timetable.value.classId,
          weekNumber: newWeek,
          name: `${timetable.value.className || ''}第${newWeek}周课程表`
        };
        
        const createdTimetable = await createTimetable(newTimetable);
        timetable.value = createdTimetable;
        timetableItems.value = []; // 清空当前课程项
      }
      
      currentWeek.value = newWeek;
    }
  } catch (error) {
    ElMessage.error('切换周次失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
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

// 打开添加课程对话框
const openAddItemDialog = (periodType?: string, periodNumber?: number, dayOfWeek?: number) => {
  isEdit.value = false;

  // 重置表单
  if (itemFormRef.value) {
    itemFormRef.value.resetFields();
  }

  // 设置默认值
  itemForm.timetableId = timetable.value?.id || 0;
  itemForm.courseName = '';
  itemForm.teacherId = undefined;
  itemForm.classroom = '';
  
  if (periodType) itemForm.periodType = periodType as any;
  if (periodNumber) itemForm.periodNumber = periodNumber as any;
  if (dayOfWeek) itemForm.dayOfWeek = dayOfWeek;

  itemDialogVisible.value = true;
};

// 编辑课程项
const editItem = (item?: TimetableItemDTO) => {
  if (!item) return;
  
  isEdit.value = true;

  // 重置表单
  if (itemFormRef.value) {
    itemFormRef.value.resetFields();
  }

  // 填充表单
  Object.assign(itemForm, item);

  itemDialogVisible.value = true;
};

// 删除课程项
const deleteItem = async (id?: number) => {
  if (!id) return;

  try {
    await deleteTimetableItem(id);
    ElMessage.success('删除课程成功');
    loadTimetableItems();
  } catch (error) {
    ElMessage.error('删除课程失败');
    console.error(error);
  }
};

// 提交课程表项目表单
const submitItem = async () => {
  if (!itemFormRef.value) return;

  await itemFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value && itemForm.id) {
          // 更新课程项
          await updateTimetableItem(itemForm.id, itemForm);
          ElMessage.success('更新课程成功');
        } else {
          // 创建课程项
          await createTimetableItem(itemForm);
          ElMessage.success('添加课程成功');
        }
        
        itemDialogVisible.value = false;
        loadTimetableItems();
      } catch (error) {
        ElMessage.error('操作失败');
        console.error(error);
      }
    }
  });
};

// 返回上一页
const goBack = () => {
  router.push({ name: 'TimetableManagement' });
};

// 加载教师列表
const loadTeachers = async () => {
  try {
    const response = await getAllTeachers();
    teachers.value = response;
  } catch (error) {
    ElMessage.error('加载教师列表失败');
    console.error(error);
  }
};

// 页面加载时获取课程表详情和教师列表
onMounted(() => {
  loadTimetableDetails();
  loadTeachers();
});
</script>

<style scoped>
.timetable-detail {
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
  cursor: pointer;
  transition: background-color 0.3s;
}

.class-cell:hover {
  background-color: #f5f7fa;
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

.item-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
}

.empty-cell {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.time-range-display {
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
}
</style> 