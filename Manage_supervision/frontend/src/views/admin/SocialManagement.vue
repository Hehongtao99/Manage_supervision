<template>
  <div class="social-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h1>朋友圈管理</h1>
          <div class="header-actions">
            <el-tabs v-model="activeTab" @tab-click="handleTabClick">
              <el-tab-pane label="帖子管理" name="posts"></el-tab-pane>
              <el-tab-pane label="评论管理" name="comments"></el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="输入关键词搜索"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 帖子列表 -->
      <div v-if="activeTab === 'posts'">
        <el-table
          v-loading="loading"
          :data="posts"
          stripe
          style="width: 100%"
          :max-height="700"
          border
        >
          <el-table-column type="expand">
            <template #default="props">
              <div class="expanded-content">
                <div class="content-section">
                  <div class="section-header">帖子内容：</div>
                  <div class="section-body">{{ props.row.content }}</div>
                </div>

                <div v-if="props.row.imageUrls && props.row.imageUrls.length > 0" class="content-section">
                  <div class="section-header">图片：</div>
                  <div class="image-grid">
                    <div v-for="(url, index) in props.row.imageUrls" :key="index" class="image-item">
                      <el-image :src="url" :preview-src-list="props.row.imageUrls" fit="cover" />
                    </div>
                  </div>
                </div>

                <div class="content-section">
                  <div class="section-header">统计信息：</div>
                  <div class="stats-grid">
                    <div class="stat-item">
                      <div class="stat-label">点赞数</div>
                      <div class="stat-value">{{ props.row.likeCount }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">评论数</div>
                      <div class="stat-value">{{ props.row.commentCount }}</div>
                    </div>
                    <div class="stat-item">
                      <div class="stat-label">转发数</div>
                      <div class="stat-value">{{ props.row.forwardCount || 0 }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="用户信息" width="180">
            <template #default="scope">
              <div class="user-info">
                <el-avatar :src="scope.row.avatar" :size="32" class="user-avatar"></el-avatar>
                <span class="username">{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="250">
            <template #default="scope">
              <div class="content-preview" :class="{ 'deleted-content': scope.row.content.includes('已被管理员下架') }">
                {{ truncateText(scope.row.content, 50) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <div class="action-buttons">
                <el-tooltip v-if="!scope.row.content.includes('已被管理员下架')" content="下架" placement="top">
                  <el-button type="danger" size="small" @click="handleTakedownPost(scope.row)" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip v-else content="恢复" placement="top">
                  <el-button type="success" size="small" @click="handleRestorePost(scope.row)" circle>
                    <el-icon><RefreshRight /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="查看详情" placement="top">
                  <el-button type="primary" size="small" @click="handleViewPost(scope.row)" circle>
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 评论列表 -->
      <div v-if="activeTab === 'comments'">
        <el-table
          v-loading="loading"
          :data="comments"
          stripe
          style="width: 100%"
          :max-height="700"
          border
        >
          <el-table-column type="expand">
            <template #default="props">
              <div class="expanded-content">
                <div class="content-section">
                  <div class="section-header">评论内容：</div>
                  <div class="section-body">{{ props.row.content }}</div>
                </div>

                <div v-if="props.row.parentId > 0" class="content-section">
                  <div class="section-header">回复信息：</div>
                  <div class="section-body">
                    <p>回复给：{{ props.row.replyUsername }}</p>
                    <p>父评论ID：{{ props.row.parentId }}</p>
                  </div>
                </div>

                <div class="content-section">
                  <div class="section-header">评论统计：</div>
                  <div class="stats-grid">
                    <div class="stat-item">
                      <div class="stat-label">点赞数</div>
                      <div class="stat-value">{{ props.row.likeCount }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="用户信息" width="180">
            <template #default="scope">
              <div class="user-info">
                <el-avatar :src="scope.row.avatar" :size="32" class="user-avatar"></el-avatar>
                <span class="username">{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="250">
            <template #default="scope">
              <div class="content-preview" :class="{ 'deleted-content': scope.row.content.includes('已被管理员下架') }">
                {{ truncateText(scope.row.content, 50) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="postId" label="帖子ID" width="100" />
          <el-table-column prop="createTime" label="评论时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <div class="action-buttons">
                <el-tooltip v-if="!scope.row.content.includes('已被管理员下架')" content="下架" placement="top">
                  <el-button type="danger" size="small" @click="handleTakedownComment(scope.row)" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip v-else content="恢复" placement="top">
                  <el-button type="success" size="small" @click="handleRestoreComment(scope.row)" circle>
                    <el-icon><RefreshRight /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="查看帖子" placement="top">
                  <el-button type="primary" size="small" @click="handleViewPostFromComment(scope.row)" circle>
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 下架对话框 -->
    <el-dialog v-model="takedownDialogVisible" title="内容下架" width="500px">
      <el-form label-width="100px">
        <el-form-item label="下架原因">
          <el-input
            v-model="takedownReason"
            type="textarea"
            :rows="4"
            placeholder="请输入下架原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="takedownDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTakedown">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, RefreshRight, View } from '@element-plus/icons-vue'
import { 
  getAllPosts, 
  getAllComments, 
  takedownPost, 
  takedownComment, 
  restorePost, 
  restoreComment,
  searchPosts,
  searchComments
} from '../../api/admin'
import type { PostResponse, CommentResponse } from '../../types/social'
import dayjs from 'dayjs'

const router = useRouter()

// 标签页
const activeTab = ref('posts')

// 列表数据
const posts = ref<PostResponse[]>([])
const comments = ref<CommentResponse[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索
const searchKeyword = ref('')
const isSearching = ref(false)

// 下架对话框
const takedownDialogVisible = ref(false)
const takedownReason = ref('')
const currentTakedownItem = ref<PostResponse | CommentResponse | null>(null)
const takedownType = ref<'post' | 'comment'>('post')

// 初始加载数据
onMounted(() => {
  loadData()
})

// 切换标签页
const handleTabClick = () => {
  currentPage.value = 1
  searchKeyword.value = ''
  isSearching.value = false
  loadData()
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'posts') {
      if (isSearching.value && searchKeyword.value) {
        await loadSearchPosts()
      } else {
        await loadPosts()
      }
    } else {
      if (isSearching.value && searchKeyword.value) {
        await loadSearchComments()
      } else {
        await loadComments()
      }
    }
  } catch (error) {
    console.error('加载数据失败', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载帖子
const loadPosts = async () => {
  const response = await getAllPosts(currentPage.value, pageSize.value)
  const { records, total: totalCount } = response.data.data
  posts.value = records
  total.value = totalCount
}

// 加载评论
const loadComments = async () => {
  const response = await getAllComments(currentPage.value, pageSize.value)
  const { records, total: totalCount } = response.data.data
  comments.value = records
  total.value = totalCount
}

// 搜索帖子
const loadSearchPosts = async () => {
  const response = await searchPosts(searchKeyword.value, currentPage.value, pageSize.value)
  const { records, total: totalCount } = response.data.data
  posts.value = records
  total.value = totalCount
}

// 搜索评论
const loadSearchComments = async () => {
  const response = await searchComments(searchKeyword.value, currentPage.value, pageSize.value)
  const { records, total: totalCount } = response.data.data
  comments.value = records
  total.value = totalCount
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  if (searchKeyword.value.trim()) {
    isSearching.value = true
  } else {
    isSearching.value = false
  }
  loadData()
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadData()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadData()
}

// 格式化日期时间
const formatDateTime = (date: string | Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 处理下架帖子
const handleTakedownPost = (post: PostResponse) => {
  currentTakedownItem.value = post
  takedownType.value = 'post'
  takedownDialogVisible.value = true
}

// 处理下架评论
const handleTakedownComment = (comment: CommentResponse) => {
  currentTakedownItem.value = comment
  takedownType.value = 'comment'
  takedownDialogVisible.value = true
}

// 确认下架
const confirmTakedown = async () => {
  if (!currentTakedownItem.value) return

  try {
    if (takedownType.value === 'post') {
      await takedownPost(currentTakedownItem.value.id, takedownReason.value)
      ElMessage.success('帖子下架成功')
    } else {
      await takedownComment(currentTakedownItem.value.id, takedownReason.value)
      ElMessage.success('评论下架成功')
    }
    takedownDialogVisible.value = false
    takedownReason.value = ''
    loadData()
  } catch (error) {
    console.error('下架失败', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理恢复帖子
const handleRestorePost = async (post: PostResponse) => {
  try {
    await ElMessageBox.confirm('确定要恢复该帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await restorePost(post.id)
    ElMessage.success('帖子恢复成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复失败', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 处理恢复评论
const handleRestoreComment = async (comment: CommentResponse) => {
  try {
    await ElMessageBox.confirm('确定要恢复该评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await restoreComment(comment.id)
    ElMessage.success('评论恢复成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复失败', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 查看帖子详情
const handleViewPost = (post: PostResponse) => {
  router.push(`/social/post/${post.id}`)
}

// 从评论查看帖子
const handleViewPostFromComment = (comment: CommentResponse) => {
  router.push(`/social/post/${comment.postId}`)
}
</script>

<style scoped>
.social-management {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h1 {
  margin: 0;
  font-size: 20px;
}

.search-bar {
  margin-bottom: 20px;
  max-width: 500px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 8px;
}

.content-preview {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deleted-content {
  color: #f56c6c;
  text-decoration: line-through;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
}

.expanded-content {
  padding: 20px;
}

.content-section {
  margin-bottom: 15px;
}

.section-header {
  font-weight: bold;
  margin-bottom: 5px;
}

.section-body {
  padding: 5px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-item {
  width: 100px;
  height: 100px;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.image-item .el-image {
  width: 100%;
  height: 100%;
}

.stats-grid {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.stat-item {
  text-align: center;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}
</style> 