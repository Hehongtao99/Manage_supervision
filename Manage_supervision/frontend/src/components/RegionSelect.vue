<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    @change="handleChange"
    popper-class="larger-region-dropdown"
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
import axios from '../utils/axios'

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
    let url = '/api/admin/regions'
    
    if (props.level === 'province') {
      // 获取省份列表
      url += '/level/1'
    } else if (props.level === 'city' && props.parentId) {
      // 获取指定省份下的城市列表
      url += `/children/${props.parentId}`
    } else if (props.level === 'district' && props.parentId) {
      // 获取指定城市下的区县列表
      url += `/children/${props.parentId}`
    } else {
      // 如果是城市或区县但没有父ID，清空选项
      options.value = []
      return
    }
    
    console.log(`正在请求地区数据: ${url}`)
    const response = await axios.get(url)
    
    if (response.data && Array.isArray(response.data)) {
      console.log(`成功获取${props.level}数据: ${response.data.length}条记录`)
      options.value = response.data
    } else {
      console.error(`获取${props.level}数据格式错误:`, response.data)
      options.value = []
    }
  } catch (error) {
    console.error(`获取${props.level}数据失败:`, error)
    if (error.response) {
      console.error(`状态码: ${error.response.status}, 错误信息:`, error.response.data)
    }
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
  emit('change', value)
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

:deep(.el-input__inner) {
  height: 44px !important;
  line-height: 44px !important;
  font-size: 16px !important;
  padding-left: 15px !important;
  font-weight: 500 !important;
}

:deep(.larger-region-dropdown) {
  --el-select-dropdown-max-height: 400px !important;
  min-width: 280px !important;
}

:deep(.larger-region-dropdown .el-select-dropdown__item) {
  padding: 14px 20px !important;
  font-size: 16px !important;
  height: auto !important;
  line-height: 1.5 !important;
}

:deep(.el-select .el-input .el-select__caret) {
  font-size: 20px !important;
}

:deep(.el-select-dropdown__item.selected) {
  font-weight: bold !important;
  color: var(--el-color-primary) !important;
  background-color: rgba(64, 158, 255, 0.15) !important;
}

:deep(.el-select:hover .el-input__inner) {
  border-color: var(--el-color-primary) !important;
}

:deep(.el-select:not(.is-disabled):hover .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-select-dropdown__list) {
  padding: 8px 0 !important; 
}

:deep(.el-select-dropdown__wrap) {
  max-height: 400px !important;
}

:deep(.el-scrollbar__view) {
  padding: 0 !important;
}
</style> 