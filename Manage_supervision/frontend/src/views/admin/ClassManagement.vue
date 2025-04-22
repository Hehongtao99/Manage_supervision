<template>
  <div class="class-management-container">
    <div class="page-header">
      <h1 class="page-title">班级管理</h1>
      <el-button type="primary" @click="showAddClassDialog">添加班级</el-button>
    </div>

    <el-card class="search-card">
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索班级名称"
          clearable
          @clear="handleSearch"
          style="width: 300px"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="classes"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="className" label="班级名称" min-width="120" />
        <el-table-column prop="grade" label="年级" min-width="100" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentCount" label="学生数量" width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="showEditClassDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" @click="showTeacherManagementDialog(scope.row)">管理教师</el-button>
            <el-button size="small" type="success" @click="showStudentManagementDialog(scope.row)">管理学生</el-button>
            <el-popconfirm
              title="确定要删除此班级吗？"
              @confirm="handleDeleteClass(scope.row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑班级对话框 -->
    <el-dialog
      v-model="classDialogVisible"
      :title="dialogType === 'add' ? '添加班级' : '编辑班级'"
      width="500px"
    >
      <el-form
        ref="classFormRef"
        :model="classForm"
        :rules="classFormRules"
        label-width="100px"
      >
        <el-form-item label="班级名称" prop="className">
          <el-input v-model.trim="classForm.className" placeholder="请输入班级名称" clearable />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model.trim="classForm.grade" placeholder="请输入年级" clearable />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model.trim="classForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="classDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitClass">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 班级教师管理对话框 -->
    <el-dialog
      v-model="teacherDialogVisible"
      :title="`管理教师 - ${selectedClass?.className || ''}`"
      width="700px"
    >
      <div class="teacher-management">
        <div class="teacher-selection">
          <h3>可选教师</h3>
          <el-input
            v-model="teacherSearchKeyword"
            placeholder="搜索教师"
            clearable
            @input="filterTeachers"
            style="margin-bottom: 10px"
          />
          <el-table
            v-loading="teachersLoading"
            :data="filteredTeachers"
            border
            style="width: 100%"
            height="300px"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                  size="small"
                  type="primary"
                  @click="handleAssignTeacher(scope.row)"
                  :disabled="isTeacherAssigned(scope.row.id)"
                >
                  添加
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="class-teachers">
          <h3>已分配教师</h3>
          <el-table
            v-loading="classTeachersLoading"
            :data="classTeachers"
            border
            style="width: 100%"
            height="300px"
          >
            <el-table-column prop="teacherId" label="ID" width="80" />
            <el-table-column prop="teacherName" label="姓名" min-width="120" />
            <el-table-column prop="assignTime" label="分配时间" min-width="180">
              <template #default="scope">
                {{ formatTime(scope.row.assignTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-popconfirm
                  title="确定要移除此教师吗？"
                  @confirm="handleRemoveTeacher(scope.row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger">移除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 班级学生管理对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="`管理学生 - ${selectedClass?.className || ''}`"
      width="700px"
    >
      <div class="student-management">
        <div class="student-selection">
          <h3>可添加学生</h3>
          <el-input
            v-model="studentSearchKeyword"
            placeholder="搜索学生"
            clearable
            @input="filterStudents"
            style="margin-bottom: 10px"
          />
          <el-table
            v-loading="unassignedStudentsLoading"
            :data="filteredUnassignedStudents"
            border
            style="width: 100%"
            height="300px"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userNumber" label="学号" width="100" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                  size="small"
                  type="primary"
                  @click="handleAddStudent(scope.row)"
                >
                  添加
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="batch-actions" style="margin-top: 10px;">
            <el-button 
              type="primary" 
              :disabled="selectedStudents.length === 0"
              @click="handleBatchAddStudents"
            >
              批量添加选中学生 ({{ selectedStudents.length }})
            </el-button>
          </div>
        </div>

        <div class="class-students">
          <h3>班级学生</h3>
          <el-table
            v-loading="classStudentsLoading"
            :data="classStudents"
            border
            style="width: 100%"
            height="300px"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userNumber" label="学号" width="100" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-popconfirm
                  title="确定要将此学生从班级中移除吗？"
                  @confirm="handleRemoveStudent(scope.row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger">移除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { 
  getAllClasses, 
  createClass, 
  updateClass, 
  deleteClass,
  getTeachersByClassId,
  assignTeacherToClass,
  removeTeacherFromClass,
  getStudentsByClassId,
  addStudentToClass,
  addStudentsToClass,
  removeStudentFromClass,
  getUnassignedStudentsByClassId
} from '@/api/class';
import { getTeachers } from '@/api/teacher';
import { formatDateTime } from '../../utils/date';

// 班级列表状态
const classes = ref<any[]>([]);
const loading = ref(false);
const searchKeyword = ref('');

// 班级表单状态
const classDialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const classForm = ref({
  id: null,
  className: '',
  grade: '',
  description: ''
});
const classFormRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }]
};
const classFormRef = ref();

// 教师管理状态
const teacherDialogVisible = ref(false);
const selectedClass = ref<any>(null);
const teachers = ref<any[]>([]);
const classTeachers = ref<any[]>([]);
const teachersLoading = ref(false);
const classTeachersLoading = ref(false);
const teacherSearchKeyword = ref('');

// 学生管理状态
const studentDialogVisible = ref(false);
const unassignedStudents = ref<any[]>([]);
const classStudents = ref<any[]>([]);
const studentSearchKeyword = ref('');
const unassignedStudentsLoading = ref(false);
const classStudentsLoading = ref(false);
const selectedStudents = ref<any[]>([]);

// 计算过滤后的教师列表
const filteredTeachers = computed(() => {
  if (!teacherSearchKeyword.value) {
    return teachers.value;
  }
  const keyword = teacherSearchKeyword.value.toLowerCase();
  return teachers.value.filter(teacher => 
    teacher.realName?.toLowerCase().includes(keyword) || 
    teacher.username?.toLowerCase().includes(keyword)
  );
});

// 计算过滤后的学生列表
const filteredUnassignedStudents = computed(() => {
  if (!studentSearchKeyword.value) {
    return unassignedStudents.value;
  }
  const keyword = studentSearchKeyword.value.toLowerCase();
  return unassignedStudents.value.filter(student => 
    student.realName?.toLowerCase().includes(keyword) || 
    student.username?.toLowerCase().includes(keyword) ||
    student.userNumber?.toLowerCase().includes(keyword)
  );
});

// 检查教师是否已分配到班级
const isTeacherAssigned = (teacherId: number) => {
  return classTeachers.value.some(t => t.teacherId === teacherId);
};

// 格式化时间
const formatTime = (time: string) => {
  return formatDateTime(time);
};

// 过滤教师列表
const filterTeachers = () => {
  // 已在computed中实现
};

// 过滤学生列表
const filterStudents = () => {
  // 已在computed中实现
};

// 加载班级列表
const loadClasses = async () => {
  loading.value = true;
  try {
    classes.value = await getAllClasses(searchKeyword.value);
  } catch (error) {
    console.error('加载班级列表失败:', error);
    ElMessage.error('加载班级列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索班级
const handleSearch = () => {
  loadClasses();
};

// 显示添加班级对话框
const showAddClassDialog = () => {
  dialogType.value = 'add';
  classForm.value = {
    id: null,
    className: '',
    grade: '',
    description: ''
  };
  classDialogVisible.value = true;
};

// 显示编辑班级对话框
const showEditClassDialog = (row: any) => {
  dialogType.value = 'edit';
  classForm.value = {
    id: row.id,
    className: row.className,
    grade: row.grade,
    description: row.description
  };
  classDialogVisible.value = true;
};

// 提交班级表单
const handleSubmitClass = async () => {
  if (!classFormRef.value) return;
  
  await classFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息');
      return;
    }
    
    try {
      if (dialogType.value === 'add') {
        await createClass(classForm.value);
        ElMessage.success('班级创建成功');
      } else {
        await updateClass(classForm.value.id as number, classForm.value);
        ElMessage.success('班级更新成功');
      }
      classDialogVisible.value = false;
      loadClasses();
    } catch (error) {
      console.error('操作失败:', error);
      ElMessage.error('操作失败，请稍后重试');
    }
  });
};

// 删除班级
const handleDeleteClass = async (id: number) => {
  try {
    await deleteClass(id);
    ElMessage.success('班级删除成功');
    loadClasses();
  } catch (error) {
    console.error('删除班级失败:', error);
    ElMessage.error('删除班级失败，请稍后重试');
  }
};

// 显示教师管理对话框
const showTeacherManagementDialog = async (classData: any) => {
  selectedClass.value = classData;
  teacherDialogVisible.value = true;
  teacherSearchKeyword.value = '';
  await Promise.all([loadTeachers(), loadClassTeachers()]);
};

// 显示学生管理对话框
const showStudentManagementDialog = async (classData: any) => {
  selectedClass.value = classData;
  studentDialogVisible.value = true;
  studentSearchKeyword.value = '';
  selectedStudents.value = [];
  await Promise.all([loadUnassignedStudents(), loadClassStudents()]);
};

// 加载所有教师
const loadTeachers = async () => {
  teachersLoading.value = true;
  try {
    const response = await getTeachers(1, 1000); // 获取所有教师，设置较大的size
    teachers.value = response.content;
  } catch (error) {
    console.error('加载教师列表失败:', error);
    ElMessage.error('加载教师列表失败，请稍后重试');
  } finally {
    teachersLoading.value = false;
  }
};

// 加载班级教师
const loadClassTeachers = async () => {
  if (!selectedClass.value?.id) return;
  
  classTeachersLoading.value = true;
  try {
    classTeachers.value = await getTeachersByClassId(selectedClass.value.id);
  } catch (error) {
    console.error('加载班级教师失败:', error);
    ElMessage.error('加载班级教师失败，请稍后重试');
  } finally {
    classTeachersLoading.value = false;
  }
};

// 加载未分配的学生
const loadUnassignedStudents = async () => {
  if (!selectedClass.value?.id) return;
  
  unassignedStudentsLoading.value = true;
  try {
    unassignedStudents.value = await getUnassignedStudentsByClassId(selectedClass.value.id);
  } catch (error) {
    console.error('加载未分配学生失败:', error);
    ElMessage.error('加载未分配学生失败，请稍后重试');
  } finally {
    unassignedStudentsLoading.value = false;
  }
};

// 加载班级学生
const loadClassStudents = async () => {
  if (!selectedClass.value?.id) return;
  
  classStudentsLoading.value = true;
  try {
    classStudents.value = await getStudentsByClassId(selectedClass.value.id);
  } catch (error) {
    console.error('加载班级学生失败:', error);
    ElMessage.error('加载班级学生失败，请稍后重试');
  } finally {
    classStudentsLoading.value = false;
  }
};

// 分配教师到班级
const handleAssignTeacher = async (teacher: any) => {
  if (!selectedClass.value?.id) return;

  try {
    await assignTeacherToClass(selectedClass.value.id, teacher.id);
    ElMessage.success('教师分配成功');
    await loadClassTeachers();
  } catch (error) {
    console.error('分配教师失败:', error);
    ElMessage.error('分配教师失败，请稍后重试');
  }
};

// 从班级移除教师
const handleRemoveTeacher = async (relation: any) => {
  if (!selectedClass.value?.id) return;

  try {
    await removeTeacherFromClass(selectedClass.value.id, relation.teacherId);
    ElMessage.success('教师移除成功');
    await loadClassTeachers();
  } catch (error) {
    console.error('移除教师失败:', error);
    ElMessage.error('移除教师失败，请稍后重试');
  }
};

// 添加学生到班级
const handleAddStudent = async (student: any) => {
  if (!selectedClass.value?.id) return;

  try {
    const result = await addStudentToClass(selectedClass.value.id, student.id);
    if (result.success) {
      ElMessage.success('学生添加成功');
      await Promise.all([loadUnassignedStudents(), loadClassStudents()]);
      // 刷新班级列表中的学生数量
      loadClasses();
    } else {
      ElMessage.error(result.message || '添加学生失败');
    }
  } catch (error) {
    console.error('添加学生失败:', error);
    ElMessage.error('添加学生失败，请稍后重试');
  }
};

// 批量添加学生到班级
const handleBatchAddStudents = async () => {
  if (!selectedClass.value?.id || selectedStudents.value.length === 0) return;

  try {
    const studentIds = selectedStudents.value.map(student => student.id);
    const result = await addStudentsToClass(selectedClass.value.id, studentIds);
    
    if (result.success) {
      ElMessage.success(`成功添加${result.count}名学生`);
      await Promise.all([loadUnassignedStudents(), loadClassStudents()]);
      selectedStudents.value = [];
      // 刷新班级列表中的学生数量
      loadClasses();
    } else {
      ElMessage.error(result.message || '批量添加学生失败');
    }
  } catch (error) {
    console.error('批量添加学生失败:', error);
    ElMessage.error('批量添加学生失败，请稍后重试');
  }
};

// 从班级移除学生
const handleRemoveStudent = async (student: any) => {
  if (!selectedClass.value?.id) return;

  try {
    const result = await removeStudentFromClass(selectedClass.value.id, student.id);
    if (result.success) {
      ElMessage.success('学生移除成功');
      await Promise.all([loadUnassignedStudents(), loadClassStudents()]);
      // 刷新班级列表中的学生数量
      loadClasses();
    } else {
      ElMessage.error(result.message || '移除学生失败');
    }
  } catch (error) {
    console.error('移除学生失败:', error);
    ElMessage.error('移除学生失败，请稍后重试');
  }
};

// 选择学生
const handleStudentSelectionChange = (selection: any[]) => {
  selectedStudents.value = selection;
};

// 初始化
onMounted(() => {
  loadClasses();
});
</script>

<style scoped>
.class-management-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-container {
  display: flex;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.teacher-management, .student-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.teacher-selection, .class-teachers, .student-selection, .class-students {
  width: 100%;
}

h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style> 