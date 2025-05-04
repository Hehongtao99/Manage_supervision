<template>
  <div class="player-orders">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
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
                <div class="companion-name">陪玩: {{ order.companionName }}</div>
                <div class="game-type">游戏: {{ order.gameType }}</div>
                <div class="service-detail">
                  {{ order.hours }}小时 × ¥{{ order.price }}/小时
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
                  v-if="order.status === 'PENDING' && order.paymentStatus === 'UNPAID'"
                  type="primary"
                  size="small"
                  @click="handlePayOrder(order)"
                >
                  支付
                </el-button>
                <el-button
                  v-if="order.status === 'ACCEPTED'"
                  type="success"
                  size="small"
                  @click="handleCompleteOrder(order)"
                >
                  确认完成
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
                  v-if="order.status === 'COMPLETED' && order.paymentStatus === 'PAID'"
                  type="warning"
                  size="small"
                  @click="handleRefundOrder(order)"
                >
                  申请退款
                </el-button>
                <el-button
                  v-if="order.status === 'REFUND_PENDING'"
                  type="info"
                  size="small"
                  disabled
                >
                  退款审核中
                </el-button>
                <el-button
                  v-if="order.status === 'COMPLETED' && order.paymentStatus === 'PAID'"
                  type="success"
                  size="small"
                  @click="handleReviewOrder(order)"
                >
                  评价服务
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
            <el-descriptions-item label="陪玩名称">{{ selectedOrder.companionName }}</el-descriptions-item>
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
            v-if="selectedOrder.status === 'PENDING' && selectedOrder.paymentStatus === 'UNPAID'"
            type="primary"
            @click="handlePayOrder(selectedOrder)"
          >
            支付订单
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'ACCEPTED'"
            type="success"
            @click="handleCompleteOrder(selectedOrder)"
          >
            确认完成
          </el-button>
          <el-button
            v-if="['PENDING', 'ACCEPTED'].includes(selectedOrder.status)"
            type="danger"
            @click="handleCancelOrder(selectedOrder)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'COMPLETED' && selectedOrder.paymentStatus === 'PAID'"
            type="warning"
            @click="handleRefundOrder(selectedOrder)"
          >
            申请退款
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'REFUND_PENDING'"
            type="info"
            disabled
          >
            退款审核中
          </el-button>
          <el-button
            v-if="selectedOrder.status === 'COMPLETED' && selectedOrder.paymentStatus === 'PAID'"
            type="success"
            @click="handleReviewOrder(selectedOrder)"
          >
            评价服务
          </el-button>
          <el-button @click="contactCompanion(selectedOrder)">联系陪玩</el-button>
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

    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="30%"
    >
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefundOrder">确认申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="评价服务"
      width="40%"
    >
      <div class="order-info-summary" v-if="selectedOrder">
        <div class="service-title">{{ selectedOrder.serviceTitle }}</div>
        <div class="companion-name">陪玩: {{ selectedOrder.companionName }}</div>
      </div>
      
      <el-form :model="reviewForm" label-width="100px" class="review-form">
        <el-form-item label="评分">
          <el-rate
            v-model="reviewForm.rating"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            :texts="['很差', '较差', '一般', '较好', '很好']"
            show-text
          />
        </el-form-item>
        
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您对本次服务的评价..."
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="reviewForm.anonymous">匿名评价</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交评价</el-button>
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
  getPlayerOrders,
  cancelOrder,
  completeOrder,
  payOrder,
  refundOrder,
  type Order
} from '../../api/order'
import { createReview, canReviewOrder } from '../../api/review'
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
const refundDialogVisible = ref(false)
const refundForm = reactive({
  orderId: 0,
  reason: ''
})

const reviewDialogVisible = ref(false)
const reviewForm = reactive({
  orderId: 0,
  rating: 5,
  content: '',
  anonymous: false
})

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
    const response = await getPlayerOrders(orderStatus.value, currentPage.value, pageSize.value)
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

// 支付订单
const handlePayOrder = (order: Order) => {
  confirmDialogTitle.value = '确认支付'
  confirmDialogMessage.value = `确定要支付订单 ${order.orderNumber} 吗？总金额：¥${order.totalAmount}`
  confirmAction.value = async () => {
    try {
      await payOrder(order.id!)
      ElMessage.success('支付成功')
      fetchOrders()
      confirmDialogVisible.value = false
      if (detailDialogVisible.value) {
        detailDialogVisible.value = false
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data || '支付失败，请重试')
    }
  }
  confirmDialogVisible.value = true
}

// 确认完成订单
const handleCompleteOrder = (order: Order) => {
  confirmDialogTitle.value = '确认完成'
  confirmDialogMessage.value = '确认服务已完成吗？'
  confirmAction.value = async () => {
    try {
      await completeOrder(order.id!)
      ElMessage.success('订单已完成')
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

// 申请退款
const handleRefundOrder = (order: Order) => {
  refundForm.orderId = order.id!
  refundForm.reason = ''
  refundDialogVisible.value = true
}

// 确认申请退款
const confirmRefundOrder = async () => {
  try {
    await refundOrder(refundForm.orderId, refundForm.reason)
    ElMessage.success('退款申请已提交')
    fetchOrders()
    refundDialogVisible.value = false
    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data || '申请退款失败，请重试')
  }
}

// 处理确认
const handleConfirm = () => {
  confirmAction.value()
}

// 联系陪玩
const contactCompanion = (order: Order) => {
  if (!order.companionId) {
    ElMessage.warning('无法获取陪玩信息')
    return
  }
  
  router.push({
    path: '/chat',
    query: { companionId: order.companionId.toString() }
  })
}

// 申请评价
const handleReviewOrder = async (order: Order) => {
  try {
    const response = await canReviewOrder(order.id!)
    if (response.canReview) {
      reviewForm.orderId = order.id!
      reviewForm.rating = 5
      reviewForm.content = ''
      reviewForm.anonymous = false
      reviewDialogVisible.value = true
    } else {
      ElMessage.warning('该订单不满足评价条件')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data || '操作失败，请重试')
  }
}

// 提交评价
const submitReview = async () => {
  if (reviewForm.rating === 0) {
    ElMessage.warning('请至少给出一颗星的评分')
    return
  }

  try {
    await createReview({
      orderId: reviewForm.orderId,
      rating: reviewForm.rating,
      content: reviewForm.content,
      anonymous: reviewForm.anonymous
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    fetchOrders()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '评价提交失败，请重试')
  }
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.player-orders {
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

.companion-name,
.game-type,
.service-detail {
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
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

/* 评价表单样式 */
.order-info-summary {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-form {
  margin-top: 10px;
}

.review-form .el-rate {
  margin-top: 5px;
}
</style> 