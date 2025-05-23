<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <section class="hero-section">
      <el-carousel height="600px" indicator-position="outside" :autoplay="true" :interval="5000" arrow="hover">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
            <!-- 装饰性元素 -->
            <div class="carousel-decorations" :class="`theme-${index + 1}`">
              <div class="floating-icon floating-icon-1">
                <el-icon size="40"><Timer /></el-icon>
              </div>
              <div class="floating-icon floating-icon-2">
                <el-icon size="35"><Trophy /></el-icon>
              </div>
              <div class="floating-icon floating-icon-3">
                <el-icon size="30"><Star /></el-icon>
              </div>
              <div class="floating-icon floating-icon-4">
                <el-icon size="25"><Medal /></el-icon>
              </div>
              <!-- 跑步轨迹装饰 -->
              <div class="running-track"></div>
              <div class="running-dots">
                <span v-for="i in 8" :key="i" class="dot" :style="{ animationDelay: i * 0.2 + 's' }"></span>
              </div>
            </div>
            
            <div class="carousel-overlay">
              <div class="carousel-content">
                <!-- 添加图标 -->
                <div class="carousel-icon">
                  <el-icon size="80" :color="item.iconColor">
                    <component :is="item.icon" />
                  </el-icon>
                </div>
                <h1 class="carousel-title">{{ item.title }}</h1>
                <p class="carousel-subtitle">{{ item.subtitle }}</p>
                <div class="carousel-features">
                  <span v-for="feature in item.features" :key="feature" class="feature-tag">
                    {{ feature }}
                  </span>
                </div>
                <div class="carousel-buttons">
                  <el-button type="primary" size="large" class="primary-btn" @click="item.action">
                    {{ item.buttonText }}
                  </el-button>
                  <el-button size="large" class="secondary-btn" @click="$router.push('/social')">
                    了解更多
                  </el-button>
                </div>
              </div>
            </div>
            
            <!-- 数据展示卡片 -->
            <div class="carousel-stats" v-if="index === 0">
              <div class="mini-stat" @click.stop="$router.push('/profile')">
                <el-icon><User /></el-icon>
                <span>1000+ 活跃用户</span>
              </div>
              <div class="mini-stat" @click.stop="$router.push('/running-dashboard')">
                <el-icon><Trophy /></el-icon>
                <span>5000+ 完成挑战</span>
              </div>
              <div class="mini-stat" @click.stop="$router.push('/running-dashboard')">
                <el-icon><Timer /></el-icon>
                <span>平均提升 20%</span>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 开始跑步区域 - 最醒目的位置 -->
    <section class="start-running-hero">
      <div class="running-hero-container">
        <div class="running-hero-content">
          <div class="hero-left">
            <h1 class="hero-title">
              <span class="gradient-text">开始你的跑步之旅</span>
            </h1>
            <p class="hero-subtitle">记录每一步，见证每一次进步</p>
            <div class="hero-stats">
              <div class="mini-stat-item" @click="$router.push('/running-record')">
                <el-icon size="24"><Timer /></el-icon>
                <span>精准记录</span>
              </div>
              <div class="mini-stat-item" @click="$router.push('/running-dashboard')">
                <el-icon size="24"><DataAnalysis /></el-icon>
                <span>数据分析</span>
              </div>
              <div class="mini-stat-item" @click="$router.push('/running-dashboard')">
                <el-icon size="24"><Trophy /></el-icon>
                <span>成就系统</span>
              </div>
            </div>
            <div class="hero-buttons">
              <el-button 
                type="primary" 
                size="large" 
                class="start-btn"
                @click="$router.push('/running-record')"
              >
                <el-icon class="btn-icon"><Timer /></el-icon>
                立即开始跑步
              </el-button>
              <el-button 
                size="large" 
                class="view-data-btn"
                @click="$router.push('/running-dashboard')"
              >
                查看跑步数据
              </el-button>
            </div>
          </div>
          <div class="hero-right">
            <div class="running-animation">
              <div class="runner-icon">
                <svg viewBox="0 0 200 200" class="runner-svg">
                  <circle cx="100" cy="100" r="90" class="track-circle" />
                  <circle cx="100" cy="100" r="90" class="progress-circle" />
                </svg>
                <div class="runner-content">
                  <el-icon size="60" color="#1890ff"><Timer /></el-icon>
                  <div class="pulse-ring"></div>
                  <div class="pulse-ring delay-1"></div>
                  <div class="pulse-ring delay-2"></div>
                </div>
              </div>
              <div class="floating-badges">
                <div class="badge badge-1">
                  <el-icon><Star /></el-icon>
                  <span>5K</span>
                </div>
                <div class="badge badge-2">
                  <el-icon><Trophy /></el-icon>
                  <span>10K</span>
                </div>
                <div class="badge badge-3">
                  <el-icon><Medal /></el-icon>
                  <span>21K</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速统计 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card" @click="$router.push('/running-dashboard')">
            <div class="stat-icon">
              <el-icon size="48"><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.totalDistance }}</h3>
              <p>总里程 (公里)</p>
            </div>
          </div>
          <div class="stat-card" @click="$router.push('/running-dashboard')">
            <div class="stat-icon">
              <el-icon size="48"><Stopwatch /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.averagePace || '--' }}</h3>
              <p>平均配速</p>
            </div>
          </div>
          <div class="stat-card" @click="$router.push('/running-dashboard')">
            <div class="stat-icon">
              <el-icon size="48"><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <h3>{{ userStats.totalRuns }}</h3>
              <p>跑步次数</p>
            </div>
          </div>
          <div class="stat-card" @click="$router.push('/friends')">
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
          <div class="feature-card" v-for="feature in features" :key="feature.id" 
               :style="{ backgroundImage: `url(${feature.bgImage})` }"
               @click="handleFeatureClick(feature)">
            <div class="feature-overlay">
              <div class="feature-icon">
                <el-icon size="64" :color="feature.color">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
              
              <!-- 装饰性点 -->
              <div class="feature-dots">
                <span class="dot" v-for="i in 3" :key="i" :style="{ backgroundColor: feature.color }"></span>
              </div>
            </div>
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
        <div v-if="latestPosts.length === 0" class="empty-posts">
          <el-empty description="暂无动态">
            <el-button type="primary" @click="$router.push('/social/create-post')">发布第一条动态</el-button>
          </el-empty>
        </div>
        <div v-else class="posts-grid">
          <div class="post-card" v-for="post in latestPosts" :key="post.id" @click="$router.push(`/social/post/${post.id}`)">
            <div class="post-header">
              <el-avatar 
                :size="40" 
                :src="post.avatar"
                @click.stop="$router.push(`/user/${post.userId}/profile`)"
                class="clickable-avatar">
                {{ post.username ? post.username.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="post-user-info">
                <h4 @click.stop="$router.push(`/user/${post.userId}/profile`)" class="clickable-username">{{ post.username }}</h4>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-content">
              <p>{{ post.content }}</p>
              <div class="post-images" v-if="post.imageUrls && post.imageUrls.length > 0">
                <img v-for="(image, index) in post.imageUrls.slice(0, 3)" :key="index" :src="image" @click.stop="$router.push(`/social/post/${post.id}`)" />
              </div>
            </div>
            <div class="post-actions" @click.stop>
              <span class="action-item comment-action"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
              <span class="action-item like-action" :class="{ liked: post.liked }"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
            </div>
          </div>
        </div>
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
import { getPostList } from '@/api/social'
import { getRunningStats } from '@/api/running'
import { getFriends } from '@/api/friends'
import type { PostResponse } from '@/types/social'
import { ElMessage } from 'element-plus'
import {
  Timer,
  Stopwatch,
  TrophyBase,
  User,
  DataAnalysis,
  ChatDotRound,
  Connection,
  Star,
  Trophy,
  Medal
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 轮播图数据
const carouselItems = ref([
  {
    image: 'https://images.unsplash.com/photo-1544717297-fa95b6ee9643?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2069&q=80',
    title: '立即开始你的第一次跑步',
    subtitle: '点击下方按钮，开启健康运动新生活',
    buttonText: '立即开始跑步',
    action: () => router.push('/running-record'),
    icon: 'Timer',
    iconColor: '#ffffff',
    features: ['一键开始', '实时记录', '即时反馈', '轻松上手']
  },
  {
    image: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80',
    title: '与跑友一起前行',
    subtitle: '分享跑步心得，交流运动经验',
    buttonText: '加入社区',
    action: () => router.push('/social'),
    icon: 'Connection',
    iconColor: '#ffffff',
    features: ['社交互动', '好友PK', '经验分享']
  },
  {
    image: 'https://images.unsplash.com/photo-1549060279-7e168fcee0c2?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80',
    title: '挑战更好的自己',
    subtitle: '设定目标，突破极限，成就更强的自己',
    buttonText: '查看数据',
    action: () => router.push('/running-dashboard'),
    icon: 'DataAnalysis',
    iconColor: '#ffffff',
    features: ['数据分析', '目标设定', '成就系统']
  },
  {
    image: 'https://images.unsplash.com/photo-1486739985386-d4fae04ca6f7?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2072&q=80',
    title: '专业训练指导',
    subtitle: '科学的训练方法，让每次跑步都更有效',
    buttonText: '开始训练',
    action: () => router.push('/running-record'),
    icon: 'Trophy',
    iconColor: '#ffffff',
    features: ['专业指导', '训练计划', '效果追踪']
  },
  {
    image: 'https://images.unsplash.com/photo-1517649763962-0c623066013b?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80',
    title: '健康生活方式',
    subtitle: '跑步不仅是运动，更是一种生活态度',
    buttonText: '了解更多',
    action: () => router.push('/social'),
    icon: 'Star',
    iconColor: '#ffffff',
    features: ['健康生活', '习惯养成', '生活品质']
  }
])

// 功能特色
const features = ref([
  {
    id: 1,
    icon: 'Timer',
    title: '精准记录',
    description: '准确记录你的跑步数据，包括距离、时间、配速等详细信息',
    color: '#1890ff',
    bgImage: 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80',
    route: '/running-record'
  },
  {
    id: 2,
    icon: 'DataAnalysis',
    title: '数据分析',
    description: '智能分析你的运动数据，提供个性化的训练建议',
    color: '#52c41a',
    bgImage: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2015&q=80',
    route: '/running-dashboard'
  },
  {
    id: 3,
    icon: 'Connection',
    title: '社交互动',
    description: '与跑友分享心得，互相鼓励，一起进步',
    color: '#722ed1',
    bgImage: 'https://images.unsplash.com/photo-1530549387789-4c1017266635?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80',
    route: '/social'
  },
  {
    id: 4,
    icon: 'Trophy',
    title: '成就系统',
    description: '完成挑战获得徽章，让跑步更有成就感',
    color: '#fa8c16',
    bgImage: 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2074&q=80',
    route: '/running-dashboard'  // 成就系统目前跳转到数据看板
  }
])

// 用户统计数据
const userStats = ref({
  totalDistance: '0',
  averagePace: '--',
  totalRuns: '0',
  friendsCount: '0'
})

// 最新动态
const latestPosts = ref<PostResponse[]>([])

// 处理功能卡片点击
const handleFeatureClick = (feature: any) => {
  if (feature.route) {
    router.push(feature.route)
  }
}

// 格式化时间
const formatTime = (date: Date | string) => {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return formatDistanceToNow(dateObj, { addSuffix: true, locale: zhCN })
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    // 获取跑步统计数据
    const statsResponse = await getRunningStats()
    if (statsResponse && statsResponse.data) {
      const stats = statsResponse.data.data || statsResponse.data
      
      // 安全地格式化数据
      const distance = parseFloat(stats.totalDistance) || 0
      const runCount = parseInt(stats.totalRunCount) || 0  // 注意字段名是 totalRunCount
      
      userStats.value.totalDistance = distance.toFixed(1)
      userStats.value.totalRuns = runCount.toString()
      userStats.value.averagePace = stats.averagePace || '--'
    }

    // 获取好友数量
    const friendsResponse = await getFriends()
    if (friendsResponse && friendsResponse.data) {
      const friends = friendsResponse.data.data || friendsResponse.data
      const friendCount = Array.isArray(friends) ? friends.length : 0
      userStats.value.friendsCount = friendCount.toString()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 如果加载失败，使用默认值0
    userStats.value = {
      totalDistance: '0.0',
      averagePace: '--',
      totalRuns: '0',
      friendsCount: '0'
    }
  }
}

// 预加载轮播图图片
const preloadImages = () => {
  carouselItems.value.forEach(item => {
    const img = new Image()
    img.src = item.image
    img.onload = () => {
      console.log(`图片加载成功: ${item.title}`)
    }
    img.onerror = () => {
      console.warn(`图片加载失败: ${item.title}`)
      // 如果图片加载失败，使用渐变色作为后备
      const gradients = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
      ]
      const index = carouselItems.value.indexOf(item)
      if (index !== -1) {
        item.image = gradients[index % gradients.length]
      }
    }
  })
}

// 加载最新动态
const loadLatestPosts = async () => {
  try {
    const response = await getPostList(1, 3) // 只获取最新的3条
    if (response && response.data && response.data.data) {
      const { records } = response.data.data
      latestPosts.value = records || []
    }
  } catch (error) {
    console.error('加载最新动态失败:', error)
    // 如果加载失败，显示默认数据
    latestPosts.value = []
  }
}

onMounted(() => {
  loadUserStats()
  preloadImages()
  loadLatestPosts()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

/* 轮播图区域 */
.hero-section {
  margin-bottom: 60px;
  position: relative;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(248, 249, 250, 0.8) 100%);
  pointer-events: none;
  z-index: 1;
}

.hero-section :deep(.el-carousel) {
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 600px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.3) 100%);
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

/* 装饰性元素 */
.carousel-decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-icon {
  position: absolute;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 5s ease-in-out infinite;
  color: white;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 不同主题的装饰颜色 */
.theme-1 .floating-icon {
  background: rgba(24, 144, 255, 0.3);
  color: #1890ff;
  box-shadow: 0 0 20px rgba(24, 144, 255, 0.4);
}

.theme-2 .floating-icon {
  background: rgba(114, 46, 209, 0.3);
  color: #722ed1;
  box-shadow: 0 0 20px rgba(114, 46, 209, 0.4);
}

.theme-3 .floating-icon {
  background: rgba(82, 196, 26, 0.3);
  color: #52c41a;
  box-shadow: 0 0 20px rgba(82, 196, 26, 0.4);
}

.theme-4 .floating-icon {
  background: rgba(250, 140, 22, 0.3);
  color: #fa8c16;
  box-shadow: 0 0 20px rgba(250, 140, 22, 0.4);
}

.theme-5 .floating-icon {
  background: rgba(255, 85, 85, 0.3);
  color: #ff5555;
  box-shadow: 0 0 20px rgba(255, 85, 85, 0.4);
}

.floating-icon-1 {
  top: 20%;
  left: 20%;
}

.floating-icon-2 {
  top: 50%;
  left: 50%;
}

.floating-icon-3 {
  top: 80%;
  left: 80%;
}

.floating-icon-4 {
  top: 50%;
  left: 20%;
}

@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0);
  }
}

/* 跑步轨迹装饰 */
.running-track {
  position: absolute;
  top: 50%;
  left: 10%;
  right: 10%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.5), transparent);
  transform: translateY(-50%);
}

.theme-1 .running-track {
  background: linear-gradient(90deg, transparent, rgba(24, 144, 255, 0.6), transparent);
}

.theme-2 .running-track {
  background: linear-gradient(90deg, transparent, rgba(114, 46, 209, 0.6), transparent);
}

.theme-3 .running-track {
  background: linear-gradient(90deg, transparent, rgba(82, 196, 26, 0.6), transparent);
}

.theme-4 .running-track {
  background: linear-gradient(90deg, transparent, rgba(250, 140, 22, 0.6), transparent);
}

.theme-5 .running-track {
  background: linear-gradient(90deg, transparent, rgba(255, 85, 85, 0.6), transparent);
}

.running-track::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  transform: translateY(-50%);
  animation: runningDot 4s linear infinite;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.6);
}

@keyframes runningDot {
  0% {
    left: -20px;
  }
  100% {
    left: calc(100% + 20px);
  }
}

.running-dots {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: white;
  margin: 2px;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0% {
    opacity: 0.2;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.2;
  }
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
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
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
  position: relative;
  background: white;
  padding: 0;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  min-height: 300px;
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.feature-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.4) 100%);
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  transition: all 0.3s;
}

.feature-card:hover .feature-overlay {
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.5) 100%);
}

.feature-icon {
  margin-bottom: 24px;
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.15);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 0 30px rgba(255, 255, 255, 0.4);
}

/* 为不同的功能卡片添加主题色 */
.feature-card:nth-child(1):hover .feature-icon {
  box-shadow: 0 0 30px rgba(24, 144, 255, 0.6);
  border-color: #1890ff;
}

.feature-card:nth-child(2):hover .feature-icon {
  box-shadow: 0 0 30px rgba(82, 196, 26, 0.6);
  border-color: #52c41a;
}

.feature-card:nth-child(3):hover .feature-icon {
  box-shadow: 0 0 30px rgba(114, 46, 209, 0.6);
  border-color: #722ed1;
}

.feature-card:nth-child(4):hover .feature-icon {
  box-shadow: 0 0 30px rgba(250, 140, 22, 0.6);
  border-color: #fa8c16;
}

.feature-card h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.feature-card p {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin: 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

/* 最新动态 */
.latest-posts-section {
  padding: 60px 0;
  background-color: #f5f5f5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.empty-posts {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s, transform 0.3s;
  cursor: pointer;
}

.post-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
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

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.05);
}

.clickable-username {
  cursor: pointer;
  transition: color 0.2s;
}

.clickable-username:hover {
  color: #1890ff;
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
  cursor: pointer;
  transition: transform 0.2s;
}

.post-images img:hover {
  transform: scale(1.05);
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

.action-item {
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 4px;
}

.action-item:hover {
  background-color: #f5f5f5;
}

.like-action:hover {
  color: #f56c6c;
}

.post-actions span.liked {
  color: #f56c6c;
}

.post-actions .el-icon {
  font-size: 16px;
}

/* 轮播图增强效果 */
.hero-section :deep(.el-carousel__indicator) {
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 10px;
  width: 30px;
  height: 4px;
}

.hero-section :deep(.el-carousel__indicator.is-active) {
  background-color: white;
}

.hero-section :deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  backdrop-filter: blur(10px);
}

.hero-section :deep(.el-carousel__arrow:hover) {
  background-color: rgba(255, 255, 255, 0.3);
}

/* 添加粒子效果 */
.carousel-decorations::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, rgba(255,255,255,0.3), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.2), transparent),
    radial-gradient(1px 1px at 90px 40px, rgba(255,255,255,0.4), transparent),
    radial-gradient(1px 1px at 130px 80px, rgba(255,255,255,0.3), transparent),
    radial-gradient(2px 2px at 160px 30px, rgba(255,255,255,0.2), transparent);
  background-size: 200px 100px;
  animation: sparkle 8s linear infinite;
}

@keyframes sparkle {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-100px);
  }
}

/* 轮播图内容元素 */
.carousel-icon {
  margin-bottom: 30px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.carousel-features {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin: 24px 0;
  flex-wrap: wrap;
}

.feature-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.carousel-buttons {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  justify-content: center;
  flex-wrap: wrap;
}

.carousel-buttons .primary-btn {
  padding: 16px 32px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 50px;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(0,0,0,0.2);
}

/* 让第一个轮播项的主按钮更醒目 */
.hero-section :deep(.el-carousel__item:first-child) .primary-btn {
  background: #ff6b6b !important;
  border-color: #ff6b6b !important;
  animation: pulse-button 2s ease-in-out infinite;
  font-size: 20px;
  padding: 20px 40px;
}

@keyframes pulse-button {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.6);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
  }
}

.carousel-buttons .primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.3);
}

.carousel-buttons .secondary-btn {
  padding: 16px 32px;
  font-size: 16px;
  background: transparent;
  color: white;
  border: 2px solid white;
  border-radius: 50px;
  transition: all 0.3s;
}

.carousel-buttons .secondary-btn:hover {
  background: rgba(255,255,255,0.1);
  transform: translateY(-2px);
}

/* 数据展示卡片 */
.carousel-stats {
  position: absolute;
  bottom: 40px;
  right: 40px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-stat {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 12px 16px;
  border-radius: 25px;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: slideInRight 1s ease-out;
  transition: transform 0.3s, background 0.3s;
  cursor: pointer;
}

.mini-stat:hover {
  transform: scale(1.05);
  background: rgba(255, 255, 255, 0.25);
}

@keyframes slideInRight {
  from {
    transform: translateX(100px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 装饰性点 */
.feature-dots {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 20px;
}

.feature-dots .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  opacity: 0.8;
  animation: featureDotBlink 2s ease-in-out infinite;
}

.feature-dots .dot:nth-child(2) {
  animation-delay: 0.3s;
}

.feature-dots .dot:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes featureDotBlink {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
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
  
  .carousel-item {
    height: 500px;
  }
  
  .carousel-stats {
    bottom: 20px;
    right: 20px;
  }
  
  .floating-icon {
    display: none;
  }
  
  .carousel-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .carousel-features {
    justify-content: center;
  }
  
  .feature-tag {
    font-size: 12px;
    padding: 4px 8px;
  }
}

/* 开始跑步区域 - 最醒目的样式 */
.start-running-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.start-running-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 添加动态背景效果 */
.start-running-hero::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><path fill="none" stroke="%23fff" stroke-width="2" opacity="0.1" d="M0,300 Q300,100 600,300 T1200,300"/></svg>') repeat-x;
  animation: wave 20s linear infinite;
  opacity: 0.3;
}

@keyframes wave {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-1200px);
  }
}

.running-hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.running-hero-content {
  display: flex;
  align-items: center;
  gap: 60px;
}

.hero-left {
  flex: 1;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  margin: 0 0 20px 0;
  line-height: 1.2;
}

.gradient-text {
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.hero-subtitle {
  font-size: 24px;
  color: rgba(255,255,255,0.9);
  margin: 0 0 40px 0;
  font-weight: 300;
}

.hero-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
}

.mini-stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 18px;
  padding: 12px 20px;
  background: rgba(255,255,255,0.1);
  border-radius: 50px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  cursor: pointer;
  transition: all 0.3s;
}

.mini-stat-item:hover {
  background: rgba(255,255,255,0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.hero-buttons {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.start-btn {
  background: #fff !important;
  color: #667eea !important;
  border: none !important;
  padding: 20px 40px !important;
  font-size: 20px !important;
  font-weight: 600 !important;
  border-radius: 50px !important;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2) !important;
  transition: all 0.3s !important;
  display: flex !important;
  align-items: center !important;
  gap: 10px !important;
}

.start-btn:hover {
  transform: translateY(-3px) !important;
  box-shadow: 0 12px 32px rgba(0,0,0,0.3) !important;
  background: #f8f8f8 !important;
}

.btn-icon {
  font-size: 24px !important;
}

.view-data-btn {
  background: transparent !important;
  color: white !important;
  border: 2px solid white !important;
  padding: 18px 36px !important;
  font-size: 18px !important;
  font-weight: 500 !important;
  border-radius: 50px !important;
  transition: all 0.3s !important;
}

.view-data-btn:hover {
  background: rgba(255,255,255,0.1) !important;
  transform: translateY(-2px) !important;
}

.hero-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.running-animation {
  position: relative;
  width: 300px;
  height: 300px;
}

.runner-icon {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.runner-svg {
  position: absolute;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.track-circle {
  fill: none;
  stroke: rgba(255,255,255,0.2);
  stroke-width: 8;
}

.progress-circle {
  fill: none;
  stroke: white;
  stroke-width: 8;
  stroke-linecap: round;
  stroke-dasharray: 565;
  stroke-dashoffset: 565;
  animation: progress 3s ease-in-out infinite;
}

@keyframes progress {
  0% {
    stroke-dashoffset: 565;
  }
  50% {
    stroke-dashoffset: 141;
  }
  100% {
    stroke-dashoffset: 565;
  }
}

.runner-content {
  position: relative;
  background: rgba(255,255,255,0.2);
  width: 140px;
  height: 140px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 3px solid rgba(255,255,255,0.3);
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(255,255,255,0.5);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 3s ease-out infinite;
}

.pulse-ring.delay-1 {
  animation-delay: 1s;
}

.pulse-ring.delay-2 {
  animation-delay: 2s;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

.floating-badges {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.badge {
  position: absolute;
  background: white;
  padding: 8px 16px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #667eea;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  animation: float-badge 4s ease-in-out infinite;
}

.badge-1 {
  top: 20px;
  right: -20px;
  animation-delay: 0s;
}

.badge-2 {
  bottom: 40px;
  right: 20px;
  animation-delay: 1.3s;
}

.badge-3 {
  top: 50%;
  left: -20px;
  animation-delay: 2.6s;
}

@keyframes float-badge {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-10px) scale(1.05);
  }
}

/* 响应式设计 - 开始跑步区域 */
@media screen and (max-width: 768px) {
  .running-hero-content {
    flex-direction: column;
    text-align: center;
  }

  .start-running-hero .hero-title {
    font-size: 36px;
  }

  .start-running-hero .hero-subtitle {
    font-size: 18px;
  }

  .hero-stats {
    justify-content: center;
    flex-wrap: wrap;
  }

  .mini-stat-item {
    font-size: 14px;
    padding: 8px 16px;
  }

  .hero-buttons {
    justify-content: center;
  }

  .start-btn {
    width: 100%;
    justify-content: center;
  }

  .view-data-btn {
    width: 100%;
  }

  .running-animation {
    width: 250px;
    height: 250px;
  }

  .badge {
    display: none;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
</style> 