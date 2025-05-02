<template>
  <div class="comment-list">
    <div class="comment-count">{{ comments.length > 0 ? `共 ${comments.length} 条评论` : '暂无评论' }}</div>
    
    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comments">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :src="comment.avatar" class="comment-avatar" />
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-username">{{ comment.username }}</span>
            <span v-if="comment.replyToUsername" class="comment-reply-to">回复 {{ comment.replyToUsername }}</span>
            <span v-if="comment.userId === currentUserId" class="comment-delete" @click="deleteUserComment(comment.id)">
              <el-icon><Delete /></el-icon>
            </span>
          </div>
          <div class="comment-text">{{ comment.content }}</div>
          <div class="comment-footer">
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            <span class="comment-reply" @click="showReplyInput(comment)">回复</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论输入框 -->
    <div class="comment-input-container">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="2"
        :placeholder="replyTo ? `回复 ${replyTo.username}` : '发表你的评论...'"
        maxlength="200"
        show-word-limit
        class="comment-input"
      />
      <div class="comment-buttons">
        <el-button v-if="replyTo" size="small" @click="cancelReply">取消回复</el-button>
        <el-button 
          type="primary" 
          size="small" 
          :disabled="!commentContent.trim()" 
          @click="submitComment"
        >
          发表评论
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { createComment, deleteComment } from '@/api/social'
import { getUserId } from '@/utils/auth'
import type { CommentResponse } from '@/types/social'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  postId: number
  comments: CommentResponse[]
}>()

const emit = defineEmits(['refresh'])

const currentUserId = computed(() => getUserId())
const commentContent = ref('')
const replyTo = ref<CommentResponse | null>(null)

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 显示回复输入框
const showReplyInput = (comment: CommentResponse) => {
  replyTo.value = comment
  commentContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  commentContent.value = ''
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  try {
    await createComment({
      postId: props.postId,
      content: commentContent.value,
      replyToId: replyTo.value ? replyTo.value.id : undefined
    })
    
    commentContent.value = ''
    replyTo.value = null
    emit('refresh')
    ElMessage.success('评论成功')
  } catch (error) {
    ElMessage.error('评论失败，请重试')
    console.error(error)
  }
}

// 删除评论
const deleteUserComment = (commentId: number) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteComment(commentId)
      ElMessage.success('删除成功')
      emit('refresh')
    } catch (error) {
      ElMessage.error('删除失败，请重试')
      console.error(error)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.comment-list {
  margin-top: 20px;
}

.comment-count {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.comments {
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  margin-bottom: 16px;
}

.comment-avatar {
  margin-right: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 4px;
}

.comment-username {
  font-weight: bold;
  color: #303133;
}

.comment-reply-to {
  color: #909399;
  font-size: 13px;
  margin-left: 8px;
}

.comment-delete {
  float: right;
  color: #909399;
  cursor: pointer;
  font-size: 14px;
}

.comment-delete:hover {
  color: #F56C6C;
}

.comment-text {
  line-height: 1.5;
  margin-bottom: 6px;
  text-align: left;
}

.comment-footer {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.comment-time {
  margin-right: 16px;
}

.comment-reply {
  color: #409EFF;
  cursor: pointer;
}

.comment-input-container {
  margin-top: 20px;
}

.comment-input {
  margin-bottom: 10px;
}

.comment-buttons {
  display: flex;
  justify-content: flex-end;
}
</style>