<template>
  <div class="player-reviews">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的评价</span>
        </div>
      </template>

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
              <div class="review-service">
                <div class="service-title">{{ review.serviceTitle }}</div>
                <div class="companion-name">陪玩: {{ review.companionName }}</div>
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
              <p>{{ review.content || '未填写评价内容' }}</p>
            </div>

            <div class="review-order-info">
              <span>订单号: {{ review.orderNumber }}</span>
              <span>游戏: {{ review.gameType }}</span>
              <span v-if="review.anonymous">
                <el-tag size="small" type="info">匿名评价</el-tag>
              </span>
            </div>

            <div class="review-reply" v-if="review.replied">
              <div class="reply-header">
                <span class="reply-label">陪玩回复:</span>
                <span class="reply-time">{{ formatDate(review.replyTime) }}</span>
              </div>
              <p class="reply-content">{{ review.reply }}</p>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils/date'
import { getMyReviews, type Review } from '../../api/review'

// 状态
const loading = ref(false)
const reviews = ref<Review[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const response = await getMyReviews(currentPage.value, pageSize.value)
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

// 初始化
onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.player-reviews {
  padding: 20px;
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
  margin-bottom: 15px;
}

.review-service {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.service-title {
  font-weight: 500;
  font-size: 16px;
}

.companion-name {
  font-size: 14px;
  color: #606266;
}

.review-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.review-time {
  color: #909399;
  font-size: 13px;
}

.review-content {
  margin-bottom: 15px;
  color: #303133;
}

.review-order-info {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}

.review-reply {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-top: 10px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.reply-label {
  font-weight: 500;
  color: #303133;
}

.reply-time {
  color: #909399;
  font-size: 13px;
}

.reply-content {
  color: #606266;
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 