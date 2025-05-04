<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="120px"
    class="order-form"
  >
    <el-form-item label="服务名称" prop="serviceTitle">
      <div>{{ serviceInfo.title }}</div>
    </el-form-item>
    
    <el-form-item label="陪玩名称" prop="companionName">
      <div>{{ serviceInfo.companionName }}</div>
    </el-form-item>
    
    <el-form-item label="游戏类型" prop="gameTypes">
      <div>{{ serviceInfo.gameTypes }}</div>
    </el-form-item>
    
    <el-form-item label="服务单价" prop="price">
      <div>{{ serviceInfo.price }} 元/小时</div>
    </el-form-item>
    
    <el-form-item label="购买小时数" prop="hours">
      <el-input-number
        v-model="form.hours"
        :min="1"
        :max="24"
        :precision="0"
        @change="calculateTotal"
      />
    </el-form-item>
    
    <el-form-item label="预约时间" prop="appointedTime">
      <el-date-picker
        v-model="form.appointedTime"
        type="datetime"
        placeholder="选择预约时间"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DD HH:mm:ss"
        :disabled-date="disabledDate"
        :disabled-hours="disabledHours"
      />
    </el-form-item>
    
    <el-form-item label="备注信息" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        :rows="3"
        placeholder="请输入备注信息，如游戏ID、段位等"
      />
    </el-form-item>
    
    <el-form-item label="总价">
      <div class="total-price">
        <span class="price-value">¥{{ totalPrice }}</span>
      </div>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="submitForm">提交订单</el-button>
      <el-button @click="cancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, PropType, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { CompanionService } from '../api/companion'
import { createOrder, CreateOrderRequest } from '../api/order'

const props = defineProps({
  serviceId: {
    type: Number,
    required: true
  },
  serviceInfo: {
    type: Object as PropType<CompanionService>,
    required: true
  }
})

const emit = defineEmits(['success', 'cancel'])

const formRef = ref()
const form = reactive<CreateOrderRequest>({
  serviceId: props.serviceId,
  hours: 1,
  appointedTime: '',
  remark: ''
})

const rules = {
  hours: [
    { required: true, message: '请选择购买小时数', trigger: 'blur' },
    { type: 'number', min: 1, message: '小时数必须大于0', trigger: 'blur' }
  ],
  appointedTime: [
    { required: true, message: '请选择预约时间', trigger: 'blur' }
  ]
}

const totalPrice = computed(() => {
  if (!props.serviceInfo.price || !form.hours) return 0
  return (props.serviceInfo.price * form.hours).toFixed(2)
})

// 计算总价
const calculateTotal = () => {
  // 已由计算属性完成
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 禁用的小时
const disabledHours = () => {
  // 这里可以根据陪玩的服务时间来禁用特定小时
  // 例如，如果陪玩只在晚上8点到凌晨2点提供服务
  const hours = []
  if (props.serviceInfo.serviceStartTime && props.serviceInfo.serviceEndTime) {
    const startHour = parseInt(props.serviceInfo.serviceStartTime.split(':')[0])
    let endHour = parseInt(props.serviceInfo.serviceEndTime.split(':')[0])
    
    if (endHour < startHour) {
      endHour += 24 // 处理跨天情况
    }
    
    for (let i = 0; i < 24; i++) {
      if (i < startHour || i > endHour) {
        hours.push(i)
      }
    }
  }
  
  return hours
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await createOrder(form)
        ElMessage.success('订单创建成功')
        emit('success', response.orderId)
      } catch (error: any) {
        console.error('创建订单失败:', error)
        ElMessage.error(error.response?.data || '订单创建失败，请重试')
      }
    } else {
      return false
    }
  })
}

// 取消
const cancel = () => {
  emit('cancel')
}

// 初始化
onMounted(() => {
  form.serviceId = props.serviceId
})
</script>

<style scoped>
.order-form {
  max-width: 500px;
  margin: 0 auto;
}

.total-price {
  font-size: 16px;
  font-weight: 500;
}

.price-value {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: 700;
}
</style> 