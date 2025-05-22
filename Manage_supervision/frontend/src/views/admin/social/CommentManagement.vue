<template>
  <div class="comment-management">
    <div class="page-header">
      <h2>评论管理</h2>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评论内容"
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
      <div v-if="comments.length === 0" class="empty-data">
        <el-empty description="没有找到评论" />
      </div>
      <el-table v-else :data="comments" border style="width: 100%">
        <el-table-column type="expand">
          <template #default="props">
            <div class="comment-detail">
              <div class="detail-item">
                <span class="detail-label">评论内容：</span>
                <span class="detail-value">{{ props.row.content }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">评论时间：</span>
                <span class="detail-value">{{ formatDateTime(props.row.createTime) }}</span>
              </div>
              <div v-if="props.row.parentId !== 0" class="detail-item">
                <span class="detail-label">回复用户：</span>
                <span class="detail-value">{{ props.row.replyToUsername || '未知用户' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">评论类型：</span>
                <span class="detail-value">{{ props.row.parentId === 0 ? '主评论' : '回复评论' }}</span>
              </div>
              <div class="detail-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewPostDetail(props.row.postId)"
                >查看帖子</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="用户信息" width="180">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="32" :src="scope.row.avatar">{{ scope.row.username.charAt(0) }}</el-avatar>
              <span class="username">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="评论内容">
          <template #default="scope">
            <div class="comment-content">
              {{ scope.row.content.length > 50 
                ? scope.row.content.substring(0, 50) + '...' 
                : scope.row.content }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="评论类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.parentId === 0 ? 'primary' : 'success'">
              {{ scope.row.parentId === 0 ? '主评论' : '回复评论' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="评论时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-popconfirm
              title="确定要删除此评论吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllComments, deleteComment } from '@/api/adminSocial'
import type { CommentResponse } from '@/types/social'
import { format } from 'date-fns'

const router = useRouter()

// 状态变量
const loading = ref(true)
const comments = ref<CommentResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const response = await getAllComments(currentPage.value, pageSize.value, searchKeyword.value)
    comments.value = response.data.data.records
    total.value = response.data.data.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 查看帖子详情
const viewPostDetail = (postId: number) => {
  router.push(`/admin/social/post/${postId}`)
}

// 删除评论
const handleDelete = async (commentId: number) => {
  try {
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    loadComments()
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除评论失败')
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadComments()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadComments()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadComments()
}

// 日期格式化
const formatDate = (dateString: string | Date) => {
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  return format(date, 'yyyy-MM-dd HH:mm')
}

const formatDateTime = (dateString: string | Date) => {
  const date = typeof dateString === 'string' ? new Date(dateString) : dateString
  return format(date, 'yyyy-MM-dd HH:mm:ss')
}

// 初始化
onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-management {
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

.user-info {
  display: flex;
  align-items: center;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.comment-content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.comment-detail {
  padding: 10px 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
}

.detail-label {
  font-weight: bold;
  width: 100px;
  color: #606266;
}

.detail-value {
  flex: 1;
  word-break: break-word;
}

.detail-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 