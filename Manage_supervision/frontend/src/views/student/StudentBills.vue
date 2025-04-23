<template>
  <div class="student-bills-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的账单</span>
          <el-select v-model="statusFilter" placeholder="支付状态" style="width: 120px;">
            <el-option label="全部" :value="null" />
            <el-option label="未支付" :value="false" />
            <el-option label="已支付" :value="true" />
          </el-select>
        </div>
      </template>
      
      <el-table :data="bills" style="width: 100%" v-loading="loading">
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
            <el-tag v-if="isOverdue(scope.row)" type="danger" size="small" style="margin-left: 5px;">
              已逾期
            </el-tag>
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
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="!scope.row.status"
              type="primary" 
              size="small" 
              @click="handlePay(scope.row)"
              :disabled="paying"
            >
              支付
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="bills.length === 0 && !loading" class="empty-data">
        <el-empty description="暂无账单数据" />
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="账单支付"
      width="450px"
      :close-on-click-modal="false"
    >
      <div class="payment-dialog-content">
        <div class="payment-header">
          <div class="payment-amount">￥{{ currentBill?.amount.toFixed(2) }}</div>
          <div class="payment-info">
            <div class="payment-name">{{ currentBill?.feeName || '未知费用' }}</div>
            <div class="payment-time">截止日期: {{ formatDate(currentBill?.dueDate) }}</div>
          </div>
        </div>
        
        <el-divider>请选择支付方式</el-divider>
        
        <el-form :model="paymentForm" label-position="top">
          <el-form-item label="支付方式">
            <div class="payment-method-container">
              <div 
                :class="['payment-method-item', paymentForm.paymentMethod === 'WECHAT' ? 'active' : '']"
                @click="paymentForm.paymentMethod = 'WECHAT'"
              >
                <div class="payment-method-icon wechat-icon">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="payment-method-name">微信支付</div>
              </div>
              
              <div 
                :class="['payment-method-item', paymentForm.paymentMethod === 'ALIPAY' ? 'active' : '']"
                @click="paymentForm.paymentMethod = 'ALIPAY'"
              >
                <div class="payment-method-icon alipay-icon">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="payment-method-name">支付宝</div>
              </div>
            </div>
          </el-form-item>
          
          <!-- 真实随机二维码支付区域 -->
          <el-form-item>
            <div class="qrcode-container">
              <div class="qrcode-box">
                <div class="qrcode-header">
                  {{ paymentForm.paymentMethod === 'WECHAT' ? '微信扫码支付' : '支付宝扫码支付' }}
                </div>
                <div class="real-qrcode">
                  <img :src="randomQrCode" alt="支付二维码" class="qrcode-image" />
                </div>
                <div class="qrcode-amount">
                  ￥{{ currentBill?.amount.toFixed(2) }}
                </div>
                <div class="qrcode-tip">
                  {{ paymentForm.paymentMethod === 'WECHAT' ? '请使用微信扫一扫' : '请使用支付宝扫一扫' }}
                </div>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="支付备注">
            <el-input v-model="paymentForm.paymentMessage" placeholder="请填写支付备注（选填）" />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPayment" :loading="paying">
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, computed, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { ChatDotRound, Money } from '@element-plus/icons-vue';
import { billApi, type Bill, type BillPaymentParams } from '@/api/bill';
import { useUserStore } from '@/stores/user';

// 获取当前用户信息
const userStore = useUserStore();
const userId = computed(() => userStore.userId);

// 账单列表相关
const bills = ref<Bill[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalElements = ref(0);
const statusFilter = ref<boolean | null>(null);

// 支付相关
const paymentDialogVisible = ref(false);
const currentBill = ref<Bill | null>(null);
const paying = ref(false);
const paymentForm = reactive<BillPaymentParams>({
  paymentMethod: 'WECHAT',
  paymentMessage: ''
});

// 随机二维码
const randomQrCode = ref('');

// 监听状态过滤器变化
watch(statusFilter, () => {
  currentPage.value = 1;
  loadBills();
});

// 监听支付方式变化，生成新的随机二维码
watch(() => paymentForm.paymentMethod, () => {
  generateRandomQrCode();
});

// 生成随机二维码URL
const generateRandomQrCode = () => {
  const type = paymentForm.paymentMethod === 'WECHAT' ? 'wechat' : 'alipay';
  const timestamp = new Date().getTime();
  const randomId = Math.floor(Math.random() * 1000000);
  // 使用QR代码生成服务，传入随机参数确保每次都是不同的二维码
  randomQrCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${type}_payment_${timestamp}_${randomId}_amount_${currentBill.value?.amount || 0}`;
};

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

// 检查是否逾期
const isOverdue = (bill: Bill) => {
  if (bill.status) return false; // 已支付不算逾期
  
  const dueDate = bill.dueDate ? new Date(bill.dueDate) : null;
  if (!dueDate) return false;
  
  return dueDate < new Date();
};

// 加载账单列表
const loadBills = async () => {
  if (!userId.value) return;
  
  loading.value = true;
  try {
    const response = await billApi.getBillsByStudentIdPaged(
      userId.value, 
      currentPage.value - 1, 
      pageSize.value
    );
    
    if (response.data && response.data.content) {
      // 处理可能缺失的费用名称
      bills.value = response.data.content.map(bill => {
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

// 处理支付按钮点击
const handlePay = (bill: Bill) => {
  currentBill.value = bill;
  paymentForm.paymentMethod = 'WECHAT'; // 默认选择微信支付
  paymentForm.paymentMessage = '';
  paymentDialogVisible.value = true;
  generateRandomQrCode(); // 生成随机二维码
};

// 确认支付
const confirmPayment = async () => {
  if (!currentBill.value || !currentBill.value.id) return;
  
  // 开始支付过程
  paying.value = true;
  
  // 模拟支付过程，先提示扫码
  const paymentMethod = paymentForm.paymentMethod === 'WECHAT' ? '微信' : '支付宝';
  ElMessage({
    message: `请使用${paymentMethod}扫描二维码支付`,
    type: 'info',
    duration: 1000
  });
  
  // 等待2秒模拟扫码
  await new Promise(resolve => setTimeout(resolve, 2000));
  
  // 提示付款中
  ElMessage({
    message: `${paymentMethod}扫码成功，正在支付...`,
    type: 'info',
    duration: 1000
  });
  
  // 再等待1.5秒
  await new Promise(resolve => setTimeout(resolve, 1500));
  
  try {
    // 调用支付API
    await billApi.payBill(currentBill.value.id, paymentForm);
    
    // 支付成功提示
    ElMessage({
      message: `${paymentMethod}支付成功，感谢您的缴费！`,
      type: 'success',
      duration: 3000
    });
    
    paymentDialogVisible.value = false;
    loadBills(); // 重新加载账单列表
  } catch (error: any) {
    console.error('支付失败:', error);
    let errorMessage = '支付失败';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    ElMessage.error(errorMessage);
  } finally {
    paying.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  if (userId.value) {
    loadBills();
  } else {
    ElMessage.warning('用户未登录');
  }
});
</script>

<style scoped>
.student-bills-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-data {
  margin: 40px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.payment-dialog-content {
  padding: 20px;
}

.payment-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.payment-amount {
  font-size: 24px;
  font-weight: bold;
  margin-right: 20px;
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.payment-time {
  font-size: 14px;
  color: #909399;
}

.payment-method-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.payment-method-item {
  width: 48%;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.payment-method-item:hover {
  border-color: #409eff;
}

.payment-method-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.payment-method-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 auto 10px;
}

.wechat-icon {
  background-color: #09BB07;
  color: white;
}

.alipay-icon {
  background-color: #1677FF;
  color: white;
}

.payment-method-name {
  font-size: 14px;
  font-weight: bold;
  text-align: center;
}

.el-divider {
  margin: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.qrcode-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.qrcode-box {
  width: 200px;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  text-align: center;
}

.qrcode-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.real-qrcode {
  width: 150px;
  height: 150px;
  margin: 15px auto;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-image {
  max-width: 100%;
  max-height: 100%;
  border: 4px solid;
  border-color: v-bind("paymentForm.paymentMethod === 'WECHAT' ? '#09BB07' : '#1677FF'");
}

.qrcode-amount {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
}

.qrcode-tip {
  font-size: 12px;
  color: #909399;
}
</style> 