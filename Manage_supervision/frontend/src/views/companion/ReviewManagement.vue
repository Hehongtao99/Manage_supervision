<template>
  <div class="review-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>评价管理</span>
          <el-button type="primary" size="small" @click="fetchReviews">刷新</el-button>
        </div>
      </template>

      <div class="review-notice">
        <el-alert
          title="评价审核说明"
          type="info"
          description="系统只显示已通过审核的评价。用户提交的评价需经过管理员审核后才能在此页面显示。"
          show-icon
          :closable="false"
        />
      </div>

      <div v-loading="loading">
        <!-- 空状态 -->
        <el-empty
          v-if="reviews.length === 0 && !loading"
          description="暂无评价记录"
        />

        <!-- 评价列表 -->
        <div class="review-list" v-else>
          <el-card
            v-for="review in reviews"
            :key="review.id"
            class="review-item"
            shadow="hover"
          >
            <div class="review-header">
              <div class="review-user">
                <el-avatar size="small" :src="review.reviewerAvatar" icon="el-icon-user" />
                <span>{{ review.reviewerName }}</span>
              </div>
              <div class="review-rating">
                <el-rate
                  v-model="review.rating"
                  disabled
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                />
                <span class="review-time">{{ formatDate(review.createTime) }}</span>
              </div>
            </div>

            <div class="review-content">
              <p>{{ review.content || '该用户未留下评价内容' }}</p>
            </div>

            <div class="review-order-info">
              <span>订单号: {{ review.orderNumber }}</span>
              <span>服务: {{ review.serviceTitle }}</span>
              <span>游戏: {{ review.gameType }}</span>
              <!-- 审核状态 -->
              <span>
                <el-tag 
                  size="small" 
                  :type="getReviewStatusType(review.reviewStatus)"
                >
                  {{ getReviewStatusText(review.reviewStatus) }}
                </el-tag>
              </span>
            </div>

            <!-- 显示审核拒绝原因 -->
            <div class="review-rejected" v-if="review.reviewStatus === 'rejected' && review.reviewComment">
              <div class="rejected-reason">
                <span class="reason-label">审核拒绝原因:</span>
                <p class="reason-content">{{ review.reviewComment }}</p>
              </div>
            </div>

            <div class="review-reply" v-if="review.replied">
              <div class="reply-header">
                <span class="reply-label">我的回复:</span>
                <span class="reply-time">{{ formatDate(review.replyTime) }}</span>
              </div>
              <p class="reply-content">{{ review.reply }}</p>
            </div>

            <div class="review-actions">
              <el-button
                v-if="!review.replied"
                type="primary"
                size="small"
                @click="handleReply(review)"
              >
                回复
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                @click="handleUpdateReply(review)"
              >
                修改回复
              </el-button>
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

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评价"
      width="40%"
    >
      <div class="user-review" v-if="selectedReview">
        <div class="user-review-header">
          <span>{{ selectedReview.reviewerName }}的评价:</span>
          <el-rate
            v-model="selectedReview.rating"
            disabled
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          />
        </div>
        <div class="user-review-content">
          <p>{{ selectedReview.content || '该用户未留下评价内容' }}</p>
        </div>
      </div>

      <el-form :model="replyForm" label-width="80px" class="reply-form">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">提交回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils/date'
import {
  getCompanionReviews,
  replyReview,
  type Review
} from '../../api/review'
import { useUserStore } from '../../stores/user'

// 获取用户store
const userStore = useUserStore()

// 状态
const loading = ref(false)
const reviews = ref<Review[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取评价状态对应的标签类型
const getReviewStatusType = (status?: string) => {
  if (!status) return 'info'
  
  const types: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

// 获取评价状态对应的文本
const getReviewStatusText = (status?: string) => {
  if (!status) return '未知状态'
  
  const texts: Record<string, string> = {
    pending: '审核中',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || '未知状态'
}

// 对话框状态
const replyDialogVisible = ref(false)
const selectedReview = ref<Review | null>(null)
const replyForm = reactive({
  reviewId: 0,
  reply: ''
})

// 加载评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    // 使用用户ID作为陪玩ID
    const companionId = userStore.userId
    console.log('当前陪玩ID:', companionId)
    if (!companionId) {
      throw new Error('未获取到陪玩ID')
    }
    const response = await getCompanionReviews(companionId, currentPage.value, pageSize.value)
    reviews.value = response.records
    total.value = response.total
  } catch (error) {
    console.error('获取评价失败:', error)
    ElMessage.error('获取评价失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchReviews()
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchReviews()
}

// 回复评价
const handleReply = (review: Review) => {
  // 只允许回复已审核通过的评价
  if (review.reviewStatus !== 'approved') {
    ElMessage.warning('只能回复已通过审核的评价')
    return
  }
  
  selectedReview.value = review
  replyForm.reviewId = review.id!
  replyForm.reply = ''
  replyDialogVisible.value = true
}

// 修改回复
const handleUpdateReply = (review: Review) => {
  selectedReview.value = review
  replyForm.reviewId = review.id!
  replyForm.reply = review.reply || ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await replyReview({
      reviewId: replyForm.reviewId,
      reply: replyForm.reply
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    fetchReviews()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '回复失败，请重试')
  }
}

// 初始化
onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.review-management {
  padding: 20px;
}

.review-notice {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  margin-bottom: 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-time {
  font-size: 12px;
  color: #909399;
}

.review-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.review-order-info {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}

.review-rejected {
  background-color: #fef0f0;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.rejected-reason {
  color: #f56c6c;
}

.reason-label {
  font-weight: 500;
}

.reason-content {
  margin: 5px 0 0;
}

.review-reply {
  background-color: #f0f9eb;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.reply-label {
  font-weight: 500;
  color: #67c23a;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  line-height: 1.6;
  margin: 0;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.user-review {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.user-review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-review-content {
  line-height: 1.6;
}

.reply-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>