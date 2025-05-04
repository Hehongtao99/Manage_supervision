<template>
  <div class="companion-orders">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="filter-box">
            <el-select
              v-model="orderStatus"
              placeholder="订单状态"
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 空状态 -->
        <el-empty
          v-if="orders.length === 0 && !loading"
          description="暂无订单记录"
        />

        <!-- 订单列表 -->
        <div class="order-list" v-else>
          <el-card
            v-for="order in orders"
            :key="order.id"
            class="order-item"
            shadow="hover"
          >
            <div class="order-header">
              <div class="order-info">
                <div class="order-number">订单号: {{ order.orderNumber }}</div>
                <el-tag
                  :type="getStatusTagType(order.status)"
                  class="order-status"
                >
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-time">{{ formatDate(order.createTime) }}</div>
            </div>

            <div class="order-content">
              <div class="service-info">
                <div class="service-title">{{ order.serviceTitle }}</div>
                <div class="player-name">玩家: {{ order.playerName }}</div>
                <div class="game-type">游戏: {{ order.gameType }}</div>
                <div class="service-detail">
                  {{ order.hours }}小时 × ¥{{ order.price }}/小时
                </div>
                <div class="appointment-time" v-if="order.appointedTime">
                  预约时间: {{ formatDate(order.appointedTime) }}
                </div>
              </div>

              <div class="order-amount">
                <div class="payment-status">
                  <el-tag
                    :type="getPaymentStatusTagType(order.paymentStatus)"
                    size="small"
                  >
                    {{ getPaymentStatusText(order.paymentStatus) }}
                  </el-tag>
                </div>
                <div class="total-amount">¥{{ order.totalAmount }}</div>
              </div>
            </div>

            <div class="order-footer">
              <div class="order-remark" v-if="order.remark">
                备注: {{ order.remark }}
              </div>
              
              <div class="order-actions">
                <el-button
                  v-if="order.status === 'PENDING' && order.paymentStatus === 'PAID'"
                  type="primary"
                  size="small"
                  @click="handleAcceptOrder(order)"
                >
                  接单
                </el-button>
                <el-button
                  v-if="order.status === 'ACCEPTED'"
                  type="success"
                  size="small"
                  @click="handleCompleteOrder(order)"
                >
                  完成订单
                </el-button>
                <el-button
                  v-if="order.status === 'REFUND_PENDING'"
                  type="primary"
                  size="small"
                  @click="handleRefundRequest(order, true)"
                >
                  同意退款
                </el-button>
                <el-button
                  v-if="order.status === 'REFUND_PENDING'"
                  type="danger"
                  size="small"
                  @click="handleRefundRequest(order, false)"
                >
                  拒绝退款
                </el-button>
                <el-button
                  v-if="['PENDING', 'ACCEPTED'].includes(order.status)"
                  type="danger"
                  size="small"
                  @click="handleCancelOrder(order)"
                >
                  取消订单
                </el-button>
                <el-button
                  size="small"
                  @click="contactPlayer(order)"
                >
                  联系玩家
                </el-button>
                <el-button
                  size="small"
                  @click="viewOrderDetail(order)"
                >
                  查看详情
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
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
      width="50%"
    >
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ selectedOrder.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(selectedOrder.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusTagType(selectedOrder.status)">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="getPaymentStatusTagType(selectedOrder.paymentStatus)">
                {{ getPaymentStatusText(selectedOrder.paymentStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付时间" v-if="selectedOrder.paymentTime">
              {{ formatDate(selectedOrder.paymentTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3 class="section-title">服务信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="服务名称">{{ selectedOrder.serviceTitle }}</el-descriptions-item>
            <el-descriptions-item label="玩家名称">{{ selectedOrder.playerName }}</el-descriptions-item>
            <el-descriptions-item label="游戏类型">{{ selectedOrder.gameType }}</el-descriptions-item>
            <el-descriptions-item label="预约时间" v-if="selectedOrder.appointedTime">
              {{ formatDate(selectedOrder.appointedTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="购买时长">{{ selectedOrder.hours }}小时</el-descriptions-item>
            <el-descriptions-item label="单价">¥{{ selectedOrder.price }}/小时</el-descriptions-item>
            <el-descriptions-item label="总价">¥{{ selectedOrder.totalAmount }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section" v-if="selectedOrder.remark">
          <h3 class="section-title">备注信息</h3>
          <div class="remark-content">{{ selectedOrder.remark }}</div>
        </div>

        <!-- 订单历史记录 -->
        <div class="detail-section">
          <h3 class="section-title">订单历史记录</h3>
          <el-table :data="orderHistory" stripe style="width: 100%">
            <el-table-column prop="createTime" label="时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operatorType" label="操作者" width="120">
              <template #default="scope">
                {{ getOperatorTypeText(scope.row.operatorType) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          </el-table>
          <div v-if="orderHistory.length === 0" class="empty-history">
            暂无历史记录
          </div>
        </div>

        <div class="detail-actions">
          <el-button
            v-if="selectedOrder.status === 'PENDING' && selectedOrder.paymentStatus === 'PAID'"
            type="primary"
            @click="handleAcceptOrder(selectedOrder)"
          >
            接单
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'ACCEPTED'"
            type="success"
            @click="handleCompleteOrder(selectedOrder)"
          >
            完成订单
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'REFUND_PENDING'"
            type="primary"
            @click="handleRefundRequest(selectedOrder, true)"
          >
            同意退款
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'REFUND_PENDING'"
            type="danger"
            @click="handleRefundRequest(selectedOrder, false)"
          >
            拒绝退款
          </el-button>
          <el-button
            v-if="['PENDING', 'ACCEPTED'].includes(selectedOrder.status)"
            type="danger"
            @click="handleCancelOrder(selectedOrder)"
          >
            取消订单
          </el-button>
          <el-button @click="contactPlayer(selectedOrder)">联系玩家</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 确认对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmDialogTitle"
      width="30%"
    >
      <span>{{ confirmDialogMessage }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirm">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="30%"
    >
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancelOrder">确认取消</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退款处理对话框 -->
    <el-dialog
      v-model="refundResponseDialogVisible"
      :title="refundAction === 'approve' ? '同意退款' : '拒绝退款'"
      width="30%"
    >
      <el-form :model="refundResponseForm" label-width="100px">
        <el-form-item :label="refundAction === 'approve' ? '同意原因' : '拒绝原因'">
          <el-input
            v-model="refundResponseForm.response"
            type="textarea"
            :rows="3"
            :placeholder="refundAction === 'approve' ? '请输入同意退款的原因' : '请输入拒绝退款的原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundResponseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefundResponse">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  getCompanionOrders,
  getOrderById,
  acceptOrder,
  cancelOrder,
  completeOrder,
  handleRefundRequest,
  type Order
} from '../../api/order'
import { getOrderHistory } from '../../api/orderHistory'
import { formatDate } from '../../utils/date'

const router = useRouter()

// 状态
const loading = ref(false)
const orders = ref<Order[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const orderStatus = ref('')
const statusOptions = ref([
  { value: 'PENDING', label: '待接单' },
  { value: 'ACCEPTED', label: '已接单' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'REFUND_PENDING', label: '退款待审核' },
  { value: 'REFUNDED', label: '已退款' }
])

// 对话框状态
const detailDialogVisible = ref(false)
const selectedOrder = ref<Order | null>(null)
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmAction = ref<() => void>(() => {})
const cancelDialogVisible = ref(false)
const cancelForm = reactive({
  orderId: 0,
  reason: ''
})
const refundResponseDialogVisible = ref(false)
const refundResponseForm = reactive({
  orderId: 0,
  approved: true,
  response: ''
})
const refundAction = ref('approve')

// 订单历史记录
const orderHistory = ref([])

// 获取操作者类型文本
const getOperatorTypeText = (type: string | undefined) => {
  if (!type) return ''
  const map: { [key: string]: string } = {
    'PLAYER': '玩家',
    'COMPANION': '陪玩',
    'ADMIN': '管理员',
    'SYSTEM': '系统'
  }
  return map[type] || type
}

// 加载订单
const fetchOrders = async () => {
  loading.value = true
  try {
    const response = await getCompanionOrders(orderStatus.value, currentPage.value, pageSize.value)
    orders.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('获取订单失败:', error)
    ElMessage.error('获取订单失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 根据状态获取标签类型
const getStatusTagType = (status: string | undefined) => {
  if (!status) return ''
  const map: { [key: string]: string } = {
    'PENDING': 'info',
    'ACCEPTED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger',
    'REFUND_PENDING': 'warning',
    'REFUNDED': 'warning'
  }
  return map[status] || 'info'
}

// 根据状态获取文本
const getStatusText = (status: string | undefined) => {
  if (!status) return ''
  const map: { [key: string]: string } = {
    'PENDING': '待接单',
    'ACCEPTED': '已接单',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUND_PENDING': '退款待审核',
    'REFUNDED': '已退款'
  }
  return map[status] || status
}

// 获取支付状态标签类型
const getPaymentStatusTagType = (status: string | undefined) => {
  if (!status) return ''
  const map: { [key: string]: string } = {
    'UNPAID': 'info',
    'PAID': 'success',
    'REFUNDED': 'warning'
  }
  return map[status] || 'info'
}

// 获取支付状态文本
const getPaymentStatusText = (status: string | undefined) => {
  if (!status) return ''
  const map: { [key: string]: string } = {
    'UNPAID': '未支付',
    'PAID': '已支付',
    'REFUNDED': '已退款'
  }
  return map[status] || status
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchOrders()
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchOrders()
}

// 查看订单详情
const viewOrderDetail = (order: Order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
  
  // 加载订单历史记录
  loadOrderHistory(order.id!)
}

// 加载订单历史记录
const loadOrderHistory = async (orderId: number) => {
  try {
    const response = await getOrderHistory(orderId)
    orderHistory.value = response
  } catch (error) {
    console.error('获取订单历史失败:', error)
    ElMessage.error('获取订单历史失败')
  }
}

// 接受订单
const handleAcceptOrder = (order: Order) => {
  confirmDialogTitle.value = '确认接单'
  confirmDialogMessage.value = `确定要接受该订单吗？`
  confirmAction.value = async () => {
    try {
      await acceptOrder(order.id!)
      ElMessage.success('接单成功')
      fetchOrders()
      confirmDialogVisible.value = false
      if (detailDialogVisible.value) {
        detailDialogVisible.value = false
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data || '接单失败，请重试')
    }
  }
  confirmDialogVisible.value = true
}

// 完成订单
const handleCompleteOrder = (order: Order) => {
  confirmDialogTitle.value = '确认完成'
  confirmDialogMessage.value = '确认该订单服务已完成吗？'
  confirmAction.value = async () => {
    try {
      await completeOrder(order.id!)
      ElMessage.success('订单已标记为完成')
      fetchOrders()
      confirmDialogVisible.value = false
      if (detailDialogVisible.value) {
        detailDialogVisible.value = false
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data || '操作失败，请重试')
    }
  }
  confirmDialogVisible.value = true
}

// 取消订单
const handleCancelOrder = (order: Order) => {
  cancelForm.orderId = order.id!
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancelOrder = async () => {
  try {
    await cancelOrder(cancelForm.orderId, cancelForm.reason)
    ElMessage.success('订单已取消')
    fetchOrders()
    cancelDialogVisible.value = false
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data || '取消订单失败，请重试')
  }
}

// 处理确认
const handleConfirm = () => {
  confirmAction.value()
}

// 联系玩家
const contactPlayer = (order: Order) => {
  if (!order.playerId) {
    ElMessage.warning('无法获取玩家信息')
    return
  }
  
  router.push({
    path: '/companion/chat',
    query: { playerId: order.playerId.toString() }
  })
}

// 处理退款申请
const handleRefundRequest = (order: Order, approve: boolean) => {
  refundResponseForm.orderId = order.id!
  refundResponseForm.response = ''
  refundResponseForm.approved = approve
  refundAction.value = approve ? 'approve' : 'reject'
  refundResponseDialogVisible.value = true
}

// 确认处理退款申请
const confirmRefundResponse = async () => {
  try {
    await handleRefundRequest(
      refundResponseForm.orderId, 
      refundResponseForm.approved, 
      refundResponseForm.response
    )
    ElMessage.success(refundResponseForm.approved ? '已同意退款' : '已拒绝退款')
    fetchOrders()
    refundResponseDialogVisible.value = false
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data || '处理退款申请失败，请重试')
  }
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.companion-orders {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-box {
  display: flex;
  gap: 10px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  margin-bottom: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-number {
  font-weight: 500;
  color: #606266;
}

.order-time {
  color: #909399;
  font-size: 13px;
}

.order-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.service-title {
  font-weight: 500;
  font-size: 16px;
}

.player-name,
.game-type,
.service-detail,
.appointment-time {
  font-size: 14px;
  color: #606266;
}

.order-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #ff6b6b;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.order-remark {
  font-size: 13px;
  color: #909399;
  max-width: 60%;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 订单详情样式 */
.order-detail {
  padding: 10px;
}

.detail-section {
  margin-bottom: 25px;
}

.section-title {
  margin-bottom: 15px;
  font-weight: 500;
  font-size: 16px;
}

.remark-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  color: #606266;
}

.empty-history {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.detail-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 