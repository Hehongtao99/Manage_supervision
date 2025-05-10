<!-- 管理员申诉处理页面 -->
<template>
  <div class="appeal-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>退款申诉管理</span>
          <el-button type="primary" @click="refreshCurrentTab">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待处理申诉" name="pending">
          <div class="order-container" v-loading="loading">
            <el-empty v-if="orders.length === 0" description="暂无待处理的申诉" />
            
            <div v-else class="order-list-content">
              <el-table
                :data="orders"
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
                
                <el-table-column prop="appealTime" label="申诉时间" width="180" />
                
                <el-table-column label="操作" width="220">
                  <template #default="scope">
                    <div class="operation-buttons">
                      <el-button
                        v-if="scope.row.status === 'APPEALING' && scope.row.teacherResponse"
                        type="success"
                        size="small"
                        @click.stop="handleApproveAppeal(scope.row)"
                      >
                        批准申诉
                      </el-button>
                      <el-button
                        v-if="scope.row.status === 'APPEALING' && scope.row.teacherResponse"
                        type="danger"
                        size="small"
                        @click.stop="handleRejectAppeal(scope.row)"
                      >
                        驳回申诉
                      </el-button>
                      <el-button
                        type="primary"
                        size="small"
                        @click.stop="showOrderDetail(scope.row)"
                      >
                        查看详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页控件 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="total"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="历史申诉记录" name="history">
          <div class="order-container" v-loading="loadingHistory">
            <el-empty v-if="historyOrders.length === 0" description="暂无历史申诉记录" />
            
            <div v-else class="order-list-content">
              <el-table
                :data="historyOrders"
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
                
                <el-table-column prop="appealTime" label="申诉时间" width="180" />
                
                <el-table-column prop="updateTime" label="处理时间" width="180" />
                
                <el-table-column label="处理结果" width="120">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'APPEAL_APPROVED' ? 'success' : 'danger'"
                    >
                      {{ scope.row.status === 'APPEAL_APPROVED' ? '已批准' : '已驳回' }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <div class="operation-buttons">
                      <el-button
                        type="primary"
                        size="small"
                        @click.stop="showOrderDetail(scope.row)"
                      >
                        查看详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 历史申诉分页控件 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="historyCurrentPage"
                  v-model:page-size="historyPageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="historyTotal"
                  @size-change="handleHistorySizeChange"
                  @current-change="handleHistoryCurrentChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申诉详情"
      width="800px"
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
        
        <el-descriptions title="订单基本信息" :column="2" border>
          <el-descriptions-item label="课程名称">{{ currentOrder.courseTitle }}</el-descriptions-item>
          <el-descriptions-item label="科目">{{ currentOrder.courseSubject }}</el-descriptions-item>
          <el-descriptions-item label="学生">{{ currentOrder.studentName }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ currentOrder.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="课时单价">¥{{ currentOrder.price }}/小时</el-descriptions-item>
          <el-descriptions-item label="购买小时数">{{ currentOrder.hours }} 小时</el-descriptions-item>
          <el-descriptions-item label="总金额" :span="2">
            <span class="price">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <el-timeline>
          <el-timeline-item 
            v-if="currentOrder.refundReason" 
            timestamp="学生申请退款" 
            type="primary"
          >
            <el-card class="timeline-card">
              <h4>退款理由：</h4>
              <p>{{ currentOrder.refundReason }}</p>
            </el-card>
          </el-timeline-item>
          
          <el-timeline-item 
            v-if="currentOrder.rejectReason"
            timestamp="教师拒绝退款" 
            type="danger"
          >
            <el-card class="timeline-card">
              <h4>拒绝理由：</h4>
              <p>{{ currentOrder.rejectReason }}</p>
            </el-card>
          </el-timeline-item>
          
          <el-timeline-item 
            v-if="currentOrder.appealReason"
            timestamp="学生申诉退款" 
            type="warning"
          >
            <el-card class="timeline-card">
              <h4>申诉理由：</h4>
              <p>{{ currentOrder.appealReason }}</p>
              <p class="timestamp">申诉时间: {{ currentOrder.appealTime }}</p>
            </el-card>
          </el-timeline-item>
          
          <el-timeline-item 
            v-if="currentOrder.teacherResponse"
            timestamp="教师回复申诉" 
            type="info"
          >
            <el-card class="timeline-card">
              <h4>教师回复：</h4>
              <p>{{ currentOrder.teacherResponse }}</p>
            </el-card>
          </el-timeline-item>
          
          <el-timeline-item 
            v-if="currentOrder.adminDecision"
            :timestamp="currentOrder.status === 'APPEAL_APPROVED' ? '管理员批准申诉' : '管理员驳回申诉'" 
            :type="currentOrder.status === 'APPEAL_APPROVED' ? 'success' : 'danger'"
          >
            <el-card class="timeline-card">
              <h4>管理员决定：</h4>
              <p>{{ currentOrder.adminDecision }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <el-divider />
        
        <div class="detail-footer">
          <div v-if="currentOrder.status === 'APPEALING' && currentOrder.teacherResponse" class="admin-actions">
            <h3>管理员决策</h3>
            <p class="action-hint">请根据学生申诉和教师回复做出决定：</p>
            <div class="action-buttons">
              <el-button type="success" @click="handleApproveAppeal(currentOrder)">
                批准申诉，同意退款
              </el-button>
              <el-button type="danger" @click="handleRejectAppeal(currentOrder)">
                驳回申诉，拒绝退款
              </el-button>
            </div>
          </div>
          
          <div v-else-if="currentOrder.status === 'APPEALING' && !currentOrder.teacherResponse" class="waiting-notice">
            <el-alert
              title="等待教师回复"
              type="warning"
              :closable="false"
              show-icon
            >
              <p>此申诉尚未得到教师回复，请等待教师回复后再做决定。</p>
            </el-alert>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批准申诉对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="批准申诉"
      width="500px"
      destroy-on-close
      @open="onApproveDialogOpen"
      @closed="onApproveDialogClosed"
    >
      <div class="approve-form">
        <p class="confirm-message">
          您确定要批准此申诉并同意退款吗？
        </p>
        <el-form ref="approveFormRef" :model="approveForm" label-width="100px">
          <el-form-item label="决定理由" prop="decision">
            <el-input 
              v-model="approveForm.decision" 
              type="textarea" 
              rows="4" 
              placeholder="请输入批准理由（可选）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="success" @click="confirmApproveAppeal" :loading="operationLoading">
            确认批准
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 驳回申诉对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回申诉"
      width="500px"
      destroy-on-close
      @open="onRejectDialogOpen"
      @closed="onRejectDialogClosed"
    >
      <div class="reject-form">
        <p class="confirm-message">
          您确定要驳回此申诉并拒绝退款吗？
        </p>
        <el-form ref="rejectFormRef" :model="rejectForm" label-width="100px">
          <el-form-item label="决定理由" prop="decision" required>
            <el-input 
              v-model="rejectForm.decision" 
              type="textarea" 
              rows="4" 
              placeholder="请输入驳回理由（必填）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmRejectAppeal" :loading="operationLoading">
            确认驳回
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getAppealingOrdersPaged, 
  approveAppeal,
  rejectAppeal,
  OrderDTO, 
  orderStatusMap,
  getProcessedAppealsPaged
} from '../../api/order'

// 状态变量
const loading = ref(false)
const loadingHistory = ref(false)
const activeTab = ref('pending')
const orders = ref<OrderDTO[]>([])
const historyOrders = ref<OrderDTO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const historyCurrentPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)
const detailDialogVisible = ref(false)
const currentOrder = ref<OrderDTO | null>(null)
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const operationLoading = ref(false)

// 表单数据
const approveForm = ref({
  decision: '管理员同意退款'
})
const rejectForm = ref({
  decision: ''
})

// 初始化加载
onMounted(() => {
  loadAppealingOrders()
})

// Tab点击处理
const handleTabClick = (tab) => {
  if (tab.props.name === 'pending') {
    loadAppealingOrders()
  } else if (tab.props.name === 'history') {
    loadHistoryAppeals()
  }
}

// 刷新当前Tab
const refreshCurrentTab = () => {
  if (activeTab.value === 'pending') {
    loadAppealingOrders()
  } else if (activeTab.value === 'history') {
    loadHistoryAppeals()
  }
}

// 加载申诉中的订单
const loadAppealingOrders = async () => {
  loading.value = true
  try {
    const data = await getAppealingOrdersPaged(currentPage.value, pageSize.value)
    orders.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取申诉订单列表失败:', error)
    ElMessage.error('获取申诉订单列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 加载历史申诉记录
const loadHistoryAppeals = async () => {
  loadingHistory.value = true
  try {
    const data = await getProcessedAppealsPaged(historyCurrentPage.value, historyPageSize.value)
    historyOrders.value = data.records
    historyTotal.value = data.total
  } catch (error) {
    console.error('获取历史申诉记录失败:', error)
    ElMessage.error('获取历史申诉记录失败，请稍后再试')
  } finally {
    loadingHistory.value = false
  }
}

// 根据状态获取标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'APPEALING':
      return 'warning'
    case 'APPEAL_REJECTED':
      return 'danger'
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

// 处理批准申诉
const handleApproveAppeal = (order: OrderDTO) => {
  currentOrder.value = order
  approveDialogVisible.value = true
}

// 处理驳回申诉
const handleRejectAppeal = (order: OrderDTO) => {
  currentOrder.value = order
  rejectDialogVisible.value = true
}

// 确认批准申诉
const confirmApproveAppeal = async () => {
  if (!currentOrder.value) return
  
  operationLoading.value = true
  try {
    await approveAppeal(currentOrder.value.id, approveForm.value.decision)
    ElMessage.success('已批准申诉，订单将退款')
    approveDialogVisible.value = false
    detailDialogVisible.value = false
    await loadAppealingOrders()
  } catch (error) {
    console.error('批准申诉失败:', error)
    ElMessage.error('批准申诉失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 确认驳回申诉
const confirmRejectAppeal = async () => {
  if (!currentOrder.value) return
  
  if (!rejectForm.value.decision.trim()) {
    ElMessage.warning('请输入驳回理由')
    return
  }
  
  operationLoading.value = true
  try {
    await rejectAppeal(currentOrder.value.id, rejectForm.value.decision)
    ElMessage.success('已驳回申诉，订单不予退款')
    rejectDialogVisible.value = false
    detailDialogVisible.value = false
    await loadAppealingOrders()
  } catch (error) {
    console.error('驳回申诉失败:', error)
    ElMessage.error('驳回申诉失败，请稍后再试')
  } finally {
    operationLoading.value = false
  }
}

// 分页处理 - 待处理申诉
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadAppealingOrders()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadAppealingOrders()
}

// 分页处理 - 历史申诉
const handleHistorySizeChange = (size: number) => {
  historyPageSize.value = size
  historyCurrentPage.value = 1
  loadHistoryAppeals()
}

const handleHistoryCurrentChange = (page: number) => {
  historyCurrentPage.value = page
  loadHistoryAppeals()
}

// 批准对话框事件
const onApproveDialogOpen = () => {
  approveForm.value.decision = '管理员同意退款'
}

const onApproveDialogClosed = () => {
  approveForm.value.decision = '管理员同意退款'
  operationLoading.value = false
}

// 驳回对话框事件
const onRejectDialogOpen = () => {
  rejectForm.value.decision = ''
}

const onRejectDialogClosed = () => {
  rejectForm.value.decision = ''
  operationLoading.value = false
}
</script>

<style scoped>
.appeal-management {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.price {
  color: #F56C6C;
  font-weight: bold;
}

.detail-footer {
  margin-top: 20px;
}

.admin-actions {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 5px;
}

.admin-actions h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #606266;
}

.action-hint {
  margin-bottom: 15px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.timeline-card {
  margin-bottom: 10px;
}

.timeline-card h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #606266;
}

.timeline-card p {
  margin: 0;
  white-space: pre-wrap;
}

.timestamp {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.confirm-message {
  margin-bottom: 20px;
  font-weight: bold;
}
</style> 