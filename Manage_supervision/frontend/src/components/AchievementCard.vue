<template>
  <el-card class="achievement-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <h3>跑步成就</h3>
      </div>
    </template>

    <div class="achievement-content">
      <!-- 当前段位 -->
      <div class="current-level">
        <div class="level-title">当前段位</div>
        <div class="level-badge-container">
          <AchievementBadge 
            :level="achievements.currentLevel" 
            :total-distance="achievements.totalDistance" 
          />
        </div>
      </div>

      <!-- 所有段位进度 -->
      <div class="level-progress">
        <div class="progress-title">段位进度</div>
        <div class="progress-container">
          <div class="level-bars">
            <div 
              v-for="level in achievementLevels" 
              :key="level.name" 
              class="level-bar-item"
            >
              <div class="level-info">
                <span class="level-name" :class="[level.name.toLowerCase()]">{{ level.displayName }}</span>
                <span class="level-range">{{ level.range }}</span>
              </div>
              <el-progress 
                :percentage="calculateLevelPercentage(level)" 
                :status="achievements.currentLevel === level.name ? 'success' : ''"
                :stroke-width="16"
                :color="getLevelColor(level.name)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 下一段位提示 -->
      <div class="next-level-tip" v-if="!isMaxLevel">
        <div class="tip-icon"><el-icon><InfoFilled /></el-icon></div>
        <div class="tip-content">
          距离 <span :class="[nextLevel.toLowerCase()]">{{ getNextLevelName() }}</span> 段位还需 
          <span class="highlight">{{ getDistanceToNextLevel() }}</span> 公里
        </div>
      </div>
      <div class="max-level-tip" v-else>
        <div class="tip-icon"><el-icon><Star /></el-icon></div>
        <div class="tip-content">
          恭喜你已达到最高段位：<span class="diamond">钻石</span>！继续保持！
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { InfoFilled, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserAchievements } from '../api/running'
import AchievementBadge from './AchievementBadge.vue'

// 状态变量
const loading = ref(false)
const achievements = reactive({
  totalDistance: 0,
  currentLevel: 'BRONZE',
  nextLevel: 'SILVER',
  achievedLevels: [] as string[]
})

// 定义所有段位及其对应的里程范围
const achievementLevels = [
  { name: 'BRONZE', displayName: '青铜', threshold: 10, maxThreshold: 50, range: '10-50公里' },
  { name: 'SILVER', displayName: '白银', threshold: 50, maxThreshold: 100, range: '50-100公里' },
  { name: 'GOLD', displayName: '黄金', threshold: 100, maxThreshold: 200, range: '100-200公里' },
  { name: 'PLATINUM', displayName: '铂金', threshold: 200, maxThreshold: 500, range: '200-500公里' },
  { name: 'DIAMOND', displayName: '钻石', threshold: 500, maxThreshold: Infinity, range: '500公里以上' }
]

// 计算是否已达到最高段位
const isMaxLevel = computed(() => {
  return achievements.currentLevel === 'DIAMOND'
})

// 获取下一个段位
const nextLevel = computed(() => {
  if (isMaxLevel.value) {
    return 'DIAMOND'
  }
  return achievements.nextLevel
})

// 获取段位颜色
const getLevelColor = (level: string) => {
  switch (level) {
    case 'BRONZE':
      return '#cd7f32'
    case 'SILVER':
      return '#c0c0c0'
    case 'GOLD':
      return '#ffd700'
    case 'PLATINUM':
      return '#e5e4e2'
    case 'DIAMOND':
      return '#b9f2ff'
    default:
      return ''
  }
}

// 计算每个段位的进度百分比
const calculateLevelPercentage = (level: { name: string; threshold: number; maxThreshold: number }) => {
  const totalDistance = achievements.totalDistance

  if (totalDistance < level.threshold) {
    // 未达到该段位
    return 0
  } else if (totalDistance >= level.maxThreshold) {
    // 已超过该段位上限
    return 100
  } else {
    // 在该段位范围内，计算百分比
    const levelRange = level.maxThreshold - level.threshold
    const distanceInLevel = totalDistance - level.threshold
    return Math.min(Math.round((distanceInLevel / levelRange) * 100), 100)
  }
}

// 获取下一段位名称
const getNextLevelName = () => {
  switch (nextLevel.value) {
    case 'BRONZE':
      return '青铜'
    case 'SILVER':
      return '白银'
    case 'GOLD':
      return '黄金'
    case 'PLATINUM':
      return '铂金'
    case 'DIAMOND':
      return '钻石'
    default:
      return '未知'
  }
}

// 计算距离下一段位还需里程
const getDistanceToNextLevel = () => {
  // 找到下一段位的门槛值
  const nextLevelObj = achievementLevels.find(level => level.name === nextLevel.value)
  if (!nextLevelObj) return 0

  const distanceNeeded = nextLevelObj.threshold - achievements.totalDistance
  return distanceNeeded > 0 ? distanceNeeded.toFixed(1) : 0
}

// 获取用户成就数据
const fetchAchievements = async () => {
  loading.value = true
  try {
    const response = await getUserAchievements()
    if (response.code === 200 && response.data) {
      achievements.totalDistance = response.data.totalDistance || 0
      achievements.currentLevel = response.data.currentLevel || 'BRONZE'
      achievements.nextLevel = response.data.nextLevel || 'SILVER'
      achievements.achievedLevels = response.data.achievedLevels || []
    }
  } catch (error) {
    console.error('获取用户成就失败:', error)
    ElMessage.error('获取用户成就失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchAchievements()
})
</script>

<style scoped>
.achievement-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.achievement-content {
  padding: 10px 0;
}

.current-level {
  margin-bottom: 20px;
}

.level-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.level-badge-container {
  display: flex;
  align-items: center;
}

.level-progress {
  margin-bottom: 20px;
}

.progress-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.level-bars {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.level-bar-item {
  width: 100%;
}

.level-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.level-name {
  font-weight: bold;
}

.level-range {
  color: #909399;
  font-size: 12px;
}

.next-level-tip, .max-level-tip {
  display: flex;
  align-items: flex-start;
  background-color: #f0f9ff;
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
}

.tip-icon {
  margin-right: 10px;
  color: #409EFF;
}

.tip-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
}

.highlight {
  color: #f56c6c;
  font-weight: bold;
}

/* 段位文字颜色 */
.bronze {
  color: #cd7f32;
}

.silver {
  color: #a9a9a9;
}

.gold {
  color: #daa520;
}

.platinum {
  color: #4682b4;
}

.diamond {
  color: #1e90ff;
}
</style> 