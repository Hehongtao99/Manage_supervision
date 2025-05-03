<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    filterable
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    >
      <span>{{ item.name }}</span>
      <span class="location-info">{{ item.locationPath }}</span>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getScenicSpotsByRegion } from '../api/hotel'

const props = defineProps({
  modelValue: {
    type: Number,
    default: undefined
  },
  provinceId: {
    type: Number,
    default: undefined
  },
  cityId: {
    type: Number,
    default: undefined
  },
  districtId: {
    type: Number,
    default: undefined
  },
  placeholder: {
    type: String,
    default: '请选择景区'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)
const options = ref<any[]>([])
const loading = ref(false)

// 监听外部 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
})

// 监听内部 selectedValue 变化
watch(() => selectedValue.value, (newVal) => {
  emit('update:modelValue', newVal)
})

// 监听地区变化，重新加载景区选项
watch([() => props.provinceId, () => props.cityId, () => props.districtId], () => {
  loadScenicSpots()
})

// 加载指定地区的景区
const loadScenicSpots = async () => {
  // 如果省份未选择，则不加载数据
  if (!props.provinceId) {
    options.value = []
    return
  }
  
  loading.value = true
  try {
    console.log(`正在加载景区数据，地区参数：provinceId=${props.provinceId}, cityId=${props.cityId}, districtId=${props.districtId}`)
    const res = await getScenicSpotsByRegion(props.provinceId, props.cityId, props.districtId)
    
    if (res && Array.isArray(res.content)) {
      console.log(`成功获取景区数据: ${res.content.length}条记录`)
      options.value = res.content
    } else {
      console.error('获取景区数据格式错误:', res)
      options.value = []
    }
  } catch (error) {
    console.error('加载景区数据失败:', error)
    if (error.response) {
      console.error(`状态码: ${error.response.status}, 错误信息:`, error.response.data)
    }
    options.value = []
  } finally {
    loading.value = false
  }
}

// 选择变化
const handleChange = (value: number) => {
  emit('change', value)
}

// 组件挂载时加载选项
onMounted(() => {
  if (props.provinceId) {
    loadScenicSpots()
  }
})
</script>

<style scoped>
.el-select {
  width: 100%;
}

.location-info {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}
</style> 