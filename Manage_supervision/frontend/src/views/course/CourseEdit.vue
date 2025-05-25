<template>
  <div class="course-edit-container">
    <div class="header">
      <h2>{{ isEdit ? '编辑课程' : '创建课程' }}</h2>
      <div class="actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="courseForm"
      :rules="rules"
      label-width="120px"
      class="course-form"
      v-loading="loading"
    >
      <el-form-item label="课程名称" prop="courseName">
        <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
      </el-form-item>

      <el-form-item label="授课教师" prop="teacherId">
        <el-select v-model="courseForm.teacherId" placeholder="请选择授课教师" filterable>
          <el-option
            v-for="teacher in teachers"
            :key="teacher.id"
            :label="teacher.realName || teacher.username"
            :value="teacher.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="课程时长(分钟)" prop="duration">
        <el-input-number v-model="courseForm.duration" :min="0" />
      </el-form-item>

      <el-form-item label="课程类别" prop="category">
        <el-select v-model="courseForm.category" placeholder="请选择课程类别" filterable>
          <el-option
            v-for="category in categories"
            :key="category.name"
            :label="category.name"
            :value="category.name"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="courseForm.status">
          <el-radio label="active">活跃</el-radio>
          <el-radio label="inactive">未激活</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="课程封面">
        <el-upload
          class="cover-uploader"
          action="/api/upload/image"
          :show-file-list="false"
          :on-success="handleCoverSuccess"
          :before-upload="beforeCoverUpload"
        >
          <img v-if="courseForm.coverImage" :src="courseForm.coverImage" class="cover-image" />
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">建议上传16:9比例的图片，大小不超过2MB</div>
      </el-form-item>

      <el-form-item label="课程简介" prop="description">
        <el-input
          v-model="courseForm.description"
          type="textarea"
          :rows="6"
          placeholder="请输入课程简介"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getCourseById, createCourse, updateCourse } from '../../api/course'
import { getAllTeachers } from '../../api/teacher'
import { getAllCourseCategories } from '../../api/courseCategory'
import type { Course } from '../../api/course'

const route = useRoute()
const router = useRouter()

// 判断是编辑还是创建
const isEdit = computed(() => {
  return route.path.includes('/edit')
})

const courseId = computed(() => {
  return isEdit.value ? Number(route.params.id) : null
})

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 课程表单数据
const courseForm = reactive<Course>({
  courseName: '',
  description: '',
  teacherId: undefined,
  duration: 0,
  category: '',
  coverImage: '',
  status: 'active'
})

// 表单验证规则
const rules = reactive<FormRules>({
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度应在2到100个字符之间', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程类别', trigger: 'change' }
  ],
  description: [
    { max: 2000, message: '简介不能超过2000个字符', trigger: 'blur' }
  ]
})

// 教师列表
const teachers = ref<any[]>([])

// 课程类别
const categories = ref<any[]>([])

// 加载课程数据（编辑模式）
const loadCourseData = async () => {
  if (!isEdit.value || !courseId.value) return
  
  loading.value = true
  try {
    const data = await getCourseById(courseId.value)
    // 将获取的数据填充到表单中
    Object.assign(courseForm, data)
  } catch (error) {
    console.error('加载课程数据失败:', error)
    ElMessage.error('加载课程数据失败，请稍后重试')
    router.push('/course')
  } finally {
    loading.value = false
  }
}

// 加载教师列表
const loadTeachers = async () => {
  try {
    const response = await getAllTeachers()
    teachers.value = response
  } catch (error) {
    console.error('加载教师列表失败:', error)
    ElMessage.error('加载教师列表失败，请稍后重试')
  }
}

// 加载课程类别
const loadCategories = async () => {
  try {
    const response = await getAllCourseCategories()
    categories.value = response
  } catch (error) {
    console.error('加载课程类别失败:', error)
    ElMessage.error('加载课程类别失败，请稍后重试')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请填写完整信息')
      return
    }
    
    loading.value = true
    try {
      if (isEdit.value && courseId.value) {
        // 编辑模式
        await updateCourse(courseId.value, courseForm)
        ElMessage.success('课程更新成功')
      } else {
        // 创建模式
        await createCourse(courseForm)
        ElMessage.success('课程创建成功')
      }
      router.push('/course')
    } catch (error) {
      console.error('保存课程失败:', error)
      ElMessage.error('保存课程失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 返回上一页
const goBack = () => {
  ElMessageBox.confirm(
    '确定要取消操作吗？未保存的数据将丢失',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      router.back()
    })
    .catch(() => {
      // 用户取消操作，不做任何处理
    })
}

// 处理封面上传成功
const handleCoverSuccess = (response: any) => {
  courseForm.coverImage = response.url
  ElMessage.success('封面上传成功')
}

// 上传前检查文件
const beforeCoverUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('封面图片只能是JPG或PNG格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('封面图片大小不能超过2MB!')
    return false
  }
  return true
}

// 组件挂载时加载数据
onMounted(() => {
  loadCourseData()
  loadTeachers()
  loadCategories()
})
</script>

<style scoped>
.course-edit-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.course-form {
  max-width: 800px;
}

.cover-uploader {
  width: 300px;
}

.cover-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.cover-image {
  width: 300px;
  height: 169px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
}
</style> 