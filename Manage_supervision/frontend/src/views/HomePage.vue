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
              <div class="mini-stat">
                <el-icon><User /></el-icon>
                <span>1000+ 活跃用户</span>
              </div>
              <div class="mini-stat">
                <el-icon><Trophy /></el-icon>
                <span>5000+ 完成挑战</span>
              </div>
              <div class="mini-stat">
                <el-icon><Timer /></el-icon>
                <span>平均提升 20%</span>
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
          <div class="feature-card" v-for="feature in features" :key="feature.id" 
               :style="{ backgroundImage: `url(${feature.bgImage})` }">
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
  Trophy,
  Medal
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 轮播图数据
const carouselItems = ref([
  {
    image: 'https://images.unsplash.com/photo-1544717297-fa95b6ee9643?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2069&q=80',
    title: '开启你的跑步之旅',
    subtitle: '记录每一步，见证自己的成长',
    buttonText: '立即开始',
    action: () => router.push('/running-record'),
    icon: 'Timer',
    iconColor: '#ffffff',
    features: ['精准记录', '数据分析', '社交互动', '成就系统']
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
    bgImage: 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80'
  },
  {
    id: 2,
    icon: 'DataAnalysis',
    title: '数据分析',
    description: '智能分析你的运动数据，提供个性化的训练建议',
    color: '#52c41a',
    bgImage: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2015&q=80'
  },
  {
    id: 3,
    icon: 'Connection',
    title: '社交互动',
    description: '与跑友分享心得，互相鼓励，一起进步',
    color: '#722ed1',
    bgImage: 'https://images.unsplash.com/photo-1530549387789-4c1017266635?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80'
  },
  {
    id: 4,
    icon: 'Trophy',
    title: '成就系统',
    description: '完成挑战获得徽章，让跑步更有成就感',
    color: '#fa8c16',
    bgImage: 'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2074&q=80'
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

onMounted(() => {
  loadUserStats()
  preloadImages()
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
  border-radius: 0 0 24px 24px;
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
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
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
  justify-content: center;
  flex-wrap: wrap;
}

.primary-btn {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 2px solid white !important;
  color: white !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.primary-btn:hover {
  background: white !important;
  color: #1890ff !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.secondary-btn {
  background: transparent !important;
  border: 2px solid rgba(255, 255, 255, 0.5) !important;
  color: white !important;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: white !important;
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
  transition: transform 0.3s;
}

.mini-stat:hover {
  transform: scale(1.05);
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
</style> 