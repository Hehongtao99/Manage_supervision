<template>
  <div class="running-record-container">
    <div class="title-container">
      <h2>添加跑步记录</h2>
      <router-link to="/running-dashboard">
        <el-button type="primary" plain icon="el-icon-data-analysis">查看数据看板</el-button>
      </router-link>
    </div>

    <el-form :model="recordForm" :rules="rules" ref="recordFormRef" label-width="100px" class="record-form">
      <el-form-item label="跑步距离" prop="distance">
        <el-input-number
          v-model="recordForm.distance"
          :precision="2"
          :step="0.1"
          :min="0.5"
          :max="100"
          controls-position="right"
          style="width: 100%;"
          @change="calculatePace"
        >
          <template #suffix>公里</template>
        </el-input-number>
        <div class="form-help-text">建议输入0.5公里以上的距离</div>
      </el-form-item>

      <el-form-item label="跑步时长" prop="duration">
        <div class="duration-input-group">
          <div class="duration-item">
            <el-input-number
              v-model="durationInput.hours"
              :min="0"
              :max="10"
              controls-position="right"
              placeholder="小时"
              @change="updateDuration"
            />
            <span class="duration-unit">小时</span>
          </div>
          <div class="duration-item">
            <el-input-number
              v-model="durationInput.minutes"
              :min="0"
              :max="59"
              controls-position="right"
              placeholder="分钟"
              @change="updateDuration"
            />
            <span class="duration-unit">分钟</span>
          </div>
          <div class="duration-item">
            <el-input-number
              v-model="durationInput.seconds"
              :min="0"
              :max="59"
              controls-position="right"
              placeholder="秒"
              @change="updateDuration"
            />
            <span class="duration-unit">秒</span>
          </div>
        </div>
        <div class="duration-display">总时长: {{ formatTotalDuration(recordForm.duration) }}</div>
        <div class="form-help-text">建议输入合理的跑步时长（如5公里30-60分钟）</div>
      </el-form-item>

      <el-form-item label="配速">
        <el-input
          v-model="calculatedPace"
          disabled
          style="width: 100%;"
        >
          <template #suffix>分:秒/公里</template>
        </el-input>
        <div class="pace-description">配速根据距离和时长自动计算</div>
      </el-form-item>

      <el-form-item label="记录时间" prop="recordDateTime">
        <el-date-picker
          v-model="recordForm.recordDateTime"
          type="datetime"
          placeholder="选择日期和时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%;"
        ></el-date-picker>
      </el-form-item>

      <el-form-item class="form-buttons">
        <el-button type="primary" @click="submitForm" :loading="loading">提交记录</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="record-list-container" v-if="recordList.length > 0">
      <h3>历史记录</h3>
      <el-table :data="recordList" style="width: 100%" max-height="400" stripe border>
        <el-table-column prop="recordDate" label="日期" width="120" sortable />
        <el-table-column prop="distance" label="距离(公里)" width="110" sortable />
        <el-table-column label="时长" width="130" sortable>
          <template #default="scope">
            {{ formatTotalDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="pace" label="配速" width="110" sortable />
        <el-table-column prop="createTime" label="创建时间" min-width="180" sortable />
      </el-table>
    </div>
    <div v-else class="no-data">
      <el-empty description="暂无跑步记录" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import { addRunningRecord, getUserRunningRecords } from '../../api/running'
import dayjs from 'dayjs'

// 表单引用
const recordFormRef = ref<FormInstance>()
// 加载状态
const loading = ref(false)
// 计算出的配速显示
const calculatedPace = ref('0:00')

// 时长输入分解
const durationInput = reactive({
  hours: 0,
  minutes: 0,
  seconds: 0
})

// 记录表单
const recordForm = reactive({
  distance: 0,
  duration: 0, // 总秒数
  pace: '',
  recordDateTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
})

// 记录列表
const recordList = ref<any[]>([])

// 更新总时长（转换为秒）
const updateDuration = () => {
  recordForm.duration = (durationInput.hours || 0) * 3600 + 
                       (durationInput.minutes || 0) * 60 + 
                       (durationInput.seconds || 0)
  calculatePace()
}

// 格式化显示总时长
const formatTotalDuration = (totalSeconds: number) => {
  if (!totalSeconds) return '0秒'
  
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  
  const parts = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)
  if (seconds > 0) parts.push(`${seconds}秒`)
  
  return parts.join('')
}

// 计算配速的函数
const calculatePace = () => {
  if (recordForm.distance <= 0 || recordForm.duration <= 0) {
    calculatedPace.value = '0:00'
    recordForm.pace = '0:00'
    return
  }
  
  // 计算每公里需要的秒数
  const paceInSeconds = recordForm.duration / recordForm.distance
  
  // 添加合理性检查
  if (paceInSeconds > 3600) { // 超过1小时/公里
    calculatedPace.value = '配速过慢'
    recordForm.pace = '配速过慢'
    return
  }
  
  if (paceInSeconds < 180) { // 少于3分钟/公里 (世界纪录约2:50/公里)
    calculatedPace.value = '配速过快'
    recordForm.pace = '配速过快'
    return
  }
  
  // 分解成分钟和秒
  const minutes = Math.floor(paceInSeconds / 60)
  const seconds = Math.round(paceInSeconds % 60)
  
  // 处理秒数进位
  let finalMinutes = minutes
  let finalSeconds = seconds
  if (finalSeconds >= 60) {
    finalMinutes += Math.floor(finalSeconds / 60)
    finalSeconds = finalSeconds % 60
  }
  
  // 格式化为 分:秒 格式
  const formattedSeconds = finalSeconds < 10 ? `0${finalSeconds}` : `${finalSeconds}`
  calculatedPace.value = `${finalMinutes}:${formattedSeconds}`
  
  // 更新表单中的配速值
  recordForm.pace = calculatedPace.value
}

// 表单校验规则
const rules = {
  distance: [
    { required: true, message: '请输入跑步距离', trigger: 'blur' },
    { type: 'number', min: 0.5, message: '距离建议至少0.5公里', trigger: 'blur' },
    { type: 'number', max: 100, message: '距离不能超过100公里', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入跑步时长', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value <= 0) {
          callback(new Error('时长必须大于0'))
        } else if (value < 60) { // 少于1分钟
          callback(new Error('时长建议至少1分钟'))
        } else if (value > 36000) { // 超过10小时
          callback(new Error('时长不能超过10小时'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  recordDateTime: [
    { required: true, message: '请选择记录时间', trigger: 'change' }
  ]
}

// 提交表单
const submitForm = async () => {
  if (!recordFormRef.value) return

  // 重新计算配速确保最新值
  calculatePace()

  await recordFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        loading.value = true
        const { data } = await addRunningRecord({
          distance: recordForm.distance,
          duration: recordForm.duration,
          pace: recordForm.pace,
          recordDateTime: recordForm.recordDateTime
        })
        
        ElMessage.success('跑步记录添加成功！')
        // 重新获取记录列表
        fetchRecords()
        // 重置表单
        resetForm()
      } catch (error) {
        console.error('添加跑步记录失败:', error)
        ElMessage.error('添加跑步记录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 重置表单
const resetForm = () => {
  if (recordFormRef.value) {
    recordFormRef.value.resetFields()
    recordForm.recordDateTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
    durationInput.hours = 0
    durationInput.minutes = 0
    durationInput.seconds = 0
    recordForm.duration = 0
    calculatedPace.value = '0:00'
    recordForm.pace = '0:00'
  }
}

// 获取记录列表
const fetchRecords = async () => {
  try {
    const { data } = await getUserRunningRecords()
    recordList.value = data || []
  } catch (error) {
    console.error('获取跑步记录列表失败:', error)
    ElMessage.error('获取跑步记录列表失败，请稍后重试')
  }
}

// 初始时计算一次配速
calculatePace()

// 生命周期钩子
onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.running-record-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.title-container {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-form {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.form-buttons {
  text-align: right;
  margin-top: 20px;
}

.duration-input-group {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.duration-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.duration-item .el-input-number {
  width: 100px;
}

.duration-unit {
  font-size: 14px;
  color: #606266;
}

.duration-display {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.record-list-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.record-list-container h3 {
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.no-data {
  margin-top: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pace-description {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 