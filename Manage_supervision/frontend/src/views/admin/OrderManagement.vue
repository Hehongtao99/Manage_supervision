<template>
  <div class="order-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-controls">
            <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusChange">
              <el-option label="全部" value="" />
              <el-option label="待接单" value="PENDING" />
              <el-option label="已接单" value="ACCEPTED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="退款待审核" value="REFUND_PENDING" />
              <el-option label="已退款" value="REFUNDED" />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- 订单列表 -->
      <div v-loading="loading">
        <el-table :data="orders" border style="width: 100%">
          <el-table-column prop="orderNumber" label="订单号" width="150" />
          <el-table-column label="玩家" width="120">
            <template #default="scope">
              {{ scope.row.playerName }}
            </template>
          </el-table-column>
          <el-table-column label="陪玩" width="120">
            <template #default="scope">
              {{ scope.row.companionName }}
            </template>
          </el-table-column>
          <el-table-column prop="serviceTitle" label="服务标题" width="180" />
          <el-table-column prop="gameType" label="游戏类型" width="120" />
          <el-table-column label="订单金额" width="100">
            <template #default="scope">
              {{ scope.row.totalAmount.toFixed(2) }}元
            </template>
          </el-table-column>
          <el-table-column label="预约时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.appointedTime) }}
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="120">
            <template #default="scope">
              <el-tag :type="getOrderStatusType(scope.row.status)">
                {{ getOrderStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="100">
            <template #default="scope">
              <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                {{ getPaymentStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewOrderDetail(scope.row)"
              >
                详情
              </el-button>
              <el-button 
                v-if="canComplete(scope.row)" 
                type="success" 
                size="small" 
                @click="handleCompleteOrder(scope.row)"
              >
                完成
              </el-button>
              <el-button 
                v-if="canCancel(scope.row)" 
                type="danger" 
                size="small" 
                @click="handleCancelOrder(scope.row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="60%"
    >
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="玩家">{{ currentOrder.playerName }}</el-descriptions-item>
          <el-descriptions-item label="陪玩">{{ currentOrder.companionName }}</el-descriptions-item>
          <el-descriptions-item label="服务">{{ currentOrder.serviceTitle }}</el-descriptions-item>
          <el-descriptions-item label="游戏类型">{{ currentOrder.gameType }}</el-descriptions-item>
          <el-descriptions-item label="购买时长">{{ currentOrder.hours }}小时</el-descriptions-item>
          <el-descriptions-item label="单价">{{ currentOrder.price }}元/小时</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ currentOrder.totalAmount.toFixed(2) }}元</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDateTime(currentOrder.appointedTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(currentOrder.status)">
              {{ getOrderStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(currentOrder.paymentStatus)">
              {{ getPaymentStatusText(currentOrder.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="currentOrder.paymentTime">
            {{ formatDateTime(currentOrder.paymentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2" v-if="currentOrder.remark">
            {{ currentOrder.remark }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-footer" v-if="canComplete(currentOrder) || canCancel(currentOrder)">
          <el-button 
            v-if="canComplete(currentOrder)" 
            type="success" 
            @click="handleCompleteOrder(currentOrder)"
          >
            完成订单
          </el-button>
          <el-button 
            v-if="canCancel(currentOrder)" 
            type="danger" 
            @click="handleCancelOrder(currentOrder)"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="40%"
    >
      <el-form :model="cancelForm">
        <el-form-item label="取消原因" required>
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            rows="4"
            placeholder="请输入取消订单的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancelOrder" :loading="submitting">确认取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../../utils/date'
import { getAllOrders, getOrderDetail, cancelOrder, completeOrder } from '../../api/admin'

// 状态变量
const orders = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const currentOrder = ref(null)
const cancelDialogVisible = ref(false)
const cancelForm = ref({
  orderId: null,
  reason: ''
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getAllOrders(
      currentPage.value, 
      pageSize.value, 
      statusFilter.value
    )
    
    orders.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取订单状态标签类型
const getOrderStatusType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'ACCEPTED':
      return 'info'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    case 'REFUND_PENDING':
      return 'warning'
    case 'REFUNDED':
      return 'info'
    default:
      return 'info'
  }
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待接单'
    case 'ACCEPTED':
      return '已接单'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    case 'REFUND_PENDING':
      return '退款待审核'
    case 'REFUNDED':
      return '已退款'
    default:
      return '未知'
  }
}

// 获取支付状态标签类型
const getPaymentStatusType = (status) => {
  switch (status) {
    case 'PAID':
      return 'success'
    case 'UNPAID':
      return 'warning'
    case 'REFUNDED':
      return 'info'
    default:
      return 'info'
  }
}

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  switch (status) {
    case 'PAID':
      return '已支付'
    case 'UNPAID':
      return '未支付'
    case 'REFUNDED':
      return '已退款'
    default:
      return '未知'
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  return formatDate(dateTimeStr)
}

// 查看订单详情
const viewOrderDetail = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

// 检查订单是否可以完成
const canComplete = (order) => {
  return order.status === 'ACCEPTED' && order.paymentStatus === 'PAID'
}

// 检查订单是否可以取消
const canCancel = (order) => {
  return order.status === 'PENDING' || order.status === 'ACCEPTED'
}

// 处理完成订单
const handleCompleteOrder = (order) => {
  ElMessageBox.confirm(
    '确定将该订单标记为已完成？此操作不可撤销。',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await completeOrder(order.id)
      
      ElMessage.success(response.message || '订单已完成')
      
      loadOrders()
      detailDialogVisible.value = false
    } catch (error) {
      console.error('完成订单失败:', error)
      ElMessage.error('完成订单失败，请重试')
    }
  }).catch(() => {})
}

// 处理取消订单
const handleCancelOrder = (order) => {
  cancelForm.value.orderId = order.id
  cancelForm.value.reason = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancelOrder = async () => {
  if (!cancelForm.value.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  
  submitting.value = true
  try {
    const response = await cancelOrder(
      cancelForm.value.orderId, 
      cancelForm.value.reason
    )
    
    ElMessage.success(response.message || '订单已取消')
    
    loadOrders()
    cancelDialogVisible.value = false
    detailDialogVisible.value = false
  } catch (error) {
    console.error('取消订单失败:', error)
    ElMessage.error('取消订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 分页相关处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadOrders()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadOrders()
}

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  padding: 10px;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>