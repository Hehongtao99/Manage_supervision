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
const rejectRefundDialogVisible = ref(false)
const rejectFormRef = ref()
const rejectRefundForm = ref({
  reason: ''
})
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmDialogType = ref('primary')
const operationLoading = ref(false)
const currentOperation = ref('')
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
    const data = await getTeacherOrders()
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

// 联系学生
const handleContactStudent = async (studentId: number) => {
  if (!studentId) {
    ElMessage.error('学生ID无效，无法开始聊天')
    return
  }

  try {
    ElMessage({
      message: '正在连接聊天...',
      type: 'info',
      duration: 1500,
      showClose: false
    })

    // 注意：这里需要传递正确的参数。startChatWithTeacher接受一个用户ID，对教师来说，这里传递学生ID
    const response = await startChatWithTeacher(studentId)
    
    if (!response || !response.id) {
      ElMessage.closeAll()
      ElMessage.error('创建聊天会话失败')
      return
    }

    // 关闭当前对话框
    detailDialogVisible.value = false
    
    // 重定向到聊天页面
    router.push({ 
      path: '/supervisor/chat',
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

// 确认操作
const handleConfirmOperation = async () => {
  if (!currentOrder.value) return
  
  operationLoading.value = true
  try {
    let result;
    
    if (currentOperation.value === 'approve-refund') {
      result = await approveRefund(currentOrder.value.id)
      if (result && result.success) {
        // 更新订单状态为已取消（已退款）
        updateOrderStatus(currentOrder.value.id, 'CANCELED')
        ElMessage.success(result.message || '已同意退款申请')
        
        // 关闭对话框
        confirmDialogVisible.value = false
        detailDialogVisible.value = false
        
        // 刷新数据
        await loadRefundOrders()
      } else {
        ElMessage.error((result && result.message) || '同意退款失败')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请稍后再试')
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

// 处理同意退款
const handleApproveRefund = (order: OrderDTO) => {
  currentOrder.value = order
  confirmDialogTitle.value = '同意退款'
  confirmDialogMessage.value = '确定要同意此退款申请吗？同意后将无法撤销。'
  confirmDialogType.value = 'success'
  currentOperation.value = 'approve-refund'
  confirmDialogVisible.value = true
}

// 处理拒绝退款
const handleRejectRefund = (order: OrderDTO) => {
  currentOrder.value = order
  rejectRefundDialogVisible.value = true
}

// 确认拒绝退款
const confirmRejectRefund = async () => {
  if (!currentOrder.value) return
  
  if (!rejectRefundForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝退款的理由')
    return
  }
  
  operationLoading.value = true
  try {
    const result = await rejectRefund(currentOrder.value.id, rejectRefundForm.value.reason)
    
    if (result.success) {
      ElMessage.success(result.message || '已拒绝退款申请')
      
      // 更新订单状态
      updateOrderStatus(currentOrder.value.id, 'REFUND_REJECTED')
      
      // 关闭对话框
      rejectRefundDialogVisible.value = false
      detailDialogVisible.value = false
      
      // 刷新数据
      await loadRefundOrders()
    } else {
      ElMessage.error(result.message || '拒绝退款失败')
    }
  } catch (error) {
    console.error('拒绝退款失败:', error)
    ElMessage.error('拒绝退款失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 拒绝退款对话框打开事件
const onRejectDialogOpen = () => {
  // 确保表单内容被清空
  rejectRefundForm.value = {
    reason: ''
  }
  // 使用nextTick等待DOM更新后重置表单
  nextTick(() => {
    if (rejectFormRef.value) {
      rejectFormRef.value.resetFields()
    }
  })
}

// 拒绝退款对话框关闭事件
const onRejectDialogClosed = () => {
  // 清空表单数据
  rejectRefundForm.value = {
    reason: ''
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

.detail-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.reject-form {
  padding: 20px 0;
}

.confirm-content {
  text-align: center;
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