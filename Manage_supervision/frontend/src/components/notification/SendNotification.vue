<template>
  <el-card shadow="hover" class="send-notification-card">
    <template #header>
      <div class="card-header">
        <h3>发送通知</h3>
      </div>
    </template>
    
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="通知标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入通知标题" />
      </el-form-item>
      
      <el-form-item label="通知内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入通知内容"
        />
      </el-form-item>
      
      <el-form-item label="选择接收学生" prop="recipientIds">
        <el-select
          v-model="form.recipientIds"
          multiple
          placeholder="请选择接收学生"
          style="width: 100%"
          :loading="loadingStudents"
        >
          <el-option
            v-for="student in students"
            :key="student.id"
            :label="student.realName ? `${student.realName} (${student.userNumber})` : student.username"
            :value="student.id"
          />
        </el-select>
        
        <div class="student-selection-actions" v-if="students.length > 0">
          <el-button text type="primary" @click="selectAllStudents">全选</el-button>
          <el-button text @click="clearStudentSelection">清空</el-button>
        </div>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" :loading="sending" @click="submitForm">发送通知</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { useNotificationStore } from '../../stores/notification';
import notificationService from '../../services/notification';
import type { UserDTO } from '../../types/user';

const notificationStore = useNotificationStore();

// 表单和验证规则
const formRef = ref<FormInstance>();
const form = ref({
  title: '',
  content: '',
  recipientIds: [] as number[]
});

const rules = ref<FormRules>({
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在2-50个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度应在5-500个字符之间', trigger: 'blur' }
  ],
  recipientIds: [
    { required: true, type: 'array', message: '请至少选择一名学生', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一名学生', trigger: 'change' }
  ]
});

// 学生列表
const students = ref<UserDTO[]>([]);
const loadingStudents = ref(false);

// 状态
const sending = ref(false);

// 加载学生列表
const loadStudents = async () => {
  loadingStudents.value = true;
  try {
    const response = await notificationService.getStudents();
    students.value = response;
  } catch (error) {
    console.error('加载学生列表失败:', error);
    ElMessage.error('加载学生列表失败，请刷新页面重试');
  } finally {
    loadingStudents.value = false;
  }
};

// 选择所有学生
const selectAllStudents = () => {
  form.value.recipientIds = students.value.map(student => student.id);
};

// 清空学生选择
const clearStudentSelection = () => {
  form.value.recipientIds = [];
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true;
      
      try {
        await notificationStore.sendNotification(
          form.value.title,
          form.value.content,
          form.value.recipientIds
        );
        
        ElMessage.success('通知发送成功');
        resetForm();
      } catch (error: any) {
        console.error('发送通知失败:', error);
        ElMessage.error(`发送通知失败: ${error.message || '未知错误'}`);
      } finally {
        sending.value = false;
      }
    } else {
      ElMessage.warning('请完成必填项');
      return false;
    }
  });
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 页面加载时获取学生列表
onMounted(() => {
  loadStudents();
});
</script>

<style scoped>
.send-notification-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-selection-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style> 