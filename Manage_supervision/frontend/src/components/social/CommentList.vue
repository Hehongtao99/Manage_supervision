<template>
  <div class="comment-list">
    <div class="comment-count">{{ totalCommentCount > 0 ? `共 ${totalCommentCount} 条评论` : '暂无评论' }}</div>
    
    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comments">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :src="comment.avatar" class="comment-avatar" />
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-username">{{ comment.username }}</span>
            <span v-if="comment.userId === currentUserId" class="comment-delete" @click="deleteUserComment(comment.id)">
              <el-icon><Delete /></el-icon>
            </span>
          </div>
          <div class="comment-text">{{ comment.content }}</div>
          <div class="comment-footer">
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            <span class="comment-reply" @click="showReplyInput(comment)">回复</span>
          </div>
          
          <!-- 子评论列表 -->
          <div v-if="comment.children && comment.children.length > 0" class="comment-children">
            <div v-for="child in comment.children" :key="child.id" class="comment-child-item">
              <el-avatar :size="30" :src="child.avatar" class="comment-child-avatar" />
              <div class="comment-child-content">
                <div class="comment-child-header">
                  <span class="comment-child-username">{{ child.username }}</span>
                  <template v-if="child.replyUserId && child.replyUsername">
                    <span class="reply-arrow">回复</span>
                    <span class="reply-target">@{{ child.replyUsername }}</span>
                  </template>
                  <span v-if="child.userId === currentUserId" class="comment-delete" @click="deleteUserComment(child.id)">
                    <el-icon><Delete /></el-icon>
                  </span>
                </div>
                <div class="comment-child-text">{{ child.content }}</div>
                <div class="comment-child-footer">
                  <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                  <span class="comment-reply" @click="showReplyInput(child, comment)">回复</span>
                </div>
              </div>
            </div>
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
        :placeholder="inputPlaceholder"
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
const parentComment = ref<CommentResponse | null>(null)

// 计算总评论数（包括子评论）
const totalCommentCount = computed(() => {
  let count = props.comments.length;
  for (const comment of props.comments) {
    if (comment.children && comment.children.length > 0) {
      count += comment.children.length;
    }
  }
  return count;
});

// 计算输入框提示
const inputPlaceholder = computed(() => {
  if (!replyTo.value) return '发表你的评论...';
  if (replyTo.value && !parentComment.value) {
    return `回复 ${replyTo.value.username}`;
  }
  return `回复 @${replyTo.value.username}`;
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 显示回复输入框
const showReplyInput = (comment: CommentResponse, parent?: CommentResponse) => {
  replyTo.value = comment
  parentComment.value = parent || null
  commentContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyTo.value = null
  parentComment.value = null
  commentContent.value = ''
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  try {
    const requestData: any = {
      postId: props.postId,
      content: commentContent.value
    }
    
    // 如果是回复评论
    if (replyTo.value) {
      // 如果有父评论，那么parentId设置为父评论ID
      if (parentComment.value) {
        requestData.parentId = parentComment.value.id
      } else {
        // 直接回复一级评论，parentId就是该评论ID
        requestData.parentId = replyTo.value.id
      }
      
      // 设置被回复用户ID
      requestData.replyUserId = replyTo.value.userId
    }
    
    await createComment(requestData)
    
    commentContent.value = ''
    replyTo.value = null
    parentComment.value = null
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
  margin-bottom: 24px;
}

.comment-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.comment-username {
  font-weight: bold;
  color: #303133;
}

.comment-delete {
  color: #909399;
  cursor: pointer;
  font-size: 14px;
  margin-left: auto;
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
  margin-bottom: 10px;
}

.comment-time {
  margin-right: 16px;
}

.comment-reply {
  color: #409EFF;
  cursor: pointer;
}

/* 子评论样式 */
.comment-children {
  padding-left: 10px;
  margin-top: 6px;
  border-left: 2px solid #f0f2f5;
}

.comment-child-item {
  display: flex;
  margin-bottom: 12px;
  padding-top: 8px;
}

.comment-child-avatar {
  margin-right: 10px;
  flex-shrink: 0;
}

.comment-child-content {
  flex: 1;
}

.comment-child-header {
  margin-bottom: 3px;
  display: flex;
  align-items: center;
}

.comment-child-username {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.reply-arrow {
  margin: 0 4px;
  color: #909399;
  font-size: 12px;
}

.reply-target {
  color: #409EFF;
  font-size: 13px;
  font-weight: 500;
}

.comment-child-text {
  line-height: 1.5;
  margin-bottom: 4px;
  text-align: left;
  font-size: 14px;
}

.comment-child-footer {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
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