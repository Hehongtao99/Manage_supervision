<template>
  <div class="hotel-list-container">
    <div class="hotel-header">
      <h2 class="hotel-title">酒店管理</h2>
      <el-button type="primary" @click="handleAddHotel">添加酒店</el-button>
    </div>

    <!-- 搜索过滤框 -->
    <div class="hotel-search">
      <el-form :inline="true" :model="queryParams" class="search-form" label-width="80px">
        <el-form-item label="酒店名称">
          <el-input v-model="queryParams.name" placeholder="请输入酒店名称" clearable @keyup.enter="handleSearch" class="wider-input" />
        </el-form-item>
        <el-form-item label="省份">
          <region-select 
            v-model="queryParams.provinceId" 
            level="province" 
            placeholder="请选择省份"
            @change="handleProvinceChange"
            class="wider-select"
          />
        </el-form-item>
        <el-form-item label="城市">
          <region-select 
            v-model="queryParams.cityId" 
            level="city"
            :parent-id="queryParams.provinceId" 
            placeholder="请选择城市"
            :disabled="!queryParams.provinceId"
            @change="handleCityChange"
            class="wider-select"
          />
        </el-form-item>
        <el-form-item label="区县">
          <region-select 
            v-model="queryParams.districtId" 
            level="district"
            :parent-id="queryParams.cityId" 
            placeholder="请选择区县"
            :disabled="!queryParams.cityId"
            class="wider-select"
          />
        </el-form-item>
        <el-form-item label="酒店等级">
          <el-select v-model="queryParams.level" placeholder="请选择酒店等级" clearable class="wider-select">
            <el-option label="五星级" value="五星级" />
            <el-option label="四星级" value="四星级" />
            <el-option label="三星级" value="三星级" />
            <el-option label="经济型" value="经济型" />
            <el-option label="未评级" value="未评级" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联景区">
          <scenic-spot-select 
            v-model="queryParams.scenicSpotId" 
            :province-id="queryParams.provinceId"
            :city-id="queryParams.cityId"
            :district-id="queryParams.districtId"
            :disabled="!queryParams.provinceId"
            placeholder="请选择关联景区"
            class="wider-select"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="wider-select">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item class="button-item">
          <el-button type="primary" @click="handleSearch" size="large">搜索</el-button>
          <el-button @click="handleReset" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 酒店列表表格 -->
    <el-table
      v-loading="loading"
      :data="hotelList"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="酒店名称" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleViewHotel(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="酒店图片" width="120">
        <template #default="scope">
          <el-image 
            v-if="scope.row.imageUrl" 
            :src="scope.row.imageUrl" 
            style="width: 80px; height: 60px;" 
            fit="cover"
            :preview-src-list="[scope.row.imageUrl]"
          />
          <span v-else>暂无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="地区" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.locationPath }}
        </template>
      </el-table-column>
      <el-table-column prop="level" label="酒店等级" width="100" />
      <el-table-column label="关联景区" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.scenicSpotName">
            {{ scope.row.scenicSpotName }}
            <el-tag v-if="scope.row.distanceToSpot" size="small" type="success">
              距离{{ scope.row.distanceToSpot }}km
            </el-tag>
          </span>
          <span v-else>无关联景区</span>
        </template>
      </el-table-column>
      <el-table-column prop="startPrice" label="起始价格" width="120">
        <template #default="scope">
          {{ scope.row.startPrice ? `¥${scope.row.startPrice}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="'active'"
            :inactive-value="'inactive'"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEditHotel(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteHotel(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 酒店表单对话框 -->
    <el-dialog
      :title="dialogInfo.title"
      v-model="dialogInfo.visible"
      width="70%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <HotelForm
        v-if="dialogInfo.visible"
        :hotel="currentHotel"
        :is-edit="dialogInfo.isEdit"
        @submit="handleFormSubmit"
        @cancel="dialogInfo.visible = false"
      />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { HotelData, HotelQueryParams } from '../../types/hotel'
import { getHotelList, deleteHotel, toggleHotelStatus } from '../../api/hotel'
import HotelForm from './components/HotelForm.vue'
import RegionSelect from '../../components/RegionSelect.vue'
import ScenicSpotSelect from '../../components/ScenicSpotSelect.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询参数
const queryParams = reactive<HotelQueryParams>({
  page: 1,
  size: 10,
  name: '',
  provinceId: undefined,
  cityId: undefined,
  districtId: undefined,
  level: undefined,
  scenicSpotId: undefined,
  status: undefined
})

// 酒店列表数据
const hotelList = ref<HotelData[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框信息
const dialogInfo = reactive({
  visible: false,
  title: '添加酒店',
  isEdit: false
})

// 当前编辑的酒店
const currentHotel = ref<HotelData>({} as HotelData)

// 获取酒店列表
const fetchHotelList = async () => {
  loading.value = true
  try {
    const res = await getHotelList({
      ...queryParams,
      page: pagination.page,
      size: pagination.size
    })
    console.log('酒店列表API返回数据:', res)
    if (res && Array.isArray(res.content)) {
      console.log('酒店数据条数:', res.content.length)
      hotelList.value = res.content
      pagination.total = res.total || 0
      pagination.page = res.page || 1
      pagination.size = res.size || 10
    } else {
      console.error('API返回的数据格式不正确:', res)
      hotelList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取酒店列表失败:', error)
    ElMessage.error('获取酒店列表失败，请稍后重试')
    hotelList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchHotelList()
}

// 重置搜索条件
const handleReset = () => {
  Object.keys(queryParams).forEach(key => {
    if (key !== 'page' && key !== 'size') {
      // @ts-ignore
      queryParams[key] = undefined
    }
  })
  pagination.page = 1
  fetchHotelList()
}

// 添加酒店
const handleAddHotel = () => {
  currentHotel.value = {
    name: '',
    provinceId: null
  } as HotelData
  dialogInfo.title = '添加酒店'
  dialogInfo.isEdit = false
  dialogInfo.visible = true
}

// 编辑酒店
const handleEditHotel = (row: HotelData) => {
  currentHotel.value = { ...row }
  dialogInfo.title = '编辑酒店'
  dialogInfo.isEdit = true
  dialogInfo.visible = true
}

// 查看酒店详情
const handleViewHotel = (row: HotelData) => {
  router.push(`/admin/hotels/${row.id}`)
}

// 删除酒店
const handleDeleteHotel = (row: HotelData) => {
  ElMessageBox.confirm(`确认删除酒店"${row.name}"吗？`, '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteHotel(row.id!)
      ElMessage.success('删除成功')
      fetchHotelList()
    } catch (error) {
      console.error('删除酒店失败:', error)
      ElMessage.error('删除酒店失败，请稍后重试')
    }
  }).catch(() => {})
}

// 切换酒店状态
const handleStatusChange = async (row: HotelData) => {
  try {
    await toggleHotelStatus(row.id!)
    ElMessage.success(`已${row.status === 'active' ? '启用' : '禁用'}酒店`)
  } catch (error) {
    console.error('切换酒店状态失败:', error)
    ElMessage.error('操作失败，请稍后重试')
    // 回滚状态
    row.status = row.status === 'active' ? 'inactive' : 'active'
  }
}

// 表单提交
const handleFormSubmit = () => {
  dialogInfo.visible = false
  fetchHotelList()
}

// 分页改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchHotelList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchHotelList()
}

// 地区选择联动
const handleProvinceChange = () => {
  queryParams.cityId = undefined
  queryParams.districtId = undefined
  queryParams.scenicSpotId = undefined
}

const handleCityChange = () => {
  queryParams.districtId = undefined
  queryParams.scenicSpotId = undefined
}

// 页面加载时获取列表
onMounted(() => {
  fetchHotelList()
})
</script>

<style scoped>
.hotel-list-container {
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
  position: relative;
  /* 自定义滚动条 - Firefox */
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 #f5f7fa;
}

/* 自定义滚动条 - Webkit (Chrome, Safari, Edge) */
.hotel-list-container::-webkit-scrollbar {
  width: 8px;
  background-color: #f5f7fa;
}

.hotel-list-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 4px;
}

.hotel-list-container::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.hotel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.hotel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.hotel-search {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 25px;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.wider-input,
.wider-select {
  width: 240px !important;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
  margin-right: 5px;
}

:deep(.el-form-item__label) {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

:deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

.button-item {
  margin-top: 5px;
}

:deep(.button-item .el-button) {
  padding: 12px 20px;
  font-size: 15px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 