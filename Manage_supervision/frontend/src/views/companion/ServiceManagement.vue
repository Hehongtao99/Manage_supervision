<template>
  <div class="service-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的陪玩服务</span>
          <el-button type="primary" @click="showCreateServiceDialog">发布新服务</el-button>
        </div>
      </template>
      
      <!-- 服务列表 -->
      <el-table
        :data="services"
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="服务标题" width="180" />
        <el-table-column prop="gameTypes" label="游戏类型" width="150" />
        <el-table-column prop="price" label="价格(元/小时)" width="120">
          <template #default="scope">
            {{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="服务时间" width="200">
          <template #default="scope">
            <span v-if="scope.row.serviceStartTime && scope.row.serviceEndTime">
              {{ formatTime(scope.row.serviceStartTime) }} - {{ formatTime(scope.row.serviceEndTime) }}
            </span>
            <span v-else>未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="availability" label="可用性" width="150" />
        <el-table-column label="审核状态" width="120">
          <template #default="scope">
            <el-tag :type="getReviewStatusType(scope.row.reviewStatus)" effect="plain">
              {{ getReviewStatusText(scope.row.reviewStatus) }}
            </el-tag>
            <el-tooltip 
              v-if="scope.row.reviewStatus === 'rejected' && scope.row.reviewComment" 
              effect="dark" 
              :content="scope.row.reviewComment" 
              placement="top"
            >
              <el-icon class="ml-1"><Warning /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status === 'active' ? '已上线' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="editService(scope.row)"
              :disabled="scope.row.reviewStatus === 'pending'"
            >
              编辑
            </el-button>
            <el-button 
              :type="scope.row.status === 'active' ? 'warning' : 'success'" 
              size="small"
              @click="toggleServiceStatus(scope.row)"
              :disabled="scope.row.reviewStatus === 'pending' || scope.row.reviewStatus === 'rejected'"
            >
              {{ scope.row.status === 'active' ? '下线' : '上线' }}
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="confirmDeleteService(scope.row)"
              :disabled="scope.row.reviewStatus === 'pending'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态提示 -->
      <el-empty 
        v-if="services.length === 0 && !loading" 
        description="您还没有发布任何陪玩服务"
      >
        <el-button type="primary" @click="showCreateServiceDialog">立即发布</el-button>
      </el-empty>
    </el-card>
    
    <!-- 创建/编辑服务对话框 -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="isEditing ? '编辑陪玩服务' : '发布新陪玩服务'"
      width="60%"
      destroy-on-close
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="rules"
        label-position="top"
        status-icon
      >
        <el-form-item label="服务标题" prop="title">
          <el-input v-model="serviceForm.title" placeholder="请输入服务标题，例如：英雄联盟陪练" />
        </el-form-item>
        
        <el-form-item label="擅长游戏类型" prop="gameTypes">
          <el-select
            v-model="serviceForm.gameTypes"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择游戏类型（可添加自定义类型）"
            style="width: 100%"
          >
            <el-option v-for="game in gameOptions" :key="game" :label="game" :value="game" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务价格(元/小时)" prop="price">
          <el-input-number 
            v-model="serviceForm.price" 
            :precision="2" 
            :step="10" 
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="服务时间段" prop="serviceTime">
          <el-time-picker
            v-model="serviceTimeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="可用性" prop="availability">
          <el-select v-model="serviceForm.availability" placeholder="请选择服务可用时间" style="width: 100%">
            <el-option label="每天" value="每天" />
            <el-option label="工作日（周一至周五）" value="工作日（周一至周五）" />
            <el-option label="周末（周六、周日）" value="周末（周六、周日）" />
            <el-option label="自定义" value="custom" />
          </el-select>
          <el-input 
            v-if="serviceForm.availability === 'custom'" 
            v-model="customAvailability" 
            placeholder="请输入自定义可用性，例如：周一、周三、周五"
            class="mt-2"
          />
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的服务内容、特色和您的游戏技能水平等信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="serviceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitServiceForm" :loading="submitting">
            {{ isEditing ? '保存修改' : '发布服务' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getMyCompanionServices, 
  createCompanionService, 
  updateCompanionService, 
  updateCompanionServiceStatus, 
  deleteCompanionService,
  type CompanionService 
} from '../../api/companion'
import { useUserStore } from '../../stores/user'
import { Warning } from '@element-plus/icons-vue'

// 用户信息
const userStore = useUserStore()

// 页面状态
const loading = ref(false)
const submitting = ref(false)
const services = ref<CompanionService[]>([])
const serviceDialogVisible = ref(false)
const isEditing = ref(false)
const currentServiceId = ref<number | undefined>(undefined)

// 游戏类型选项
const gameOptions = [
  '英雄联盟', '王者荣耀', '绝地求生', '穿越火线', '和平精英',
  'DOTA2', '魔兽世界', '炉石传说', 'CS:GO', '无畏契约',
  '第五人格', 'FIFA', 'NBA 2K', '使命召唤', '守望先锋',
  '原神', '明日方舟', '我的世界', '云顶之弈', 'Apex英雄'
]

// 服务表单
const serviceFormRef = ref()
const serviceForm = reactive<{
  title: string
  gameTypes: string[]
  price: number
  description: string
  availability: string
}>({
  title: '',
  gameTypes: [],
  price: 50,
  description: '',
  availability: '每天'
})

// 服务时间范围
const serviceTimeRange = ref<[Date, Date] | null>(null)
const customAvailability = ref('')

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入服务标题', trigger: 'blur' },
    { min: 3, max: 50, message: '标题长度应在3到50个字符之间', trigger: 'blur' }
  ],
  gameTypes: [
    { required: true, message: '请选择至少一种游戏类型', trigger: 'change' },
    { type: 'array', min: 1, message: '请选择至少一种游戏类型', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请设置服务价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10到500个字符之间', trigger: 'blur' }
  ]
}

// 监听自定义可用性
watch(() => serviceForm.availability, (newVal) => {
  if (newVal !== 'custom') {
    customAvailability.value = ''
  }
})

// 初始化页面
onMounted(async () => {
  await fetchServices()
})

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const response = await getMyCompanionServices()
    services.value = response
  } catch (error) {
    console.error('获取服务列表失败:', error)
    ElMessage.error('获取服务列表失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 显示创建服务对话框
const showCreateServiceDialog = () => {
  isEditing.value = false
  currentServiceId.value = undefined
  resetServiceForm()
  serviceDialogVisible.value = true
}

// 编辑服务
const editService = (service: CompanionService) => {
  isEditing.value = true
  currentServiceId.value = service.id
  
  // 填充表单数据
  serviceForm.title = service.title
  serviceForm.gameTypes = service.gameTypes.split(',')
  serviceForm.price = service.price
  serviceForm.description = service.description
  serviceForm.availability = service.availability || '每天'
  
  // 设置服务时间
  if (service.serviceStartTime && service.serviceEndTime) {
    const startTime = new Date()
    const [startHour, startMinute] = service.serviceStartTime.split(':')
    startTime.setHours(parseInt(startHour), parseInt(startMinute), 0)
    
    const endTime = new Date()
    const [endHour, endMinute] = service.serviceEndTime.split(':')
    endTime.setHours(parseInt(endHour), parseInt(endMinute), 0)
    
    serviceTimeRange.value = [startTime, endTime]
  } else {
    serviceTimeRange.value = null
  }
  
  // 自定义可用性处理
  if (!['每天', '工作日（周一至周五）', '周末（周六、周日）'].includes(serviceForm.availability)) {
    customAvailability.value = serviceForm.availability
    serviceForm.availability = 'custom'
  }
  
  serviceDialogVisible.value = true
}

// 重置服务表单
const resetServiceForm = () => {
  serviceForm.title = ''
  serviceForm.gameTypes = []
  serviceForm.price = 50
  serviceForm.description = ''
  serviceForm.availability = '每天'
  serviceTimeRange.value = null
  customAvailability.value = ''
  
  if (serviceFormRef.value) {
    serviceFormRef.value.resetFields()
  }
}

// 获取审核状态标签类型
const getReviewStatusType = (status) => {
  switch (status) {
    case 'approved':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取审核状态文本
const getReviewStatusText = (status) => {
  switch (status) {
    case 'approved':
      return '已通过'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已拒绝'
    default:
      return '未知'
  }
}

// 提交服务表单
const submitServiceForm = async () => {
  if (!serviceFormRef.value) return
  
  await serviceFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // 处理自定义可用性
      if (serviceForm.value.availability === 'custom' && customAvailability.value) {
        serviceForm.value.availability = customAvailability.value
      }
      
      // 处理服务时间
      if (serviceTimeRange.value && serviceTimeRange.value[0] && serviceTimeRange.value[1]) {
        serviceForm.value.serviceStartTime = formatTimeToString(serviceTimeRange.value[0])
        serviceForm.value.serviceEndTime = formatTimeToString(serviceTimeRange.value[1])
      }
      
      // 处理游戏类型，从数组转为逗号分隔的字符串
      if (Array.isArray(serviceForm.value.gameTypes)) {
        serviceForm.value.gameTypes = serviceForm.value.gameTypes.join(',')
      }
      
      let result
      if (isEditing.value && currentServiceId.value) {
        // 更新服务
        result = await updateCompanionService(currentServiceId.value, serviceForm.value)
        ElMessage.success('服务已更新，需要等待管理员审核后才能显示')
      } else {
        // 创建新服务
        result = await createCompanionService(serviceForm.value)
        ElMessage.success('服务已创建，需要等待管理员审核后才能显示')
      }
      
      // 重新加载服务列表
      loadServices()
      
      // 关闭对话框
      serviceDialogVisible.value = false
    } catch (error) {
      console.error('提交服务失败:', error)
      ElMessage.error('操作失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

// 切换服务状态
const toggleServiceStatus = async (service) => {
  try {
    const newStatus = service.status === 'active' ? 'inactive' : 'active'
    
    // 只有审核通过的服务才能更改状态
    if (service.reviewStatus !== 'approved') {
      ElMessage.warning('只有审核通过的服务才能更改状态')
      return
    }
    
    const result = await updateCompanionServiceStatus(service.id, newStatus)
    
    ElMessage.success(newStatus === 'active' ? '服务已上线' : '服务已下线')
    
    // 更新本地数据
    service.status = newStatus
  } catch (error) {
    console.error('更新服务状态失败:', error)
    ElMessage.error('更新服务状态失败')
  }
}

// 确认删除服务
const confirmDeleteService = async (service: CompanionService) => {
  if (!service.id) return
  
  try {
    await ElMessageBox.confirm('确定要删除该服务吗？此操作不可撤销！', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteCompanionService(service.id)
    ElMessage.success('服务已删除')
    await fetchServices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除服务失败:', error)
      ElMessage.error('删除服务失败')
    }
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  return timeStr
}
</script>

<style scoped>
.service-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

.mt-2 {
  margin-top: 8px;
}

.ml-1 {
  margin-left: 4px;
}
</style> 