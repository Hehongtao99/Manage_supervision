<template>
  <el-dialog
    v-model="dialogVisible"
    title="转发到朋友圈"
    width="500px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @closed="handleClose"
  >
    <div class="forward-dialog">
      <!-- 转发评论输入区 -->
      <el-input
        v-model="forwardForm.forwardComment"
        type="textarea"
        :rows="3"
        :maxlength="140"
        placeholder="分享你的评论..."
        show-word-limit
      />

      <!-- 原贴内容预览区 -->
      <div class="original-post-preview" v-if="originalPost">
        <div class="preview-header">
          <span class="preview-icon">
            <el-icon><Document /></el-icon>
          </span>
          <span class="preview-title">原帖来自: {{ originalPost.username }}</span>
        </div>
        <div class="preview-content">
          {{ truncateText(originalPost.content, 50) }}
        </div>
        <div class="preview-images" v-if="originalPost.imageUrls && originalPost.imageUrls.length > 0">
          <el-image 
            class="preview-image" 
            :src="originalPost.imageUrls[0]" 
            fit="cover"
          />
          <div class="image-count" v-if="originalPost.imageUrls.length > 1">
            +{{ originalPost.imageUrls.length - 1 }}
          </div>
        </div>
      </div>

      <!-- 地点选择器 -->
      <div class="location-selector">
        <el-input
          v-model="forwardForm.location"
          placeholder="添加所在位置"
          clearable
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 可见范围选择 -->
      <div class="visibility-selector">
        <el-radio-group v-model="forwardForm.visibility">
          <el-radio :label="0">公开</el-radio>
          <el-radio :label="1">仅好友可见</el-radio>
        </el-radio-group>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">转发</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Location } from '@element-plus/icons-vue'
import { forwardPost, getOriginalPost } from '@/api/social'
import type { PostResponse, PostForwardRequest } from '@/types/social'

const props = defineProps<{
  modelValue: boolean
  postId: number
}>()

const emit = defineEmits(['update:modelValue', 'success'])

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 原始帖子数据
const originalPost = ref<PostResponse | null>(null)

// 转发表单数据
const forwardForm = ref<PostForwardRequest>({
  originalPostId: props.postId,
  forwardComment: '',
  location: '',
  visibility: 1 // 默认仅好友可见
})

// 加载状态
const loading = ref(false)

// 监听对话框打开，获取原始帖子数据
watch(
  () => dialogVisible.value,
  async (val) => {
    if (val) {
      try {
        loading.value = true
        const response = await getOriginalPost(props.postId)
        originalPost.value = response.data.data
      } catch (error) {
        console.error('获取原始帖子数据失败', error)
        ElMessage.error('获取原始帖子数据失败')
        dialogVisible.value = false
      } finally {
        loading.value = false
      }
    }
  }
)

// 监听postId变化
watch(
  () => props.postId,
  (val) => {
    forwardForm.value.originalPostId = val
  }
)

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

// 提交转发
const handleSubmit = async () => {
  if (!forwardForm.value.forwardComment.trim()) {
    ElMessage.warning('请输入转发评论')
    return
  }

  try {
    loading.value = true
    await forwardPost(forwardForm.value)
    ElMessage.success('转发成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('转发失败', error)
    ElMessage.error('转发失败，请重试')
  } finally {
    loading.value = false
  }
}

// 关闭对话框时重置表单
const handleClose = () => {
  forwardForm.value = {
    originalPostId: props.postId,
    forwardComment: '',
    location: '',
    visibility: 1
  }
  originalPost.value = null
}
</script>

<style scoped>
.forward-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.original-post-preview {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-top: 8px;
  border-left: 4px solid #e6e6e6;
}

.preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.preview-icon {
  margin-right: 8px;
}

.preview-content {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
}

.preview-images {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-count {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.location-selector {
  margin-top: 8px;
}

.visibility-selector {
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 