<template>
  <div class="post-management">
    <div class="page-header">
      <h2>朋友圈管理</h2>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子内容"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-card v-if="loading" class="loading-card">
      <el-skeleton :rows="10" animated />
    </el-card>

    <div v-else>
      <div v-if="posts.length === 0" class="empty-data">
        <el-empty description="没有找到朋友圈帖子" />
      </div>
      <div v-else class="post-list">
        <el-card v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <div class="user-info">
              <el-avatar :src="post.avatar" class="avatar">{{ post.username.charAt(0) }}</el-avatar>
              <div class="user-details">
                <div class="username">{{ post.username }}</div>
                <div class="post-time">{{ formatDate(post.createTime) }}</div>
              </div>
            </div>
            <div class="post-actions">
              <el-button type="primary" size="small" @click="viewPostDetail(post.id)">
                查看详情
              </el-button>
              <el-popconfirm
                title="确定要删除这条朋友圈吗？"
                @confirm="handleDelete(post.id)"
              >
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          
          <div class="post-content">
            <div class="post-text">{{ post.content }}</div>
            
            <div v-if="post.imageUrls && post.imageUrls.length > 0" class="post-images">
              <div
                v-for="(image, index) in post.imageUrls.slice(0, 4)"
                :key="index"
                class="image-item"
                @click="previewImage(post.imageUrls, index)"
              >
                <el-image
                  :src="image"
                  fit="cover"
                  :preview-src-list="post.imageUrls"
                  hide-on-click-modal
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-if="index === 3 && post.imageUrls.length > 4" class="image-more">
                  +{{ post.imageUrls.length - 4 }}
                </div>
              </div>
            </div>
            
            <div v-if="post.isForward && post.originalPost" class="original-post">
              <div class="forward-icon">
                <el-icon><Right /></el-icon>
              </div>
              <el-card shadow="never" class="original-card">
                <div class="original-header">
                  <span class="original-username">{{ post.originalPost.username }}：</span>
                  <span class="original-content">{{ post.originalPost.content }}</span>
                </div>
                <div v-if="post.originalPost.imageUrls && post.originalPost.imageUrls.length > 0" class="original-images">
                  <el-image
                    v-for="(image, index) in post.originalPost.imageUrls.slice(0, 1)"
                    :key="index"
                    :src="image"
                    fit="cover"
                    class="original-image"
                  ></el-image>
                  <div v-if="post.originalPost.imageUrls.length > 1" class="original-more">
                    ...等{{ post.originalPost.imageUrls.length }}张图片
                  </div>
                </div>
              </el-card>
            </div>
          </div>
          
          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount }} 评论
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ post.likeCount }} 点赞
              </span>
              <span v-if="post.forwardCount" class="stat-item">
                <el-icon><Share /></el-icon>
                {{ post.forwardCount }} 转发
              </span>
            </div>
          </div>
        </el-card>
      </div>
      
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
    </div>

    <el-dialog v-model="imageDialogVisible" title="图片预览" width="80%">
      <div class="image-preview">
        <el-carousel
          ref="carousel"
          :initial-index="currentImageIndex"
          height="500px"
          indicator-position="outside"
          :autoplay="false"
        >
          <el-carousel-item v-for="(url, index) in previewImageUrls" :key="index">
            <div class="carousel-item">
              <img :src="url" alt="预览图片" class="preview-image" />
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  Search, 
  Picture, 
  ChatDotRound, 
  Star, 
  Share, 
  Right 
} from '@element-plus/icons-vue'
import { getAllPosts, deletePost } from '@/api/adminSocial'
import type { PostResponse } from '@/types/social'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

const router = useRouter()

// 状态变量
const loading = ref(true)
const posts = ref<PostResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 图片预览
const imageDialogVisible = ref(false)
const previewImageUrls = ref<string[]>([])
const currentImageIndex = ref(0)

// 加载帖子列表
const loadPosts = async () => {
  loading.value = true
  try {
    const response = await getAllPosts(currentPage.value, pageSize.value, searchKeyword.value)
    posts.value = response.data.data.records
    total.value = response.data.data.total
  } catch (error) {
    console.error('获取朋友圈列表失败:', error)
    ElMessage.error('获取朋友圈列表失败')
  } finally {
    loading.value = false
  }
}

// 查看帖子详情
const viewPostDetail = (postId: number) => {
  router.push(`/admin/social/post/${postId}`)
}

// 删除帖子
const handleDelete = async (postId: number) => {
  try {
    await deletePost(postId)
    ElMessage.success('删除成功')
    loadPosts()
  } catch (error) {
    console.error('删除帖子失败:', error)
    ElMessage.error('删除帖子失败')
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadPosts()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadPosts()
}

// 图片预览
const previewImage = (images: string[], index: number) => {
  previewImageUrls.value = images
  currentImageIndex.value = index
  imageDialogVisible.value = true
}

// 日期格式化
const formatDate = (dateString: string | Date) => {
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  return formatDistanceToNow(date, { addSuffix: true, locale: zhCN })
}

// 初始化
onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  width: 300px;
}

.loading-card {
  margin-bottom: 20px;
}

.empty-data {
  margin: 40px 0;
  display: flex;
  justify-content: center;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  margin-bottom: 10px;
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

.avatar {
  margin-right: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  font-size: 15px;
}

.post-time {
  font-size: 12px;
  color: #8c8c8c;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.post-content {
  margin-bottom: 15px;
}

.post-text {
  margin-bottom: 10px;
  word-break: break-word;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 15px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
  color: #909399;
  font-size: 24px;
}

.image-more {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 18px;
}

.original-post {
  display: flex;
  margin: 10px 0;
}

.forward-icon {
  padding: 5px;
  color: #8c8c8c;
}

.original-card {
  flex: 1;
  background-color: #f7f7f7;
  padding: 5px;
  border-radius: 4px;
}

.original-header {
  margin-bottom: 8px;
  font-size: 14px;
}

.original-username {
  font-weight: bold;
  color: #409EFF;
}

.original-content {
  color: #303133;
}

.original-images {
  display: flex;
  align-items: center;
}

.original-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  margin-right: 8px;
}

.original-more {
  font-size: 12px;
  color: #8c8c8c;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.carousel-item {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style> 