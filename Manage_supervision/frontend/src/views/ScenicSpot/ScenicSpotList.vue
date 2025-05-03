<template>
  <div class="scenic-spot-list-container">
    <div class="scenic-spot-header">
      <h2 class="scenic-spot-title">景区管理</h2>
      <el-button type="primary" @click="handleAddScenicSpot">添加景区</el-button>
    </div>

    <!-- 搜索过滤框 -->
    <div class="scenic-spot-search">
      <el-form :inline="true" :model="queryParams" class="search-form" label-width="80px">
        <el-form-item label="景区名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入景区名称" 
            clearable 
            @keyup.enter="handleSearch"
            class="wider-input"
          />
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
        <el-form-item label="景区等级">
          <el-select 
            v-model="queryParams.level" 
            placeholder="请选择景区等级" 
            clearable
            class="wider-select"
            popper-class="larger-dropdown"
          >
            <el-option label="5A" value="5A" />
            <el-option label="4A" value="4A" />
            <el-option label="3A" value="3A" />
            <el-option label="2A" value="2A" />
            <el-option label="1A" value="1A" />
            <el-option label="未评级" value="UNRATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryParams.status" 
            placeholder="请选择状态" 
            clearable
            class="wider-select"
            popper-class="larger-dropdown"
          >
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" size="large">搜索</el-button>
          <el-button @click="handleReset" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 景区列表表格 -->
    <el-table
      v-loading="loading"
      :data="scenicSpotList"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="景区名称" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleViewScenicSpot(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="景区图片" width="120">
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
      <el-table-column prop="level" label="景区等级" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.level === '5A'" type="success">5A</el-tag>
          <el-tag v-else-if="scope.row.level === '4A'" type="primary">4A</el-tag>
          <el-tag v-else-if="scope.row.level === '3A'" type="warning">3A</el-tag>
          <el-tag v-else-if="scope.row.level === '2A'" type="info">2A</el-tag>
          <el-tag v-else-if="scope.row.level === '1A'" type="info">1A</el-tag>
          <span v-else>未评级</span>
        </template>
      </el-table-column>
      <el-table-column prop="ticketPrice" label="门票价格" width="120">
        <template #default="scope">
          <template v-if="scope.row.ticketPrice === 0 || scope.row.ticketPrice === null || scope.row.ticketPrice === undefined">
            <span class="free-price">免费</span>
          </template>
          <template v-else>
            <span class="ticket-price">¥ {{ formatPrice(scope.row.ticketPrice) }}</span>
          </template>
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
          <el-button size="small" type="primary" @click="handleEditScenicSpot(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteScenicSpot(scope.row)">删除</el-button>
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

    <!-- 景区表单对话框 -->
    <el-dialog
      :title="dialogInfo.title"
      v-model="dialogInfo.visible"
      width="60%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ScenicSpotForm
        v-if="dialogInfo.visible"
        :scenic-spot="currentScenicSpot"
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
import type { ScenicSpotData, ScenicSpotQueryParams } from '../../types/scenicSpot'
import { getScenicSpotList, deleteScenicSpot, updateScenicSpot } from '../../api/scenicSpot'
import ScenicSpotForm from './components/ScenicSpotForm.vue'
import RegionSelect from '../../components/RegionSelect.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询参数
const queryParams = reactive<ScenicSpotQueryParams>({
  page: 1,
  size: 10,
  name: '',
  provinceId: undefined,
  cityId: undefined,
  districtId: undefined,
  level: undefined,
  status: undefined
})

// 景区列表数据
const scenicSpotList = ref<ScenicSpotData[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框信息
const dialogInfo = reactive({
  visible: false,
  title: '添加景区',
  isEdit: false
})

// 当前编辑的景区
const currentScenicSpot = ref<ScenicSpotData>({} as ScenicSpotData)

// 获取景区列表
const fetchScenicSpotList = async () => {
  loading.value = true
  try {
    const res = await getScenicSpotList({
      ...queryParams,
      page: pagination.page,
      size: pagination.size
    })
    console.log('景区列表API返回数据:', res)
    if (res && Array.isArray(res.content)) {
      console.log('景区数据条数:', res.content.length)
      scenicSpotList.value = res.content
      pagination.total = res.total || 0
      pagination.page = res.page || 1
      pagination.size = res.size || 10
    } else {
      console.error('API返回的数据格式不正确:', res)
      scenicSpotList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取景区列表失败:', error)
    ElMessage.error('获取景区列表失败，请稍后重试')
    scenicSpotList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchScenicSpotList()
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
  fetchScenicSpotList()
}

// 添加景区
const handleAddScenicSpot = () => {
  currentScenicSpot.value = {} as ScenicSpotData
  dialogInfo.title = '添加景区'
  dialogInfo.isEdit = false
  dialogInfo.visible = true
}

// 编辑景区
const handleEditScenicSpot = (row: ScenicSpotData) => {
  currentScenicSpot.value = { ...row }
  dialogInfo.title = '编辑景区'
  dialogInfo.isEdit = true
  dialogInfo.visible = true
}

// 查看景区详情
const handleViewScenicSpot = (row: ScenicSpotData) => {
  console.log('查看景区详情:', row.id)
  router.push({
    path: `/admin/scenic-spots/${row.id}`,
    query: { id: row.id.toString() }
  })
}

// 删除景区
const handleDeleteScenicSpot = (row: ScenicSpotData) => {
  ElMessageBox.confirm(`确认删除景区"${row.name}"吗？`, '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteScenicSpot(row.id)
      ElMessage.success('删除成功')
      fetchScenicSpotList()
    } catch (error) {
      console.error('删除景区失败:', error)
      ElMessage.error('删除景区失败，请稍后重试')
    }
  }).catch(() => {})
}

// 切换景区状态
const handleStatusChange = async (row: ScenicSpotData) => {
  try {
    // 发送当前状态到后端保存，而不是仅仅通知后端切换状态
    await updateScenicSpot(row.id as number, { 
      id: row.id,
      status: row.status
    } as ScenicSpotData)
    
    ElMessage.success(`已${row.status === 'active' ? '启用' : '禁用'}景区`)
  } catch (error) {
    console.error('切换景区状态失败:', error)
    ElMessage.error('操作失败，请稍后重试')
    // 回滚状态
    row.status = row.status === 'active' ? 'inactive' : 'active'
  }
}

// 表单提交
const handleFormSubmit = () => {
  dialogInfo.visible = false
  fetchScenicSpotList()
}

// 分页改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchScenicSpotList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchScenicSpotList()
}

// 地区选择联动
const handleProvinceChange = () => {
  queryParams.cityId = undefined
  queryParams.districtId = undefined
}

const handleCityChange = () => {
  queryParams.districtId = undefined
}

// 格式化价格显示
const formatPrice = (price: number): string => {
  if (typeof price !== 'number') return '0.00';
  return price.toFixed(2);
}

// 页面加载时获取列表
onMounted(() => {
  fetchScenicSpotList()
})
</script>

<style scoped>
.scenic-spot-list-container {
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
  position: relative;
  /* 自定义滚动条 - Firefox */
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 #f5f7fa;
}

/* 自定义滚动条 - Webkit (Chrome, Safari, Edge) */
.scenic-spot-list-container::-webkit-scrollbar {
  width: 8px;
  background-color: #f5f7fa;
}

.scenic-spot-list-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 4px;
}

.scenic-spot-list-container::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.scenic-spot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.scenic-spot-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.scenic-spot-search {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.wider-input,
.wider-select {
  width: 240px !important;
}

:deep(.wider-input .el-input__inner),
:deep(.wider-select .el-input__inner) {
  height: 42px !important;
  line-height: 42px !important;
  font-size: 15px !important;
  padding-left: 15px !important;
}

:deep(.search-form .el-form-item__label) {
  font-size: 15px !important;
  font-weight: 500 !important;
}

:deep(.search-form .el-button) {
  height: 42px !important;
  padding: 0 20px !important;
  font-size: 15px !important;
}

.el-form-item {
  margin-bottom: 18px;
  margin-right: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 全局样式，为了使下拉框更大更明显 */
:deep(.el-select-dropdown__item) {
  padding: 8px 20px;
  font-size: 14px;
}

:deep(.larger-dropdown) {
  --el-select-dropdown-max-height: 300px;
}

:deep(.el-select-dropdown) {
  min-width: 220px !important;
}

:deep(.el-select .el-input__inner) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

:deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

.free-price {
  color: #67c23a;
  font-weight: 500;
}

.ticket-price {
  color: #e6a23c;
  font-weight: 500;
}
</style> 