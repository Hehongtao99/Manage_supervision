<template>
  <div class="attendance-management">
    <el-card class="attendance-card">
      <template #header>
        <div class="card-header">
          <span class="title">考勤管理</span>
          <el-button type="primary" @click="showCreateDialog">创建考勤</el-button>
        </div>
      </template>
      
      <!-- 考勤列表 -->
      <el-table :data="attendanceList" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="120"></el-table-column>
        <el-table-column prop="description" label="说明" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column label="有效时间" width="240">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }} ~ {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="签到统计" width="120">
          <template #default="{ row }">
            {{ row.checkedInUsers }} / {{ row.totalUsers }}
            <el-progress 
              :percentage="calculatePercentage(row.checkedInUsers, row.totalUsers)" 
              :format="() => ''" 
              :stroke-width="6">
            </el-progress>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" type="primary" @click="viewDetails(row)">
                详情
              </el-button>
              <el-button size="small" type="info" @click="handleExport(row)">
                导出
              </el-button>
              <el-button size="small" type="warning" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="confirmDelete(row.id!)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange">
        </el-pagination>
      </div>
    </el-card>
    
    <!-- 创建/编辑考勤对话框 -->
    <el-dialog 
      :title="dialogType === 'create' ? '创建考勤' : '编辑考勤'" 
      v-model="dialogVisible"
      width="500px">
      <el-form :model="attendanceForm" ref="attendanceFormRef" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="attendanceForm.title" placeholder="请输入考勤标题"></el-input>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="attendanceForm.description" type="textarea" rows="3" placeholder="请输入考勤说明"></el-input>
        </el-form-item>
        <el-form-item label="有效时间" prop="timeRange">
          <el-date-picker
            v-model="attendanceForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAttendance" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 考勤详情对话框 -->
    <el-dialog 
      title="考勤详情" 
      v-model="detailsVisible"
      width="900px">
      <div v-if="currentAttendance">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ currentAttendance.title }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ currentAttendance.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">{{ currentAttendance.description }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(currentAttendance.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(currentAttendance.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAttendance)">{{ getStatusText(currentAttendance) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="签到统计">
            <span>{{ currentAttendance.checkedInUsers }} / {{ currentAttendance.totalUsers }}</span>
            <el-progress 
              :percentage="calculatePercentage(currentAttendance.checkedInUsers, currentAttendance.totalUsers)" 
              :stroke-width="6">
            </el-progress>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-tabs v-model="activeTab" class="mt-20">
          <el-tab-pane label="已签到学生" name="checked">
            <el-table :data="checkedInUsers" v-loading="recordsLoading" stripe>
              <el-table-column prop="username" label="用户名" width="120"></el-table-column>
              <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
              <el-table-column prop="userNumber" label="学号" width="120"></el-table-column>
              <el-table-column label="签到时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.checkInTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag type="success" v-if="row.status === '正常'">正常</el-tag>
                  <el-tag type="warning" v-else>{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="位置" min-width="120"></el-table-column>
              <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip></el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                background
                layout="total, prev, pager, next"
                :current-page="recordsPage"
                :page-size="recordsPageSize"
                :total="recordsTotal"
                @current-change="handleRecordsPageChange">
              </el-pagination>
            </div>
          </el-tab-pane>
          <el-tab-pane label="未签到学生" name="unchecked">
            <el-table :data="uncheckedUsers" v-loading="uncheckedLoading" stripe>
              <el-table-column prop="username" label="用户名" width="120"></el-table-column>
              <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
              <el-table-column prop="userNumber" label="学号" min-width="120"></el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as attendanceApi from '@/api/attendance';
import { AttendanceDTO, AttendanceRecordDTO } from '@/api/attendance';
import { formatDateTime, formatDate } from '@/utils/format';

// 考勤列表数据
const attendanceList = ref<AttendanceDTO[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 当前选中的考勤
const currentAttendance = ref<AttendanceDTO | null>(null);

// 考勤详情数据
const detailsVisible = ref(false);
const activeTab = ref('checked');
const checkedInUsers = ref<AttendanceRecordDTO[]>([]);
const uncheckedUsers = ref<AttendanceRecordDTO[]>([]);
const recordsLoading = ref(false);
const uncheckedLoading = ref(false);
const recordsPage = ref(1);
const recordsPageSize = ref(10);
const recordsTotal = ref(0);

// 考勤表单数据
const dialogVisible = ref(false);
const dialogType = ref<'create' | 'edit'>('create');
const submitting = ref(false);
const attendanceFormRef = ref();
const attendanceForm = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  timeRange: [] as string[],
});

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入考勤标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '说明长度不能超过500个字符', trigger: 'blur' }
  ],
  timeRange: [
    { required: true, message: '请选择考勤有效时间', trigger: 'change' },
    { 
      validator: (rule: any, value: string[], callback: Function) => {
        if (value && value.length === 2) {
          const start = new Date(value[0]);
          const end = new Date(value[1]);
          if (start >= end) {
            callback(new Error('结束时间必须大于开始时间'));
          } else {
            callback();
          }
        } else {
          callback(new Error('请选择有效的时间范围'));
        }
      }, 
      trigger: 'change' 
    }
  ]
};

// 计算状态类型和文本
const getStatusType = (attendance: AttendanceDTO) => {
  const now = new Date();
  const startTime = new Date(attendance.startTime);
  const endTime = new Date(attendance.endTime);
  
  if (now < startTime) {
    return 'info';
  } else if (now >= startTime && now <= endTime) {
    return 'success';
  } else {
    return 'danger';
  }
};

const getStatusText = (attendance: AttendanceDTO) => {
  const now = new Date();
  const startTime = new Date(attendance.startTime);
  const endTime = new Date(attendance.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now >= startTime && now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 判断考勤是否已过期
const isExpired = (attendance: AttendanceDTO) => {
  const now = new Date();
  const endTime = new Date(attendance.endTime);
  return now > endTime;
};

// 计算签到百分比
const calculatePercentage = (checked: number = 0, total: number = 0) => {
  return total > 0 ? Math.round((checked / total) * 100) : 0;
};

// 加载考勤列表
const loadAttendanceList = async () => {
  loading.value = true;
  try {
    const response = await attendanceApi.getAttendanceList(
      currentPage.value,
      pageSize.value
    );
    attendanceList.value = response.content;
    total.value = response.total;
  } catch (error) {
    console.error('Failed to load attendance list', error);
    ElMessage.error('加载考勤列表失败');
  } finally {
    loading.value = false;
  }
};

// 显示创建考勤对话框
const showCreateDialog = () => {
  dialogType.value = 'create';
  attendanceForm.id = undefined;
  attendanceForm.title = '';
  attendanceForm.description = '';
  attendanceForm.timeRange = [];
  dialogVisible.value = true;
};

// 显示编辑考勤对话框
const editAttendance = (attendance: AttendanceDTO) => {
  dialogType.value = 'edit';
  attendanceForm.id = attendance.id;
  attendanceForm.title = attendance.title;
  attendanceForm.description = attendance.description || '';
  attendanceForm.timeRange = [attendance.startTime, attendance.endTime];
  dialogVisible.value = true;
};

// 提交考勤表单
const submitAttendance = async () => {
  if (!attendanceFormRef.value) return;
  
  await attendanceFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      const attendance: AttendanceDTO = {
        title: attendanceForm.title,
        description: attendanceForm.description,
        startTime: attendanceForm.timeRange[0],
        endTime: attendanceForm.timeRange[1]
      };
      
      if (dialogType.value === 'create') {
        await attendanceApi.createAttendance(attendance);
        ElMessage.success('创建考勤成功');
      } else {
        await attendanceApi.updateAttendance(attendanceForm.id!, attendance);
        ElMessage.success('更新考勤成功');
      }
      
      dialogVisible.value = false;
      loadAttendanceList();
    } catch (error) {
      console.error('Failed to submit attendance', error);
      ElMessage.error('提交考勤失败');
    } finally {
      submitting.value = false;
    }
  });
};

// 确认删除考勤
const confirmDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这个考勤吗？所有相关的签到记录也将被删除。', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await attendanceApi.deleteAttendance(id);
      ElMessage.success('删除考勤成功');
      loadAttendanceList();
    } catch (error) {
      console.error('Failed to delete attendance', error);
      ElMessage.error('删除考勤失败');
    }
  }).catch(() => {});
};

// 查看考勤详情
const viewDetails = async (attendance: AttendanceDTO) => {
  currentAttendance.value = attendance;
  detailsVisible.value = true;
  activeTab.value = 'checked';
  recordsPage.value = 1;
  
  loadAttendanceRecords();
  loadUncheckedUsers();
};

// 加载考勤记录
const loadAttendanceRecords = async () => {
  if (!currentAttendance.value?.id) return;
  
  recordsLoading.value = true;
  try {
    const response = await attendanceApi.getAttendanceRecords(
      currentAttendance.value.id,
      recordsPage.value,
      recordsPageSize.value
    );
    checkedInUsers.value = response.content;
    recordsTotal.value = response.total;
  } catch (error) {
    console.error('Failed to load attendance records', error);
    ElMessage.error('加载签到记录失败');
  } finally {
    recordsLoading.value = false;
  }
};

// 加载未签到用户列表
const loadUncheckedUsers = async () => {
  if (!currentAttendance.value?.id) return;
  
  uncheckedLoading.value = true;
  try {
    uncheckedUsers.value = await attendanceApi.getUncheckedUsers(currentAttendance.value.id);
  } catch (error) {
    console.error('Failed to load unchecked users', error);
    ElMessage.error('加载未签到学生列表失败');
  } finally {
    uncheckedLoading.value = false;
  }
};

// 分页事件处理
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadAttendanceList();
};

const handleRecordsPageChange = (page: number) => {
  recordsPage.value = page;
  loadAttendanceRecords();
};

// 导出考勤记录
const handleExport = async (attendance: attendanceApi.AttendanceDTO) => {
  try {
    const response = await attendanceApi.exportAttendanceRecords(attendance.id!);
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.href = url;
    link.download = `${attendance.title}-考勤记录.xlsx`;
    document.body.appendChild(link);
    link.click();
    
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    ElMessage.success('考勤记录导出成功');
  } catch (error) {
    console.error('导出考勤记录失败', error);
    ElMessage.error('导出考勤记录失败');
  }
};

// 编辑考勤 - 改名为handleEdit与按钮命名一致
const handleEdit = (attendance: attendanceApi.AttendanceDTO) => {
  dialogType.value = 'edit';
  currentAttendance.value = attendance;
  
  // 复制数据到表单
  attendanceForm.id = attendance.id;
  attendanceForm.title = attendance.title;
  attendanceForm.description = attendance.description || '';
  attendanceForm.timeRange = [attendance.startTime, attendance.endTime];
  
  dialogVisible.value = true;
};

// 页面加载时获取数据
onMounted(() => {
  loadAttendanceList();
});
</script>

<style scoped>
.attendance-management {
  padding: 20px;
}

.attendance-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mt-20 {
  margin-top: 20px;
}
</style> 