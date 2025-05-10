<template>
  <div class="course-application">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程申请管理</span>
          <el-button type="primary" @click="showCreateApplicationDialog">
            申请课程
          </el-button>
        </div>
      </template>
      
      <div class="application-list" v-loading="loading">
        <el-empty v-if="applications.length === 0" description="暂无课程申请记录">
          <el-button type="primary" @click="showCreateApplicationDialog">立即申请课程</el-button>
        </el-empty>
        
        <el-table
          v-else
          :data="applications"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="课程标题" width="180" />
          <el-table-column prop="subject" label="课程科目" width="150" />
          <el-table-column prop="hourlyPrice" label="每小时价格" width="120">
            <template #default="scope">
              ¥{{ scope.row.hourlyPrice }}
            </template>
          </el-table-column>
          <el-table-column label="工作时间" width="150">
            <template #default="scope">
              {{ scope.row.workTimeStart }}:00 - {{ scope.row.workTimeEnd }}:00
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row.status)"
                effect="plain"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="viewApplicationDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="scope.row.status === 'REJECTED'"
                type="success"
                link
                @click="resubmitApplication(scope.row)"
              >
                重新提交
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程申请详情"
      width="60%"
    >
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程标题">{{ currentApplication.title }}</el-descriptions-item>
          <el-descriptions-item label="课程科目">{{ currentApplication.subject }}</el-descriptions-item>
          <el-descriptions-item label="每小时价格">¥{{ currentApplication.hourlyPrice }}</el-descriptions-item>
          <el-descriptions-item label="工作时间">{{ currentApplication.workTimeStart }}:00 - {{ currentApplication.workTimeEnd }}:00</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentApplication.status)">
              {{ getStatusText(currentApplication.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentApplication.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后更新" :span="2">{{ currentApplication.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="课程描述" :span="2">
            {{ currentApplication.description }}
          </el-descriptions-item>
          <el-descriptions-item label="拒绝理由" v-if="currentApplication.status === 'REJECTED'" :span="2">
            <div class="rejection-reason">
              {{ currentApplication.rejectionReason }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="course-image" v-if="currentApplication.imagePaths && currentApplication.imagePaths.length > 0">
          <h3>课程图片：</h3>
          <div class="image-gallery">
            <el-image
              v-for="(path, index) in currentApplication.imagePaths"
              :key="index"
              style="width: 300px; height: auto; margin-right: 10px; margin-bottom: 10px;"
              :src="path"
              :preview-src-list="currentApplication.imagePaths"
              :initial-index="index"
              fit="contain"
              :z-index="3000"
              @error="handleImageError(index)"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentApplication && currentApplication.status === 'REJECTED'"
            type="primary"
            @click="resubmitApplication(currentApplication)"
          >
            重新提交
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 创建申请对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="isResubmit ? '修改课程申请' : '创建课程申请'"
      width="60%"
    >
      <el-form
        ref="formRef"
        :model="applicationForm"
        :rules="rules"
        label-width="120px"
      >
        <!-- 添加简历提示 -->
        <el-alert
          title="提示：您的个人简历信息（毕业学校、主要教授科目、个人介绍）将自动随课程申请一起提交给管理员审核。"
          type="info"
          description="请确保您已在个人资料页的'个人简历'选项卡中填写了详细的简历信息，完善的简历有助于提高课程申请审核通过率。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div style="margin-top: 10px;">
              <el-button type="primary" size="small" @click="goToResumeEdit">
                编辑简历
              </el-button>
            </div>
          </template>
        </el-alert>
        
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="applicationForm.title" placeholder="请输入课程标题" />
        </el-form-item>
        
        <el-form-item label="课程科目" prop="subject">
          <el-select 
            v-model="applicationForm.subject" 
            placeholder="请选择课程科目"
            filterable
            class="subject-select"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.name"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="每小时价格" prop="hourlyPrice">
          <el-input v-model="applicationForm.hourlyPrice" type="number" placeholder="请输入每小时价格(元)" />
        </el-form-item>
        
        <el-form-item label="工作时间">
          <div class="work-time-container">
            <el-form-item prop="workTimeStart" class="work-time-item">
              <el-time-select
                v-model="applicationForm.workTimeStart"
                start="08:00"
                step="01:00"
                end="21:00"
                placeholder="开始时间"
                class="work-time-select"
                @change="validateWorkTime"
              />
            </el-form-item>
            <span class="separator">至</span>
            <el-form-item prop="workTimeEnd" class="work-time-item">
              <el-time-select
                v-model="applicationForm.workTimeEnd"
                start="09:00"
                step="01:00"
                end="22:00"
                placeholder="结束时间"
                class="work-time-select"
                :min-time="applicationForm.workTimeStart"
                @change="validateWorkTime"
              />
            </el-form-item>
          </div>
        </el-form-item>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="applicationForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入课程描述，包括您的教学经验、教学特点等"
          />
        </el-form-item>
        
        <el-form-item label="课程图片" prop="images">
          <el-upload
            class="course-image-uploader"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :limit="5"
            :file-list="fileList"
            multiple
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                可上传多张图片(最多5张)，JPG/PNG 格式，每张不超过2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApplication" :loading="submitLoading">
            {{ isResubmit ? '重新提交' : '提交申请' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadUserFile } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import {
  CourseApplicationDTO,
  createCourseApplication,
  getTeacherCourseApplications,
  getTeacherCourseApplication,
  resubmitCourseApplication
} from '../../api/courseApplication'
import { SubjectDTO, getAllSubjects } from '../../api/subject'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const applications = ref<CourseApplicationDTO[]>([])
const currentApplication = ref<CourseApplicationDTO | null>(null)
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const isResubmit = ref(false)
const fileList = ref<UploadUserFile[]>([])
const formRef = ref<FormInstance>()
const userStore = useUserStore()
const router = useRouter()
const subjects = ref<SubjectDTO[]>([])

// 表单数据
const applicationForm = reactive({
  id: null as number | null,
  title: '',
  subject: '',
  hourlyPrice: '',
  description: '',
  workTimeStart: '',
  workTimeEnd: '',
  images: [] as File[]
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请输入课程科目', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  hourlyPrice: [
    { required: true, message: '请输入每小时价格', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效的价格', trigger: 'blur' }
  ],
  workTimeStart: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  workTimeEnd: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchApplications(),
    fetchSubjects()
  ])
})

// 获取课程申请列表
const fetchApplications = async () => {
  loading.value = true
  try {
    applications.value = await getTeacherCourseApplications()
  } catch (error) {
    ElMessage.error('获取课程申请列表失败')
  } finally {
    loading.value = false
  }
}

// 获取科目列表
const fetchSubjects = async () => {
  try {
    subjects.value = await getAllSubjects()
  } catch (error) {
    ElMessage.error('获取科目列表失败')
  }
}

// 查看申请详情
const viewApplicationDetail = async (application: CourseApplicationDTO) => {
  try {
    currentApplication.value = await getTeacherCourseApplication(application.id)
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取申请详情失败')
  }
}

// 显示创建申请对话框
const showCreateApplicationDialog = () => {
  resetForm()
  isResubmit.value = false
  createDialogVisible.value = true
}

// 重新提交被拒绝的申请
const resubmitApplication = (application: CourseApplicationDTO) => {
  resetForm()
  
  // 填充表单数据
  applicationForm.id = application.id
  applicationForm.title = application.title
  applicationForm.subject = application.subject
  applicationForm.hourlyPrice = application.hourlyPrice.toString()
  applicationForm.description = application.description
  
  // 处理工作时间
  applicationForm.workTimeStart = `${application.workTimeStart.toString().padStart(2, '0')}:00`
  applicationForm.workTimeEnd = `${application.workTimeEnd.toString().padStart(2, '0')}:00`
  
  // 如果有图片，显示已有图片
  if (application.imagePaths && application.imagePaths.length > 0) {
    fileList.value = application.imagePaths.map((path, index) => ({
      name: `existing-image-${index}`,
      url: path
    }))
  }
  
  isResubmit.value = true
  detailDialogVisible.value = false
  createDialogVisible.value = true
}

// 验证工作时间
const validateWorkTime = () => {
  if (!applicationForm.workTimeStart || !applicationForm.workTimeEnd) {
    return
  }
  
  const start = parseInt(applicationForm.workTimeStart.split(':')[0])
  const end = parseInt(applicationForm.workTimeEnd.split(':')[0])
  
  if (end <= start) {
    ElMessage.warning('结束时间必须晚于开始时间')
    applicationForm.workTimeEnd = ''
  }
}

// 处理图片上传
const handleImageChange = (file: UploadUserFile) => {
  if (!file.raw) return

  // 限制文件大小 (2MB)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.warning('图片大小不能超过 2MB!')
    return false
  }
  
  // 限制文件类型
  const isImage = file.raw instanceof File && 
    ['image/jpeg', 'image/png', 'image/jpg'].includes(file.raw.type)
  if (!isImage) {
    ElMessage.warning('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  
  // 添加到图片列表
  if (file.raw) {
    applicationForm.images.push(file.raw)
  }
  return true
}

// 处理图片移除
const handleImageRemove = (file: UploadUserFile) => {
  // 如果是刚上传的文件，从applicationForm.images中移除
  if (file.raw) {
    const index = applicationForm.images.findIndex(img => img === file.raw)
    if (index !== -1) {
      applicationForm.images.splice(index, 1)
    }
  }
}

// 处理图片加载错误
const handleImageError = (index: number) => {
  if (currentApplication.value && currentApplication.value.imagePaths) {
    console.error(`图片加载失败: ${currentApplication.value.imagePaths[index]}`)
  }
}

// 提交申请
const submitApplication = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写完整的表单信息')
      return
    }
    
    // 解析工作时间
    const workTimeStart = parseInt(applicationForm.workTimeStart.split(':')[0])
    const workTimeEnd = parseInt(applicationForm.workTimeEnd.split(':')[0])
    
    // 确认提交
    try {
      await ElMessageBox.confirm(
        '确定提交该课程申请吗？提交后需等待管理员审核。',
        '提交确认',
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      
      submitLoading.value = true
      
      if (isResubmit.value && applicationForm.id) {
        // 重新提交申请
        const result = await resubmitCourseApplication(
          applicationForm.id,
          applicationForm.title,
          applicationForm.subject,
          applicationForm.hourlyPrice,
          applicationForm.description,
          workTimeStart,
          workTimeEnd,
          applicationForm.images
        )
        
        if (result.success) {
          ElMessage.success('课程申请重新提交成功，等待审核')
          createDialogVisible.value = false
          await fetchApplications()
        } else {
          ElMessage.error(result.message || '重新提交申请失败')
        }
      } else {
        // 创建新申请
        const result = await createCourseApplication(
          applicationForm.title,
          applicationForm.subject,
          applicationForm.hourlyPrice,
          applicationForm.description,
          workTimeStart,
          workTimeEnd,
          applicationForm.images
        )
        
        if (result.success) {
          ElMessage.success('课程申请提交成功，等待审核')
          createDialogVisible.value = false
          await fetchApplications()
        } else {
          ElMessage.error(result.message || '提交申请失败')
        }
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败: ' + error.message)
      }
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  applicationForm.id = null
  applicationForm.title = ''
  applicationForm.subject = ''
  applicationForm.hourlyPrice = ''
  applicationForm.description = ''
  applicationForm.workTimeStart = ''
  applicationForm.workTimeEnd = ''
  applicationForm.images = []
  fileList.value = []
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    case 'PENDING':
    default:
      return 'warning'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return '已通过'
    case 'REJECTED':
      return '已拒绝'
    case 'PENDING':
    default:
      return '审核中'
  }
}

// 导航到简历编辑页面
const goToResumeEdit = () => {
  router.push({
    path: '/supervisor/profile',
    query: { tab: 'resume' }
  });
  createDialogVisible.value = false;
};
</script>

<style scoped>
.course-application {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.application-list {
  margin-top: 20px;
}

.work-time-container {
  display: flex;
  align-items: center;
}

.work-time-select {
  width: 140px;
}

.work-time-item {
  margin-bottom: 0;
}

.work-time-item :deep(.el-form-item__error) {
  position: absolute;
  top: 100%;
  left: 0;
}

.separator {
  margin: 0 15px;
}

.course-image-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.course-image-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-upload__tip {
  line-height: 1.2;
  color: var(--el-text-color-secondary);
  margin-top: 7px;
}

.application-detail {
  padding: 20px 0;
}

.course-image {
  margin-top: 20px;
}

.rejection-reason {
  color: #f56c6c;
  font-weight: bold;
  padding: 10px;
  border-left: 3px solid #f56c6c;
  background-color: #fef0f0;
}

.dialog-footer {
  margin-top: 20px;
}

.subject-select {
  width: 100%;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
}
</style> 