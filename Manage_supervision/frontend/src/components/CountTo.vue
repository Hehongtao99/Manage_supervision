<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

// 定义组件属性
const props = defineProps({
  startVal: {
    type: Number,
    default: 0
  },
  endVal: {
    type: Number,
    default: 0
  },
  duration: {
    type: Number,
    default: 2000
  },
  decimals: {
    type: Number,
    default: 0
  },
  separator: {
    type: String,
    default: ','
  },
  prefix: {
    type: String,
    default: ''
  },
  suffix: {
    type: String,
    default: ''
  }
})

// 当前显示的数值
const displayValue = ref(props.startVal)
// 动画定时器
let timer: number | null = null

// 格式化数字
const formatNumber = (num: number): string => {
  let number = Number(Number(num).toFixed(props.decimals))
  let numStr = number.toString().split('.') 
  let integer = numStr[0].replace(/\B(?=(\d{3})+(?!\d))/g, props.separator)
  let decimal = props.decimals > 0 && numStr.length > 1 ? '.' + numStr[1] : ''
  
  return props.prefix + integer + decimal + props.suffix
}

// 开始动画
const startCountTo = () => {
  // 清除之前的动画
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
  
  // 重设起始值
  displayValue.value = props.startVal
  
  // 如果开始值和结束值相同，不需要动画
  if (props.startVal === props.endVal) {
    return
  }
  
  const step = Math.abs(props.endVal - props.startVal) / (props.duration / 30)
  const isIncreasing = props.startVal < props.endVal
  
  // 使用setInterval实现动画效果
  timer = window.setInterval(() => {
    if (isIncreasing) {
      displayValue.value += step
      if (displayValue.value >= props.endVal) {
        displayValue.value = props.endVal
        window.clearInterval(timer as number)
        timer = null
      }
    } else {
      displayValue.value -= step
      if (displayValue.value <= props.endVal) {
        displayValue.value = props.endVal
        window.clearInterval(timer as number)
        timer = null
      }
    }
  }, 30)
}

// 监听属性变化，重新开始动画
watch(() => props.endVal, () => {
  startCountTo()
}, { immediate: false })

// 组件挂载时开始动画
onMounted(() => {
  startCountTo()
})
</script>

<template>
  <div class="count-to">
    {{ formatNumber(displayValue) }}
  </div>
</template>

<style scoped>
.count-to {
  font-family: 'DIN Alternate', 'Arial', sans-serif;
  font-weight: 600;
  transition: color 0.3s ease;
  display: inline-block;
}
</style> 