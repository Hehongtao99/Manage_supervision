<template>
  <div class="companion-market">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>陪玩大厅</span>
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索游戏、陪玩名称..."
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <el-icon class="el-input__icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-container">
        <div class="filter-item">
          <span class="filter-label">游戏类型：</span>
          <el-select 
            v-model="filters.gameType" 
            placeholder="选择游戏类型" 
            clearable
            @change="handleFilter"
          >
            <el-option
              v-for="game in gameOptions"
              :key="game"
              :label="game"
              :value="game"
            />
          </el-select>
        </div>
        
        <div class="filter-item">
          <span class="filter-label">价格区间：</span>
          <el-select 
            v-model="filters.priceRange" 
            placeholder="选择价格区间" 
            clearable
            @change="handleFilter"
          >
            <el-option label="50元以下" value="0-50" />
            <el-option label="50-100元" value="50-100" />
            <el-option label="100-200元" value="100-200" />
            <el-option label="200元以上" value="200+" />
          </el-select>
        </div>
      </div>
      
      <!-- 服务列表 -->
      <div v-loading="loading">
        <el-empty 
          v-if="filteredServices.length === 0 && !loading" 
          description="暂无符合条件的陪玩服务"
        />
        
        <div class="service-grid" v-else>
          <el-card 
            v-for="service in filteredServices" 
            :key="service.id" 
            class="service-card"
            shadow="hover"
            @click="viewServiceDetail(service)"
          >
            <div class="service-header">
              <el-avatar 
                :size="50" 
                :src="service.companionAvatar || '/default-avatar.png'"
                class="companion-avatar"
              >
                {{ service.companionName?.charAt(0) }}
              </el-avatar>
              <div class="service-title-wrapper">
                <h3 class="service-title">{{ service.title }}</h3>
                <div class="companion-name">{{ service.companionName }}</div>
                <div class="rating-stars">
                  <el-rate 
                    v-model="service.rating" 
                    disabled 
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  />
                  <span class="rating-score">{{ parseFloat(service.rating || 0).toFixed(1) }}分</span>
                </div>
              </div>
            </div>
            
            <div class="service-info">
              <div class="game-types">
                <el-tag 
                  v-for="(game, index) in service.gameTypes.split(',')" 
                  :key="index"
                  type="success"
                  size="small"
                  class="game-tag"
                >
                  {{ game }}
                </el-tag>
              </div>
              
              <div class="service-desc">{{ truncateText(service.description, 80) }}</div>
              
              <div class="service-meta">
                <div class="service-price">
                  <span class="price-value">¥{{ service.price }}</span>/小时
                </div>
                
                <div class="service-time" v-if="service.serviceStartTime && service.serviceEndTime">
                  <el-icon><Clock /></el-icon>
                  {{ service.serviceStartTime }} - {{ service.serviceEndTime }}
                </div>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="totalServices > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[8, 12, 24, 36]"
            :total="totalServices"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="陪玩服务详情"
      width="50%"
    >
      <div v-if="selectedService" class="service-detail">
        <div class="detail-header">
          <el-avatar 
            :size="60" 
            :src="selectedService.companionAvatar || '/default-avatar.png'"
            class="detail-avatar"
          >
            {{ selectedService.companionName?.charAt(0) }}
          </el-avatar>
          <div class="detail-title-box">
            <h2 class="detail-title">{{ selectedService.title }}</h2>
            <div class="detail-companion">陪玩: {{ selectedService.companionName }}</div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-info">
          <div class="detail-section">
            <h4 class="section-title">游戏类型</h4>
            <div class="section-content game-tags">
              <el-tag 
                v-for="(game, index) in selectedService.gameTypes.split(',')" 
                :key="index"
                type="success"
                class="game-tag"
              >
                {{ game }}
              </el-tag>
            </div>
          </div>
          
          <div class="detail-section">
            <h4 class="section-title">服务价格</h4>
            <div class="section-content detail-price">
              <span class="price-value">¥{{ selectedService.price }}</span>/小时
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedService.serviceStartTime && selectedService.serviceEndTime">
            <h4 class="section-title">服务时间</h4>
            <div class="section-content">
              <el-icon><Clock /></el-icon>
              {{ selectedService.serviceStartTime }} - {{ selectedService.serviceEndTime }}
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedService.availability">
            <h4 class="section-title">可用时间</h4>
            <div class="section-content">{{ selectedService.availability }}</div>
          </div>
          
          <div class="detail-section description-section">
            <h4 class="section-title">服务介绍</h4>
            <div class="section-content">{{ selectedService.description }}</div>
          </div>
          
          <!-- 添加评价展示区域 -->
          <div class="detail-section reviews-section">
            <h4 class="section-title">用户评价</h4>
            <div class="user-rating">
              <span class="rating-label">综合评分：</span>
              <el-rate
                v-model="selectedService.rating"
                disabled
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              />
              <span class="rating-score">{{ parseFloat(selectedService.rating || 0).toFixed(1) }} 分</span>
            </div>
            
            <div class="reviews-list" v-loading="reviewsLoading">
              <div v-if="!reviewsLoading && companionReviews.length === 0" class="no-reviews">
                暂无评价记录
              </div>
              <div v-else class="review-items">
                <div v-for="review in companionReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <span class="reviewer-name">{{ review.anonymous ? '匿名用户' : review.reviewerName }}</span>
                    <el-rate
                      :model-value="parseFloat(review.rating || 0)"
                      disabled
                      :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                    />
                    <span class="review-date">{{ formatDate(review.createTime || '') }}</span>
                  </div>
                  <div class="review-content">
                    {{ review.content || '该用户未留下评价内容' }}
                  </div>
                  <div class="review-reply" v-if="review.replied && review.reply">
                    <span class="reply-label">陪玩回复：</span>
                    <span class="reply-content">{{ review.reply }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-actions">
          <el-button type="primary" @click="orderService(selectedService)">
            <el-icon><ShoppingCart /></el-icon> 下单
          </el-button>
          <el-button @click="contactCompanion(selectedService)">
            <el-icon><ChatDotRound /></el-icon> 联系陪玩
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 下单对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="确认订单"
      width="50%"
      :before-close="closeOrderDialog"
    >
      <OrderForm 
        v-if="selectedService" 
        :service-id="selectedService.id" 
        :service-info="selectedService"
        @success="handleOrderSuccess"
        @cancel="closeOrderDialog"
      />
    </el-dialog>

    <!-- 订单成功提示对话框 -->
    <el-dialog
      v-model="orderSuccessDialogVisible"
      title="订单创建成功"
      width="30%"
    >
      <div class="order-success">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p class="success-message">订单已创建成功！</p>
        <p class="success-note">您可以在"我的订单"页面查看订单详情和进行支付。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="orderSuccessDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="goToOrderDetail">查看订单</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicCompanionServices, type CompanionService, getCompanionRating, getCompanionReviews } from '../../api/companion'
import { Search, Clock, ChatDotRound, ShoppingCart, CircleCheck, Star } from '@element-plus/icons-vue'
import OrderForm from '../../components/OrderForm.vue'
import { formatDate } from '../../utils/date'

const router = useRouter()

// 状态
const loading = ref(false)
const services = ref<CompanionService[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const totalServices = ref(0)
const searchQuery = ref('')
const detailDialogVisible = ref(false)
const selectedService = ref<CompanionService | null>(null)
const orderDialogVisible = ref(false)
const orderSuccessDialogVisible = ref(false)

// 筛选条件
const filters = reactive({
  gameType: '',
  priceRange: ''
})

// 游戏类型选项
const gameOptions = [
  '英雄联盟', '王者荣耀', '绝地求生', '穿越火线', '和平精英',
  'DOTA2', '魔兽世界', '炉石传说', 'CS:GO', '无畏契约',
  '第五人格', 'FIFA', 'NBA 2K', '使命召唤', '守望先锋',
  '原神', '明日方舟', '我的世界', '云顶之弈', 'Apex英雄'
]

// 根据筛选条件和搜索过滤服务列表
const filteredServices = computed(() => {
  let result = [...services.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(service => 
      service.title.toLowerCase().includes(query) || 
      service.description.toLowerCase().includes(query) ||
      service.gameTypes.toLowerCase().includes(query) ||
      (service.companionName && service.companionName.toLowerCase().includes(query))
    )
  }
  
  // 游戏类型过滤
  if (filters.gameType) {
    result = result.filter(service => 
      service.gameTypes.split(',').some(game => 
        game.trim().toLowerCase() === filters.gameType.toLowerCase()
      )
    )
  }
  
  // 价格区间过滤
  if (filters.priceRange) {
    const [min, max] = filters.priceRange.split('-')
    
    if (max === '+') {
      // 200+元
      result = result.filter(service => service.price >= parseFloat(min))
    } else {
      // 具体区间
      result = result.filter(service => 
        service.price >= parseFloat(min) && 
        service.price <= parseFloat(max)
      )
    }
  }
  
  return result
})

// 修改fetchServicesWithRatings函数，使用新的API接口
const fetchServicesWithRatings = async () => {
  loading.value = true
  try {
    const response = await getPublicCompanionServices(currentPage.value, pageSize.value)
    const servicesData = response.records
    
    // 并行获取所有陪玩的评分
    const ratingPromises = servicesData.map(async (service) => {
      if (service.companionId) {
        try {
          const ratingData = await getCompanionRating(service.companionId)
          service.rating = ratingData.rating || 0
        } catch (error) {
          console.error(`获取陪玩${service.companionId}的评分失败:`, error)
          service.rating = 0
        }
      }
      return service
    })
    
    services.value = await Promise.all(ratingPromises)
    totalServices.value = response.total
  } catch (error) {
    console.error('获取陪玩服务列表失败:', error)
    ElMessage.error('获取陪玩服务列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 替换原来的fetchServices函数
const fetchServices = fetchServicesWithRatings

// 添加评价存储变量
const companionReviews = ref([])
const reviewsLoading = ref(false)

// 获取陪玩的评价
const fetchCompanionReviews = async (companionId: number) => {
  if (!companionId) {
    console.error('缺少陪玩ID，无法获取评价')
    companionReviews.value = []
    reviewsLoading.value = false
    return
  }
  
  reviewsLoading.value = true
  console.log('开始获取陪玩评价，ID:', companionId)
  
  try {
    const response = await getCompanionReviews(companionId, 1, 3) // 只获取最近3条评价
    console.log('获取到评价响应:', response)
    
    // 确保响应数据正确
    if (response && response.records) {
      companionReviews.value = response.records
    } else {
      console.warn('评价API响应缺少records字段:', response)
      companionReviews.value = []
    }
  } catch (error) {
    console.error('获取陪玩评价失败:', error)
    ElMessage.error('获取评价失败，请稍后再试')
    companionReviews.value = []
  } finally {
    reviewsLoading.value = false
    console.log('评价加载完成，状态:', {
      loading: reviewsLoading.value,
      reviewCount: companionReviews.value.length
    })
  }
}

// 更新查看服务详情函数
const viewServiceDetail = (service: CompanionService) => {
  selectedService.value = service
  detailDialogVisible.value = true
  
  // 获取陪玩评价
  if (service.companionId) {
    fetchCompanionReviews(service.companionId)
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchServices()
}

// 联系陪玩
const contactCompanion = (service: CompanionService) => {
  if (!service.companionId) {
    ElMessage.warning('无法获取陪玩信息')
    return
  }
  
  // 关闭详情对话框
  detailDialogVisible.value = false
  
  // 导航到聊天页面并传递陪玩ID
  router.push({
    path: '/chat',
    query: { companionId: service.companionId.toString() }
  })
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchServices()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchServices()
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

// 下单服务
const orderService = (service: CompanionService) => {
  selectedService.value = service
  orderDialogVisible.value = true
}

// 处理订单成功
const handleOrderSuccess = () => {
  orderDialogVisible.value = false
  orderSuccessDialogVisible.value = true
}

// 关闭订单对话框
const closeOrderDialog = () => {
  orderDialogVisible.value = false
}

// 跳转到订单详情
const goToOrderDetail = () => {
  router.push('/orders')
}

// 初始化
onMounted(() => {
  fetchServices()
})
</script>

<style scoped>
.companion-market {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 250px;
}

.filter-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.service-card {
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.service-card:hover {
  transform: translateY(-5px);
}

.service-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.companion-avatar {
  margin-right: 12px;
}

.service-title-wrapper {
  flex: 1;
}

.service-title {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 600;
}

.companion-name {
  font-size: 13px;
  color: #606266;
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.game-types {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.game-tag {
  margin-right: 0;
}

.service-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  min-height: 40px;
}

.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.service-price {
  font-size: 14px;
  color: #606266;
}

.price-value {
  font-size: 18px;
  font-weight: 600;
  color: #ff6b6b;
}

.service-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 详情对话框样式 */
.service-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
}

.detail-avatar {
  margin-right: 20px;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 20px;
}

.detail-companion {
  color: #606266;
  font-size: 14px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 20px 0;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.section-content {
  color: #606266;
}

.game-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-price {
  font-size: 16px;
}

.description-section .section-content {
  line-height: 1.6;
  white-space: pre-line;
}

.detail-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.order-success {
  text-align: center;
}

.success-icon {
  font-size: 40px;
  color: #67C23A;
  margin-bottom: 10px;
}

.success-message {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}

.success-note {
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.reviews-section {
  margin-top: 20px;
}

.user-rating {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.rating-label {
  margin-right: 10px;
}

.rating-score {
  font-size: 14px;
  color: #F7BA2A;
  margin-left: 5px;
  font-weight: 500;
}

.rating-stars {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.reviews-list {
  margin-top: 10px;
}

.review-items {
  margin-top: 10px;
}

.review-item {
  margin-bottom: 10px;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.reviewer-name {
  margin-right: 10px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-content {
  margin-bottom: 5px;
}

.review-reply {
  margin-top: 5px;
}

.reply-label {
  font-size: 12px;
  font-weight: 500;
}

.reply-content {
  margin-left: 10px;
}

.no-reviews {
  text-align: center;
  color: #909399;
}
</style> 