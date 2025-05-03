<template>
  <div class="hotel-detail-container" v-loading="loading">
    <div class="page-header">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/hotels' }">酒店管理</el-breadcrumb-item>
          <el-breadcrumb-item>酒店详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="actions">
        <el-button @click="handleBack">返回列表</el-button>
        <el-button type="primary" @click="handleEdit">编辑酒店</el-button>
      </div>
    </div>

    <div v-if="hotel.id" class="hotel-detail-content">
      <div class="hotel-header">
        <div class="hotel-info">
          <h1 class="hotel-name">{{ hotel.name }}</h1>
          <div class="hotel-meta">
            <el-tag v-if="hotel.level" size="small">{{ hotel.level }}</el-tag>
            <el-tag v-if="hotel.status === 'active'" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">禁用</el-tag>
            <span class="hotel-location">{{ hotel.locationPath }}</span>
          </div>
        </div>
        <div class="hotel-extra">
          <div v-if="hotel.startPrice" class="price">¥{{ hotel.startPrice }} <span>起</span></div>
        </div>
      </div>

      <el-row :gutter="20" class="detail-section">
        <el-col :md="16">
          <el-card shadow="never" class="detail-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            <div class="hotel-image" v-if="hotel.imageUrl">
              <el-image 
                :src="hotel.imageUrl" 
                fit="cover"
                :preview-src-list="[hotel.imageUrl]"
              />
            </div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="酒店名称">{{ hotel.name }}</el-descriptions-item>
              <el-descriptions-item label="酒店等级">{{ hotel.level || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ hotel.contactPhone || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="起始价格">{{ hotel.startPrice ? `¥${hotel.startPrice}` : '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="所在地区" :span="2">{{ hotel.locationPath }}</el-descriptions-item>
              <el-descriptions-item label="详细地址" :span="2">{{ hotel.address || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="酒店描述" :span="2">
                <div class="description-content">{{ hotel.description || '暂无描述' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :md="8">
          <el-card shadow="never" class="detail-card">
            <template #header>
              <div class="card-header">
                <span>关联景区</span>
              </div>
            </template>
            <div v-if="hotel.scenicSpotId" class="scenic-info">
              <h3>{{ hotel.scenicSpotName }}</h3>
              <p v-if="hotel.distanceToSpot">距离酒店约 {{ hotel.distanceToSpot }} 公里</p>
            </div>
            <el-empty v-else description="未关联景区" />
          </el-card>

          <el-card shadow="never" class="detail-card">
            <template #header>
              <div class="card-header">
                <span>管理信息</span>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="状态">
                <el-tag v-if="hotel.status === 'active'" type="success">启用</el-tag>
                <el-tag v-else type="info">禁用</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="排序值">{{ hotel.sort || 0 }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ hotel.createTime }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ hotel.updateTime }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" description="酒店信息不存在" />

    <!-- 编辑对话框 -->
    <el-dialog
      title="编辑酒店"
      v-model="dialogVisible"
      width="70%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <HotelForm
        v-if="dialogVisible"
        :hotel="hotel"
        :is-edit="true"
        @submit="handleFormSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { HotelData } from '../../types/hotel'
import { getHotelDetail } from '../../api/hotel'
import HotelForm from './components/HotelForm.vue'

const route = useRoute()
const router = useRouter()
const hotelId = ref<number>(Number(route.params.id))
const hotel = ref<HotelData>({} as HotelData)
const loading = ref<boolean>(false)
const dialogVisible = ref<boolean>(false)

// 获取酒店详情
const fetchHotelDetail = async () => {
  if (!hotelId.value) return

  loading.value = true
  try {
    const data = await getHotelDetail(hotelId.value)
    hotel.value = data
  } catch (error) {
    console.error('获取酒店详情失败:', error)
    ElMessage.error('获取酒店详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/admin/hotels')
}

// 编辑酒店
const handleEdit = () => {
  dialogVisible.value = true
}

// 表单提交后刷新详情
const handleFormSubmit = () => {
  dialogVisible.value = false
  fetchHotelDetail()
}

onMounted(() => {
  fetchHotelDetail()
})
</script>

<style scoped>
.hotel-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.hotel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.hotel-name {
  font-size: 24px;
  margin: 0 0 8px 0;
}

.hotel-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hotel-location {
  color: #606266;
}

.hotel-extra .price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.hotel-extra .price span {
  font-size: 14px;
  font-weight: normal;
}

.detail-section {
  margin-top: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotel-image {
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.hotel-image .el-image {
  width: 100%;
  height: 300px;
}

.description-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.scenic-info {
  padding: 10px 0;
}

.scenic-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.scenic-info p {
  margin: 0;
  color: #606266;
}
</style> 