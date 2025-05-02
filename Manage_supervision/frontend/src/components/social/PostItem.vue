<template>
  <div class="post-item">
    <el-card class="post-card" shadow="hover">
      <!-- 帖子头部 -->
      <div class="post-header">
        <el-avatar :src="post.avatar" class="post-avatar" />
        <div class="post-user-info">
          <div class="post-username">
            {{ post.username }}
            <el-tag size="small" type="info" class="visibility-tag">仅好友可见</el-tag>
          </div>
          <div class="post-time">{{ formatTime(post.createTime) }}</div>
        </div>
        <div class="post-actions" v-if="post.userId === currentUserId">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><MoreFilled /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 帖子内容 -->
      <div class="post-content">{{ post.content }}</div>

      <!-- 跑步记录信息 -->
      <div v-if="hasRunningRecord" class="running-record-card">
        <div class="running-record-header">
          <el-icon class="running-icon"><Timer /></el-icon>
          <span>跑步记录</span>
        </div>
        <div class="running-record-content">
          <div class="running-record-stats">
            <div class="stat-item">
              <div class="stat-value">{{post.runningRecord.distance}}</div>
              <div class="stat-label">公里</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{post.runningRecord.duration}}</div>
              <div class="stat-label">分钟</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{post.runningRecord.pace}}</div>
              <div class="stat-label">配速</div>
            </div>
          </div>
          <div class="running-record-date">
            {{ formatRunningDate(post.runningRecord.recordDate) }}
          </div>
        </div>
      </div>

      <!-- 帖子图片 -->
      <div class="post-images" v-if="post.imageUrls && post.imageUrls.length > 0">
        <el-image-viewer
          v-if="showViewer"
          :url-list="post.imageUrls"
          :initial-index="viewerIndex"
          @close="closeViewer"
        />
        <div :class="imageGridClass">
          <div
            v-for="(url, index) in post.imageUrls"
            :key="index"
            class="image-item"
            @click="previewImage(index)"
          >
            <el-image :src="url" fit="cover" loading="lazy" />
          </div>
        </div>
      </div>

      <!-- 帖子底部 -->
      <div class="post-footer">
        <div class="post-stats">
          <span class="post-likes">{{ post.likeCount }} 点赞</span>
          <span class="post-comments">{{ post.commentCount }} 评论</span>
        </div>
        <div class="post-actions">
          <el-button 
            type="text" 
            :class="['post-like-btn', post.liked ? 'liked' : '']"
            @click="handleLike"
          >
            <el-icon><StarFilled v-if="post.liked" /><Star v-else /></el-icon>
            {{ post.liked ? '已点赞' : '点赞' }}
          </el-button>
          <el-button 
            type="text" 
            class="post-comment-btn"
            @click="handleViewDetail"
          >
            <el-icon><ChatDotRound /></el-icon>
            评论
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 编辑帖子对话框 -->
    <post-edit-dialog
      v-model="editDialogVisible"
      :post="post"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ThumbUp, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'
import PostEditDialog from '@/components/social/PostEditDialog.vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'
import { toggleLike, deletePost } from '@/api/social'
import type { PostResponse } from '@/types/social'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  post: PostResponse
}>()

const emit = defineEmits(['refresh'])

const router = useRouter()
const userStore = useUserStore()
const currentUserId = computed(() => userStore.userId)

// 图片预览状态
const showViewer = ref(false)
const viewerIndex = ref(0)

// 编辑对话框状态
const editDialogVisible = ref(false)

// 根据图片数量确定网格布局类
const imageGridClass = computed(() => {
  const count = props.post.imageUrls?.length || 0
  if (count === 1) return 'image-grid-single'
  if (count === 2) return 'image-grid-two'
  if (count === 3) return 'image-grid-three'
  if (count === 4) return 'image-grid-four'
  if (count >= 5) return 'image-grid-multi'
  return ''
})

// 预览图片
const previewImage = (index: number) => {
  viewerIndex.value = index
  showViewer.value = true
}

// 关闭图片预览
const closeViewer = () => {
  showViewer.value = false
}

// 打印帖子数据，调试用
onMounted(() => {
  console.log('PostItem组件接收到的帖子数据:', props.post)
  console.log('是否有跑步记录对象:', !!props.post.runningRecord)
  if (props.post.runningRecord) {
    console.log('跑步记录ID:', props.post.runningRecord.id)
    console.log('跑步记录内容:', props.post.runningRecord)
    console.log('跑步距离:', props.post.runningRecord.distance)
    console.log('跑步时长:', props.post.runningRecord.duration)
  }
})

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 格式化跑步日期
const formatRunningDate = (date: string | any) => {
  if (!date) return ''
  try {
    return dayjs(date).format('YYYY-MM-DD')
  } catch (e) {
    return String(date)
  }
}

// 计算是否有跑步记录
const hasRunningRecord = computed(() => {
  return !!props.post.runningRecord && 
         (typeof props.post.runningRecord.id !== 'undefined') && 
         (props.post.runningRecord.distance || 
          props.post.runningRecord.duration || 
          props.post.runningRecord.pace)
})

// 处理点赞
const handleLike = async () => {
  try {
    await toggleLike(props.post.id)
    emit('refresh')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
    console.error(error)
  }
}

// 查看详情
const handleViewDetail = () => {
  router.push(`/social/post/${props.post.id}`)
}

// 下拉菜单命令处理
const handleCommand = (command: string) => {
  if (command === 'delete') {
    ElMessageBox.confirm('确定要删除该帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await deletePost(props.post.id)
        ElMessage.success('删除成功')
        emit('refresh')
      } catch (error) {
        ElMessage.error('删除失败，请重试')
        console.error(error)
      }
    }).catch(() => {})
  } else if (command === 'edit') {
    editDialogVisible.value = true
  }
}

// 编辑成功后的处理
const handleEditSuccess = () => {
  emit('refresh')
}
</script>

<style scoped>
.post-item {
  margin-bottom: 20px;
}

.post-card {
  border-radius: 8px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.post-avatar {
  margin-right: 12px;
}

.post-user-info {
  flex: 1;
}

.post-username {
  font-weight: bold;
  font-size: 16px;
  color: #333;
  display: flex;
  align-items: center;
}

.visibility-tag {
  margin-left: 8px;
  font-size: 10px;
  font-weight: normal;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.post-content {
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  text-align: left;
  line-height: 1.6;
}

.running-record-card {
  background-color: #f8f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  padding: 12px;
  margin-bottom: 16px;
}

.running-record-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #409eff;
  font-weight: bold;
}

.running-icon {
  margin-right: 6px;
}

.running-record-content {
  padding: 4px 0;
}

.running-record-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 8px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.running-record-date {
  text-align: right;
  font-size: 12px;
  color: #909399;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 8px;
  margin-bottom: 12px;
}

.post-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.post-footer {
  display: flex;
  flex-direction: column;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.post-stats {
  display: flex;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;
}

.post-likes {
  margin-right: 12px;
}

.post-actions {
  display: flex;
}

.post-like-btn,
.post-comment-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.post-like-btn.liked {
  color: #ff6a00;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
}

.image-grid-single {
  height: 250px;
  width: 100%;
}

.image-grid-single .image-item {
  width: 100%;
  height: 100%;
}

.image-grid-two {
  display: flex;
  height: 180px;
  gap: 3px;
}

.image-grid-two .image-item {
  width: 50%;
  height: 100%;
}

.image-grid-three {
  display: flex;
  height: 150px;
  gap: 3px;
}

.image-grid-three .image-item {
  width: 33.33%;
  height: 100%;
}

.image-grid-four {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 150px);
  gap: 3px;
}

.image-grid-multi {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 120px);
  gap: 3px;
}

.image-item {
  overflow: hidden;
  cursor: pointer;
  border-radius: 4px;
}

.image-item .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}

.image-item:hover .el-image {
  transform: scale(1.05);
}
</style> 