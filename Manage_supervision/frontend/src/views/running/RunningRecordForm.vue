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
          :min="0.1"
          :max="100"
          controls-position="right"
          style="width: 100%;"
          @change="calculatePace"
        >
          <template #suffix>公里</template>
        </el-input-number>
      </el-form-item>

      <el-form-item label="跑步时长" prop="duration">
        <el-input-number
          v-model="recordForm.duration"
          :min="1"
          :max="1440"
          controls-position="right"
          style="width: 100%;"
          @change="calculatePace"
        >
          <template #suffix>分钟</template>
        </el-input-number>
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

      <el-form-item label="记录日期" prop="recordDate">
        <el-date-picker
          v-model="recordForm.recordDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
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
        <el-table-column prop="duration" label="时长(分钟)" width="110" sortable />
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
import { addRunningRecord, getRunningRecords } from '@/api/running'
import dayjs from 'dayjs'

// 表单引用
const recordFormRef = ref<FormInstance>()
// 加载状态
const loading = ref(false)
// 计算出的配速显示
const calculatedPace = ref('0:00')

// 记录表单
const recordForm = reactive({
  distance: 0,
  duration: 0,
  pace: '',
  recordDate: dayjs().format('YYYY-MM-DD')
})

// 记录列表
const recordList = ref<any[]>([])

// 计算配速的函数
const calculatePace = () => {
  if (recordForm.distance <= 0 || recordForm.duration <= 0) {
    calculatedPace.value = '0:00'
    recordForm.pace = '0:00'
    return
  }
  
  // 计算每公里需要的分钟数
  const paceInMinutes = recordForm.duration / recordForm.distance
  
  // 分解成分钟和秒
  const minutes = Math.floor(paceInMinutes)
  const seconds = Math.round((paceInMinutes - minutes) * 60)
  
  // 格式化为 分:秒 格式
  const formattedSeconds = seconds < 10 ? `0${seconds}` : `${seconds}`
  calculatedPace.value = `${minutes}:${formattedSeconds}`
  
  // 更新表单中的配速值
  recordForm.pace = calculatedPace.value
}

// 表单校验规则
const rules = {
  distance: [
    { required: true, message: '请输入跑步距离', trigger: 'blur' },
    { type: 'number', min: 0.1, message: '距离必须大于0.1公里', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入跑步时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '时长必须大于1分钟', trigger: 'blur' }
  ],
  recordDate: [
    { required: true, message: '请选择记录日期', trigger: 'change' }
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
          recordDate: recordForm.recordDate
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
    recordForm.recordDate = dayjs().format('YYYY-MM-DD')
    calculatedPace.value = '0:00'
    recordForm.pace = '0:00'
  }
}

// 获取记录列表
const fetchRecords = async () => {
  try {
    const { data } = await getRunningRecords()
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
</style> 