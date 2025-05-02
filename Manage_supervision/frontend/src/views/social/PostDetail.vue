<template>
  <div class="post-detail-page">
    <div class="page-container">
      <!-- 返回按钮 -->
      <div class="back-link">
        <el-button type="text" @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
      </div>
      
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton style="width: 100%" animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 60px" />
            <div style="padding: 14px">
              <el-skeleton-item variant="p" style="width: 100%" />
              <el-skeleton-item variant="text" style="width: 60%" />
              <el-skeleton-item variant="text" style="width: 100%" />
              <el-skeleton-item variant="text" style="width: 100%" />
            </div>
          </template>
        </el-skeleton>
      </div>
      
      <!-- 帖子详情 -->
      <div v-else-if="post" class="post-detail">
        <post-item :post="post" @refresh="fetchPostDetail" />
        
        <!-- 评论区 -->
        <div class="comment-section">
          <comment-list 
            :post-id="postId" 
            :comments="comments" 
            @refresh="fetchComments"
          />
        </div>
      </div>
      
      <!-- 错误提示 -->
      <el-empty v-else description="帖子不存在或已被删除" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import PostItem from '@/components/social/PostItem.vue'
import CommentList from '@/components/social/CommentList.vue'
import { getPostDetail, getCommentList } from '@/api/social'
import type { PostResponse, CommentResponse } from '@/types/social'

const route = useRoute()
const router = useRouter()

// 获取帖子ID
const postId = computed(() => Number(route.params.id))

// 数据
const post = ref<PostResponse | null>(null)
const comments = ref<CommentResponse[]>([])
const loading = ref(false)

// 获取帖子详情
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const response = await getPostDetail(postId.value)
    post.value = response.data.data
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败，请重试')
    post.value = null
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await getCommentList(postId.value)
    comments.value = response.data.data
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败，请重试')
    comments.value = []
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  if (!postId.value || isNaN(postId.value)) {
    ElMessage.error('无效的帖子ID')
    router.push('/social')
    return
  }
  
  fetchPostDetail()
  fetchComments()
})
</script>

<style scoped>
.post-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.back-link {
  margin-bottom: 20px;
}

.loading-container {
  padding: 24px 0;
}

.post-detail {
  margin-bottom: 20px;
}

.comment-section {
  margin-top: 24px;
  border-top: 1px solid #ebeef5;
  padding-top: 24px;
}
</style> 