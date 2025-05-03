<template>
  <div class="travel-recommendation-container">
    <!-- Hero section with background image -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">发现您的下一个完美旅行目的地</h1>
        <p class="hero-subtitle">根据您的喜好，定制专属旅行体验</p>
      </div>
    </div>

    <div class="content-wrapper">
      <el-card shadow="never" class="search-card">
        <h2 class="form-section-title"><i class="el-icon-map-location"></i> 出行信息</h2>
        <el-form :model="form" label-width="100px" label-position="left">
          <!-- 出发地 -->
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="出发省份">
                <public-region-select 
                  v-model="form.fromProvinceId" 
                  level="province" 
                  placeholder="请选择出发省份"
                  @change="handleFromProvinceChange"
                  class="region-select"
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
                  class="region-select"
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
                  class="region-select"
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
                  class="region-select"
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
                  class="region-select"
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
                  class="region-select"
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
                  class="travel-mode-select"
                >
                  <el-option label="飞机" value="plane">
                    <span class="travel-mode-option">
                      <i class="travel-icon el-icon-airplane"></i> 飞机
                    </span>
                  </el-option>
                  <el-option label="火车" value="train">
                    <span class="travel-mode-option">
                      <i class="travel-icon el-icon-train"></i> 火车
                    </span>
                  </el-option>
                  <el-option label="高铁" value="highSpeedTrain">
                    <span class="travel-mode-option">
                      <i class="travel-icon el-icon-train"></i> 高铁
                    </span>
                  </el-option>
                  <el-option label="汽车" value="car">
                    <span class="travel-mode-option">
                      <i class="travel-icon el-icon-bus"></i> 汽车
                    </span>
                  </el-option>
                  <el-option label="自驾" value="selfDriving">
                    <span class="travel-mode-option">
                      <i class="travel-icon el-icon-car"></i> 自驾
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 搜索按钮 -->
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading" class="search-button">
              <i class="el-icon-search"></i> 开始推荐
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 推荐结果 -->
      <div v-if="recommendationResult.scenicSpots.length > 0 || recommendationResult.hotels.length > 0" class="recommendation-result">
        <h2 class="result-title">为您精选的旅行建议</h2>
        
        <!-- 推荐景区 -->
        <div v-if="recommendationResult.scenicSpots.length > 0" class="result-section">
          <div class="section-header">
            <h3 class="section-title">推荐景区</h3>
            <span class="result-info">共找到 {{ recommendationResult.scenicSpots.length }} 个景区</span>
          </div>
          
          <el-row :gutter="24" class="card-row">
            <el-col 
              v-for="spot in recommendationResult.scenicSpots" 
              :key="spot.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6" 
              class="card-item"
            >
              <div class="scenic-card">
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
                  <div v-if="spot.level" class="scenic-level">
                    <el-tag size="small" effect="plain">{{ spot.level }}</el-tag>
                  </div>
                </div>
                <div class="scenic-info">
                  <h4 class="scenic-name">{{ spot.name }}</h4>
                  <div class="scenic-price-container" v-if="spot.ticketPrice">
                    <span class="price-label">门票</span>
                    <span class="scenic-price">¥{{ spot.ticketPrice }}</span>
                  </div>
                  <div class="scenic-location">
                    <i class="location-icon el-icon-location"></i>
                    {{ spot.locationPath }}
                  </div>
                  <div class="scenic-desc">{{ truncateText(spot.description, 60) }}</div>
                  <div class="scenic-footer">
                    <el-button type="text" class="view-more-btn">查看详情</el-button>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- 推荐酒店 -->
        <div v-if="recommendationResult.hotels.length > 0" class="result-section">
          <div class="section-header">
            <h3 class="section-title">推荐酒店</h3>
            <span class="result-info">共找到 {{ recommendationResult.hotels.length }} 家酒店</span>
          </div>
          
          <el-row :gutter="24" class="card-row">
            <el-col 
              v-for="hotel in recommendationResult.hotels" 
              :key="hotel.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6" 
              class="card-item"
            >
              <div class="hotel-card">
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
                  <div v-if="hotel.level" class="hotel-level">
                    <el-tag size="small" type="warning" effect="plain">{{ hotel.level }}</el-tag>
                  </div>
                </div>
                <div class="hotel-info">
                  <h4 class="hotel-name">{{ hotel.name }}</h4>
                  <div class="hotel-price-container" v-if="hotel.startPrice">
                    <span class="price-label">价格</span>
                    <span class="hotel-price">¥{{ hotel.startPrice }} <span class="price-unit">起</span></span>
                  </div>
                  <div class="hotel-location">
                    <i class="location-icon el-icon-location"></i>
                    {{ hotel.locationPath }}
                  </div>
                  <div class="hotel-desc">{{ truncateText(hotel.description, 60) }}</div>
                  <div class="hotel-footer">
                    <el-button type="text" class="view-more-btn">查看详情</el-button>
                    <el-button type="text" class="reserve-btn">立即预订</el-button>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 无结果提示 -->
      <div 
        v-if="searched && recommendationResult.scenicSpots.length === 0 && recommendationResult.hotels.length === 0"
        class="empty-result"
      >
        <el-empty 
          description="暂无推荐结果，请尝试选择其他地区"
          :image-size="200"
        >
          <template #description>
            <p class="empty-description">暂无推荐结果</p>
            <p class="empty-suggestion">请尝试选择其他目的地或放宽筛选条件</p>
          </template>
          <el-button type="primary" @click="handleSearch">重新搜索</el-button>
        </el-empty>
      </div>
    </div>
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
/* 基础容器样式 */
.travel-recommendation-container {
  position: relative;
  height: 100vh;
  overflow-y: scroll; /* 强制显示垂直滚动条 */
  background-color: #f5f7fa;
  padding-bottom: 60px;
  box-sizing: border-box;
  /* 自定义滚动条 - Firefox */
  scrollbar-width: thin;
  scrollbar-color: #909399 #f5f7fa;
}

/* 自定义滚动条 - Webkit (Chrome, Safari, Edge) */
.travel-recommendation-container::-webkit-scrollbar {
  width: 10px;
  background-color: #f5f7fa;
}

.travel-recommendation-container::-webkit-scrollbar-thumb {
  background-color: #909399;
  border-radius: 10px;
  border: 2px solid #f5f7fa;
}

.travel-recommendation-container::-webkit-scrollbar-thumb:hover {
  background-color: #606266;
}

.travel-recommendation-container::-webkit-scrollbar-track {
  background-color: #f5f7fa;
  border-radius: 10px;
}

/* Hero 区域样式 - 优化背景和动画 */
.hero-section {
  height: 450px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.4)), url('https://images.unsplash.com/photo-1530789253388-582c481c54b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=1350&q=80');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 40px;
  border-radius: 0 0 30px 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(32, 58, 173, 0.5) 0%, rgba(142, 45, 226, 0.5) 100%);
  z-index: 1;
  animation: gradientShift 15s infinite alternate;
}

@keyframes gradientShift {
  0% {
    opacity: 0.5;
    background-position: 0% 50%;
  }
  50% {
    opacity: 0.7;
    background-position: 100% 50%;
  }
  100% {
    opacity: 0.5;
    background-position: 0% 50%;
  }
}

.hero-content {
  text-align: center;
  max-width: 800px;
  padding: 0 20px;
  position: relative;
  z-index: 2;
  animation: fadeInUp 1.2s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-title {
  font-size: 3.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: 1.5px;
  background: linear-gradient(to right, #ffffff, #e0e0e0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: titleGlow 3s infinite alternate;
}

@keyframes titleGlow {
  from { text-shadow: 0 0 10px rgba(255, 255, 255, 0.2), 0 4px 8px rgba(0, 0, 0, 0.5); }
  to { text-shadow: 0 0 20px rgba(255, 255, 255, 0.4), 0 4px 8px rgba(0, 0, 0, 0.5); }
}

.hero-subtitle {
  font-size: 1.7rem;
  margin-bottom: 35px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  opacity: 0.95;
  font-weight: 300;
  animation: fadeIn 2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 0.95; }
}

.content-wrapper {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 30px;
}

/* 表单区域样式 - 增强卡片效果 */
.search-card {
  margin-bottom: 50px;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1), 0 5px 15px rgba(0, 0, 0, 0.05);
  padding: 30px;
  background-color: #fff;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  animation: cardSlideUp 0.8s ease-out;
}

@keyframes cardSlideUp {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.search-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 8px;
  height: 100%;
  background: linear-gradient(to bottom, #4a90e2, #67c23a);
  border-radius: 4px 0 0 4px;
}

.search-card:hover {
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 10px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-8px);
}

.form-section-title {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 1.8rem;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-bottom: 14px;
  display: flex;
  align-items: center;
}

.form-section-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 70px;
  height: 4px;
  background: linear-gradient(to right, #4a90e2, #67c23a);
  border-radius: 4px;
  animation: expandWidth 1.5s ease-out;
}

@keyframes expandWidth {
  from { width: 0; opacity: 0; }
  to { width: 70px; opacity: 1; }
}

.region-select {
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.3s;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset, 0 0 10px rgba(0, 0, 0, 0.05);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset, 0 0 10px rgba(64, 158, 255, 0.3);
}

.travel-mode-select {
  width: 100%;
}

.travel-mode-option {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.travel-icon {
  margin-right: 10px;
  font-size: 18px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #f2f6fc;
  color: #409EFF;
  transition: all 0.3s ease;
}

:deep(.el-select-dropdown__item:hover) .travel-icon {
  transform: translateY(-2px);
  background-color: #e6f1fe;
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.search-button {
  padding: 12px 35px;
  font-size: 16px;
  transition: all 0.35s;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a90e2 0%, #409EFF 100%);
  border: none;
  font-weight: 500;
  letter-spacing: 0.5px;
  margin-top: 15px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.search-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3c83d7 0%, #3b90eb 100%);
  transition: left 0.5s ease;
  z-index: -1;
}

.search-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.4);
}

.search-button:hover::before {
  left: 0;
}

.search-button:active {
  transform: translateY(-2px);
  box-shadow: 0 5px 10px rgba(64, 158, 255, 0.4);
}

/* 结果区域样式 - 动画和卡片增强 */
.recommendation-result {
  margin-top: 60px;
  animation: fadeIn 0.8s ease-in-out;
}

.result-title {
  margin-bottom: 40px;
  font-size: 2.2rem;
  font-weight: 600;
  color: #303133;
  text-align: center;
  position: relative;
  padding-bottom: 18px;
}

.result-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(to right, #409EFF, #67c23a);
  border-radius: 4px;
  animation: expandWidth 1.5s ease-out;
}

.result-section {
  margin-bottom: 60px;
  border-radius: 20px;
  background-color: #fff;
  padding: 35px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08), 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
}

.result-section:hover {
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.12), 0 8px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-5px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-size: 1.6rem;
  margin: 0;
  color: #303133;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: "";
  width: 5px;
  height: 20px;
  background: linear-gradient(to bottom, #409EFF, #67c23a);
  margin-right: 12px;
  border-radius: 3px;
}

.result-info {
  font-size: 14px;
  color: #606266;
  background-color: #f2f6fc;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
}

.result-info::before {
  content: '🔍';
  margin-right: 6px;
}

.card-row {
  margin-bottom: 0;
}

.card-item {
  margin-bottom: 30px;
  transition: all 0.5s ease;
  padding: 0 12px;
  perspective: 1000px;
}

.card-item:hover {
  transform: translateY(-10px);
  z-index: 10;
}

/* 景区卡片样式 - 3D效果和动画 */
.scenic-card, .hotel-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08), 0 5px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.5s ease, transform 0.4s ease;
  background-color: #fff;
  position: relative;
  transform-style: preserve-3d;
}

.scenic-card:hover, .hotel-card:hover {
  box-shadow: 0 20px 32px rgba(0, 0, 0, 0.15), 0 10px 15px rgba(0, 0, 0, 0.1);
  transform: rotateX(5deg) rotateY(-5deg);
}

.scenic-card:hover::after, .hotel-card:hover::after {
  opacity: 1;
  height: 5px;
}

.scenic-card::after, .hotel-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(to right, #409EFF, #67c23a);
  opacity: 0.8;
  transition: all 0.4s ease;
}

.scenic-image, .hotel-image {
  height: 220px;
  position: relative;
  overflow: hidden;
}

.scenic-image::after, .hotel-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0) 30%, rgba(0,0,0,0) 70%, rgba(0,0,0,0.2) 100%);
  z-index: 1;
  pointer-events: none;
}

.scenic-image .el-image, .hotel-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.8s ease;
}

.scenic-card:hover .el-image, .hotel-card:hover .el-image {
  transform: scale(1.12);
}

.scenic-level, .hotel-level {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 3;
  transition: transform 0.3s ease;
}

.scenic-card:hover .scenic-level, .hotel-card:hover .hotel-level {
  transform: scale(1.05);
}

:deep(.scenic-level .el-tag) {
  font-weight: 600;
  border-radius: 20px;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
  backdrop-filter: blur(5px);
  background-color: rgba(255, 255, 255, 0.85);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  border: none;
}

:deep(.hotel-level .el-tag) {
  font-weight: 600;
  border-radius: 20px;
  padding: 0 12px;
  height: 24px;
  line-height: 22px;
  backdrop-filter: blur(5px);
  background-color: rgba(255, 255, 255, 0.85);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  border-color: #e6a23c;
  color: #e6a23c;
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
  padding: 22px;
}

.scenic-name, .hotel-name {
  margin: 0 0 18px 0;
  font-size: 19px;
  font-weight: 600;
  color: #303133;
  min-height: 24px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.3s ease;
}

.scenic-card:hover .scenic-name, .hotel-card:hover .hotel-name {
  color: #409EFF;
}

.scenic-price-container, .hotel-price-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  background-color: #f9fcff;
  padding: 12px 15px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.scenic-card:hover .scenic-price-container, .hotel-card:hover .hotel-price-container {
  background-color: #f2f8ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.price-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.price-label::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  background-color: #f56c6c;
  margin-right: 8px;
  border-radius: 2px;
}

.scenic-price, .hotel-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 22px;
  text-shadow: 0 1px 1px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.scenic-card:hover .scenic-price, .hotel-card:hover .hotel-price {
  transform: scale(1.05);
}

.price-unit {
  font-size: 14px;
  font-weight: normal;
  opacity: 0.8;
  margin-left: 2px;
}

.scenic-location, .hotel-location {
  color: #606266;
  font-size: 14px;
  margin-bottom: 18px;
  display: flex;
  align-items: flex-start;
  background-color: #f2f6fc;
  padding: 10px 15px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.scenic-card:hover .scenic-location, .hotel-card:hover .hotel-location {
  background-color: #e6f1ff;
}

.location-icon {
  margin-right: 8px;
  color: #409EFF;
  font-size: 16px;
}

.scenic-desc, .hotel-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 22px;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  background-color: #fafbfd;
  padding: 15px;
  border-radius: 10px;
  border-left: 4px solid #e0e0e0;
  transition: all 0.3s ease;
}

.scenic-card:hover .scenic-desc, .hotel-card:hover .hotel-desc {
  border-left-color: #409EFF;
  background-color: #f8faff;
}

.scenic-footer, .hotel-footer {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  border-top: 1px solid #ebeef5;
  padding-top: 18px;
}

.view-more-btn, .reserve-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  transition: all 0.3s;
  position: relative;
}

.view-more-btn::after, .reserve-btn::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: currentColor;
  transition: width 0.3s ease;
}

.view-more-btn:hover::after, .reserve-btn:hover::after {
  width: 100%;
}

.view-more-btn:hover, .reserve-btn:hover {
  transform: translateY(-3px);
}

.reserve-btn {
  color: #409EFF;
  font-weight: 600;
}

/* 无结果提示美化 */
.empty-result {
  background-color: #fff;
  border-radius: 20px;
  padding: 60px 40px;
  margin-top: 60px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
  animation: fadeInUp 0.8s ease-out;
}

.empty-description {
  font-size: 22px;
  color: #606266;
  margin-bottom: 15px;
  font-weight: 500;
}

.empty-suggestion {
  font-size: 16px;
  color: #909399;
  margin-bottom: 35px;
  max-width: 450px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

:deep(.empty-result .el-empty__image) {
  filter: drop-shadow(0 6px 10px rgba(0, 0, 0, 0.15));
  transition: all 0.5s ease;
}

:deep(.empty-result:hover .el-empty__image) {
  transform: scale(1.05) translateY(-5px);
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.2));
}

:deep(.empty-result .el-button) {
  margin-top: 15px;
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

:deep(.empty-result .el-button:hover) {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-section {
    height: 350px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.4rem;
  }
  
  .search-card {
    padding: 25px;
  }
  
  .result-section {
    padding: 25px;
  }
  
  .scenic-image, .hotel-image {
    height: 200px;
  }
}

@media (max-width: 480px) {
  .hero-section {
    height: 300px;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1.2rem;
  }
  
  .content-wrapper {
    padding: 0 15px;
  }
  
  .search-card {
    padding: 20px;
  }
  
  .form-section-title {
    font-size: 1.4rem;
  }
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

/* 新增动画和转场效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.scenic-card:nth-child(odd), .hotel-card:nth-child(odd) {
  animation: float 6s ease-in-out infinite;
}

.scenic-card:nth-child(even), .hotel-card:nth-child(even) {
  animation: float 8s ease-in-out infinite;
  animation-delay: 2s;
}
</style> 