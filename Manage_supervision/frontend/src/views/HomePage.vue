<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <section class="hero-section">
      <el-carousel height="500px" indicator-position="outside" :autoplay="true" :interval="4000">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-item" :style="{ background: item.image }">
            <div class="carousel-overlay">
              <div class="carousel-content">
                <h1 class="carousel-title">{{ item.title }}</h1>
                <p class="carousel-subtitle">{{ item.subtitle }}</p>
                <el-button type="primary" size="large" @click="item.action">
                  {{ item.buttonText }}
                </el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 快速统计 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon size="48"><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.totalDistance }}</h3>
              <p>总里程 (公里)</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon size="48"><Stopwatch /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.totalTime }}</h3>
              <p>总时长 (小时)</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon size="48"><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.totalRuns }}</h3>
              <p>跑步次数</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon size="48"><User /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.friendsCount }}</h3>
              <p>跑友数量</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">为什么选择 Run2gather</h2>
        <div class="features-grid">
          <div class="feature-card" v-for="feature in features" :key="feature.id">
            <div class="feature-icon">
              <el-icon size="64" :color="feature.color">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新动态 -->
    <section class="latest-posts-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">社区动态</h2>
          <el-button type="primary" @click="$router.push('/social')">查看更多</el-button>
        </div>
        <div class="posts-grid">
          <div class="post-card" v-for="post in latestPosts" :key="post.id">
            <div class="post-header">
              <el-avatar :size="40" :src="post.user.avatar">
                {{ post.user.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="post-user-info">
                <h4>{{ post.user.username }}</h4>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
              <div class="post-images" v-if="post.images && post.images.length > 0">
                <img v-for="image in post.images.slice(0, 3)" :key="image" :src="image" />
              </div>
            </div>
            <div class="post-actions">
              <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
              <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 开始跑步按钮 -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>准备好开始你的跑步之旅了吗？</h2>
        <p>记录每一步，分享每一刻，与跑友一起成长</p>
        <el-button type="primary" size="large" @click="$router.push('/running-record')">
          开始跑步
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  Timer,
  Stopwatch,
  TrophyBase,
  User,
  DataAnalysis,
  ChatDotRound,
  Connection,
  Star,
  Trophy
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 轮播图数据
const carouselItems = ref([
  {
    image: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    title: '开启你的跑步之旅',
    subtitle: '记录每一步，见证自己的成长',
    buttonText: '立即开始',
    action: () => router.push('/running-record')
  },
  {
    image: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    title: '与跑友一起前行',
    subtitle: '分享跑步心得，交流运动经验',
    buttonText: '加入社区',
    action: () => router.push('/social')
  },
  {
    image: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    title: '挑战更好的自己',
    subtitle: '设定目标，突破极限，成就更强的自己',
    buttonText: '查看数据',
    action: () => router.push('/running-dashboard')
  }
])

// 功能特色
const features = ref([
  {
    id: 1,
    icon: 'Timer',
    title: '精准记录',
    description: '准确记录你的跑步数据，包括距离、时间、配速等详细信息',
    color: '#1890ff'
  },
  {
    id: 2,
    icon: 'DataAnalysis',
    title: '数据分析',
    description: '智能分析你的运动数据，提供个性化的训练建议',
    color: '#52c41a'
  },
  {
    id: 3,
    icon: 'Connection',
    title: '社交互动',
    description: '与跑友分享心得，互相鼓励，一起进步',
    color: '#722ed1'
  },
  {
    id: 4,
    icon: 'Trophy',
    title: '成就系统',
    description: '完成挑战获得徽章，让跑步更有成就感',
    color: '#fa8c16'
  }
])

// 用户统计数据
const userStats = ref({
  totalDistance: '0',
  totalTime: '0',
  totalRuns: '0',
  friendsCount: '0'
})

// 最新动态
const latestPosts = ref([
  {
    id: 1,
    user: { username: '跑步达人', avatar: '' },
    content: '今天完成了10公里晨跑，感觉身体状态越来越好了！',
    images: [],
    commentCount: 5,
    likeCount: 12,
    createdAt: new Date(Date.now() - 1000 * 60 * 30) // 30分钟前
  },
  {
    id: 2,
    user: { username: '马拉松小王子', avatar: '' },
    content: '参加了昨天的半程马拉松，成绩比上次提升了5分钟！',
    images: [],
    commentCount: 8,
    likeCount: 25,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2) // 2小时前
  },
  {
    id: 3,
    user: { username: '夜跑女神', avatar: '' },
    content: '夜跑的感觉真不错，城市的灯光很美，空气也很清新',
    images: [],
    commentCount: 3,
    likeCount: 18,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 4) // 4小时前
  }
])

// 格式化时间
const formatTime = (date: Date) => {
  return formatDistanceToNow(date, { addSuffix: true, locale: zhCN })
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    // 这里应该调用实际的API获取用户统计数据
    // 暂时使用模拟数据
    userStats.value = {
      totalDistance: '156.8',
      totalTime: '24.5',
      totalRuns: '32',
      friendsCount: '18'
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadUserStats()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 轮播图区域 */
.hero-section {
  margin-bottom: 60px;
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 500px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-content {
  text-align: center;
  color: white;
}

.carousel-title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 16px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.carousel-subtitle {
  font-size: 24px;
  margin: 0 0 32px 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

/* 统计区域 */
.stats-section {
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.stat-card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  color: #1890ff;
  margin-bottom: 16px;
}

.stat-content h3 {
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.stat-content p {
  color: #666;
  margin: 0;
  font-size: 16px;
}

/* 功能特色 */
.features-section {
  padding: 80px 0;
  background: #f8f9fa;
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 60px 0;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 40px;
}

.feature-card {
  background: white;
  padding: 40px 30px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  margin-bottom: 24px;
}

.feature-card h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* 最新动态 */
.latest-posts-section {
  padding: 80px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.post-user-info {
  margin-left: 12px;
}

.post-user-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
}

.post-time {
  color: #999;
  font-size: 14px;
}

.post-content p {
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.post-images img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.post-actions {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.post-actions span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* CTA区域 */
.cta-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
  color: white;
  text-align: center;
}

.cta-content h2 {
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 16px 0;
}

.cta-content p {
  font-size: 18px;
  margin: 0 0 32px 0;
  opacity: 0.9;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .carousel-title {
    font-size: 32px;
  }
  
  .carousel-subtitle {
    font-size: 18px;
  }
  
  .section-title {
    font-size: 28px;
  }
  
  .stats-grid,
  .features-grid,
  .posts-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
}
</style> 