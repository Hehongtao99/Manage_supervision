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
            
            <el-table-column prop="studentName" label="学生" width="120" />
            
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
            
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <div class="operation-buttons">
                  <el-button
                    v-if="scope.row.status === 'REFUND_PENDING'"
                    type="success"
                    size="small"
                    @click.stop="handleApproveRefund(scope.row)"
                  >
                    同意退款
                  </el-button>
                  <el-button
                    v-if="scope.row.status === 'REFUND_PENDING'"
                    type="danger"
                    size="small"
                    @click.stop="handleRejectRefund(scope.row)"
                  >
                    拒绝退款
                  </el-button>
                  <el-button
                    v-if="scope.row.status === 'APPEALING' && !scope.row.teacherResponse"
                    type="warning"
                    size="small"
                    @click.stop="handleRespondToAppeal(scope.row)"
                  >
                    回复申诉
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
                    @click.stop="handleContactStudent(scope.row.studentId)"
                  >
                    联系学生
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
            <span class="label">学生：</span>
            <span class="value">{{ currentOrder.studentName }}</span>
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
        </div>
        
        <div class="detail-footer">
          <template v-if="currentOrder.status === 'REFUND_PENDING'">
            <el-button type="success" @click="handleApproveRefund(currentOrder)">
              同意退款
            </el-button>
            <el-button type="danger" @click="handleRejectRefund(currentOrder)">
              拒绝退款
            </el-button>
          </template>
          
          <template v-if="currentOrder.status === 'APPEALING' && !currentOrder.teacherResponse">
            <el-button type="warning" @click="handleRespondToAppeal(currentOrder)">
              回复申诉
            </el-button>
          </template>
          
          <el-button 
            type="info" 
            @click="handleContactStudent(currentOrder.studentId)"
          >
            联系学生
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 确认对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmDialogTitle"
      width="400px"
      destroy-on-close
    >
      <div class="confirm-content">
        <p>{{ confirmDialogMessage }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button :type="confirmDialogType" @click="handleConfirmOperation" :loading="operationLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 拒绝退款对话框 -->
    <el-dialog
      v-model="rejectRefundDialogVisible"
      title="拒绝退款"
      width="500px"
      destroy-on-close
      @open="onRejectDialogOpen"
      @closed="onRejectDialogClosed"
    >
      <div class="reject-form">
        <el-form ref="rejectFormRef" :model="rejectRefundForm" label-width="80px">
          <el-form-item label="拒绝理由" prop="reason" required>
            <el-input 
              v-model="rejectRefundForm.reason" 
              type="textarea" 
              rows="4" 
              placeholder="请输入拒绝退款的理由"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectRefundDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmRejectRefund" :loading="operationLoading">
            确定拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 回复申诉对话框 -->
    <el-dialog
      v-model="respondAppealDialogVisible"
      title="回复申诉"
      width="500px"
      destroy-on-close
      @open="onRespondAppealDialogOpen"
      @closed="onRespondAppealDialogClosed"
    >
      <div class="respond-form">
        <div v-if="currentOrder" class="appeal-reason-display">
          <h4>学生申诉理由：</h4>
          <p>{{ currentOrder.appealReason }}</p>
        </div>
        <el-divider />
        <el-form ref="respondAppealFormRef" :model="respondAppealForm" label-width="80px">
          <el-form-item label="回复内容" prop="response" required>
            <el-input 
              v-model="respondAppealForm.response" 
              type="textarea" 
              rows="6" 
              placeholder="请输入对学生申诉的回复内容，该内容将提交给管理员作为裁决参考"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="respondAppealDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="confirmRespondToAppeal" :loading="operationLoading">
            提交回复
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Service } from '@element-plus/icons-vue'
import { 
  getTeacherOrders, 
  approveRefund,
  rejectRefund,
  respondToAppeal,
  OrderDTO, 
  orderStatusMap 
} from '../../api/order'
import { startChatWithStudent } from '../../api/courses'
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
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmDialogType = ref('primary')
const confirmOperation = ref('')
const rejectRefundDialogVisible = ref(false)
const respondAppealDialogVisible = ref(false)
const operationLoading = ref(false)
const router = useRouter()

// 表单数据
const rejectRefundForm = ref({
  reason: ''
})
const respondAppealForm = ref({
  response: ''
})

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
    const data = await getTeacherOrders()
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
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败，请稍后再试')
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

// 处理同意退款
const handleApproveRefund = (order: OrderDTO) => {
  currentOrder.value = order
  confirmDialogTitle.value = '同意退款'
  confirmDialogMessage.value = `您确定要同意此订单的退款申请吗？\n订单号：${order.orderNumber}\n学生：${order.studentName}\n金额：¥${order.totalAmount}`
  confirmDialogType.value = 'success'
  confirmOperation.value = 'approve'
  confirmDialogVisible.value = true
}

// 处理拒绝退款
const handleRejectRefund = (order: OrderDTO) => {
  currentOrder.value = order
  rejectRefundDialogVisible.value = true
}

// 处理回复申诉
const handleRespondToAppeal = (order: OrderDTO) => {
  currentOrder.value = order
  respondAppealDialogVisible.value = true
}

// 确认操作
const handleConfirmOperation = async () => {
  if (!currentOrder.value) return
  
  operationLoading.value = true
  try {
    if (confirmOperation.value === 'approve') {
      const result = await approveRefund(currentOrder.value.id)
      ElMessage.success('已同意退款申请')
    }
    
    confirmDialogVisible.value = false
    detailDialogVisible.value = false
    await loadRefundOrders()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 确认拒绝退款
const confirmRejectRefund = async () => {
  if (!currentOrder.value) return
  
  if (!rejectRefundForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  
  operationLoading.value = true
  try {
    await rejectRefund(currentOrder.value.id, rejectRefundForm.value.reason)
    ElMessage.success('已拒绝退款申请')
    rejectRefundDialogVisible.value = false
    detailDialogVisible.value = false
    await loadRefundOrders()
  } catch (error) {
    console.error('拒绝退款失败:', error)
    ElMessage.error('拒绝退款失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 确认回复申诉
const confirmRespondToAppeal = async () => {
  if (!currentOrder.value) return
  
  if (!respondAppealForm.value.response.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  operationLoading.value = true
  try {
    await respondToAppeal(currentOrder.value.id, respondAppealForm.value.response)
    ElMessage.success('已提交申诉回复，等待管理员处理')
    respondAppealDialogVisible.value = false
    detailDialogVisible.value = false
    await loadRefundOrders()
  } catch (error) {
    console.error('回复申诉失败:', error)
    ElMessage.error('回复申诉失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 联系学生
const handleContactStudent = (studentId: number) => {
  startChatWithStudent(studentId).then(() => {
    router.push('/chat')
  }).catch(error => {
    console.error('开始聊天失败:', error)
    ElMessage.error('联系学生失败，请稍后再试')
  })
}

// 拒绝对话框打开事件
const onRejectDialogOpen = () => {
  rejectRefundForm.value.reason = ''
}

// 拒绝对话框关闭事件
const onRejectDialogClosed = () => {
  rejectRefundForm.value.reason = ''
  operationLoading.value = false
}

// 回复申诉对话框打开事件
const onRespondAppealDialogOpen = () => {
  respondAppealForm.value.response = ''
}

// 回复申诉对话框关闭事件
const onRespondAppealDialogClosed = () => {
  respondAppealForm.value.response = ''
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

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.appeal-reason-display {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.appeal-reason-display h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #606266;
}

.appeal-reason-display p {
  margin: 0;
  white-space: pre-wrap;
  color: #E6A23C;
}
</style> 