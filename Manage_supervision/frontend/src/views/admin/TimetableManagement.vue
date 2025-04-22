<template>
  <div class="timetable-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>课程表管理</h2>
          <el-button type="primary" @click="openTimetableDialog()">新增课程表</el-button>
        </div>
      </template>

      <div class="filter-container">
        <el-select v-model="selectedClassId" placeholder="选择班级" clearable style="width: 200px;">
          <el-option v-for="cls in classes" :key="cls.id" :label="cls.className" :value="cls.id" />
        </el-select>

        <el-button type="primary" @click="loadTimetables">查询</el-button>
      </div>

      <el-table :data="timetables" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
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
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="scope">
            <el-button size="small" @click="viewTimetable(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="openTimetableDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除此课程表?" @confirm="deleteTimetableHandler(scope.row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-text" v-if="timetables.length === 0 && !loading">
        暂无课程表数据，请先创建课程表
      </div>
    </el-card>

    <!-- 课程表表单对话框 -->
    <el-dialog v-model="timetableDialogVisible" :title="isEdit ? '编辑课程表' : '新增课程表'" width="500px">
      <el-form :model="timetableForm" label-width="100px" ref="timetableFormRef" :rules="timetableRules">
        <el-form-item label="班级" prop="classId">
          <el-select v-model="timetableForm.classId" placeholder="选择班级" style="width: 100%;">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程表名称" prop="name">
          <el-input v-model="timetableForm.name" />
        </el-form-item>
        <div class="form-tip">注：创建后可在课程表详情页面通过左右滑动查看/创建不同周次的课表</div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="timetableDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTimetable">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElForm } from 'element-plus';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { getTimetablesByClassId, createTimetable, updateTimetable, deleteTimetable } from '@/api/timetable';
import { getAllClasses } from '@/api/class';
import type { TimetableDTO } from '@/types/timetable';
import type { ClassDTO } from '@/types/class';

const router = useRouter();

// 加载状态
const loading = ref(false);

// 班级列表
const classes = ref<ClassDTO[]>([]);

// 课程表列表
const timetables = ref<TimetableDTO[]>([]);

// 筛选条件
const selectedClassId = ref<number | null>(null);

// 课程表表单
const timetableFormRef = ref<FormInstance>();
const timetableDialogVisible = ref(false);
const isEdit = ref(false);
const timetableForm = reactive<TimetableDTO>({
  classId: 0,
  weekNumber: 1, // 默认创建第一周的课表
  name: ''
});

// 表单校验规则
const timetableRules = reactive<FormRules>({
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  name: [{ required: true, message: '请输入课程表名称', trigger: 'blur' }]
});

// 加载所有班级
const loadClasses = async () => {
  try {
    const response = await getAllClasses();
    classes.value = response;
    
    // 如果有班级数据，并且还没有选择班级，自动选择第一个班级
    if (classes.value && classes.value.length > 0) {
      if (selectedClassId.value === null || selectedClassId.value === undefined) {
        const firstClass = classes.value[0];
        if (firstClass && firstClass.id !== undefined && firstClass.id !== null) {
          console.log('自动选择第一个班级:', firstClass.className, '(ID:', firstClass.id, ')');
          selectedClassId.value = firstClass.id;
          // 自动加载第一个班级的课程表
          loadTimetables();
        } else {
          console.warn('第一个班级ID无效:', firstClass);
        }
      }
    } else {
      console.log('没有可用的班级数据');
    }
  } catch (error) {
    ElMessage.error('加载班级列表失败');
    console.error(error);
  }
};

// 加载课程表
const loadTimetables = async () => {
  if (!selectedClassId.value && selectedClassId.value !== 0) {
    ElMessage.warning('请先选择班级');
    timetables.value = []; // 清空当前课表列表
    return;
  }
  
  // 确保班级ID是有效的数值
  const classId = Number(selectedClassId.value);
  if (isNaN(classId) || classId < 0) {
    ElMessage.warning('无效的班级ID');
    timetables.value = []; // 清空当前课表列表
    return;
  }
  
  loading.value = true;
  try {
    // 加载指定班级的所有课程表
    timetables.value = await getTimetablesByClassId(classId);
  } catch (error) {
    ElMessage.error('加载课程表失败');
    console.error(error);
    timetables.value = []; // 确保在出错时清空列表
  } finally {
    loading.value = false;
  }
};

// 打开课程表对话框
const openTimetableDialog = (timetable?: TimetableDTO) => {
  isEdit.value = !!timetable;

  // 重置表单
  if (timetableFormRef.value) {
    timetableFormRef.value.resetFields();
  }

  if (timetable) {
    // 编辑模式，填充表单
    Object.assign(timetableForm, timetable);
  } else {
    // 新增模式，初始化表单
    // 确保班级ID是有效的
    const classId = selectedClassId.value !== null && selectedClassId.value !== undefined 
      ? Number(selectedClassId.value) 
      : (classes.value.length > 0 ? classes.value[0].id : 0);
    
    if (classId === 0 || isNaN(classId)) {
      ElMessage.warning('请先选择一个班级');
    }
    
    timetableForm.classId = classId;
    timetableForm.weekNumber = 1; // 默认第一周
    
    // 根据班级ID设置课表名称
    const selectedClass = classes.value.find(c => c.id === classId);
    timetableForm.name = selectedClass 
      ? `${selectedClass.className}第1周课表` 
      : '新课表';
  }

  timetableDialogVisible.value = true;
};

// 提交课程表表单
const submitTimetable = async () => {
  if (!timetableFormRef.value) return;

  await timetableFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 确保数据类型正确
        const timetableData = {
          ...timetableForm,
          classId: Number(timetableForm.classId),
          weekNumber: Number(timetableForm.weekNumber)
        };
        
        console.log('准备提交的课表数据:', timetableData);
        
        if (isEdit.value && timetableForm.id) {
          // 更新课程表
          await updateTimetable(timetableForm.id, timetableData);
          ElMessage.success('更新课程表成功');
        } else {
          // 创建课程表
          await createTimetable(timetableData);
          ElMessage.success('创建课程表成功');
        }
        
        timetableDialogVisible.value = false;
        loadTimetables();
      } catch (error: any) {
        console.error('课表操作失败:', error);
        
        // 提取错误信息
        const errorMessage = error.response?.data?.error || error.message || '操作失败';
        ElMessage.error(`课表操作失败: ${errorMessage}`);
      }
    }
  });
};

// 删除课程表
const deleteTimetableHandler = async (id: number) => {
  try {
    await deleteTimetable(id);
    ElMessage.success('删除课程表成功');
    loadTimetables();
  } catch (error) {
    ElMessage.error('删除课程表失败');
    console.error(error);
  }
};

// 查看课程表详情
const viewTimetable = (timetable: TimetableDTO) => {
  router.push({
    name: 'TimetableDetail',
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
  loadClasses();
});
</script>

<style scoped>
.timetable-management {
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

.form-tip {
  font-size: 13px;
  color: #909399;
  margin: 5px 0 15px 100px;
}
</style>