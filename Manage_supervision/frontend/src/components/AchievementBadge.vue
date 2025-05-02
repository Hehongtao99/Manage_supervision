<template>
  <div class="achievement-badge" :class="[level.toLowerCase()]">
    <el-tooltip 
      :content="`${levelName}：总跑量 ${levelRange}`" 
      placement="top"
      :offset="10"
    >
      <div class="badge-content">
        <div class="badge-icon">
          <el-icon v-if="level === 'BRONZE'"><Medal /></el-icon>
          <el-icon v-else-if="level === 'SILVER'"><Medal /></el-icon>
          <el-icon v-else-if="level === 'GOLD'"><Trophy /></el-icon>
          <el-icon v-else-if="level === 'PLATINUM'"><Trophy /></el-icon>
          <el-icon v-else-if="level === 'DIAMOND'"><StarFilled /></el-icon>
          <el-icon v-else><Medal /></el-icon>
        </div>
        <span class="badge-text">{{ levelName }}</span>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Medal, Trophy, StarFilled } from '@element-plus/icons-vue'

const props = defineProps({
  level: {
    type: String,
    required: true,
    validator: (value: string) => {
      return ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'].includes(value)
    }
  },
  totalDistance: {
    type: Number,
    default: 0
  }
})

const levelName = computed(() => {
  switch (props.level) {
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
})

const levelRange = computed(() => {
  switch (props.level) {
    case 'BRONZE':
      return '10-50公里'
    case 'SILVER':
      return '50-100公里'
    case 'GOLD':
      return '100-200公里'
    case 'PLATINUM':
      return '200-500公里'
    case 'DIAMOND':
      return '500公里以上'
    default:
      return '0公里'
  }
})
</script>

<style scoped>
.achievement-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-right: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.achievement-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.badge-content {
  display: flex;
  align-items: center;
}

.badge-icon {
  margin-right: 4px;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.badge-text {
  line-height: 1;
}

/* 段位样式 */
.bronze {
  background-color: #cd7f32;
  color: white;
}

.silver {
  background-color: #c0c0c0;
  color: #333;
}

.gold {
  background-color: #ffd700;
  color: #8b4513;
}

.platinum {
  background-color: #e5e4e2;
  color: #4682b4;
  background-image: linear-gradient(135deg, #e5e4e2, #c0c0c0, #e5e4e2);
}

.diamond {
  background-color: #b9f2ff;
  color: #104e8b;
  background-image: linear-gradient(135deg, #b9f2ff, #00bfff, #b9f2ff);
}
</style> 