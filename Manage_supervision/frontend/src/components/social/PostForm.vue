<template>
  <div class="post-form">
    <el-form :model="postForm" ref="formRef" :rules="rules" label-position="top">
      <!-- 帖子内容 -->
      <el-form-item prop="content">
        <el-input
          v-model="postForm.content"
          type="textarea"
          :rows="4"
          placeholder="分享你的想法..."
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <!-- 图片上传 -->
      <el-form-item label="图片（最多6张）" class="upload-container">
        <el-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :limit="6"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :on-exceed="handleExceed"
        >
          <template #default>
            <el-icon><Plus /></el-icon>
          </template>
        </el-upload>
      </el-form-item>

      <!-- 跑步记录选择 -->
      <el-form-item label="选择跑步记录">
        <div class="running-record-selector">
          <el-select 
            v-model="selectedRunningRecord" 
            filterable 
            clearable 
            placeholder="选择跑步记录分享" 
            style="width: 100%"
            @change="handleRunningRecordChange"
            :no-data-text="'暂无跑步记录'"
          >
            <el-option
              v-for="record in runningRecords"
              :key="record.id"
              :label="`${formatRecordDate(record.recordDate)} - ${record.distance}公里 (${record.pace})`"
              :value="record.id"
            >
              <div class="running-record-option">
                <span>{{ formatRecordDate(record.recordDate) }}</span>
                <span>{{ record.distance }}公里</span>
                <span>{{ record.duration }}分钟</span>
                <span>配速: {{ record.pace }}</span>
              </div>
            </el-option>
            <template v-if="runningRecords.length === 0">
              <el-option disabled value="">
                <span>暂无跑步记录，请先添加跑步记录</span>
              </el-option>
            </template>
          </el-select>
          <div v-if="selectedRunningRecord" class="selected-record-preview">
            <div class="record-preview-card">
              <div class="record-preview-header">
                <el-icon class="record-icon"><Timer /></el-icon>
                <span>跑步记录预览</span>
              </div>
              <div class="record-preview-content">
                <div class="record-stats">
                  <div class="stat-preview-item">
                    <div class="stat-preview-value">{{selectedRecordData?.distance}}</div>
                    <div class="stat-preview-label">公里</div>
                  </div>
                  <div class="stat-preview-item">
                    <div class="stat-preview-value">{{selectedRecordData?.duration}}</div>
                    <div class="stat-preview-label">分钟</div>
                  </div>
                  <div class="stat-preview-item">
                    <div class="stat-preview-value">{{selectedRecordData?.pace}}</div>
                    <div class="stat-preview-label">配速</div>
                  </div>
                </div>
                <div class="record-preview-date">
                  {{ formatRecordDate(selectedRecordData?.recordDate) }}
                </div>
              </div>
            </div>
          </div>
          <div v-if="runningRecords.length === 0" class="no-records-tip">
            <el-link type="primary" @click="navigateToRunningRecord">去添加跑步记录</el-link>
          </div>
        </div>
      </el-form-item>

      <!-- 可见性说明 -->
      <el-form-item>
        <div class="visibility-info">
          <el-tag type="info">仅好友可见</el-tag>
          <span class="visibility-text">朋友圈帖子默认仅对好友可见</span>
        </div>
      </el-form-item>

      <!-- 发布按钮 -->
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submitForm">发布</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Timer } from '@element-plus/icons-vue'
import type { FormInstance, UploadUserFile, UploadFile } from 'element-plus'
import { createPost, uploadImage } from '@/api/social'
import { getRunningRecords } from '@/api/running'
import type { PostCreateRequest } from '@/types/social'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

interface Props {
  onSuccess?: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  onSuccess: () => {}
})

// 表单引用
const formRef = ref<FormInstance>()
// 表单数据
const postForm = reactive<PostCreateRequest>({
  content: '',
  imageUrls: [],
  location: '',
  visibility: 1 // 固定为仅好友可见
})
// 文件列表
const fileList = ref<UploadUserFile[]>([])
// 加载状态
const loading = ref(false)

// 路由实例
const router = useRouter()

// 跑步记录数据
const runningRecords = ref<any[]>([])
const selectedRunningRecord = ref<number | null>(null)
const selectedRecordData = ref<any>(null)

// 表单验证规则
const rules = {
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在1-1000个字符之间', trigger: 'blur' }
  ]
}

// 处理文件变更
const handleFileChange = (uploadFile: UploadFile) => {
  // 限制文件类型为图片
  const isImage = uploadFile.raw?.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    const index = fileList.value.indexOf(uploadFile)
    if (index !== -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 处理文件移除
const handleFileRemove = (uploadFile: UploadFile) => {
  const index = fileList.value.indexOf(uploadFile)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
}

// 处理超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传6张图片')
}

// 格式化记录日期
const formatRecordDate = (date: any) => {
  if (!date) return ''
  // 处理ISO日期字符串或LocalDate对象
  try {
    return dayjs(date).format('YYYY-MM-DD')
  } catch (e) {
    console.error('日期格式化错误:', e)
    return String(date)
  }
}

// 处理跑步记录变更
const handleRunningRecordChange = (recordId: number) => {
  if (!recordId) {
    selectedRecordData.value = null
    return
  }
  
  selectedRecordData.value = runningRecords.value.find(record => record.id === recordId)
  // 选择了跑步记录后不自动添加到帖子内容中
}

// 获取跑步记录
const fetchRunningRecords = async () => {
  try {
    const response = await getRunningRecords()
    console.log('跑步记录响应:', response)
    
    // 检查response的数据结构
    if (!response) {
      console.error('获取跑步记录失败: 无响应')
      return
    }
    
    let records = []
    
    // 处理不同格式的响应
    if (response.data && response.data.code === 200) {
      // 标准响应格式
      records = response.data.data || []
    } else if (Array.isArray(response)) {
      // 直接返回数组的情况
      records = response
    } else if (Array.isArray(response.data)) {
      // 返回data数组的情况
      records = response.data
    } else {
      console.warn('无法识别的跑步记录数据格式:', response)
      records = []
    }
    
    // 确保records是数组
    if (!Array.isArray(records)) {
      console.warn('跑步记录不是数组格式:', records)
      records = []
    }
    
    // 记录日志并更新状态
    console.log('处理后的跑步记录数据:', records)
    runningRecords.value = records
    
    // 如果没有记录，给用户提示
    if (records.length === 0) {
      console.log('没有找到跑步记录')
    }
  } catch (error) {
    console.error('获取跑步记录失败:', error)
    ElMessage.error('获取跑步记录失败')
    runningRecords.value = []
  }
}

// 上传图片
const uploadImages = async () => {
  if (fileList.value.length === 0) return []
  
  const uploadPromises = fileList.value.map(async file => {
    try {
      if (!file.raw) {
        throw new Error('文件不存在')
      }
      const response = await uploadImage(file.raw)
      return response.data.data
    } catch (error) {
      console.error('图片上传失败:', error)
      throw error
    }
  })
  
  return Promise.all(uploadPromises)
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 先上传图片
      const imageUrls = await uploadImages()
      
      // 打印跑步记录ID，调试用
      console.log('发布帖子时关联的跑步记录ID:', selectedRunningRecord.value)
      console.log('发布帖子时关联的跑步记录数据:', selectedRecordData.value)
      
      // 发布帖子，设置visibility为1，表示仅好友可见
      const postData = {
        content: postForm.content,
        imageUrls,
        location: postForm.location,
        visibility: 1, // 确保始终为仅好友可见
        runningRecordId: selectedRunningRecord.value // 添加关联的跑步记录ID
      }
      
      console.log('发送的帖子数据:', postData)
      
      const response = await createPost(postData)
      console.log('帖子发布响应:', response)
      
      ElMessage.success('发布成功')
      
      // 重置表单
      resetForm()
      
      // 发布成功后直接返回朋友圈列表页
      router.push('/social')
      
      // 调用传入的成功回调
      props.onSuccess()
    } catch (error) {
      console.error('发布失败:', error)
      ElMessage.error('发布失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  fileList.value = []
  postForm.content = ''
  postForm.imageUrls = []
  postForm.location = ''
  selectedRunningRecord.value = null
  selectedRecordData.value = null
}

// 导航到跑步记录页面
const navigateToRunningRecord = () => {
  router.push('/running-record')
}

// 生命周期钩子
onMounted(() => {
  // 获取用户跑步记录
  fetchRunningRecords()
})
</script>

<style scoped>
.post-form {
  margin: 20px 0;
}

.upload-container :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.upload-container :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.running-record-selector {
  margin-bottom: 15px;
}

.running-record-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.running-record-option span {
  margin-right: 10px;
}

.selected-record-info {
  margin-top: 8px;
}

.selected-record-preview {
  margin-top: 16px;
}

.record-preview-card {
  background-color: #f8f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  padding: 12px;
  margin-bottom: 16px;
}

.record-preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #409eff;
  font-weight: bold;
}

.record-icon {
  margin-right: 6px;
}

.record-preview-content {
  padding: 4px 0;
}

.record-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 8px;
}

.stat-preview-item {
  text-align: center;
  flex: 1;
}

.stat-preview-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.stat-preview-label {
  font-size: 12px;
  color: #909399;
}

.record-preview-date {
  text-align: right;
  font-size: 12px;
  color: #909399;
}

.visibility-info {
  display: flex;
  align-items: center;
}

.visibility-text {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.no-records-tip {
  margin-top: 8px;
  text-align: right;
}
</style> 