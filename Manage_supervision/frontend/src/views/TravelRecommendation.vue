<template>
  <div class="travel-recommendation-container">
    <h1 class="page-title">旅游推荐</h1>

    <el-card shadow="never" class="search-card">
      <el-form :model="form" label-width="100px" label-position="left">
        <h2 class="form-section-title">出行信息</h2>
        
        <!-- 出发地 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="出发省份">
              <public-region-select 
                v-model="form.fromProvinceId" 
                level="province" 
                placeholder="请选择出发省份"
                @change="handleFromProvinceChange"
                class="from-province-select"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出发城市">
              <public-region-select 
                v-model="form.fromCityId" 
                level="city"
                :parent-id="form.fromProvinceId" 
                placeholder="请选择出发城市"
                :disabled="!form.fromProvinceId"
                @change="handleFromCityChange"
                class="from-city-select"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出发区县">
              <public-region-select 
                v-model="form.fromDistrictId" 
                level="district"
                :parent-id="form.fromCityId" 
                placeholder="请选择出发区县"
                :disabled="!form.fromCityId"
                class="from-district-select"
                @change="handleFromDistrictChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 目的地 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="目的地省份">
              <public-region-select 
                v-model="form.toProvinceId" 
                level="province" 
                placeholder="请选择目的地省份"
                @change="handleToProvinceChange"
                class="to-province-select"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目的地城市">
              <public-region-select 
                v-model="form.toCityId" 
                level="city"
                :parent-id="form.toProvinceId" 
                placeholder="请选择目的地城市"
                :disabled="!form.toProvinceId"
                @change="handleToCityChange"
                class="to-city-select"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目的地区县">
              <public-region-select 
                v-model="form.toDistrictId" 
                level="district"
                :parent-id="form.toCityId" 
                placeholder="请选择目的地区县"
                :disabled="!form.toCityId"
                class="to-district-select"
                @change="handleToDistrictChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 出行方式 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="出行方式">
              <el-select 
                v-model="form.travelMode" 
                placeholder="请选择出行方式" 
                style="width: 100%"
              >
                <el-option label="飞机" value="plane" />
                <el-option label="火车" value="train" />
                <el-option label="高铁" value="highSpeedTrain" />
                <el-option label="汽车" value="car" />
                <el-option label="自驾" value="selfDriving" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 搜索按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            开始推荐
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 推荐结果 -->
    <div v-if="recommendationResult.scenicSpots.length > 0 || recommendationResult.hotels.length > 0" class="recommendation-result">
      <h2 class="result-title">推荐结果</h2>
      
      <!-- 推荐景区 -->
      <el-card v-if="recommendationResult.scenicSpots.length > 0" shadow="hover" class="result-card">
        <template #header>
          <div class="card-header">
            <span>推荐景区</span>
            <span class="result-info">共找到 {{ recommendationResult.scenicSpots.length }} 个景区</span>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col 
            v-for="spot in recommendationResult.scenicSpots" 
            :key="spot.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            class="card-item"
          >
            <el-card shadow="hover" class="scenic-card">
              <div class="scenic-image">
                <el-image 
                  :src="spot.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image'" 
                  fit="cover"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="scenic-info">
                <h3 class="scenic-name">{{ spot.name }}</h3>
                <div class="scenic-meta">
                  <el-tag size="small" v-if="spot.level">{{ spot.level }}</el-tag>
                  <span class="scenic-price" v-if="spot.ticketPrice">¥{{ spot.ticketPrice }}</span>
                </div>
                <div class="scenic-location">{{ spot.locationPath }}</div>
                <div class="scenic-desc">{{ truncateText(spot.description, 50) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 推荐酒店 -->
      <el-card v-if="recommendationResult.hotels.length > 0" shadow="hover" class="result-card">
        <template #header>
          <div class="card-header">
            <span>推荐酒店</span>
            <span class="result-info">共找到 {{ recommendationResult.hotels.length }} 家酒店</span>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col 
            v-for="hotel in recommendationResult.hotels" 
            :key="hotel.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            class="card-item"
          >
            <el-card shadow="hover" class="hotel-card">
              <div class="hotel-image">
                <el-image 
                  :src="hotel.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image'" 
                  fit="cover"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="hotel-info">
                <h3 class="hotel-name">{{ hotel.name }}</h3>
                <div class="hotel-meta">
                  <el-tag size="small" v-if="hotel.level">{{ hotel.level }}</el-tag>
                  <span class="hotel-price" v-if="hotel.startPrice">¥{{ hotel.startPrice }} 起</span>
                </div>
                <div class="hotel-location">{{ hotel.locationPath }}</div>
                <div class="hotel-desc">{{ truncateText(hotel.description, 50) }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 无结果提示 -->
    <el-empty 
      v-if="searched && recommendationResult.scenicSpots.length === 0 && recommendationResult.hotels.length === 0"
      description="暂无推荐结果，请尝试选择其他地区"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import PublicRegionSelect from '../components/PublicRegionSelect.vue'
import { getScenicSpotsByRegion, getHotScenicSpots, getHotelsByScenicSpot } from '../api/travel'
import type { ScenicSpotData } from '../types/scenicSpot'
import type { HotelData } from '../types/hotel'

// 表单数据
const form = reactive({
  fromProvinceId: null,
  fromCityId: null,
  fromDistrictId: null,
  toProvinceId: null,
  toCityId: null,
  toDistrictId: null,
  travelMode: 'plane'
})

// 推荐结果
const recommendationResult = reactive({
  scenicSpots: [] as ScenicSpotData[],
  hotels: [] as HotelData[]
})

const loading = ref(false)
const searched = ref(false)

// 区域选择事件处理
const handleFromProvinceChange = (value, name) => {
  form.fromCityId = null
  form.fromDistrictId = null
  
  if (value && name) {
    const provinceOption = { id: value, name }
    if (!options.provinces.some(p => p.id === value)) {
      options.provinces.push(provinceOption)
    }
  }
}

const handleFromCityChange = (value, name) => {
  form.fromDistrictId = null
  
  if (value && name) {
    const cityOption = { id: value, name }
    if (!options.cities.some(c => c.id === value)) {
      options.cities.push(cityOption)
    }
  }
}

const handleToProvinceChange = (value, name) => {
  form.toCityId = null
  form.toDistrictId = null
  
  if (value && name) {
    const provinceOption = { id: value, name }
    if (!options.provinces.some(p => p.id === value)) {
      options.provinces.push(provinceOption)
    }
  }
}

const handleToCityChange = (value, name) => {
  form.toDistrictId = null
  
  if (value && name) {
    const cityOption = { id: value, name }
    if (!options.cities.some(c => c.id === value)) {
      options.cities.push(cityOption)
    }
  }
}

const handleFromDistrictChange = (value, name) => {
  if (value && name) {
    const districtOption = { id: value, name }
    if (!options.districts.some(d => d.id === value)) {
      options.districts.push(districtOption)
    }
  }
}

const handleToDistrictChange = (value, name) => {
  if (value && name) {
    const districtOption = { id: value, name }
    if (!options.districts.some(d => d.id === value)) {
      options.districts.push(districtOption)
    }
  }
}

// 存储选项数据，用于获取地区名称
const options = reactive({
  provinces: [],
  cities: [],
  districts: []
})

// 随机选择数组中的n个元素
const getRandomItems = (array, n) => {
  const shuffled = [...array].sort(() => 0.5 - Math.random())
  return shuffled.slice(0, n)
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return '暂无描述'
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 搜索处理
const handleSearch = async () => {
  // 验证目的地是否选择
  if (!form.toProvinceId) {
    ElMessage.warning('请至少选择目的地省份')
    return
  }
  
  loading.value = true
  searched.value = true
  recommendationResult.scenicSpots = []
  recommendationResult.hotels = []
  
  try {
    console.log('开始获取目的地景区数据...')
    console.log('目的地省份ID:', form.toProvinceId)
    console.log('目的地城市ID:', form.toCityId)
    console.log('目的地区县ID:', form.toDistrictId)
    
    // 获取目的地景区
    const scenicResponse = await getScenicSpotsByRegion(
      form.toProvinceId,
      form.toCityId,
      form.toDistrictId
    )
    
    console.log('API返回的景区数据:', scenicResponse)
    
    // 检查数据结构，兼容不同的API返回格式
    const scenicSpots = scenicResponse.records || scenicResponse.content || [];
    const totalCount = scenicResponse.total || 0;
    
    console.log(`获取到${totalCount}个景区数据`, scenicSpots);
    
    if (scenicSpots && scenicSpots.length > 0) {
      console.log(`API返回了${scenicSpots.length}个景区`)
      
      // 详细记录每个景区的信息，用于排查
      scenicSpots.forEach((spot, index) => {
        console.log(`景区${index + 1}:`, {
          id: spot.id,
          name: spot.name,
          provinceId: spot.provinceId,
          cityId: spot.cityId,
          districtId: spot.districtId,
          locationPath: spot.locationPath
        })
      })
      
      // 直接使用API返回的数据，不再进行二次筛选
      // 如果API已经根据参数过滤了数据，就不需要前端再次过滤
      const availableSpots = scenicSpots;
      console.log(`可用景区数量: ${availableSpots.length}`)
      
      if (availableSpots.length > 0) {
        // 随机选择最多5个景区
        const randomScenicSpots = getRandomItems(availableSpots, Math.min(5, availableSpots.length))
        console.log('随机选择的景区:', randomScenicSpots.map(spot => spot.name))
        recommendationResult.scenicSpots = randomScenicSpots
        
        // 为每个景区获取周边酒店，并合并结果
        let allHotels = []
        
        // 依次获取每个景区的酒店
        for (const spot of randomScenicSpots.slice(0, 2)) { // 只取前两个景区的酒店，避免请求过多
          try {
            console.log(`获取景区ID${spot.id}的周边酒店...`)
            const hotelResponse = await getHotelsByScenicSpot(spot.id, 3)
            
            // 检查酒店数据结构
            const hotels = hotelResponse.records || hotelResponse.content || hotelResponse || [];
            
            if (hotels && hotels.length > 0) {
              console.log(`景区${spot.id}周边找到${hotels.length}家酒店`)
              allHotels = [...allHotels, ...hotels]
            }
          } catch (error) {
            console.error(`获取景区${spot.id}周边酒店失败:`, error)
          }
        }
        
        // 如果有多家酒店，随机选择5家展示
        if (allHotels.length > 0) {
          console.log(`总共找到${allHotels.length}家酒店`)
          recommendationResult.hotels = getRandomItems(allHotels, Math.min(5, allHotels.length))
        } else {
          console.log('未找到任何酒店')
          recommendationResult.hotels = []
        }
      } else {
        ElMessage.warning(`未找到${getLocationDescription()}的景区，请尝试选择其他地区`)
        recommendationResult.scenicSpots = []
        recommendationResult.hotels = []
      }
    } else {
      ElMessage.warning(`未找到${getLocationDescription()}的景区，请尝试选择其他地区`)
      recommendationResult.scenicSpots = []
      recommendationResult.hotels = []
    }
  } catch (error) {
    console.error('获取推荐数据失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('获取推荐数据失败，请稍后重试')
    recommendationResult.scenicSpots = []
    recommendationResult.hotels = []
  } finally {
    loading.value = false
  }
}

// 获取当前选择的地区描述
const getLocationDescription = () => {
  let description = ''
  
  if (form.toProvinceId) {
    const provinceOption = options.provinces.find(p => p.id === form.toProvinceId)
    if (provinceOption) {
      description += provinceOption.name
    }
  }
  
  if (form.toCityId) {
    const cityOption = options.cities.find(c => c.id === form.toCityId)
    if (cityOption) {
      description += ` ${cityOption.name}`
    }
  }
  
  if (form.toDistrictId) {
    const districtOption = options.districts.find(d => d.id === form.toDistrictId)
    if (districtOption) {
      description += ` ${districtOption.name}`
    }
  }
  
  return description || '所选地区'
}
</script>

<style scoped>
.travel-recommendation-container {
  padding: 20px;
  height: calc(100vh - 64px); /* 减去顶部导航栏的高度 */
  overflow-y: auto; /* 添加垂直滚动条 */
}

.page-title {
  margin-bottom: 24px;
  font-size: 24px;
}

.search-card {
  margin-bottom: 24px;
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #fff;
}

.form-section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: bold;
}

.recommendation-result {
  margin-top: 30px;
}

.result-title {
  margin-bottom: 20px;
  font-size: 20px;
}

.result-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-info {
  font-size: 14px;
  color: #909399;
}

.card-item {
  margin-bottom: 20px;
}

/* 景区卡片样式 */
.scenic-card, .hotel-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scenic-image, .hotel-image {
  height: 180px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.scenic-image .el-image, .hotel-image .el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

.scenic-info, .hotel-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.scenic-name, .hotel-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
}

.scenic-meta, .hotel-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.scenic-price, .hotel-price {
  color: #f56c6c;
  font-weight: bold;
}

.scenic-location, .hotel-location {
  color: #606266;
  font-size: 12px;
  margin-bottom: 8px;
}

.scenic-desc, .hotel-desc {
  color: #909399;
  font-size: 12px;
  margin-top: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 确保区域选择器更大 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  width: 100%;
}
</style> 