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
                    v-if="scope.row.status === 'REFUND_REJECTED'"
                    type="warning"
                    size="small"
                    @click.stop="handleAppealRefund(scope.row)"
                  >
                    申诉
                  </el-button>
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
          
          <div v-if="currentOrder.appealReason" class="detail-item appeal-reason">
            <span class="label">申诉理由：</span>
            <span class="value highlight-reason">{{ currentOrder.appealReason }}</span>
          </div>
          
          <div v-if="currentOrder.teacherResponse" class="detail-item teacher-response">
            <span class="label">教师回复：</span>
            <span class="value">{{ currentOrder.teacherResponse }}</span>
          </div>
          
          <div v-if="currentOrder.adminDecision" class="detail-item admin-decision">
            <span class="label">管理员决定：</span>
            <span class="value highlight-reason">{{ currentOrder.adminDecision }}</span>
          </div>
          
          <div v-if="currentOrder.status === 'REFUND_REJECTED'" class="operation-tips">
            <el-alert
              title="您的退款申请已被拒绝"
              type="error"
              :closable="false"
              show-icon
            >
              <p>如有疑问，您可以联系教师沟通或提交申诉。</p>
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
          
          <div v-if="currentOrder.status === 'APPEALING'" class="operation-tips">
            <el-alert
              title="您的申诉正在处理中"
              type="warning"
              :closable="false"
              show-icon
            >
              <p>请耐心等待教师回复和管理员处理您的申诉。</p>
            </el-alert>
          </div>
          
          <div v-if="currentOrder.status === 'APPEAL_APPROVED'" class="operation-tips">
            <el-alert
              title="您的申诉已通过，订单已退款"
              type="success"
              :closable="false"
              show-icon
            >
              <p>退款金额将在1-7个工作日内退回您的支付账户。</p>
            </el-alert>
          </div>
          
          <div v-if="currentOrder.status === 'APPEAL_REJECTED'" class="operation-tips">
            <el-alert
              title="您的申诉未通过，无法退款"
              type="error"
              :closable="false"
              show-icon
            >
              <p>如有疑问，您可以联系教师或管理员沟通。</p>
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
            <el-button type="warning" @click="handleAppealRefund(currentOrder)">
              申诉
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
    
    <!-- 申诉对话框 -->
    <el-dialog
      v-model="appealDialogVisible"
      title="申诉退款"
      width="500px"
      destroy-on-close
      @open="onAppealDialogOpen"
      @closed="onAppealDialogClosed"
    >
      <div class="appeal-form">
        <el-form ref="appealFormRef" :model="appealForm" label-width="80px">
          <el-form-item label="申诉理由" prop="reason" required>
            <el-input 
              v-model="appealForm.reason" 
              type="textarea" 
              rows="6" 
              placeholder="请详细描述您的申诉理由，以便管理员做出决定（必填）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="appealDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="confirmAppeal" :loading="operationLoading">
            提交申诉
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
  appealRefund,
  OrderDTO, 
  orderStatusMap 
} from '../../api/order'
import { startChatWithTeacher } from '../../api/courses'
import { useRouter } from 'vue-router'

// 退款状态映射
const refundStatusMap = {
  'REFUND_PENDING': '退款申请中',
  'REFUND_REJECTED': '退款已拒绝',
  'CANCELED': '已退款',
  'APPEALING': '申诉中',
  'APPEAL_APPROVED': '申诉通过',
  'APPEAL_REJECTED': '申诉驳回'
}

// 状态变量
const loading = ref(false)
const orders = ref<OrderDTO[]>([])
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const currentOrder = ref<OrderDTO | null>(null)
const refundDialogVisible = ref(false)
const appealDialogVisible = ref(false)
const operationLoading = ref(false)
const router = useRouter()

// 表单数据
const refundForm = ref({
  reason: ''
})
const appealForm = ref({
  reason: ''
})

// 当前操作的订单
const orderToProcess = ref<OrderDTO | null>(null)

// 计算过滤后的订单
const filteredOrders = computed(() => {
  if (!statusFilter.value) {
    return orders.value
  }
  return orders.value.filter(order => order.status === statusFilter.value)
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
    // 只筛选出退款相关的订单
    orders.value = data.filter((order: OrderDTO) => 
      order.status === 'REFUND_PENDING' || 
      order.status === 'REFUND_REJECTED' || 
      order.status === 'CANCELED' ||
      order.status === 'APPEALING' ||
      order.status === 'APPEAL_APPROVED' ||
      order.status === 'APPEAL_REJECTED'
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
    case 'APPEALING':
      return 'warning'
    case 'REFUND_REJECTED':
    case 'APPEAL_REJECTED':
      return 'danger'
    case 'CANCELED':
    case 'APPEAL_APPROVED':
      return 'success'
    default:
      return 'info'
  }
}

// 显示订单详情
const showOrderDetail = (order: OrderDTO) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

// 联系教师
const handleContactTeacher = (teacherId: number) => {
  startChatWithTeacher(teacherId).then(() => {
    router.push('/chat')
  }).catch(error => {
    console.error('开始聊天失败:', error)
    ElMessage.error('联系教师失败，请稍后再试')
  })
}

// 处理退款申请
const handleReapplyRefund = (order: OrderDTO) => {
  orderToProcess.value = order
  refundDialogVisible.value = true
}

// 处理申诉
const handleAppealRefund = (order: OrderDTO) => {
  orderToProcess.value = order
  appealDialogVisible.value = true
}

// 确认退款申请
const confirmRefundOrder = async () => {
  if (!orderToProcess.value) return
  if (!refundForm.value.reason.trim()) {
    ElMessage.warning('请输入退款理由')
    return
  }
  
  operationLoading.value = true
  try {
    await cancelOrder(orderToProcess.value.id, refundForm.value.reason)
    ElMessage.success('退款申请已提交')
    refundDialogVisible.value = false
    await loadRefundOrders()
    
    // 如果当前正在查看该订单的详情，则刷新详情
    if (detailDialogVisible.value && currentOrder.value?.id === orderToProcess.value.id) {
      const data = await getStudentOrders()
      const updatedOrder = data.find((o: OrderDTO) => o.id === orderToProcess.value?.id)
      if (updatedOrder) {
        currentOrder.value = updatedOrder
      }
    }
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('退款申请提交失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 确认申诉
const confirmAppeal = async () => {
  if (!orderToProcess.value) return
  if (!appealForm.value.reason.trim()) {
    ElMessage.warning('请输入申诉理由')
    return
  }
  
  operationLoading.value = true
  try {
    await appealRefund(orderToProcess.value.id, appealForm.value.reason)
    ElMessage.success('申诉已提交，请耐心等待处理')
    appealDialogVisible.value = false
    await loadRefundOrders()
    
    // 如果当前正在查看该订单的详情，则刷新详情
    if (detailDialogVisible.value && currentOrder.value?.id === orderToProcess.value.id) {
      const data = await getStudentOrders()
      const updatedOrder = data.find((o: OrderDTO) => o.id === orderToProcess.value?.id)
      if (updatedOrder) {
        currentOrder.value = updatedOrder
      }
    }
  } catch (error) {
    console.error('提交申诉失败:', error)
    ElMessage.error('申诉提交失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 退款对话框打开事件
const onRefundDialogOpen = () => {
  refundForm.value.reason = ''
}

// 退款对话框关闭事件
const onRefundDialogClosed = () => {
  refundForm.value.reason = ''
  operationLoading.value = false
}

// 申诉对话框打开事件
const onAppealDialogOpen = () => {
  appealForm.value.reason = ''
}

// 申诉对话框关闭事件
const onAppealDialogClosed = () => {
  appealForm.value.reason = ''
  operationLoading.value = false
}
</script>

<style scoped>
.after-sales-management {
  padding: 20px;
  height: 100%;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.order-container {
  min-height: 200px;
}

.order-number {
  font-family: monospace;
  color: #409EFF;
}

.amount {
  color: #F56C6C;
  font-weight: bold;
}

.operation-buttons {
  display: flex;
  gap: 5px;
}

/* 订单详情样式 */
.order-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-id {
  font-size: 16px;
  font-weight: bold;
}

.detail-content {
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
}

.label {
  width: 100px;
  color: #909399;
}

.value {
  flex: 1;
}

.price {
  color: #F56C6C;
  font-weight: bold;
}

.highlight-reason {
  color: #E6A23C;
  font-weight: bold;
}

.message, .refund-reason, .reject-reason, .appeal-reason, .teacher-response, .admin-decision {
  margin-top: 15px;
  border-top: 1px dashed #EBEEF5;
  padding-top: 10px;
}

.operation-tips {
  margin-top: 20px;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style> 