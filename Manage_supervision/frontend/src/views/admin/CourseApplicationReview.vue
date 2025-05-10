<template>
  <div class="course-application-review">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程申请审核</span>
          <div class="filter-container">
            <el-select v-model="filterStatus" placeholder="筛选状态" clearable>
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-button type="primary" @click="fetchApplications">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="application-list" v-loading="loading">
        <el-empty v-if="filteredApplications.length === 0" description="暂无课程申请记录" />
        
        <el-table
          v-else
          :data="filteredApplications"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="teacherName" label="申请教师" width="120" />
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
          <el-table-column label="操作" fixed="right" width="220">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="viewApplicationDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="success"
                link
                @click="approveApplication(scope.row)"
              >
                通过
              </el-button>
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="danger"
                link
                @click="rejectApplication(scope.row)"
              >
                拒绝
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
          <el-descriptions-item label="申请教师">{{ currentApplication.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="申请ID">{{ currentApplication.id }}</el-descriptions-item>
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
        
        <!-- 教师简历信息 -->
        <div class="teacher-resume" v-if="currentApplication">
          <h3>教师简历</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="毕业学校">
              {{ currentApplication.teacherGraduationSchool || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="主要教授科目">
              {{ currentApplication.teacherTeachingSubjects || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="个人介绍">
              <div class="bio-content">
                {{ currentApplication.teacherBio || '未填写个人介绍' }}
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
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
            v-if="currentApplication && currentApplication.status === 'PENDING'"
            type="success"
            @click="approveApplication(currentApplication)"
          >
            通过申请
          </el-button>
          <el-button
            v-if="currentApplication && currentApplication.status === 'PENDING'"
            type="danger"
            @click="rejectApplication(currentApplication)"
          >
            拒绝申请
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 拒绝申请对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申请"
      width="40%"
    >
      <div v-if="currentApplication">
        <p>您即将拒绝 <strong>{{ currentApplication.teacherName }}</strong> 的课程申请：
          <strong>{{ currentApplication.subject }}</strong>
        </p>
        
        <el-form :model="rejectForm" ref="rejectFormRef">
          <el-form-item
            label="拒绝理由"
            prop="reason"
            :rules="[{ required: true, message: '请输入拒绝理由', trigger: 'blur' }]"
          >
            <el-input
              v-model="rejectForm.reason"
              type="textarea"
              rows="4"
              placeholder="请输入拒绝理由，以便教师修改和重新提交"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            @click="submitReject"
            :loading="submitLoading"
          >
            确认拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import {
  CourseApplicationDTO,
  getAllCourseApplications,
  getAdminCourseApplication,
  reviewCourseApplication
} from '../../api/courseApplication'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const applications = ref<CourseApplicationDTO[]>([])
const currentApplication = ref<CourseApplicationDTO | null>(null)
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const filterStatus = ref('')
const rejectFormRef = ref<FormInstance>()

// 拒绝表单
const rejectForm = reactive({
  reason: ''
})

// 过滤后的申请列表
const filteredApplications = computed(() => {
  if (!filterStatus.value) {
    return applications.value
  }
  return applications.value.filter(app => app.status === filterStatus.value)
})

// 初始化
onMounted(async () => {
  await fetchApplications()
})

// 获取课程申请列表
const fetchApplications = async () => {
  loading.value = true
  try {
    applications.value = await getAllCourseApplications()
  } catch (error) {
    ElMessage.error('获取课程申请列表失败')
  } finally {
    loading.value = false
  }
}

// 查看申请详情
const viewApplicationDetail = async (application: CourseApplicationDTO) => {
  try {
    currentApplication.value = await getAdminCourseApplication(application.id)
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取申请详情失败')
  }
}

// 通过申请
const approveApplication = async (application: CourseApplicationDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 ${application.teacherName} 的 "${application.subject}" 课程申请吗？`,
      '确认通过',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    submitLoading.value = true
    const result = await reviewCourseApplication(application.id, true)
    
    if (result.success) {
      ElMessage.success('课程申请已通过')
      // 刷新申请列表和当前显示的申请
      await fetchApplications()
      if (detailDialogVisible.value && currentApplication.value) {
        currentApplication.value.status = 'APPROVED'
      }
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// 拒绝申请
const rejectApplication = (application: CourseApplicationDTO) => {
  currentApplication.value = application
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 提交拒绝
const submitReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid || !currentApplication.value) {
      return
    }
    
    try {
      submitLoading.value = true
      const result = await reviewCourseApplication(
        currentApplication.value.id,
        false,
        rejectForm.reason
      )
      
      if (result.success) {
        ElMessage.success('已拒绝课程申请')
        rejectDialogVisible.value = false
        // 刷新申请列表和当前显示的申请
        await fetchApplications()
        if (detailDialogVisible.value && currentApplication.value) {
          currentApplication.value.status = 'REJECTED'
          currentApplication.value.rejectionReason = rejectForm.reason
        }
      } else {
        ElMessage.error(result.message || '操作失败')
      }
    } catch (error: any) {
      ElMessage.error('操作失败: ' + error.message)
    } finally {
      submitLoading.value = false
    }
  })
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
      return '待审核'
  }
}

// 处理图片加载错误
const handleImageError = (index: number) => {
  if (currentApplication.value && currentApplication.value.imagePaths) {
    // 记录日志，方便调试
    console.error(`图片加载失败: ${currentApplication.value.imagePaths[index]}`);
  }
};
</script>

<style scoped>
.course-application-review {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.application-list {
  margin-top: 20px;
}

.application-detail {
  padding: 20px 0;
}

.course-image {
  margin-top: 20px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
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

.teacher-resume {
  margin-top: 20px;
  margin-bottom: 20px;
}

.teacher-resume h3 {
  font-size: 16px;
  margin-bottom: 10px;
  padding-left: 5px;
  border-left: 3px solid #409EFF;
}

.bio-content {
  white-space: pre-line;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  min-height: 60px;
}
</style> 