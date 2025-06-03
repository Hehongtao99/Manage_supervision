<template>
  <el-dialog
    v-model="visible"
    title="编辑朋友圈"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" label-width="80px">
      <!-- 文本内容 -->
      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="分享你的动态..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 图片上传 -->
      <el-form-item label="图片">
        <div class="image-upload-container">
          <div v-for="(url, index) in form.imageUrls" :key="index" class="image-preview-item">
            <el-image :src="url" fit="cover" class="preview-image" />
            <div class="image-actions">
              <el-button size="small" type="danger" circle @click="removeImage(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <el-upload
            v-if="form.imageUrls.length < 6"
            :action="null"
            list-type="picture-card"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
            :multiple="false"
            :accept="'image/jpeg,image/jpg,image/png'"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </div>
        <div class="image-tip">最多上传6张图片</div>
      </el-form-item>

      <!-- 跑步记录 -->
      <el-form-item label="跑步记录">
        <div v-if="form.runningRecordId && currentRunningRecord" class="running-record-preview">
          <div class="running-record-info">
            <span>{{ formatRunningDate(currentRunningRecord.recordDate) }}</span>
            <div class="running-stats">
              <span>{{ currentRunningRecord.distance }}公里</span>
              <span>{{ currentRunningRecord.duration }}分钟</span>
              <span>配速: {{ currentRunningRecord.pace }}</span>
            </div>
          </div>
          <el-button type="danger" size="small" @click="removeRunningRecord">移除</el-button>
        </div>
        <div v-else-if="runningRecords.length > 0" class="running-record-selector">
          <el-select 
            v-model="selectedRunningRecordId" 
            placeholder="选择关联的跑步记录"
            filterable
            clearable
            :loading="loadingRunningRecords"
          >
            <el-option
              v-for="record in runningRecords"
              :key="record.id"
              :label="`${formatRunningDate(record.recordDate)} - ${record.distance}公里 - ${record.duration}分钟`"
              :value="record.id"
            />
          </el-select>
          <el-button 
            type="primary" 
            size="small" 
            @click="addRunningRecord"
            :disabled="!selectedRunningRecordId"
          >
            添加
          </el-button>
        </div>
        <div v-else class="no-records-tip">
          <el-alert
            title="暂无跑步记录"
            type="info"
            description="您可以先去创建跑步记录，然后再来关联到朋友圈"
            show-icon
            :closable="false"
          />
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { uploadImage, updatePost, getRunningRecordsList } from '@/api/social'
import type { PostResponse } from '@/types/social'
import dayjs from 'dayjs'

// 接收父组件传递的参数
const props = defineProps<{
  modelValue: boolean
  post: PostResponse | null
}>()

// 向父组件发送事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

// 对话框是否可见
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const form = reactive({
  id: 0,
  content: '',
  imageUrls: [] as string[],
  runningRecordId: null as number | null
})

// 跑步记录相关数据
const runningRecords = ref<any[]>([])
const loadingRunningRecords = ref(false)
const selectedRunningRecordId = ref<number | null>(null)
const currentRunningRecord = computed(() => {
  if (!form.runningRecordId || !runningRecords.value.length) return null
  return runningRecords.value.find(record => record.id === form.runningRecordId)
})

// 加载状态
const submitting = ref(false)

// 监听post变化，初始化表单数据
watch(() => props.post, (newPost) => {
  if (newPost) {
    console.log('编辑对话框接收到的帖子数据:', newPost)
    form.id = newPost.id
    form.content = newPost.content || ''
    form.imageUrls = [...(newPost.imageUrls || [])]
    form.runningRecordId = newPost.runningRecord ? newPost.runningRecord.id : null
    console.log('设置的跑步记录ID:', form.runningRecordId)
  }
}, { immediate: true })

// 监听跑步记录数据加载完成，重新设置表单数据
watch(() => runningRecords.value, (records) => {
  if (records.length > 0 && props.post && props.post.runningRecord) {
    // 确保跑步记录ID正确设置
    form.runningRecordId = props.post.runningRecord.id
    console.log('跑步记录加载完成，重新设置跑步记录ID:', form.runningRecordId)
  }
}, { immediate: true })

// 监听对话框显示状态，当对话框打开时重新加载跑步记录
watch(() => visible.value, (isVisible) => {
  if (isVisible) {
    console.log('对话框打开，重新加载跑步记录')
    fetchRunningRecords()
  }
})

// 获取用户的跑步记录列表
const fetchRunningRecords = async () => {
  loadingRunningRecords.value = true
  try {
    const res = await getRunningRecordsList()
    console.log('获取到的跑步记录数据:', res)
    if (res.data && (res.data.code === 200 || res.data.code === 0)) {
      runningRecords.value = res.data.data || []
      console.log('处理后的跑步记录列表:', runningRecords.value)
    } else {
      console.error('获取跑步记录失败，响应码:', res.data?.code)
      ElMessage.error('获取跑步记录失败')
    }
  } catch (error) {
    console.error('获取跑步记录出错:', error)
    ElMessage.error('获取跑步记录失败')
  } finally {
    loadingRunningRecords.value = false
  }
}

// 格式化跑步日期
const formatRunningDate = (date: string | null) => {
  if (!date) return ''
  try {
    return dayjs(date).format('YYYY-MM-DD')
  } catch (e) {
    return String(date)
  }
}

// 添加跑步记录关联
const addRunningRecord = () => {
  if (selectedRunningRecordId.value) {
    form.runningRecordId = selectedRunningRecordId.value
    selectedRunningRecordId.value = null
  }
}

// 移除跑步记录关联
const removeRunningRecord = () => {
  form.runningRecordId = null
}

// 组件挂载时加载跑步记录
onMounted(() => {
  // 只在跑步记录为空时才加载，避免重复加载
  if (runningRecords.value.length === 0) {
    fetchRunningRecords()
  }
})

// 处理图片选择
const handleImageChange = async (uploadFile: UploadFile) => {
  if (!uploadFile.raw) {
    ElMessage.error('图片上传失败，请重试')
    return
  }

  // 检查文件类型
  if (!['image/jpeg', 'image/jpg', 'image/png'].includes(uploadFile.raw.type)) {
    ElMessage.error('只能上传JPG/PNG格式的图片')
    return
  }

  // 检查文件大小（限制为5MB）
  if (uploadFile.raw.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    // 确保uploadFile.raw是File对象
    const file = uploadFile.raw
    
    submitting.value = true
    const response = await uploadImage(file)
    submitting.value = false
    
    // 添加到图片列表
    if (response.data?.data) {
      form.imageUrls.push(response.data.data)
    }
  } catch (error) {
    submitting.value = false
    ElMessage.error('图片上传失败，请重试')
    console.error('图片上传错误:', error)
  }
}

// 移除图片
const removeImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}

// 提交表单
const submitForm = async () => {
  // 验证内容和图片不能同时为空
  if (!form.content && form.imageUrls.length === 0) {
    ElMessage.warning('内容和图片不能同时为空')
    return
  }

  try {
    submitting.value = true
    await updatePost({
      id: form.id,
      content: form.content,
      imageUrls: form.imageUrls,
      runningRecordId: form.runningRecordId
    })
    
    submitting.value = false
    ElMessage.success('更新成功')
    closeDialog()
    emit('success')
  } catch (error) {
    submitting.value = false
    ElMessage.error('更新失败，请重试')
    console.error('更新朋友圈错误:', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = 0
  form.content = ''
  form.imageUrls = []
  form.runningRecordId = null
  selectedRunningRecordId.value = null
}

// 关闭对话框
const closeDialog = () => {
  visible.value = false
  // 延迟重置表单，确保对话框完全关闭后再重置
  setTimeout(() => {
    resetForm()
  }, 100)
}
</script>

<style scoped>
.image-upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  display: flex;
  gap: 5px;
}

.image-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.running-record-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px 15px;
  border: 1px solid #eee;
}

.running-record-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.running-stats {
  display: flex;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

.running-record-selector {
  display: flex;
  gap: 10px;
  align-items: center;
}

.running-record-selector .el-select {
  flex: 1;
}

.no-records-tip {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #eee;
}

.no-records-tip .el-alert {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 