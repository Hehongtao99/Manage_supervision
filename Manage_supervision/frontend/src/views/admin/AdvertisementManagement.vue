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
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="申请编号">
          <el-input v-model="searchForm.applicationNumber" placeholder="请输入申请编号" clearable />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="searchForm.area" placeholder="请输入区域" clearable />
        </el-form-item>
        <el-form-item label="广告位置">
          <el-input v-model="searchForm.location" placeholder="请输入广告位置" clearable />
        </el-form-item>
        <el-form-item label="广告类型">
          <el-select v-model="searchForm.adType" placeholder="请选择广告类型" clearable>
            <el-option label="墙体广告" value="墙体广告" />
            <el-option label="立柱广告" value="立柱广告" />
            <el-option label="LED显示屏" value="LED显示屏" />
            <el-option label="灯箱广告" value="灯箱广告" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="广告性质">
          <el-select v-model="searchForm.adNature" placeholder="请选择广告性质" clearable>
            <el-option label="商业" value="商业" />
            <el-option label="公益" value="公益" />
            <el-option label="政府" value="政府" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积范围">
          <div class="size-range">
            <el-input-number v-model="searchForm.minSize" :min="0" :precision="2" :step="0.1" placeholder="最小值" />
            <span class="range-separator">-</span>
            <el-input-number v-model="searchForm.maxSize" :min="0" :precision="2" :step="0.1" placeholder="最大值" />
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
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
          <h4>选择区域</h4>
          <el-tree
            ref="regionTreeRef"
            :data="regionOptions"
            node-key="id"
            :props="{
              label: 'label',
              children: 'children'
            }"
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleRegionNodeClick"
          ></el-tree>
        </div>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="right"
          class="form-container"
        >
          <div class="selected-address">
            <p v-if="selectedAddress">当前选择: <span class="address">{{ selectedAddress }}</span></p>
            <p v-else class="no-address">尚未选择地址</p>
          </div>

          <el-form-item label="区域" prop="area">
            <el-input v-model="form.area" placeholder="请输入区域" />
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
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
          </el-form-item>
        </el-form>
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
        <el-descriptions-item label="行政区域">{{ getFullAddress(currentItem) }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ currentItem.detailedAddress || '无' }}</el-descriptions-item>
        <el-descriptions-item label="广告位置">{{ currentItem.location }}</el-descriptions-item>
        <el-descriptions-item label="广告设置类型">{{ currentItem.adType }}</el-descriptions-item>
        <el-descriptions-item label="广告性质">{{ currentItem.adNature }}</el-descriptions-item>
        <el-descriptions-item label="面积(㎡)">{{ currentItem.size }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentItem.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentItem.applicantPhone || '未提供' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentItem.remark || '无' }}</el-descriptions-item>
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
  provinceId: null,
  cityId: null,
  districtId: null,
  streetId: null,
  detailedAddress: ''
})

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

// 区域选择相关
const regionOptions = ref([])
const selectedRegions = ref([])
const selectedAddress = ref('')
const regionTreeRef = ref(null)

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
  form.provinceId = null
  form.cityId = null
  form.districtId = null
  form.streetId = null
  form.detailedAddress = ''
  
  // 清空选中状态
  selectedAddress.value = ''
  if (regionTreeRef.value) {
    regionTreeRef.value.setCurrentKey(null)
  }
  
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
const handleView = (row) => {
  currentItem.value = { ...row }
  viewDialogVisible.value = true
}

// 编辑广告申请（新增函数）
const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.area = row.area || ''
  form.location = row.location || ''
  form.adType = row.adType || ''
  form.adNature = row.adNature || ''
  form.size = row.size || 1.0
  form.remark = row.remark || ''
  form.applicantId = row.applicantId || currentUser.id
  form.provinceId = row.provinceId || null
  form.cityId = row.cityId || null
  form.districtId = row.districtId || null
  form.streetId = row.streetId || null
  form.detailedAddress = row.detailedAddress || ''
  
  // 更新选中的地址
  selectedAddress.value = row.fullAddress || getFullAddress(row) || ''
  
  // 尝试在树中找到对应的节点并设置为当前选中
  setTimeout(() => {
    if (regionTreeRef.value) {
      // 按照详细程度依次尝试选中
      if (row.streetId) {
        regionTreeRef.value.setCurrentKey(row.streetId)
      } else if (row.districtId) {
        regionTreeRef.value.setCurrentKey(row.districtId)
      } else if (row.cityId) {
        regionTreeRef.value.setCurrentKey(row.cityId)
      } else if (row.provinceId) {
        regionTreeRef.value.setCurrentKey(row.provinceId)
      }
    }
    
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  }, 300)
  
  dialogVisible.value = true
}

// 批准申请
const handleApprove = (row) => {
  reviewType.value = 'approve'
  reviewForm.id = row.id
  reviewForm.status = 'approved'
  reviewForm.remark = ''
  reviewDialogVisible.value = true
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

// 加载区域树
const loadRegionTree = async () => {
  try {
    const response = await axios.get('/api/regions/tree')
    regionOptions.value = response.data
  } catch (error) {
    console.error('加载区域树失败:', error)
    ElMessage.error('加载区域树失败')
  }
}

// 处理区域点击事件
const handleRegionNodeClick = (data, node) => {
  console.log('节点点击:', data, node)
  
  // 获取节点级别
  const level = data.level
  
  // 根据节点级别设置不同的字段
  if (level === 1) { // 省级
    form.provinceId = data.id
    form.cityId = null
    form.districtId = null
    form.streetId = null
  } else if (level === 2) { // 市级
    // 查找当前节点的父节点
    const parentNode = node.parent
    if (parentNode && parentNode.data) {
      form.provinceId = parentNode.data.id
    }
    form.cityId = data.id
    form.districtId = null
    form.streetId = null
  } else if (level === 3) { // 区/县级
    // 获取当前节点的层级关系
    const parentNode = node.parent
    let grandParentNode = null
    
    if (parentNode) {
      grandParentNode = parentNode.parent
      form.cityId = parentNode.data.id
      
      if (grandParentNode) {
        form.provinceId = grandParentNode.data.id
      }
    }
    
    form.districtId = data.id
    form.streetId = null
  } else if (level === 4) { // 街道/乡镇级
    // 获取当前节点的完整层级关系
    const parentNode = node.parent
    let cityNode = null
    let provinceNode = null
    
    if (parentNode) { // 区/县
      form.districtId = parentNode.data.id
      cityNode = parentNode.parent
      
      if (cityNode) { // 市
        form.cityId = cityNode.data.id
        provinceNode = cityNode.parent
        
        if (provinceNode) { // 省
          form.provinceId = provinceNode.data.id
        }
      }
    }
    
    form.streetId = data.id
  }
  
  // 更新选中的地址显示
  updateSelectedAddress()
  
  console.log('设置的区域ID:', form.provinceId, form.cityId, form.districtId, form.streetId)
}

// 更新选中的地址显示
const updateSelectedAddress = async () => {
  try {
    // 获取完整地址路径
    const response = await axios.get('/api/regions/address-path', {
      params: {
        provinceId: form.provinceId,
        cityId: form.cityId,
        districtId: form.districtId,
        streetId: form.streetId
      }
    })
    
    const addressText = response.data || '未选择地址'
    selectedAddress.value = addressText
    
    // 自动填写区域字段
    if (addressText && addressText !== '未选择地址') {
      form.area = addressText
    }
  } catch (error) {
    console.error('获取地址路径失败:', error)
    selectedAddress.value = '获取地址路径失败'
  }
}

// 获取完整地址
const getFullAddress = (item) => {
  if (!item) return '未设置'
  
  let address = ''
  
  if (item.provinceName) address += item.provinceName
  if (item.cityName) address += item.cityName
  if (item.districtName) address += item.districtName
  if (item.streetName) address += item.streetName
  
  if (!address) {
    // 尝试通过fullAddress字段获取
    if (item.fullAddress) return item.fullAddress
    return '未设置'
  }
  
  return address
}

// 页面加载时获取数据
onMounted(() => {
  loadAdvertisementList()
  loadRegionTree()
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

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.size-range {
  display: flex;
  align-items: center;
  width: 220px;
}

.range-separator {
  margin: 0 5px;
}

.address-cascader {
  width: 100%;
}

.dialog-content {
  display: flex;
  min-height: 450px;
}

.region-tree-container {
  flex: 0 0 260px;
  margin-right: 20px;
  border-right: 1px solid #ebeef5;
  padding-right: 20px;
  overflow: auto;
}

.region-tree-container h4 {
  margin-top: 0;
  margin-bottom: 15px;
}

.form-container {
  flex: 1;
}

.selected-address {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.selected-address .address {
  font-weight: bold;
  color: #409eff;
}

.no-address {
  color: #999;
}
</style> 