<!--
  广告申请管理页面
-->
<template>
  <div class="advertisement-management">
    <el-card class="search-card">
      <div class="search-header">
        <h3>广告申请管理</h3>
        <div class="search-actions">
          <el-button type="primary" @click="handleCreate">创建申请</el-button>
        </div>
      </div>
      
      <!-- 优化后的筛选表单 -->
      <div class="search-container">
        <el-form :inline="false" :model="searchForm" class="search-form">
          <div class="search-grid">
            <el-form-item label="申请编号" class="search-item">
              <el-input 
                v-model="searchForm.applicationNumber" 
                placeholder="请输入申请编号" 
                clearable
              >
                <template #prefix>
                  <el-icon><Document /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="区域" class="search-item">
              <el-input 
                v-model="searchForm.area" 
                placeholder="请输入区域" 
                clearable
              >
                <template #prefix>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="广告位置" class="search-item">
              <el-input 
                v-model="searchForm.location" 
                placeholder="请输入广告位置" 
                clearable
              >
                <template #prefix>
                  <el-icon><Position /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="广告类型" class="search-item">
              <el-select 
                v-model="searchForm.adType" 
                placeholder="请选择广告类型" 
                clearable
                style="width: 100%"
              >
                <el-option label="墙体广告" value="墙体广告" />
                <el-option label="立柱广告" value="立柱广告" />
                <el-option label="LED显示屏" value="LED显示屏" />
                <el-option label="灯箱广告" value="灯箱广告" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="广告性质" class="search-item">
              <el-select 
                v-model="searchForm.adNature" 
                placeholder="请选择广告性质" 
                clearable
                style="width: 100%"
              >
                <el-option label="商业" value="商业" />
                <el-option label="公益" value="公益" />
                <el-option label="政府" value="政府" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="面积范围 (㎡)" class="search-item size-range-item">
              <div class="size-range-container">
                <el-input-number 
                  v-model="searchForm.minSize" 
                  :min="0" 
                  :precision="2" 
                  :step="0.1" 
                  placeholder="最小值"
                  controls-position="right"
                  class="size-input"
                />
                <div class="range-separator">
                  <el-icon><Minus /></el-icon>
                </div>
                <el-input-number 
                  v-model="searchForm.maxSize" 
                  :min="0" 
                  :precision="2" 
                  :step="0.1" 
                  placeholder="最大值"
                  controls-position="right"
                  class="size-input"
                />
              </div>
            </el-form-item>
            
            <el-form-item label="状态" class="search-item">
              <el-select 
                v-model="searchForm.status" 
                placeholder="请选择状态" 
                clearable
                style="width: 100%"
              >
                <el-option label="待审核" value="pending">
                  <span class="status-option">
                    <el-tag type="warning" size="small">待审核</el-tag>
                  </span>
                </el-option>
                <el-option label="已批准" value="approved">
                  <span class="status-option">
                    <el-tag type="success" size="small">已批准</el-tag>
                  </span>
                </el-option>
                <el-option label="已拒绝" value="rejected">
                  <span class="status-option">
                    <el-tag type="danger" size="small">已拒绝</el-tag>
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item class="search-item search-buttons">
              <div class="button-group">
                <el-button type="primary" @click="handleSearch" :icon="Search">
                  查询
                </el-button>
                <el-button @click="resetSearch" :icon="RefreshRight">
                  重置
                </el-button>
              </div>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="advertisementList" style="width: 100%" border>
        <el-table-column prop="applicationNumber" label="编号" width="180" />
        <el-table-column prop="area" label="区域" width="120" />
        <el-table-column prop="location" label="广告位置" width="180" />
        <el-table-column prop="adType" label="广告设置类型" width="150" />
        <el-table-column prop="adNature" label="广告性质" width="120" />
        <el-table-column prop="size" label="面积(㎡)" width="100" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="success" 
              link 
              @click="handleApprove(scope.row)">批准</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="danger" 
              link 
              @click="handleReject(scope.row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
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
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '创建广告申请' : '编辑广告申请'"
      width="900px"
    >
      <div class="dialog-content">
        <div class="region-tree-container">
          <h4><el-icon><Location /></el-icon> 省市区选择</h4>
          <el-card shadow="hover" class="region-card">
            <el-tree
              :data="regionTreeData"
              :props="regionTreeProps"
              :load="loadRegionNode"
              lazy
              @node-click="handleRegionNodeClick"
              highlight-current
              node-key="id"
              :default-expanded-keys="expandedKeys"
              class="region-tree"
              :expand-on-click-node="false"
            />
          </el-card>

          <transition name="fade">
            <div v-if="regionInfo.imageUrl" class="region-image">
              <el-card shadow="hover" class="image-card">
                <div class="image-title">街道图片</div>
                <img :src="regionInfo.imageUrl" alt="街道图片" class="street-image" />
              </el-card>
            </div>
          </transition>
          
          <transition name="fade">
            <div v-if="regionInfo.longitude && regionInfo.latitude" class="region-location">
              <el-card shadow="hover" class="location-card">
                <div class="location-header">
                  <el-icon class="location-icon"><MapLocation /></el-icon> 位置信息
                </div>
                <div class="location-info">
                  <div class="location-item">
                    <span class="location-label">经度：</span>
                    <span class="location-value">{{ regionInfo.longitude }}</span>
                  </div>
                  <div class="location-item">
                    <span class="location-label">纬度：</span>
                    <span class="location-value">{{ regionInfo.latitude }}</span>
                  </div>
                </div>
              </el-card>
            </div>
          </transition>
        </div>

        <div class="form-wrapper">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            label-position="right"
            class="form-container"
          >
            <el-form-item label="区域" prop="area">
              <el-input v-model="form.area" placeholder="区域" readonly />
            </el-form-item>
            <el-form-item label="详细地址" prop="detailedAddress">
              <el-input v-model="form.detailedAddress" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item label="广告位置" prop="location">
              <el-input v-model="form.location" placeholder="请输入广告位置" />
            </el-form-item>
            <el-form-item label="广告类型" prop="adType">
              <el-select v-model="form.adType" placeholder="请选择广告设置类型" style="width: 100%">
                <el-option label="墙体广告" value="墙体广告" />
                <el-option label="立柱广告" value="立柱广告" />
                <el-option label="LED显示屏" value="LED显示屏" />
                <el-option label="灯箱广告" value="灯箱广告" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="广告性质" prop="adNature">
              <el-select v-model="form.adNature" placeholder="请选择广告性质" style="width: 100%">
                <el-option label="商业" value="商业" />
                <el-option label="公益" value="公益" />
                <el-option label="政府" value="政府" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="面积(㎡)" prop="size">
              <el-input-number v-model="form.size" :min="0.1" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="广告申请详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请编号">{{ currentItem.applicationNumber }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ currentItem.area }}</el-descriptions-item>
        <el-descriptions-item label="省市区">
          {{ currentItem.provinceName || '' }}
          {{ currentItem.cityName || '' }}
          {{ currentItem.districtName || '' }}
          {{ currentItem.streetName || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ currentItem.detailedAddress || '无' }}</el-descriptions-item>
        <el-descriptions-item label="广告位置">{{ currentItem.location }}</el-descriptions-item>
        <el-descriptions-item label="广告设置类型">{{ currentItem.adType }}</el-descriptions-item>
        <el-descriptions-item label="广告性质">{{ currentItem.adNature }}</el-descriptions-item>
        <el-descriptions-item label="面积(㎡)">{{ currentItem.size }}</el-descriptions-item>
        <el-descriptions-item label="经度">{{ currentItem.longitude || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="纬度">{{ currentItem.latitude || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentItem.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentItem.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentItem.streetImageUrl" label="街道图片">
          <img :src="currentItem.streetImageUrl" class="street-detail-image" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <div v-if="currentItem.status === 'pending'">
            <el-button type="success" @click="handleApprove(currentItem)">批准申请</el-button>
            <el-button type="danger" @click="handleReject(currentItem)">拒绝申请</el-button>
          </div>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewType === 'approve' ? '批准申请' : '拒绝申请'"
      width="500px"
    >
      <el-form ref="reviewFormRef" :model="reviewForm" label-width="80px">
        <el-form-item label="备注">
          <el-input 
            v-model="reviewForm.remark" 
            type="textarea" 
            :rows="3" 
            :placeholder="reviewType === 'approve' ? '请输入批准备注' : '请输入拒绝理由'" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import axios from '../../utils/axios'
import { useUserStore } from '../../stores/user'

// 添加图标组件导入
import { Location, MapLocation, Search, RefreshRight, Document, Minus, Position } from '@element-plus/icons-vue'

const userStore = useUserStore()
const currentUser = userStore.user

// 列表查询相关
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const advertisementList = ref([])

// 搜索表单
const searchForm = reactive({
  applicationNumber: '',
  area: '',
  location: '',
  adType: '',
  adNature: '',
  minSize: null as number | null,
  maxSize: null as number | null,
  status: ''
})

// 创建/编辑表单相关
const dialogVisible = ref(false)
const dialogType = ref('create')  // create或edit
const formRef = ref<FormInstance>()
const form = reactive({
  id: null,
  area: '',
  location: '',
  adType: '',
  adNature: '',
  size: 1.0,
  remark: '',
  applicantId: currentUser.id,
  detailedAddress: '',
  longitude: null,
  latitude: null,
  provinceId: null,
  cityId: null,
  districtId: null,
  streetId: null
})

// 地区选择相关
const selectedRegion = reactive({
  province: null,
  city: null,
  district: null,
  street: null
})

const regionOptions = reactive({
  provinces: [],
  cities: [],
  districts: [],
  streets: []
})

const regionInfo = reactive({
  longitude: null,
  latitude: null,
  imageUrl: null
})

// 添加级联选择器需要的数据
const regionCascadeValue = ref([])
const regionTreeData = ref([])
const regionCascaderProps = reactive({
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: false,
  expandTrigger: 'hover',
  lazy: true,
  lazyLoad: async (node, resolve) => {
    if (node.level === 0) {
      // 加载省份
      try {
        const response = await axios.get('/api/regions/children/0')
        const provinces = response.data
        resolve(provinces)
      } catch (error) {
        console.error('加载省份数据失败', error)
        ElMessage.error('加载省份数据失败')
        resolve([])
      }
    } else {
      // 加载子节点
      try {
        const parentId = node.data.id
        const response = await axios.get(`/api/regions/children/${parentId}`)
        const children = response.data
        resolve(children)
      } catch (error) {
        console.error('加载子节点数据失败', error)
        ElMessage.error('加载子节点数据失败')
        resolve([])
      }
    }
  }
})

// 修改为树形结构需要的数据
const regionTreeProps = reactive({
  label: 'name',
  children: 'children',
  isLeaf: 'isLeaf'
})

const expandedKeys = ref([]) // 存储展开的节点ID

// 表单验证规则
const rules = {
  area: [{ required: true, message: '请输入区域', trigger: 'blur' }],
  location: [{ required: true, message: '请输入广告位置', trigger: 'blur' }],
  adType: [{ required: true, message: '请选择广告设置类型', trigger: 'change' }],
  adNature: [{ required: true, message: '请选择广告性质', trigger: 'change' }],
  size: [{ required: true, message: '请输入面积', trigger: 'blur' }]
}

// 查看详情相关
const viewDialogVisible = ref(false)
const currentItem = ref({})

// 审核相关
const reviewDialogVisible = ref(false)
const reviewType = ref('approve')  // approve或reject
const reviewFormRef = ref<FormInstance>()
const reviewForm = reactive({
  id: null,
  status: '',
  remark: ''
})

// 状态格式化
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已批准'
    case 'rejected': return '已拒绝'
    default: return '未知'
  }
}

// 状态标签类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

// 加载广告申请列表
const loadAdvertisementList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      applicationNumber: searchForm.applicationNumber || null,
      area: searchForm.area || null,
      location: searchForm.location || null,
      adType: searchForm.adType || null,
      adNature: searchForm.adNature || null,
      minSize: searchForm.minSize || null,
      maxSize: searchForm.maxSize || null,
      status: searchForm.status || null
    }

    const response = await axios.get('/api/advertisement', { params })
    advertisementList.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载广告申请列表失败', error)
    ElMessage.error('加载广告申请列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadAdvertisementList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.applicationNumber = ''
  searchForm.area = ''
  searchForm.location = ''
  searchForm.adType = ''
  searchForm.adNature = ''
  searchForm.minSize = null
  searchForm.maxSize = null
  searchForm.status = ''
  handleSearch()
}

// 加载省份数据
const loadProvinces = async () => {
  try {
    const response = await axios.get('/api/regions/children/0')
    regionOptions.provinces = response.data
  } catch (error) {
    console.error('加载省份数据失败', error)
    ElMessage.error('加载省份数据失败')
  }
}

// 加载城市数据
const loadCities = async (provinceId) => {
  if (!provinceId) {
    regionOptions.cities = []
    return
  }
  try {
    const response = await axios.get(`/api/regions/children/${provinceId}`)
    regionOptions.cities = response.data
  } catch (error) {
    console.error('加载城市数据失败', error)
    ElMessage.error('加载城市数据失败')
  }
}

// 加载区县数据
const loadDistricts = async (cityId) => {
  if (!cityId) {
    regionOptions.districts = []
    return
  }
  try {
    const response = await axios.get(`/api/regions/children/${cityId}`)
    regionOptions.districts = response.data
  } catch (error) {
    console.error('加载区县数据失败', error)
    ElMessage.error('加载区县数据失败')
  }
}

// 加载街道数据
const loadStreets = async (districtId) => {
  if (!districtId) {
    regionOptions.streets = []
    return
  }
  try {
    const response = await axios.get(`/api/regions/children/${districtId}`)
    regionOptions.streets = response.data
  } catch (error) {
    console.error('加载街道数据失败', error)
    ElMessage.error('加载街道数据失败')
  }
}

// 加载街道详情
const loadStreetDetail = async (streetId) => {
  if (!streetId) {
    regionInfo.longitude = null
    regionInfo.latitude = null
    regionInfo.imageUrl = null
    return
  }
  try {
    const response = await axios.get(`/api/regions/${streetId}`)
    const streetData = response.data
    regionInfo.longitude = streetData.longitude
    regionInfo.latitude = streetData.latitude
    regionInfo.imageUrl = streetData.imageUrl

    // 将经纬度设置到表单
    form.longitude = streetData.longitude
    form.latitude = streetData.latitude
  } catch (error) {
    console.error('加载街道详情失败', error)
    ElMessage.error('加载街道详情失败')
  }
}

// 省份选择改变
const handleProvinceChange = (provinceId) => {
  selectedRegion.city = null
  selectedRegion.district = null
  selectedRegion.street = null
  regionOptions.cities = []
  regionOptions.districts = []
  regionOptions.streets = []
  form.provinceId = provinceId
  // 清空区域
  form.area = ''
  form.detailedAddress = ''
  // 加载城市数据
  loadCities(provinceId)
  // 获取省份名称
  const province = regionOptions.provinces.find(p => p.id === provinceId)
  if (province) {
    updateAreaField()
  }
}

// 城市选择改变
const handleCityChange = (cityId) => {
  selectedRegion.district = null
  selectedRegion.street = null
  regionOptions.districts = []
  regionOptions.streets = []
  form.cityId = cityId
  // 加载区县数据
  loadDistricts(cityId)
  updateAreaField()
}

// 区县选择改变
const handleDistrictChange = (districtId) => {
  selectedRegion.street = null
  regionOptions.streets = []
  form.districtId = districtId
  // 加载街道数据
  loadStreets(districtId)
  updateAreaField()
}

// 街道选择改变
const handleStreetChange = (streetId) => {
  form.streetId = streetId
  // 加载街道详情
  loadStreetDetail(streetId)
  updateAreaField()
}

// 更新区域字段
const updateAreaField = () => {
  const provinceObj = regionOptions.provinces.find(p => p.id === selectedRegion.province)
  const cityObj = regionOptions.cities.find(c => c.id === selectedRegion.city)
  const districtObj = regionOptions.districts.find(d => d.id === selectedRegion.district)
  const streetObj = regionOptions.streets.find(s => s.id === selectedRegion.street)

  let area = ''
  let address = ''

  if (provinceObj) {
    area += provinceObj.name
    address += provinceObj.name
  }
  if (cityObj) {
    area += cityObj.name
    address += cityObj.name
  }
  if (districtObj) {
    area += districtObj.name
    address += districtObj.name
  }
  if (streetObj) {
    area += streetObj.name
    address += streetObj.name
  }
  form.area = area
  form.detailedAddress = address
}

// 新增级联选择器变化处理方法
const handleRegionChange = async (value) => {
  if (!value || value.length === 0) {
    // 清空选择
    form.provinceId = null
    form.cityId = null
    form.districtId = null
    form.streetId = null
    form.area = ''
    form.detailedAddress = ''
    regionInfo.longitude = null
    regionInfo.latitude = null
    regionInfo.imageUrl = null
    form.longitude = null
    form.latitude = null
    return
  }

  // 根据级联选择器的值设置表单字段
  const level = value.length
  let provinceId, cityId, districtId, streetId
  let area = ''
  let address = ''
  
  // 获取最后一个选中节点的详细信息
  const lastNodeId = value[value.length - 1]
  const response = await axios.get(`/api/regions/${lastNodeId}`)
  const nodeData = response.data
  
  // 递归获取完整路径
  const pathResponse = await axios.get(`/api/regions/${lastNodeId}/path`)
  const pathData = pathResponse.data
  
  // 根据路径设置各级ID和名称
  for (const node of pathData) {
    if (node.level === 1) {
      provinceId = node.id
      area += node.name
      address += node.name
      form.provinceId = node.id
    } else if (node.level === 2) {
      cityId = node.id
      area += node.name
      address += node.name
      form.cityId = node.id
    } else if (node.level === 3) {
      districtId = node.id
      area += node.name
      address += node.name
      form.districtId = node.id
    } else if (node.level === 4) {
      streetId = node.id
      area += node.name
      address += node.name
      form.streetId = node.id
      
      // 如果是街道级别，获取经纬度和图片
      regionInfo.longitude = node.longitude
      regionInfo.latitude = node.latitude
      regionInfo.imageUrl = node.imageUrl
      form.longitude = node.longitude ? node.longitude.valueOf() : null
      form.latitude = node.latitude ? node.latitude.valueOf() : null
    }
  }
  
  form.area = area
  form.detailedAddress = address
}

// 创建申请
const handleCreate = () => {
  dialogType.value = 'create'
  form.id = null
  form.area = ''
  form.location = ''
  form.adType = ''
  form.adNature = ''
  form.size = 1.0
  form.remark = ''
  form.applicantId = currentUser.id
  form.detailedAddress = ''
  form.longitude = null
  form.latitude = null
  form.provinceId = null
  form.cityId = null
  form.districtId = null
  form.streetId = null

  // 重置区域选择
  selectedRegion.province = null
  selectedRegion.city = null
  selectedRegion.district = null
  selectedRegion.street = null
  
  // 重置级联选择器的值
  regionCascadeValue.value = []
  
  // 清空展开的节点
  expandedKeys.value = []

  // 重置区域信息
  regionInfo.longitude = null
  regionInfo.latitude = null
  regionInfo.imageUrl = null

  // 加载省份数据
  loadProvinces()
  dialogVisible.value = true
  // 下一帧重置表单验证
  setTimeout(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  }, 0)
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'create') {
          await axios.post('/api/advertisement', form)
          ElMessage.success('创建成功')
        } else {
          await axios.put(`/api/advertisement/${form.id}`, form)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        loadAdvertisementList()
      } catch (error) {
        console.error('操作失败', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 查看详情
const handleView = async (row) => {
  try {
    // 获取广告详细信息
    const response = await axios.get(`/api/advertisement/${row.id}`);
    currentItem.value = response.data;
    
    // 确保经纬度是数字类型
    if (currentItem.value.longitude) {
      currentItem.value.longitude = Number(currentItem.value.longitude);
    }
    if (currentItem.value.latitude) {
      currentItem.value.latitude = Number(currentItem.value.latitude);
    }
    
    // 显示详情对话框
    viewDialogVisible.value = true;
  } catch (error) {
    console.error('获取广告详情失败', error);
    ElMessage.error('获取广告详情失败');
    
    // 如果获取失败，使用原始行数据
    currentItem.value = { ...row };
    
    // 确保经纬度是正确的类型
    if (currentItem.value.longitude) {
      currentItem.value.longitude = Number(currentItem.value.longitude);
    }
    if (currentItem.value.latitude) {
      currentItem.value.latitude = Number(currentItem.value.latitude);
    }
    
    viewDialogVisible.value = true;
  }
}

// 批准申请
const handleApprove = (row) => {
  reviewType.value = 'approve'
  reviewForm.id = row.id
  reviewForm.status = 'approved'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

// 编辑广告申请
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.area = row.area || ''
  form.location = row.location || ''
  form.adType = row.adType || ''
  form.adNature = row.adNature || ''
  form.size = row.size || 1.0
  form.remark = row.remark || ''
  form.applicantId = row.applicantId || currentUser.id
  form.detailedAddress = row.detailedAddress || ''
  form.longitude = row.longitude || null
  form.latitude = row.latitude || null
  form.provinceId = row.provinceId || null
  form.cityId = row.cityId || null
  form.districtId = row.districtId || null
  form.streetId = row.streetId || null
  
  // 重置区域信息
  regionInfo.longitude = row.longitude
  regionInfo.latitude = row.latitude
  regionInfo.imageUrl = row.streetImageUrl
  
  // 设置展开节点
  expandedKeys.value = []
  if (row.provinceId) expandedKeys.value.push(row.provinceId)
  if (row.cityId) expandedKeys.value.push(row.cityId)
  if (row.districtId) expandedKeys.value.push(row.districtId)
  
  dialogVisible.value = true
  
  // 下一帧重置表单验证
  setTimeout(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  }, 0)
}

// 拒绝申请
const handleReject = (row) => {
  reviewType.value = 'reject'
  reviewForm.id = row.id
  reviewForm.status = 'rejected'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  try {
    await axios.post(`/api/advertisement/${reviewForm.id}/review`, {
      status: reviewForm.status,
      remark: reviewForm.remark
    })
    
    ElMessage.success(reviewType.value === 'approve' ? '已批准申请' : '已拒绝申请')
    reviewDialogVisible.value = false
    viewDialogVisible.value = false
    loadAdvertisementList()
  } catch (error) {
    console.error('审核失败', error)
    ElMessage.error('审核失败')
  }
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadAdvertisementList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadAdvertisementList()
}

// 加载树节点数据
const loadRegionNode = async (node, resolve) => {
  const loading = ElMessage.info({
    message: '正在加载地区数据...',
    duration: 0,
    showClose: false
  })
  
  try {
    let parentId = 0
    if (node.level > 0) {
      parentId = node.data.id
    }
    
    const response = await axios.get(`/api/regions/children/${parentId}`)
    const children = response.data
    
    // 为最后一级节点（街道）添加叶子节点标记
    if (node.level === 3) {
      children.forEach(item => {
        item.isLeaf = true
      })
    }
    
    // 延迟一小段时间，让动画效果更明显
    setTimeout(() => {
      resolve(children)
      loading.close()
    }, 300)
  } catch (error) {
    console.error('加载地区数据失败', error)
    ElMessage.error('加载地区数据失败')
    resolve([])
    loading.close()
  }
}

// 树节点点击事件
const handleRegionNodeClick = async (data, node) => {
  // 节点点击后的处理逻辑
  if (node.isLeaf) {
    // 如果是叶子节点（街道级别），加载详细信息
    await loadNodeDetail(data.id, data)
  } else {
    // 非叶子节点
    if (node.level === 1) {
      // 省级
      form.provinceId = data.id
      form.cityId = null
      form.districtId = null
      form.streetId = null
      updateRegionInfo(data, null, null, null)
    } else if (node.level === 2) {
      // 市级
      form.provinceId = node.parent.data.id
      form.cityId = data.id
      form.districtId = null
      form.streetId = null
      updateRegionInfo(node.parent.data, data, null, null)
    } else if (node.level === 3) {
      // 区县级
      form.provinceId = node.parent.parent.data.id
      form.cityId = node.parent.data.id
      form.districtId = data.id
      form.streetId = null
      updateRegionInfo(node.parent.parent.data, node.parent.data, data, null)
    }
  }
}

// 加载节点详细信息
const loadNodeDetail = async (nodeId, nodeData) => {
  try {
    const response = await axios.get(`/api/regions/${nodeId}`)
    const nodeDetail = response.data
    
    if (nodeDetail.level === 4) {
      // 街道级别
      form.streetId = nodeDetail.id
      
      // 递归获取完整路径
      const pathResponse = await axios.get(`/api/regions/${nodeId}/path`)
      const pathData = pathResponse.data
      
      let province, city, district, street
      
      // 根据路径设置各级ID和名称
      for (const node of pathData) {
        if (node.level === 1) {
          province = node
          form.provinceId = node.id
        } else if (node.level === 2) {
          city = node
          form.cityId = node.id
        } else if (node.level === 3) {
          district = node
          form.districtId = node.id
        } else if (node.level === 4) {
          street = node
          form.streetId = node.id
        }
      }
      
      // 展开所有父节点
      if (province) expandedKeys.value.push(province.id)
      if (city) expandedKeys.value.push(city.id)
      if (district) expandedKeys.value.push(district.id)
      
      // 更新区域信息
      updateRegionInfo(province, city, district, street)
      
      // 如果是街道级别，获取经纬度和图片
      regionInfo.longitude = nodeDetail.longitude
      regionInfo.latitude = nodeDetail.latitude
      regionInfo.imageUrl = nodeDetail.imageUrl
      form.longitude = nodeDetail.longitude ? nodeDetail.longitude.valueOf() : null
      form.latitude = nodeDetail.latitude ? nodeDetail.latitude.valueOf() : null
    }
    
  } catch (error) {
    console.error('加载节点详情失败', error)
    ElMessage.error('加载节点详情失败')
  }
}

// 更新区域信息
const updateRegionInfo = (province, city, district, street) => {
  let area = []
  
  if (province) area.push(province.name)
  if (city) area.push(city.name)
  if (district) area.push(district.name)
  if (street) area.push(street.name)
  
  form.area = area.join('')
  // 不再自动填充详细地址，让用户自行填写具体门牌号等信息
}

// 页面加载时获取数据
onMounted(() => {
  loadAdvertisementList()
  loadProvinces()
})
</script>

<style scoped>
.advertisement-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-header h3 {
  margin: 0;
}

.search-container {
  background: linear-gradient(135deg, #f5f9ff 0%, #ebf4ff 100%);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e6f1fc;
  box-shadow: 0 2px 12px rgba(44, 140, 240, 0.08);
}

.search-form {
  width: 100%;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px 16px;
  align-items: end;
}

.search-item {
  margin: 0;
  flex: 1;
  min-width: 0;
}

.search-item :deep(.el-form-item__label) {
  color: #4a5568;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.search-item :deep(.el-input) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-item :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-item :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(44, 140, 240, 0.15);
  border-color: #2c8cf0;
}

.search-item :deep(.el-input__wrapper.is-focus) {
  border-color: #2c8cf0;
  box-shadow: 0 0 0 3px rgba(44, 140, 240, 0.1);
}

.search-item :deep(.el-select) {
  width: 100%;
}

.search-item :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-item :deep(.el-select .el-input__wrapper:hover) {
  border-color: #2c8cf0;
  box-shadow: 0 2px 8px rgba(44, 140, 240, 0.15);
}

.size-range-item {
  grid-column: span 1;
}

.size-range-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.size-input {
  flex: 1;
  min-width: 0;
}

.size-input :deep(.el-input-number) {
  width: 100%;
}

.size-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.size-input :deep(.el-input__wrapper:hover) {
  border-color: #2c8cf0;
  box-shadow: 0 2px 8px rgba(44, 140, 240, 0.15);
}

.range-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #2c8cf0 0%, #43a6ff 100%);
  border-radius: 50%;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(44, 140, 240, 0.25);
}

.search-buttons {
  grid-column: span 1;
  display: flex;
  align-items: end;
}

.button-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.button-group .el-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 12px 20px;
  min-width: 0;
}

.button-group .el-button--primary {
  background: linear-gradient(135deg, #2c8cf0 0%, #43a6ff 100%);
  border: none;
  box-shadow: 0 3px 12px rgba(44, 140, 240, 0.3);
}

.button-group .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 16px rgba(44, 140, 240, 0.4);
}

.button-group .el-button:not(.el-button--primary) {
  background: white;
  border: 1px solid #d4e6f1;
  color: #4a5568;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.button-group .el-button:not(.el-button--primary):hover {
  background: #f8fcff;
  border-color: #2c8cf0;
  color: #2c8cf0;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(44, 140, 240, 0.15);
}

.status-option {
  display: flex;
  align-items: center;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px 12px;
  }
}

@media (max-width: 768px) {
  .search-container {
    padding: 16px;
  }
  
  .search-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .size-range-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .size-input {
    width: 100%;
  }
  
  .range-separator {
    transform: rotate(90deg);
  }
  
  .button-group {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .search-container {
    padding: 12px;
  }
  
  .button-group .el-button {
    padding: 10px 16px;
    font-size: 14px;
  }
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-content {
  display: flex;
  min-height: 500px;
  gap: 30px;
  overflow: visible;
}

.form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
  border-left: 1px solid #E6F1FC;
  padding-left: 30px;
  padding-right: 15px;
  min-width: 0; /* 允许flexbox子元素收缩 */
}

.form-container {
  flex: 1;
  min-width: 0; /* 确保表单元素可以收缩，不会溢出 */
}

.region-tree-container {
  flex: none;
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 340px;
  min-width: 340px;
  max-width: 340px;
  height: 100%;
  overflow: visible;
  padding-right: 0;
  padding-bottom: 10px;
  position: relative;
  z-index: 10;
}

.region-tree-container h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #2C8CF0;
  font-size: 16px;
  font-weight: 600;
  position: relative;
  padding: 8px 0 8px 15px;
  border-left: 4px solid #2C8CF0;
  background: linear-gradient(90deg, rgba(44, 140, 240, 0.08) 0%, rgba(44, 140, 240, 0) 100%);
  border-radius: 0 4px 4px 0;
}

.region-tree-container h4 .el-icon {
  margin-right: 8px;
  color: #2C8CF0;
  font-size: 18px;
  vertical-align: middle;
}

.region-tree {
  height: 380px;
  min-height: 380px;
  overflow-y: auto;
  overflow-x: hidden;
  border: 1px solid #E6F1FC;
  border-radius: 8px;
  padding: 15px 5px 15px 15px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.05);
  background-color: #FAFCFF;
  scrollbar-width: thin;
  scrollbar-color: #C0D8F0 #F5F9FF;
  margin-right: 10px;
  position: relative;
}

/* 让树的内容可以撑开足够宽度 */
.region-tree :deep(.el-scrollbar__wrap) {
  overflow-x: visible !important;
}

.region-tree :deep(.el-scrollbar__view) {
  min-width: 100%;
  width: fit-content;
}

/* 确保树节点内容不被截断 */
:deep(.el-tree-node__content > span:last-child) {
  overflow: visible;
  text-overflow: clip;
  padding-right: 15px;
}

.region-image {
  margin-top: 15px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.street-image {
  max-width: 100%;
  max-height: 180px;
  border-radius: 4px;
  object-fit: cover;
  transition: transform 0.3s ease;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  border: 3px solid white;
}

.street-image:hover {
  transform: scale(1.02);
}

.region-location {
  margin-top: 15px;
}

.street-detail-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  object-fit: cover;
}

/* 动画过渡效果 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.region-card {
  margin-bottom: 15px;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FCFF 100%);
}

.region-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(44, 140, 240, 0.15);
}

.region-card :deep(.el-card__body) {
  padding: 12px;
}

.image-card, .location-card {
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FCFF 100%);
}

.image-card:hover, .location-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(44, 140, 240, 0.15);
}

.image-title {
  font-size: 14px;
  color: #2C8CF0;
  margin-bottom: 10px;
  font-weight: 500;
  padding: 8px 12px;
  background-color: #F0F8FF;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.location-header {
  font-size: 14px;
  color: #2C8CF0;
  margin-bottom: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #F0F8FF;
  border-radius: 4px;
}

.location-icon {
  margin-right: 8px;
  font-size: 16px;
}

.location-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.location-item {
  display: flex;
  font-size: 13px;
}

.location-label {
  color: #909399;
  width: 50px;
}

.location-value {
  color: #606266;
  font-weight: 500;
}

/* 美化树节点样式 */
:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 6px;
  margin: 4px 0;
  transition: all 0.3s ease;
  padding-left: 5px;
  padding-right: 10px;
  white-space: nowrap;
  overflow: visible;
}

:deep(.el-tree-node__content:hover) {
  background-color: #E6F1FC;
  transform: translateX(2px);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #E6F1FC;
  color: #2C8CF0;
  font-weight: bold;
  box-shadow: 0 2px 6px rgba(44, 140, 240, 0.15);
}

:deep(.el-tree-node__expand-icon) {
  color: #2C8CF0;
  font-size: 14px;
}

:deep(.el-tree-node__expand-icon.expanded) {
  transform: rotate(90deg) scale(1.1);
}

:deep(.el-tree-node__label) {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: visible;
}

:deep(.el-tree-node.is-current .el-tree-node__label) {
  color: #2C8CF0;
}

:deep(.el-form-item__label) {
  color: #606880;
  font-weight: 500;
}

:deep(.el-input__inner) {
  border-radius: 6px;
}

:deep(.el-input__inner:focus) {
  border-color: #2C8CF0;
  box-shadow: 0 0 0 2px rgba(44, 140, 240, 0.1);
}

:deep(.el-select .el-input__inner:focus) {
  border-color: #2C8CF0;
}

:deep(.el-textarea__inner) {
  border-radius: 6px;
}

:deep(.el-textarea__inner:focus) {
  border-color: #2C8CF0;
  box-shadow: 0 0 0 2px rgba(44, 140, 240, 0.1);
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background-color: #F5F9FF;
  border-color: #E6F1FC;
  color: #2C8CF0;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: #2C8CF0;
  background-color: #E6F1FC;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  background: linear-gradient(90deg, #2C8CF0 0%, #43A6FF 100%);
  padding: 15px 20px;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: #FFFFFF;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #FFFFFF;
}

:deep(.el-dialog__body) {
  padding: 25px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #F0F2F5;
  padding: 15px 25px;
}

/* 增强树节点样式 */
:deep(.el-tree-node) {
  white-space: nowrap;
  overflow: visible;
  min-width: 100%;
  width: fit-content;
}

:deep(.el-tree-node__children) {
  overflow: visible;
  min-width: 100%;
  width: fit-content;
}

:deep(.el-tree) {
  min-width: 100%;
  width: fit-content;
}

:deep(.el-tree-node__label) {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: visible;
  display: inline-block;
}

:deep(.el-icon) {
  margin-right: 6px;
  vertical-align: middle;
}

/* 自定义滚动条样式 */
.region-tree::-webkit-scrollbar {
  width: 8px;
}

.region-tree::-webkit-scrollbar-track {
  background: #F5F9FF;
  border-radius: 6px;
}

.region-tree::-webkit-scrollbar-thumb {
  background-color: #C0D8F0;
  border-radius: 6px;
  border: 2px solid #F5F9FF;
}
</style> 