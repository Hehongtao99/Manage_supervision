<template>
  <div class="after-sales-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>售后管理</span>
          <div class="filter-container">
            <el-select v-model="statusFilter" placeholder="申请状态" clearable @change="filterOrders">
              <el-option label="全部状态" value="" />
              <el-option v-for="(value, key) in refundStatusMap" :key="key" :label="value" :value="key" />
            </el-select>
            <el-button type="primary" @click="loadRefundOrders">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="order-container" v-loading="loading">
        <el-empty v-if="filteredOrders.length === 0" description="暂无退款申请" />
        
        <div v-else class="order-list-content">
          <el-table
            :data="filteredOrders"
            style="width: 100%"
            @row-click="showOrderDetail"
            row-key="id"
          >
            <el-table-column prop="orderNumber" label="订单编号" width="180">
              <template #default="scope">
                <span class="order-number">{{ scope.row.orderNumber }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="courseTitle" label="课程名称" show-overflow-tooltip />
            
            <el-table-column prop="teacherName" label="教师" width="120" />
            
            <el-table-column prop="totalAmount" label="总金额" width="120">
              <template #default="scope">
                <span class="amount">¥{{ scope.row.totalAmount }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ orderStatusMap[scope.row.status] || scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="createTime" label="创建时间" width="180" />
            
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <div class="operation-buttons">
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="showOrderDetail(scope.row)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    @click.stop="handleContactTeacher(scope.row.teacherId)"
                  >
                    联系教师
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="退款申请详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-header">
          <div class="order-id">订单号：{{ currentOrder.orderNumber }}</div>
          <el-tag :type="getStatusType(currentOrder.status)" size="large">
            {{ orderStatusMap[currentOrder.status] || currentOrder.status }}
          </el-tag>
        </div>
        
        <el-divider />
        
        <div class="detail-content">
          <div class="detail-item">
            <span class="label">课程名称：</span>
            <span class="value">{{ currentOrder.courseTitle }}</span>
          </div>
          <div class="detail-item">
            <span class="label">科目：</span>
            <span class="value">{{ currentOrder.courseSubject }}</span>
          </div>
          <div class="detail-item">
            <span class="label">教师：</span>
            <span class="value">{{ currentOrder.teacherName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">课时单价：</span>
            <span class="value price">¥{{ currentOrder.price }}/小时</span>
          </div>
          <div class="detail-item">
            <span class="label">购买小时数：</span>
            <span class="value">{{ currentOrder.hours }} 小时</span>
          </div>
          <div class="detail-item">
            <span class="label">总金额：</span>
            <span class="value price">¥{{ currentOrder.totalAmount }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ currentOrder.createTime }}</span>
          </div>
          
          <div v-if="currentOrder.message" class="detail-item message">
            <span class="label">留言：</span>
            <span class="value">{{ currentOrder.message }}</span>
          </div>
          
          <div v-if="currentOrder.refundReason" class="detail-item refund-reason">
            <span class="label">退款理由：</span>
            <span class="value highlight-reason">{{ currentOrder.refundReason }}</span>
          </div>
          
          <div v-if="currentOrder.rejectReason" class="detail-item reject-reason">
            <span class="label">拒绝理由：</span>
            <span class="value">{{ currentOrder.rejectReason }}</span>
          </div>
          
          <div v-if="currentOrder.status === 'REFUND_REJECTED'" class="operation-tips">
            <el-alert
              title="您的退款申请已被拒绝"
              type="error"
              :closable="false"
              show-icon
            >
              <p>如有疑问，您可以联系教师沟通或重新提交退款申请。</p>
            </el-alert>
          </div>
          
          <div v-if="currentOrder.status === 'REFUND_PENDING'" class="operation-tips">
            <el-alert
              title="您的退款申请正在处理中"
              type="warning"
              :closable="false"
              show-icon
            >
              <p>请耐心等待教师处理您的退款申请。</p>
            </el-alert>
          </div>
          
          <div v-if="currentOrder.status === 'CANCELED'" class="operation-tips">
            <el-alert
              title="您的退款申请已通过，订单已取消"
              type="success"
              :closable="false"
              show-icon
            >
              <p>退款金额将在1-7个工作日内退回您的支付账户。</p>
            </el-alert>
          </div>
        </div>
        
        <div class="detail-footer">
          <template v-if="currentOrder.status === 'REFUND_REJECTED'">
            <el-button type="warning" @click="handleReapplyRefund(currentOrder)">
              重新申请退款
            </el-button>
          </template>
          
          <el-button 
            type="info" 
            @click="handleContactTeacher(currentOrder.teacherId)"
          >
            联系教师
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 申请退款确认对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="400px"
      destroy-on-close
      @open="onRefundDialogOpen"
      @closed="onRefundDialogClosed"
    >
      <div class="refund-form">
        <el-form ref="refundFormRef" :model="refundForm" label-width="80px">
          <el-form-item label="退款理由" prop="reason" required>
            <el-input 
              v-model="refundForm.reason" 
              type="textarea" 
              rows="4" 
              placeholder="请输入退款理由（必填）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="confirmRefundOrder" :loading="operationLoading">
            确认申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getStudentOrders, 
  cancelOrder,
  OrderDTO, 
  orderStatusMap 
} from '../../api/order'
import { startChatWithTeacher } from '../../api/courses'
import { useRouter } from 'vue-router'

// 退款状态映射
const refundStatusMap = {
  'REFUND_PENDING': '退款申请中',
  'REFUND_REJECTED': '退款已拒绝',
  'CANCELED': '已退款'
}

// 状态变量
const loading = ref(false)
const orders = ref<OrderDTO[]>([])
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const currentOrder = ref<OrderDTO | null>(null)
const refundDialogVisible = ref(false)
const refundFormRef = ref()
const refundForm = ref({
  reason: ''
})
const operationLoading = ref(false)
const router = useRouter()

// 计算过滤后的订单 - 只显示跟退款相关的订单
const filteredOrders = computed(() => {
  // 先过滤只保留退款相关状态的订单
  let result = orders.value.filter(order => 
    order.status === 'REFUND_PENDING' || 
    order.status === 'REFUND_REJECTED' ||
    order.status === 'CANCELED'
  );
  
  // 根据状态过滤器进一步过滤
  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value);
  }
  
  return result;
})

// 初始化加载
onMounted(() => {
  loadRefundOrders()
})

// 加载订单列表
const loadRefundOrders = async () => {
  loading.value = true
  try {
    const data = await getStudentOrders()
    // 过滤出与退款相关的订单
    orders.value = data.filter((order: OrderDTO) => 
      order.status === 'REFUND_PENDING' || 
      order.status === 'REFUND_REJECTED' ||
      order.status === 'CANCELED'
    )
  } catch (error) {
    console.error('获取退款订单列表失败:', error)
    ElMessage.error('获取退款订单列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 过滤订单
const filterOrders = () => {
  // 使用计算属性自动过滤
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'REFUND_PENDING':
      return 'warning'
    case 'REFUND_REJECTED':
      return 'danger'
    case 'CANCELED':
      return 'info'
    default:
      return 'info'
  }
}

// 显示订单详情
const showOrderDetail = (row: OrderDTO) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

// 联系教师
const handleContactTeacher = async (teacherId: number) => {
  if (!teacherId) {
    ElMessage.error('教师ID无效，无法开始聊天')
    return
  }

  try {
    ElMessage({
      message: '正在连接聊天...',
      type: 'info',
      duration: 1500,
      showClose: false
    })

    const response = await startChatWithTeacher(teacherId)
    
    if (!response || !response.id) {
      ElMessage.closeAll()
      ElMessage.error('创建聊天会话失败')
      return
    }

    // 关闭当前对话框
    detailDialogVisible.value = false
    
    // 重定向到聊天页面
    router.push({ 
      path: '/chat',
      query: { 
        conversationId: response.id.toString()
      } 
    })
  } catch (error) {
    ElMessage.closeAll()
    console.error('聊天初始化错误:', error)
    ElMessage.error('连接聊天失败，请稍后再试')
  }
}

// 重新申请退款
const handleReapplyRefund = (order: OrderDTO) => {
  currentOrder.value = order
  refundDialogVisible.value = true
}

// 退款申请对话框打开事件
const onRefundDialogOpen = () => {
  // 确保表单内容被清空
  refundForm.value = {
    reason: ''
  }
  // 使用nextTick等待DOM更新后重置表单
  nextTick(() => {
    if (refundFormRef.value) {
      refundFormRef.value.resetFields()
    }
  })
}

// 退款申请对话框关闭事件
const onRefundDialogClosed = () => {
  // 清空表单数据
  refundForm.value = {
    reason: ''
  }
}

// 确认申请退款
const confirmRefundOrder = async () => {
  if (!currentOrder.value) return
  
  if (!refundForm.value.reason.trim()) {
    ElMessage.warning('请输入退款理由')
    return
  }
  
  operationLoading.value = true
  try {
    const result = await cancelOrder(currentOrder.value.id, refundForm.value.reason)
    
    if (result.success) {
      ElMessage.success(result.message || '退款申请已提交')
      
      // 更新订单状态
      updateOrderStatus(currentOrder.value.id, 'REFUND_PENDING')
      
      // 关闭对话框
      refundDialogVisible.value = false
      detailDialogVisible.value = false
      
      // 刷新数据
      await loadRefundOrders()
    } else {
      ElMessage.error(result.message || '退款申请失败')
    }
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('退款申请失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 更新订单状态
const updateOrderStatus = (orderId: number, newStatus: string) => {
  const index = orders.value.findIndex(o => o.id === orderId)
  if (index !== -1) {
    orders.value[index].status = newStatus
  }
}
</script>

<style scoped>
.after-sales-management {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.order-list-content {
  margin-top: 20px;
}

.order-number {
  font-family: monospace;
  color: #666;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.operation-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.operation-buttons .el-button {
  margin-left: 0;
  margin-right: 5px;
  margin-bottom: 5px;
}

.order-detail {
  padding: 0 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-id {
  font-size: 16px;
  font-weight: bold;
}

.detail-content {
  margin-top: 20px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-item .label {
  width: 100px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
}

.detail-item .price {
  color: #f56c6c;
  font-weight: bold;
}

.highlight-reason {
  font-weight: bold;
  color: #e6a23c;
}

.message {
  border-left: 3px solid #e6a23c;
  padding-left: 10px;
  margin-top: 20px;
}

.refund-reason {
  border-left: 3px solid #e6a23c;
  padding-left: 10px;
  margin-top: 20px;
  background-color: #fdf6ec;
  padding: 10px;
  border-radius: 4px;
}

.reject-reason {
  border-left-color: #f56c6c;
  background-color: #fef0f0;
  padding: 10px;
  border-radius: 4px;
}

.operation-tips {
  margin-top: 20px;
  margin-bottom: 20px;
}

.detail-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.refund-form {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-container {
    width: 100%;
    margin-top: 15px;
  }
  
  .operation-buttons {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 5px;
  }
  
  .operation-buttons .el-button {
    margin-bottom: 5px;
  }
  
  .detail-item {
    flex-direction: column;
  }
  
  .detail-item .label {
    width: 100%;
    margin-bottom: 5px;
  }
  
  .detail-footer {
    flex-wrap: wrap;
  }
}
</style> 