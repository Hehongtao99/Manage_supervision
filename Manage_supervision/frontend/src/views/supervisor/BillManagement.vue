<template>
  <div class="bill-management-container">
    <div class="page-header">
      <div class="title-wrapper">
        <span>班级账单管理</span>
        <el-select v-model="selectedClass" placeholder="选择班级" @change="handleClassChange" clearable>
          <el-option
            v-for="item in classes"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
        
        <el-select v-model="statusFilter" placeholder="支付状态" style="width: 120px; margin-left: 10px;">
          <el-option label="全部" :value="null" />
          <el-option label="未支付" :value="false" />
          <el-option label="已支付" :value="true" />
        </el-select>
      </div>

      <div class="search-wrapper">
        <el-input
          v-model="searchQuery"
          placeholder="搜索学生姓名"
          prefix-icon="el-icon-search"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-empty v-if="classes.length === 0" description="暂无班级数据" />

    <template v-else>
      <el-table 
        :data="filteredBills" 
        style="width: 100%" 
        :default-sort="{ prop: 'createTime', order: 'descending' }"
        v-loading="loading"
        border
      >
        <el-table-column prop="studentName" label="学生姓名" sortable />
        <el-table-column prop="feeName" label="费用名称" sortable />
        <el-table-column prop="amount" label="金额" sortable>
          <template #default="scope">
            <span>¥{{ scope.row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="支付状态" width="100" sortable>
          <template #default="scope">
            <el-tag :type="scope.row.status ? 'success' : 'danger'">
              {{ scope.row.status ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" sortable>
          <template #default="scope">
            <span>{{ formatDate(scope.row.dueDate) }}</span>
            <el-tag v-if="isOverdue(scope.row)" type="danger" size="small" style="margin-left: 5px;">
              已逾期
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" sortable>
          <template #default="scope">
            {{ scope.row.paymentTime ? formatDateTime(scope.row.paymentTime) : '未支付' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="scope">
            <template v-if="scope.row.paymentMethod === 'WECHAT'">
              <el-tag type="success">微信支付</el-tag>
            </template>
            <template v-else-if="scope.row.paymentMethod === 'ALIPAY'">
              <el-tag type="primary">支付宝</el-tag>
            </template>
            <template v-else>
              <span>-</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small"
              @click="handleView(scope.row)"
              :disabled="!scope.row.status"
            >
              查看详情
            </el-button>
            <el-button 
              type="success" 
              size="small"
              @click="handleMarkAsPaid(scope.row)"
              :disabled="scope.row.status"
            >
              标记为已支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredBills.length === 0 && !loading" description="暂无账单数据" />

      <div class="pagination-container" v-if="filteredBills.length > 0">
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
    </template>

    <!-- 账单详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="账单支付详情"
      width="500px"
    >
      <div v-if="currentBill" class="bill-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学生姓名">{{ currentBill.studentName }}</el-descriptions-item>
          <el-descriptions-item label="费用名称">{{ currentBill.feeName }}</el-descriptions-item>
          <el-descriptions-item label="账单金额">¥{{ currentBill.amount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="currentBill.status ? 'success' : 'danger'">
              {{ currentBill.status ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ formatDate(currentBill.dueDate) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDateTime(currentBill.paymentTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <template v-if="currentBill.paymentMethod === 'WECHAT'">
              <el-tag type="success">微信支付</el-tag>
            </template>
            <template v-else-if="currentBill.paymentMethod === 'ALIPAY'">
              <el-tag type="primary">支付宝</el-tag>
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="支付备注">{{ currentBill.paymentMessage || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 标记为已支付对话框 -->
    <el-dialog
      v-model="markPaidDialogVisible"
      title="标记账单为已支付"
      width="500px"
    >
      <div v-if="currentBill" class="mark-paid-form">
        <el-form :model="paymentForm" label-width="100px">
          <el-form-item label="学生姓名">
            <el-input v-model="currentBill.studentName" disabled />
          </el-form-item>
          <el-form-item label="费用名称">
            <el-input v-model="currentBill.feeName" disabled />
          </el-form-item>
          <el-form-item label="账单金额">
            <el-input v-model="amountDisplay" disabled />
          </el-form-item>
          <el-form-item label="支付方式">
            <el-radio-group v-model="paymentForm.paymentMethod">
              <el-radio label="WECHAT">微信支付</el-radio>
              <el-radio label="ALIPAY">支付宝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="支付备注">
            <el-input v-model="paymentForm.paymentMessage" placeholder="请填写支付备注（选填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="markPaidDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmMarkAsPaid">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { billApi, type Bill, type BillPaymentParams } from '@/api/bill';
import { getTeacherClasses } from '@/api/class';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

// 班级列表
interface ClassInfo {
  id: number;
  name: string;
}
const classes = ref<ClassInfo[]>([]);
const selectedClass = ref<number | null>(null);

// 账单列表相关
const bills = ref<Bill[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const statusFilter = ref<boolean | null>(null);
const searchQuery = ref('');

// 账单详情对话框
const dialogVisible = ref(false);
const markPaidDialogVisible = ref(false);
const currentBill = ref<Bill | null>(null);

// 支付表单
const paymentForm = ref<BillPaymentParams>({
  paymentMethod: 'WECHAT',
  paymentMessage: ''
});

// 金额显示格式
const amountDisplay = computed(() => {
  return currentBill.value ? `¥${currentBill.value.amount.toFixed(2)}` : '';
});

// 过滤后的账单列表
const filteredBills = computed(() => {
  if (!bills.value) return [];
  
  return bills.value.filter(bill => {
    const matchesStatus = statusFilter.value === null || bill.status === statusFilter.value;
    const matchesSearch = !searchQuery.value || 
                         (bill.studentName && bill.studentName.toLowerCase().includes(searchQuery.value.toLowerCase()));
    
    return matchesStatus && matchesSearch;
  });
});

onMounted(() => {
  loadClasses();
});

// 监听状态过滤器变化
watch(statusFilter, () => {
  if (selectedClass.value) {
    loadBills();
  } 
});

// 加载教师的班级
const loadClasses = async () => {
  try {
    loading.value = true;
    console.log('开始加载班级数据');
    const response = await getTeacherClasses();
    console.log('获取到教师班级数据:', response);
    
    if (response && response.data) {
      classes.value = response.data;
      console.log('设置班级列表:', classes.value);
      
      // 如果有班级，默认选中第一个
      if (classes.value && classes.value.length > 0) {
        selectedClass.value = classes.value[0].id;
        console.log('默认选中班级:', selectedClass.value);
        loadBills();
      } else {
        console.log('没有找到班级数据');
        bills.value = []; // 清空账单列表
      }
    } else {
      console.error('班级数据格式不正确:', response);
      ElMessage.warning('获取班级数据格式不正确');
      classes.value = [];
      bills.value = [];
    }
  } catch (error) {
    console.error('加载班级失败:', error);
    ElMessage.error('加载班级列表失败');
    classes.value = [];
    bills.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载账单列表
const loadBills = async () => {
  if (!selectedClass.value) {
    console.log('未选择班级，不加载账单');
    bills.value = [];
    totalElements.value = 0;
    return;
  }
  
  try {
    loading.value = true;
    console.log(`开始加载班级${selectedClass.value}的账单数据`);
    
    const response = await billApi.getBillsByClassId(
      selectedClass.value,
      currentPage.value - 1,
      pageSize.value
    );
    
    console.log('获取到账单数据:', response.data);
    
    if (response.data && response.data.content) {
      bills.value = response.data.content;
      totalElements.value = response.data.totalElements;
      
      // 处理可能缺失的费用名称
      bills.value.forEach(bill => {
        if (!bill.feeName) {
          console.warn(`账单 ID ${bill.id} 缺少费用名称`);
          bill.feeName = '未知费用项目';
        }
      });
    } else {
      console.error('账单数据格式不正确:', response.data);
      ElMessage.warning('获取账单数据格式不正确');
      bills.value = [];
      totalElements.value = 0;
    }
  } catch (error) {
    console.error('加载账单失败:', error);
    ElMessage.error('加载账单数据失败');
    bills.value = [];
    totalElements.value = 0;
  } finally {
    loading.value = false;
  }
};

// 处理班级变更
const handleClassChange = () => {
  currentPage.value = 1;
  loadBills();
};

// 处理搜索
const handleSearch = () => {
  // 客户端过滤，不需要重新加载数据
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  loadBills();
};

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  loadBills();
};

// 处理查看详情
const handleView = (bill: Bill) => {
  currentBill.value = bill;
  dialogVisible.value = true;
};

// 处理标记为已支付
const handleMarkAsPaid = (bill: Bill) => {
  currentBill.value = bill;
  paymentForm.value = {
    paymentMethod: 'WECHAT',
    paymentMessage: ''
  };
  markPaidDialogVisible.value = true;
};

// 确认标记为已支付
const confirmMarkAsPaid = async () => {
  if (!currentBill.value || !currentBill.value.id) return;
  
  try {
    await billApi.payBill(currentBill.value.id, paymentForm.value);
    ElMessage.success('账单已标记为已支付');
    markPaidDialogVisible.value = false;
    loadBills(); // 重新加载账单列表
  } catch (error) {
    console.error('标记账单失败:', error);
    ElMessage.error('标记账单为已支付失败');
  }
};

// 日期格式化
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 日期时间格式化
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 检查是否逾期
const isOverdue = (bill: Bill) => {
  if (bill.status) return false; // 已支付不算逾期
  
  const dueDate = bill.dueDate ? new Date(bill.dueDate) : null;
  if (!dueDate) return false;
  
  const today = new Date();
  return dueDate < today;
};
</script>

<style scoped>
.bill-management-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 18px;
  font-weight: bold;
}

.search-wrapper {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.bill-details {
  margin-top: 10px;
}

.mark-paid-form {
  margin-top: 20px;
}
</style> 