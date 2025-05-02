<template>
  <div class="social-timeline">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>朋友圈</h2>
      <el-button 
        type="primary" 
        plain 
        icon="Plus" 
        @click="navigateToCreatePost"
      >
        发布动态
      </el-button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list-container">
      <el-empty v-if="posts.length === 0 && !loading" description="暂无动态" />
      <div v-else>
        <post-item 
          v-for="post in posts" 
          :key="post.id" 
          :post="post"
          @refresh="fetchPosts"
        />
      </div>
      
      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMoreData && posts.length > 0">
        <el-button 
          :loading="loading" 
          type="primary" 
          plain 
          @click="loadMore"
        >
          {{ loading ? '加载中' : '加载更多' }}
        </el-button>
      </div>
      
      <!-- 回到顶部 -->
      <el-backtop :right="20" :bottom="20" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PostItem from '@/components/social/PostItem.vue'
import { getPostList } from '@/api/social'
import type { PostResponse } from '@/types/social'

const router = useRouter()

// 数据
const posts = ref<PostResponse[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMoreData = ref(true)

// 获取帖子列表
const fetchPosts = async (reset = true) => {
  if (reset) {
    currentPage.value = 1
    posts.value = []
  }
  
  loading.value = true
  try {
    const response = await getPostList(currentPage.value, pageSize.value)
    console.log('API返回的完整响应:', response)
    
    const { records, total: totalCount, pages } = response.data.data
    
    // 更详细地记录获取到的帖子数据
    console.log('获取到的帖子列表数据:', records)
    
    // 检查每个帖子的跑步记录
    records.forEach((post, index) => {
      console.log(`[帖子${index+1}, ID=${post.id}] 帖子内容:`, post.content)
      console.log(`[帖子${index+1}, ID=${post.id}] 是否有跑步记录:`, !!post.runningRecord)
      
      if (post.runningRecord) {
        console.log(`[帖子${index+1}, ID=${post.id}] 跑步记录详情:`, JSON.stringify(post.runningRecord))
        console.log(`[帖子${index+1}, ID=${post.id}] 跑步记录ID:`, post.runningRecord.id)
        console.log(`[帖子${index+1}, ID=${post.id}] 跑步距离:`, post.runningRecord.distance)
        console.log(`[帖子${index+1}, ID=${post.id}] 跑步时长:`, post.runningRecord.duration)
        console.log(`[帖子${index+1}, ID=${post.id}] 跑步配速:`, post.runningRecord.pace)
      }
    })
    
    if (reset) {
      posts.value = records
    } else {
      posts.value = [...posts.value, ...records]
    }
    
    total.value = totalCount
    hasMoreData.value = currentPage.value < pages
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error('获取帖子列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (loading.value || !hasMoreData.value) return
  currentPage.value++
  fetchPosts(false)
}

// 跳转到发布页面
const navigateToCreatePost = () => {
  router.push('/social/create-post')
}

// 生命周期钩子
onMounted(() => {
  console.log('SocialTimeline 组件挂载，获取帖子列表')
  fetchPosts()
})

// 当页面被重新激活时（如从其他路由返回）也刷新数据
onActivated(() => {
  console.log('SocialTimeline 组件被激活，重新获取帖子列表')
  fetchPosts()
})
</script>

<style scoped>
.social-timeline {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.post-list-container {
  overflow-y: auto;
  padding-bottom: 20px;
}

.load-more {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 