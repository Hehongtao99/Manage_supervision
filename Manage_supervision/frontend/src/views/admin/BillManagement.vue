<template>
  <div class="bill-management-container">
    <el-tabs v-model="activeTab">
      <!-- 账单列表 -->
      <el-tab-pane label="账单列表" name="billList">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>账单管理</span>
              <div class="header-actions">
                <el-select v-model="statusFilter" placeholder="支付状态" style="width: 120px; margin-right: 10px;">
                  <el-option label="全部" :value="null" />
                  <el-option label="未支付" :value="false" />
                  <el-option label="已支付" :value="true" />
                </el-select>
                <el-button type="primary" @click="handleGenerateBill">生成账单</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="bills" style="width: 100%" v-loading="loading">
            <el-table-column prop="studentName" label="学生姓名" />
            <el-table-column prop="feeName" label="费用名称" />
            <el-table-column prop="amount" label="金额">
              <template #default="scope">
                {{ scope.row.amount.toFixed(2) }} 元
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status ? 'success' : 'danger'">
                  {{ scope.row.status ? '已支付' : '未支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="截止日期">
              <template #default="scope">
                {{ formatDate(scope.row.dueDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentTime" label="支付时间">
              <template #default="scope">
                {{ scope.row.paymentTime ? formatDateTime(scope.row.paymentTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式">
              <template #default="scope">
                <span v-if="scope.row.paymentMethod === 'WECHAT'">
                  <el-tag type="success">微信支付</el-tag>
                </span>
                <span v-else-if="scope.row.paymentMethod === 'ALIPAY'">
                  <el-tag type="primary">支付宝</el-tag>
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMessage" label="支付消息">
              <template #default="scope">
                <el-tooltip 
                  v-if="scope.row.paymentMessage" 
                  :content="scope.row.paymentMessage" 
                  placement="top"
                  :show-after="500"
                >
                  <span class="payment-message-text">{{ scope.row.paymentMessage }}</span>
                </el-tooltip>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button 
                  size="small" 
                  :type="scope.row.status ? 'warning' : 'success'"
                  @click="handleUpdateBillStatus(scope.row)"
                >
                  {{ scope.row.status ? '标记未支付' : '标记已支付' }}
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="handleDeleteBill(scope.row)"
                  :disabled="scope.row.status"
                  v-tooltip="scope.row.status ? '已支付账单不能删除' : ''"
                >
                  删除
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
              :total="totalElements"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 账单生成 -->
      <el-tab-pane label="账单生成" name="billGenerate">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>生成账单</span>
            </div>
          </template>
          
          <el-form :model="generateForm" label-width="120px" :rules="rules" ref="generateFormRef">
            <el-form-item label="费用标准" prop="feeStandardId">
              <el-select v-model="generateForm.feeStandardId" placeholder="请选择费用标准" style="width: 100%">
                <el-option
                  v-for="item in feeStandards"
                  :key="item.id"
                  :label="`${item.feeName} (${item.amount}元)`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="账单生成方式">
              <el-radio-group v-model="generateType">
                <el-radio :label="'all'">所有学生</el-radio>
                <el-radio :label="'class'">按班级</el-radio>
                <el-radio :label="'students'">选择学生</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="generateType === 'class'" label="班级" prop="classId">
              <el-select v-model="generateForm.classId" placeholder="请选择班级" style="width: 100%">
                <el-option
                  v-for="item in classes"
                  :key="item.id"
                  :label="item.className"
                  :value="item.id"
                />
              </el-select>
              <div v-if="classStudents.length > 0" class="class-students-info mt-2">
                <div class="text-sm text-gray-600 mb-1">该班级包含以下学生:</div>
                <el-tag 
                  v-for="student in classStudents" 
                  :key="student.id" 
                  class="mr-1 mb-1" 
                  size="small"
                >
                  {{ student.username || student.realName }}
                </el-tag>
              </div>
            </el-form-item>
            
            <el-form-item v-if="generateType === 'students'" label="学生" prop="studentIds">
              <el-select
                v-model="generateForm.studentIds"
                multiple
                filterable
                placeholder="请选择学生"
                style="width: 100%"
              >
                <el-option
                  v-for="item in students"
                  :key="item.id"
                  :label="item.username || item.realName"
                  :value="item.id"
                >
                  <div class="flex items-center">
                    <span>{{ item.username || item.realName }}</span>
                    <span class="text-gray-500 ml-2" v-if="item.userNumber">(学号: {{ item.userNumber }})</span>
                  </div>
                </el-option>
              </el-select>
              <div class="text-sm text-gray-500 mt-1" v-if="generateForm.studentIds.length > 0">
                已选择 {{ generateForm.studentIds.length }} 名学生
              </div>
            </el-form-item>
            
            <el-form-item label="截止日期" prop="dueDate">
              <el-date-picker
                v-model="generateForm.dueDate"
                type="datetime"
                placeholder="选择截止日期"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitGenerateBill" :loading="generating">
                生成账单
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { billApi, type Bill } from '@/api/bill';
import { feeStandardApi, type FeeStandard } from '@/api/feeStandard';
import { getAllClasses, getStudentsByClassId } from '@/api/class';
import { getStudentsAll } from '@/api/student';
import type { ClassDTO } from '@/types/class';

// 从后端API获取班级和学生信息
interface Class {
  id: number;
  className: string;
}

interface Student {
  id: number;
  username: string;
}

const activeTab = ref('billList');

// 账单列表相关
const bills = ref<Bill[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const statusFilter = ref<boolean | null>(null);

// 账单生成相关
const generateType = ref('all');
const generateForm = reactive({
  feeStandardId: undefined as number | undefined,
  classId: undefined as number | undefined,
  studentIds: [] as number[],
  dueDate: '' as string | Date
});
const feeStandards = ref<FeeStandard[]>([]);
const classes = ref<ClassDTO[]>([]);
const students = ref<Student[]>([]);
const classStudents = ref<Student[]>([]);
const generating = ref(false);
const generateFormRef = ref<FormInstance>();

// 表单验证规则
const rules = reactive<FormRules>({
  feeStandardId: [
    { required: true, message: '请选择费用标准', trigger: 'change' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  studentIds: [
    { required: true, message: '请选择至少一名学生', trigger: 'change', type: 'array' }
  ],
  dueDate: [
    { required: true, message: '请选择截止日期', trigger: 'change', type: 'date' }
  ]
});

// 监听生成类型变化，重置相关字段
watch(generateType, (newVal) => {
  if (newVal === 'all') {
    generateForm.classId = undefined;
    generateForm.studentIds = [];
  } else if (newVal === 'class') {
    generateForm.studentIds = [];
    if (classes.value.length === 0) {
      loadClasses();
    }
  } else if (newVal === 'students') {
    generateForm.classId = undefined;
    if (students.value.length === 0) {
      loadAllStudents();
    }
  }
});

// 监听选中的班级变化，加载对应班级的学生
watch(() => generateForm.classId, async (newClassId) => {
  if (newClassId && generateType.value === 'class') {
    try {
      // 加载班级学生
      const studentsData = await getStudentsByClassId(newClassId);
      if (studentsData && Array.isArray(studentsData)) {
        classStudents.value = studentsData;
      } else {
        console.error('班级学生数据格式不正确:', studentsData);
        ElMessage.error('无法正确解析班级学生数据');
        classStudents.value = [];
      }
    } catch (error) {
      console.error('加载班级学生失败:', error);
      ElMessage.error('无法获取班级学生列表');
      classStudents.value = [];
    }
  } else {
    classStudents.value = [];
  }
});

// 监听状态过滤器变化
watch(statusFilter, () => {
  currentPage.value = 1;
  loadBills();
});

// 格式化日期
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 格式化日期时间
const formatDateTime = (dateString: string | undefined) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 加载账单列表
const loadBills = async () => {
  loading.value = true;
  try {
    let response;
    if (statusFilter.value !== null) {
      response = await billApi.getBillsByStatus(statusFilter.value, currentPage.value - 1, pageSize.value);
    } else {
      response = await billApi.getAllBills(currentPage.value - 1, pageSize.value);
    }
    
    // 处理费用名称显示
    if (response.data && response.data.content) {
      bills.value = response.data.content.map(bill => {
        // 确保费用名称不为空
        if (!bill.feeName || bill.feeName.trim() === '') {
          console.warn(`账单 ID ${bill.id} 缺少费用名称`);
          bill.feeName = '未知费用项目';
        }
        return bill;
      });
      totalElements.value = response.data.totalElements;
    } else {
      bills.value = [];
      totalElements.value = 0;
    }
  } catch (error) {
    console.error('加载账单失败:', error);
    ElMessage.error('加载账单失败');
    bills.value = [];
    totalElements.value = 0;
  } finally {
    loading.value = false;
  }
};

// 加载费用标准
const loadFeeStandards = async () => {
  try {
    const response = await feeStandardApi.getAllFeeStandards();
    feeStandards.value = response.data;
  } catch (error) {
    console.error('加载费用标准失败:', error);
    ElMessage.error('加载费用标准失败');
  }
};

// 加载所有班级
const loadClasses = async () => {
  try {
    const classesData = await getAllClasses();
    classes.value = classesData;
  } catch (error) {
    console.error('加载班级数据失败:', error);
    ElMessage.error('无法获取班级列表');
  }
};

// 加载所有学生
const loadAllStudents = async () => {
  try {
    const studentsData = await getStudentsAll();
    if (studentsData && Array.isArray(studentsData)) {
      students.value = studentsData;
    } else {
      console.error('学生数据格式不正确:', studentsData);
      ElMessage.error('学生数据格式不正确');
    }
  } catch (error) {
    console.error('加载学生数据失败:', error);
    ElMessage.error('无法获取学生列表');
  }
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  loadBills();
};

// 处理每页显示数量变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  loadBills();
};

// 切换到账单生成页面
const handleGenerateBill = () => {
  activeTab.value = 'billGenerate';
};

// 更新账单状态
const handleUpdateBillStatus = async (row: Bill) => {
  try {
    if (row.id !== undefined) {
      await billApi.updateBillStatus(row.id, !row.status);
      ElMessage.success(`账单状态已更新为${!row.status ? '已支付' : '未支付'}`);
      loadBills();
    }
  } catch (error) {
    console.error('更新账单状态失败:', error);
    ElMessage.error('更新账单状态失败');
  }
};

// 删除账单
const handleDeleteBill = (row: Bill) => {
  if (row.status) {
    ElMessage.warning('已支付的账单不能删除');
    return;
  }
  
  ElMessageBox.confirm('确定要删除该账单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (row.id !== undefined) {
        await billApi.deleteBill(row.id);
        ElMessage.success('删除成功');
        loadBills();
      }
    } catch (error: any) {
      console.error('删除账单失败:', error);
      let errorMessage = '删除账单失败';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      ElMessage.error(errorMessage);
    }
  }).catch(() => {});
};

// 提交生成账单
const submitGenerateBill = async () => {
  if (!generateFormRef.value) return;
  
  await generateFormRef.value.validate(async (valid) => {
    if (valid) {
      generating.value = true;
      try {
        const params = {
          feeStandardId: generateForm.feeStandardId!,
          dueDate: new Date(generateForm.dueDate).toISOString()
        } as any;
        
        if (generateType.value === 'class' && generateForm.classId) {
          params.classId = generateForm.classId;
          console.log('按班级生成账单', params);
        } else if (generateType.value === 'students' && generateForm.studentIds.length > 0) {
          params.studentIds = generateForm.studentIds;
          console.log('按学生列表生成账单', params);
        } else {
          // 所有学生生成账单，不需要额外参数
          console.log('为所有学生生成账单', params);
        }
        
        const response = await billApi.generateBills(params);
        
        if (response && response.data) {
          const generatedCount = response.data.length;
          if (generatedCount > 0) {
            ElMessage.success(`成功生成 ${generatedCount} 个账单`);
            activeTab.value = 'billList';
            loadBills();
            generateFormRef.value.resetFields();
          } else {
            // 未生成任何账单的情况
            if (generateType.value === 'all') {
              // 为所有学生生成账单
              ElMessage.warning('没有生成任何账单。可能是因为所有学生已有此费用标准的账单，或系统中没有学生账号。');
            } else if (generateType.value === 'class') {
              // 按班级生成账单
              ElMessage.warning(`没有生成任何账单。可能是班级中的学生已有此费用标准的账单，或该班级没有学生。`);
            } else {
              // 按选定学生生成账单
              ElMessage.warning('没有生成任何账单。所选学生可能已有此费用标准的账单。');
            }
          }
        } else {
          ElMessage.warning('服务器返回了空数据，请联系管理员。');
        }
      } catch (error: any) {
        console.error('生成账单失败:', error);
        let errorMessage = '账单生成失败';
        
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message;
        } else if (error.message) {
          errorMessage = `账单生成失败: ${error.message}`;
        }
        
        ElMessage.error(errorMessage);
      } finally {
        generating.value = false;
      }
    } else {
      ElMessage.error('请完成必填项');
    }
  });
};

// 在组件挂载时加载费用标准
onMounted(() => {
  loadBills();
  loadFeeStandards();
  loadClasses();
  loadAllStudents();
});
</script>

<style scoped>
.bill-management-container {
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

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.class-students-info {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.mt-2 {
  margin-top: 8px;
}

.mb-1 {
  margin-bottom: 4px;
}

.mr-1 {
  margin-right: 4px;
}

.text-sm {
  font-size: 0.875rem;
}

.text-gray-600 {
  color: #4b5563;
}

.text-gray-500 {
  color: #6b7280;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}

.payment-message-text {
  display: inline-block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

/* 表单样式 */
.generate-form {
  max-width: 600px;
  margin: 0 auto;
}
</style> 