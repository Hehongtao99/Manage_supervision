<template>
  <div class="order-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <div class="filter-container">
            <el-select v-model="statusFilter" placeholder="订单状态" clearable @change="filterOrders">
              <el-option label="全部状态" value="" />
              <el-option v-for="(value, key) in orderStatusMap" :key="key" :label="value" :value="key" />
            </el-select>
            <el-button type="primary" @click="loadOrders">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="order-container" v-loading="loading">
        <el-empty v-if="filteredOrders.length === 0" description="暂无订单记录" />
        
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
            
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 'PENDING' || scope.row.status === 'REFUND_REJECTED'"
                  type="warning"
                  size="small"
                  @click.stop="handleCancelOrder(scope.row)"
                >
                  申请退款
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
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
            <span class="value">{{ currentOrder.refundReason }}</span>
          </div>
          
          <div v-if="currentOrder.rejectReason" class="detail-item reject-reason">
            <span class="label">拒绝理由：</span>
            <span class="value">{{ currentOrder.rejectReason }}</span>
          </div>
        </div>
        
        <div class="detail-footer">
          <el-button 
            v-if="currentOrder.status === 'PENDING' || currentOrder.status === 'REFUND_REJECTED'"
            type="warning" 
            @click="handleCancelOrder(currentOrder)"
          >
            申请退款
          </el-button>
          <el-button 
            type="primary" 
            @click="handleContactTeacher(currentOrder.teacherId)"
          >
            联系教师
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 申请退款确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="申请退款"
      width="400px"
      destroy-on-close
      @closed="handleCancelDialogClosed"
    >
      <div class="refund-form">
        <el-form label-width="80px">
          <el-form-item label="退款理由" required>
            <el-input 
              v-model="refundReason" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入退款理由（必填）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">返回</el-button>
          <el-button type="warning" @click="confirmCancelOrder" :loading="cancelLoading">
            确认申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, shallowRef } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getStudentOrders, cancelOrder, OrderDTO, orderStatusMap } from '../../api/order'
import { startChatWithTeacher } from '../../api/courses'
import { useRouter } from 'vue-router'

// 状态变量
const loading = ref(false)
const orders = ref<OrderDTO[]>([])
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const currentOrder = ref<OrderDTO | null>(null)
const cancelDialogVisible = ref(false)
const orderToCancel = ref<OrderDTO | null>(null)
const cancelLoading = ref(false)
// 使用普通的ref而不是shallowRef
const refundReason = ref('')
const router = useRouter()

// 计算过滤后的订单
const filteredOrders = computed(() => {
  if (!statusFilter.value) {
    return orders.value
  }
  return orders.value.filter(order => order.status === statusFilter.value)
})

// 初始化加载
onMounted(() => {
  loadOrders()
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const data = await getStudentOrders()
    orders.value = data
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
    case 'PENDING':
      return 'success'
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

// 申请退款
const handleCancelOrder = (order: OrderDTO) => {
  try {
    // 设置当前要取消的订单
    orderToCancel.value = order
    
    // 完全重置refundReason，创建一个新对象
    refundReason.value = ''
    
    // 确保上述状态更新完成后再打开对话框
    nextTick(() => {
      // 重置可能的加载状态
      cancelLoading.value = false
      // 打开对话框
      cancelDialogVisible.value = true
    })
  } catch (error) {
    console.error('打开退款申请对话框失败:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 确认申请退款
const confirmCancelOrder = async () => {
  try {
    // 验证订单存在
    if (!orderToCancel.value) {
      ElMessage.warning('未选择订单')
      return
    }
    
    // 验证退款理由是否填写
    if (!refundReason.value || !refundReason.value.trim()) {
      ElMessage.warning('请输入退款理由')
      return
    }

    // 设置加载状态
    cancelLoading.value = true
    
    // 提交退款申请
    const result = await cancelOrder(orderToCancel.value.id, refundReason.value)
    
    if (result.success) {
      ElMessage.success(result.message || '退款申请已提交')
      
      // 更新订单状态
      if (orderToCancel.value) {
        const index = orders.value.findIndex(o => o.id === orderToCancel.value?.id)
        if (index !== -1) {
          orders.value[index].status = 'REFUND_PENDING'
          // 更新状态文本
          orders.value[index].statusText = '退款申请中'
          // 在前端保存退款理由
          orders.value[index].refundReason = refundReason.value
        }
      }
      
      // 关闭对话框
      cancelDialogVisible.value = false
      // 如果从详情页打开，也关闭详情对话框
      if (detailDialogVisible.value) {
        detailDialogVisible.value = false
      }
    } else {
      ElMessage.error(result.message || '退款申请失败')
    }
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('退款申请失败，请稍后再试')
  } finally {
    cancelLoading.value = false
  }
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

// 处理取消对话框关闭
const handleCancelDialogClosed = () => {
  // 重置相关状态
  cancelLoading.value = false
  // 完全重新创建一个对象，避免引用问题
  refundReason.value = ''
}
</script>

<style scoped>
.order-list {
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

.message, .reject-reason {
  border-left: 3px solid #e6a23c;
  padding-left: 10px;
  margin-top: 20px;
}

.reject-reason {
  border-left-color: #f56c6c;
}

.detail-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.refund-form {
  padding: 20px;
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
  
  .detail-item {
    flex-direction: column;
  }
  
  .detail-item .label {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style> 