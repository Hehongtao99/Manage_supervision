<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    @change="handleChange"
    :style="{ width: '100%' }"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </el-select>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getRegionsByLevel, getRegionsByParentId } from '../api/travel'

const props = defineProps({
  modelValue: {
    type: Number,
    default: undefined
  },
  level: {
    type: String,
    required: true,
    validator: (value: string) => ['province', 'city', 'district'].includes(value)
  },
  parentId: {
    type: Number,
    default: undefined
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)
const options = ref<any[]>([])
const levelMap = {
  province: 1,
  city: 2,
  district: 3
}

// 监听外部 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
})

// 监听内部 selectedValue 变化
watch(() => selectedValue.value, (newVal) => {
  emit('update:modelValue', newVal)
})

// 监听父ID变化，重新加载选项
watch(() => props.parentId, (newVal) => {
  if (props.level !== 'province') {
    loadOptions()
  }
})

// 加载选项
const loadOptions = async () => {
  try {
    if (props.level === 'province') {
      // 获取省份列表
      console.log('正在请求省份数据，API路径: /api/travel/regions/level/1')
      const response = await getRegionsByLevel(1)
      if (Array.isArray(response)) {
        console.log(`成功获取省份数据: ${response.length}条记录`, response)
        options.value = response
      } else {
        console.error('获取省份数据格式错误:', response)
        options.value = []
      }
    } else if (props.level === 'city' && props.parentId) {
      // 获取指定省份下的城市列表
      console.log(`正在请求城市数据，API路径: /api/travel/regions/parent/${props.parentId}`)
      const response = await getRegionsByParentId(props.parentId)
      if (Array.isArray(response)) {
        console.log(`成功获取城市数据: ${response.length}条记录`, response)
        options.value = response
      } else {
        console.error('获取城市数据格式错误:', response)
        options.value = []
      }
    } else if (props.level === 'district' && props.parentId) {
      // 获取指定城市下的区县列表
      console.log(`正在请求区县数据，API路径: /api/travel/regions/parent/${props.parentId}`)
      const response = await getRegionsByParentId(props.parentId)
      if (Array.isArray(response)) {
        console.log(`成功获取区县数据: ${response.length}条记录`, response)
        options.value = response
      } else {
        console.error('获取区县数据格式错误:', response)
        options.value = []
      }
    } else {
      // 如果是城市或区县但没有父ID，清空选项
      options.value = []
    }
  } catch (error) {
    console.error(`获取${props.level}数据失败:`, error)
    console.error('错误详情:', error.response?.data || error.message)
    options.value = []
    
    // 如果是重要的省级数据，尝试重试
    if (props.level === 'province') {
      console.log('尝试重新加载省份数据...')
      setTimeout(() => {
        loadOptions()
      }, 2000)
    }
  }
}

// 选择变化
const handleChange = (value: number) => {
  if (value) {
    const selectedOption = options.value.find(opt => opt.id === value)
    if (selectedOption) {
      emit('change', value, selectedOption.name)
    } else {
      emit('change', value)
    }
  } else {
    emit('change', value)
  }
}

// 组件挂载时加载选项
onMounted(() => {
  loadOptions()
})
</script>

<style scoped>
.el-select {
  width: 100%;
}

:deep(.el-input) {
  width: 100%;
}
</style>
