<template>
  <div class="companion-service-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>陪玩服务管理</span>
          <div class="header-controls">
            <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusChange">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已上线" value="active" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已下线" value="inactive" />
            </el-select>
            <el-button v-if="statusFilter !== 'pending'" type="primary" @click="viewPendingServices">
              查看待审核服务 
              <el-badge v-if="pendingCount > 0" :value="pendingCount" class="pending-badge" />
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 服务列表 -->
      <div v-loading="loading">
        <el-table :data="services" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="陪玩信息" width="200">
            <template #default="scope">
              <div class="companion-info">
                <el-avatar :size="40" :src="scope.row.companionAvatar || '/default-avatar.png'">
                  {{ scope.row.companionName?.charAt(0) }}
                </el-avatar>
                <span class="companion-name">{{ scope.row.companionName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="服务标题" width="200" />
          <el-table-column prop="gameTypes" label="游戏类型" width="150" />
          <el-table-column prop="price" label="价格(元/小时)" width="120">
            <template #default="scope">
              {{ scope.row.price.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="服务时间" width="180">
            <template #default="scope">
              <span v-if="scope.row.serviceStartTime && scope.row.serviceEndTime">
                {{ formatTime(scope.row.serviceStartTime) }} - {{ formatTime(scope.row.serviceEndTime) }}
              </span>
              <span v-else>未设置</span>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="120">
            <template #default="scope">
              <el-tag 
                :type="getReviewStatusType(scope.row.reviewStatus)" 
                effect="plain"
              >
                {{ getReviewStatusText(scope.row.reviewStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="服务状态" width="100">
            <template #default="scope">
              <el-tag 
                :type="getStatusType(scope.row.status)" 
                effect="plain"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button 
                v-if="scope.row.reviewStatus === 'pending'" 
                type="success" 
                size="small" 
                @click="handleReview(scope.row, true)"
              >
                通过
              </el-button>
              <el-button 
                v-if="scope.row.reviewStatus === 'pending'" 
                type="danger" 
                size="small" 
                @click="handleReview(scope.row, false)"
              >
                拒绝
              </el-button>
              <el-button
                v-if="scope.row.reviewStatus === 'approved' && scope.row.status === 'inactive'"
                type="success"
                size="small"
                @click="handleActivate(scope.row)"
              >
                上线
              </el-button>
              <el-button
                v-if="scope.row.reviewStatus === 'approved' && scope.row.status === 'active'"
                type="warning"
                size="small"
                @click="handleDeactivate(scope.row)"
              >
                下线
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="viewServiceDetail(scope.row)"
              >
                详情
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
    
    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="服务详情"
      width="60%"
    >
      <div v-if="currentService" class="service-detail">
        <el-descriptions border :column="2">
          <el-descriptions-item label="ID">{{ currentService.id }}</el-descriptions-item>
          <el-descriptions-item label="陪玩">{{ currentService.companionName }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentService.title }}</el-descriptions-item>
          <el-descriptions-item label="游戏类型">{{ currentService.gameTypes }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ currentService.price }} 元/小时</el-descriptions-item>
          <el-descriptions-item label="服务时间">
            <span v-if="currentService.serviceStartTime && currentService.serviceEndTime">
              {{ formatTime(currentService.serviceStartTime) }} - {{ formatTime(currentService.serviceEndTime) }}
            </span>
            <span v-else>未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="可用性">{{ currentService.availability || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getReviewStatusType(currentService.reviewStatus)">
              {{ getReviewStatusText(currentService.reviewStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务状态">
            <el-tag :type="getStatusType(currentService.status)">
              {{ getStatusText(currentService.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">
            {{ currentService.reviewerName || '未审核' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ currentService.reviewTime ? formatDate(currentService.reviewTime) : '未审核' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" v-if="currentService.reviewComment">
            {{ currentService.reviewComment }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(currentService.createdTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(currentService.updatedTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务描述" :span="2">
            {{ currentService.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-footer" v-if="currentService.reviewStatus === 'pending'">
          <el-button type="success" @click="handleReview(currentService, true)">审核通过</el-button>
          <el-button type="danger" @click="handleReview(currentService, false)">拒绝服务</el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="40%"
    >
      <el-form :model="rejectForm">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            rows="4"
            placeholder="请输入拒绝原因，将通知陪玩服务提供者"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject" :loading="reviewLoading">确认拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../../utils/date'
import { 
  getAllCompanionServices, 
  getPendingCompanionServices,
  reviewCompanionService
} from '../../api/admin'

// 状态变量
const services = ref([])
const loading = ref(false)
const reviewLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const currentService = ref(null)
const rejectDialogVisible = ref(false)
const pendingCount = ref(0)
const rejectForm = ref({
  serviceId: null,
  reason: ''
})

// 加载服务列表
const loadServices = async () => {
  loading.value = true
  try {
    const response = await getAllCompanionServices(
      currentPage.value, 
      pageSize.value, 
      statusFilter.value
    )
    
    services.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('获取陪玩服务列表失败:', error)
    ElMessage.error('获取陪玩服务列表失败')
  } finally {
    loading.value = false
  }
}

// 获取待审核服务数量
const loadPendingCount = async () => {
  try {
    const response = await getPendingCompanionServices(1, 1)
    pendingCount.value = response.total
  } catch (error) {
    console.error('获取待审核服务数量失败:', error)
  }
}

// 定时任务：定期检查待审核服务数量
let pendingCheckInterval = null

// 获取审核状态标签类型
const getReviewStatusType = (status) => {
  switch (status) {
    case 'approved':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取审核状态文本
const getReviewStatusText = (status) => {
  switch (status) {
    case 'approved':
      return '已通过'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已拒绝'
    default:
      return '未知'
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'active':
      return 'success'
    case 'inactive':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'active':
      return '已上线'
    case 'inactive':
      return '已下线'
    default:
      return '未知'
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr
}

// 查看服务详情
const viewServiceDetail = (service) => {
  currentService.value = service
  detailDialogVisible.value = true
}

// 处理审核
const handleReview = (service, approved) => {
  if (approved) {
    ElMessageBox.confirm(
      '确定审核通过该陪玩服务？通过后将在陪玩市场公开显示。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      reviewService(service.id, true)
    }).catch(() => {})
  } else {
    rejectForm.value.serviceId = service.id
    rejectForm.value.reason = ''
    rejectDialogVisible.value = true
  }
}

// 确认拒绝
const confirmReject = () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  reviewService(rejectForm.value.serviceId, false, rejectForm.value.reason)
  rejectDialogVisible.value = false
}

// 审核服务
const reviewService = async (serviceId, approved, reason = '') => {
  reviewLoading.value = true
  try {
    const response = await reviewCompanionService(serviceId, approved, reason)
    
    ElMessage.success(response.message || '操作成功')
    loadServices()
    loadPendingCount()
    detailDialogVisible.value = false
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核操作失败，请重试')
  } finally {
    reviewLoading.value = false
  }
}

// 处理上线服务
const handleActivate = (service) => {
  ElMessageBox.confirm(
    '确定将该陪玩服务上线？上线后将对所有用户可见。',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await updateServiceStatus(service.id, 'active')
      ElMessage.success('服务已成功上线')
      loadServices()
    } catch (error) {
      console.error('上线服务失败:', error)
      ElMessage.error('上线服务失败，请重试')
    }
  }).catch(() => {})
}

// 处理下线服务
const handleDeactivate = (service) => {
  ElMessageBox.confirm(
    '确定将该陪玩服务下线？下线后将不对用户显示。',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await updateServiceStatus(service.id, 'inactive')
      ElMessage.success('服务已成功下线')
      loadServices()
    } catch (error) {
      console.error('下线服务失败:', error)
      ElMessage.error('下线服务失败，请重试')
    }
  }).catch(() => {})
}

// 更新服务状态
const updateServiceStatus = async (serviceId, status) => {
  try {
    const response = await fetch(`/api/admin/companion-services/${serviceId}/status?status=${status}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    return await response.json()
  } catch (error) {
    console.error('更新服务状态失败:', error)
    throw error
  }
}

// 分页相关处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadServices()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadServices()
}

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  loadServices()
}

// 查看待审核服务
const viewPendingServices = () => {
  statusFilter.value = 'pending'
  currentPage.value = 1
  loadServices()
}

onMounted(() => {
  loadServices()
  loadPendingCount()
  
  // 每分钟检查一次待审核服务数量
  pendingCheckInterval = setInterval(() => {
    loadPendingCount()
  }, 60000)
})

onBeforeUnmount(() => {
  if (pendingCheckInterval) {
    clearInterval(pendingCheckInterval)
  }
})
</script>

<style scoped>
.companion-service-management {
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

.companion-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.companion-name {
  font-weight: 500;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.service-detail {
  padding: 10px;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pending-badge {
  margin-top: -8px;
  margin-left: 5px;
}
</style> 