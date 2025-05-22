<template>
  <div class="post-detail">
    <div class="page-header">
      <div class="back-link">
        <el-button @click="router.back()" icon="ArrowLeft">返回</el-button>
      </div>
      <h2>帖子详情</h2>
      <div class="placeholder"></div>
    </div>

    <el-skeleton v-if="loading" :rows="15" animated />

    <div v-else-if="!post" class="empty-data">
      <el-empty description="帖子不存在或已被删除" />
      <el-button @click="router.push('/admin/social/posts')" type="primary">返回列表</el-button>
    </div>

    <div v-else class="post-content-wrapper">
      <el-card class="post-card">
        <div class="post-header">
          <div class="user-info">
            <el-avatar :src="post.avatar">{{ post.username.charAt(0) }}</el-avatar>
            <div class="user-details">
              <div class="username">{{ post.username }}</div>
              <div class="post-time">
                {{ formatDate(post.createTime) }} (ID: {{ post.id }})
              </div>
            </div>
          </div>
          <div class="post-actions">
            <el-popconfirm
              title="确定要删除这条朋友圈吗？"
              @confirm="handleDeletePost"
            >
              <template #reference>
                <el-button type="danger">删除帖子</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>

        <div class="post-content">
          <div class="post-text">{{ post.content }}</div>

          <div v-if="post.imageUrls && post.imageUrls.length > 0" class="post-images">
            <el-image
              v-for="(url, index) in post.imageUrls"
              :key="index"
              :src="url"
              fit="cover"
              :preview-src-list="post.imageUrls"
              class="post-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>

          <div v-if="post.isForward && post.originalPost" class="original-post">
            <div class="forward-header">
              <el-icon><Share /></el-icon>
              <span>转发</span>
            </div>
            <el-card shadow="hover" class="original-card">
              <div class="original-user">
                <el-avatar :size="24" :src="post.originalPost.avatar">
                  {{ post.originalPost.username?.charAt(0) }}
                </el-avatar>
                <span class="original-username">{{ post.originalPost.username }}</span>
              </div>
              <div class="original-content">{{ post.originalPost.content }}</div>
              <div v-if="post.originalPost.imageUrls && post.originalPost.imageUrls.length > 0" class="original-images">
                <el-image
                  v-for="(url, index) in post.originalPost.imageUrls.slice(0, 3)"
                  :key="index"
                  :src="url"
                  fit="cover"
                  :preview-src-list="post.originalPost.imageUrls"
                  class="original-image"
                />
                <div v-if="post.originalPost.imageUrls.length > 3" class="more-images">
                  +{{ post.originalPost.imageUrls.length - 3 }}
                </div>
              </div>
              <div class="original-time">{{ formatDate(post.originalPost.createTime) }}</div>
            </el-card>
          </div>
        </div>

        <div class="post-meta">
          <div class="post-stats">
            <div class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ post.commentCount }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ post.likeCount }}</span>
            </div>
            <div v-if="post.forwardCount !== undefined" class="stat-item">
              <el-icon><Share /></el-icon>
              <span>{{ post.forwardCount }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <div class="section-divider">
        <div class="divider-line"></div>
        <div class="divider-text">评论列表</div>
        <div class="divider-line"></div>
      </div>

      <el-skeleton v-if="commentsLoading" :rows="5" animated />

      <div v-else-if="comments.length === 0" class="empty-comments">
        <el-empty description="暂无评论" />
      </div>

      <div v-else class="comments-section">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <el-card shadow="hover">
            <div class="comment-header">
              <div class="user-info">
                <el-avatar :size="32" :src="comment.avatar">{{ comment.username.charAt(0) }}</el-avatar>
                <div class="user-details">
                  <div class="username">{{ comment.username }}</div>
                  <div class="comment-time">{{ formatDate(comment.createTime) }} (ID: {{ comment.id }})</div>
                </div>
              </div>
              <div class="comment-actions">
                <el-popconfirm
                  title="确定要删除这条评论吗？"
                  @confirm="() => handleDeleteComment(comment.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>

            <div class="comment-content">
              <template v-if="comment.replyToUsername">
                <span class="reply-to">@{{ comment.replyToUsername }}</span>
              </template>
              {{ comment.content }}
            </div>

            <div v-if="comment.children && comment.children.length > 0" class="comment-replies">
              <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <div class="user-info">
                    <el-avatar :size="24" :src="reply.avatar">{{ reply.username.charAt(0) }}</el-avatar>
                    <div class="user-details">
                      <div class="username">{{ reply.username }}</div>
                      <div class="comment-time">{{ formatDate(reply.createTime) }} (ID: {{ reply.id }})</div>
                    </div>
                  </div>
                  <div class="reply-actions">
                    <el-popconfirm
                      title="确定要删除这条回复吗？"
                      @confirm="() => handleDeleteComment(reply.id)"
                    >
                      <template #reference>
                        <el-button type="danger" size="small">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </div>

                <div class="reply-content">
                  <template v-if="reply.replyToUsername">
                    <span class="reply-to">@{{ reply.replyToUsername }}</span>
                  </template>
                  {{ reply.content }}
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Share, Picture, Star, ChatDotRound } from '@element-plus/icons-vue'
import { getPostDetail, getPostComments, deletePost, deleteComment } from '@/api/adminSocial'
import type { PostResponse, CommentResponse } from '@/types/social'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()
const postId = ref(Number(route.params.id))

// 状态变量
const loading = ref(true)
const post = ref<PostResponse | null>(null)
const commentsLoading = ref(true)
const comments = ref<CommentResponse[]>([])

// 获取帖子详情
const loadPostDetail = async () => {
  loading.value = true
  try {
    const response = await getPostDetail(postId.value)
    post.value = response.data.data
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
    post.value = null
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const loadComments = async () => {
  commentsLoading.value = true
  try {
    const response = await getPostComments(postId.value)
    comments.value = response.data.data
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

// 删除帖子
const handleDeletePost = async () => {
  try {
    await deletePost(postId.value)
    ElMessage.success('删除成功')
    router.push('/admin/social/posts')
  } catch (error) {
    console.error('删除帖子失败:', error)
    ElMessage.error('删除帖子失败')
  }
}

// 删除评论
const handleDeleteComment = async (commentId: number) => {
  try {
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    // 重新加载评论列表
    loadComments()
    // 重新加载帖子详情以更新评论数
    loadPostDetail()
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除评论失败')
  }
}

// 日期格式化
const formatDate = (dateString: string | Date) => {
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  return format(date, 'yyyy-MM-dd HH:mm:ss')
}

// 加载数据
onMounted(() => {
  loadPostDetail()
  loadComments()
})
</script>

<style scoped>
.post-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.placeholder {
  width: 80px;
}

.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin: 40px 0;
}

.post-content-wrapper {
  margin-bottom: 30px;
}

.post-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  margin-left: 10px;
}

.username {
  font-weight: bold;
  font-size: 15px;
}

.post-time, .comment-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.post-content {
  margin-bottom: 15px;
}

.post-text {
  margin-bottom: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.post-image {
  width: 180px;
  height: 180px;
  border-radius: 4px;
  object-fit: cover;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
}

.original-post {
  margin: 15px 0;
  border-radius: 4px;
}

.forward-header {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 5px;
  color: #8c8c8c;
  font-size: 14px;
}

.original-card {
  padding: 10px;
  background-color: #f7f7f7;
}

.original-user {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.original-username {
  margin-left: 8px;
  font-weight: bold;
  font-size: 14px;
  color: #409EFF;
}

.original-content {
  margin-bottom: 8px;
  font-size: 14px;
}

.original-images {
  display: flex;
  gap: 5px;
  margin-bottom: 8px;
}

.original-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.more-images {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background-color: rgba(0, 0, 0, 0.05);
  color: #8c8c8c;
  border-radius: 4px;
}

.original-time {
  font-size: 12px;
  color: #8c8c8c;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.section-divider {
  display: flex;
  align-items: center;
  margin: 30px 0 20px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background-color: #e4e7ed;
}

.divider-text {
  padding: 0 15px;
  font-size: 16px;
  color: #606266;
}

.empty-comments {
  margin: 30px 0;
  display: flex;
  justify-content: center;
}

.comments-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  margin-bottom: 10px;
}

.comment-header, .reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-content, .reply-content {
  margin-bottom: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-to {
  color: #409EFF;
  font-weight: bold;
  margin-right: 5px;
}

.comment-replies {
  margin-top: 10px;
  padding-left: 15px;
  border-left: 2px solid #ebeef5;
}

.reply-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f7f7f7;
  border-radius: 4px;
}

.reply-item:last-child {
  margin-bottom: 0;
}
</style> 