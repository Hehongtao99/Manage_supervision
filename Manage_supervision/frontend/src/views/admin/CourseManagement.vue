<template>
  <div class="course-management">
    <div class="header">
      <h2>课程管理</h2>
      <div class="header-buttons">
        <el-button type="success" @click="showCategoryManagement">
          <el-icon><Setting /></el-icon>
          类别管理
        </el-button>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.courseName"
            placeholder="请输入课程名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="授课教师">
          <el-input
            v-model="searchForm.teacherName"
            placeholder="请输入教师姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="课程类别">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择课程类别"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="活跃" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCourses">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 课程列表 -->
    <div class="table-section">
      <el-table
        :data="courseList"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="courseDescription" label="课程简介" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="courseDuration" label="课程时长" width="100">
          <template #default="{ row }">
            {{ row.courseDuration }}小时
          </template>
        </el-table-column>
        <el-table-column prop="courseCategory" label="课程类别" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '活跃' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editCourse(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteCourseConfirm(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程简介" prop="courseDescription">
          <el-input
            v-model="courseForm.courseDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入课程简介"
          />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select
            v-model="courseForm.teacherId"
            placeholder="请选择授课教师"
            style="width: 100%"
            @change="handleTeacherChange"
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName || teacher.username"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程时长" prop="courseDuration">
          <el-input-number
            v-model="courseForm.courseDuration"
            :min="1"
            :max="1000"
            placeholder="请输入课程时长"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999;">小时</span>
        </el-form-item>
        <el-form-item label="课程类别" prop="courseCategory">
          <el-select
            v-model="courseForm.courseCategory"
            placeholder="请选择课程类别"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio label="active">活跃</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 课程类别管理对话框 -->
    <el-dialog
      title="课程类别管理"
      v-model="categoryDialogVisible"
      width="800px"
    >
      <div class="category-management">
        <div class="category-header">
          <el-button type="primary" @click="showCreateCategoryDialog">
            <el-icon><Plus /></el-icon>
            新增类别
          </el-button>
        </div>
        
        <el-table
          :data="categoryList"
          v-loading="categoryLoading"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="categoryName" label="类别名称" min-width="150" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editCategory(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteCategoryConfirm(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 新增/编辑类别对话框 -->
    <el-dialog
      :title="categoryDialogTitle"
      v-model="categoryFormDialogVisible"
      width="500px"
      @close="resetCategoryForm"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="100px"
      >
        <el-form-item label="类别名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入类别名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入类别描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategoryForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting } from '@element-plus/icons-vue'
import {
  getCourseList,
  createCourse,
  updateCourse,
  deleteCourse,
  getAllCategories,
  getAllTeachers,
  getCourseCategoryList,
  createCourseCategory,
  updateCourseCategory,
  deleteCourseCategory,
  type Course,
  type CourseCategory,
  type PageResponse
} from '../../api/course'
import { format } from 'date-fns'

// 响应式数据
const loading = ref(false)
const categoryLoading = ref(false)
const courseList = ref<Course[]>([])
const categories = ref<string[]>([])
const teachers = ref<any[]>([])
const categoryList = ref<CourseCategory[]>([])
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const categoryFormDialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const categoryDialogTitle = ref('新增类别')
const isEdit = ref(false)
const isCategoryEdit = ref(false)
const currentCourseId = ref<number>()
const currentCategoryId = ref<number>()

// 搜索表单
const searchForm = reactive({
  courseName: '',
  teacherName: '',
  category: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 课程表单
const courseForm = reactive<Course>({
  courseName: '',
  courseDescription: '',
  teacherId: undefined,
  teacherName: '',
  courseDuration: 1,
  courseCategory: '',
  status: 'active'
})

// 类别表单
const categoryForm = reactive<CourseCategory>({
  categoryName: '',
  description: '',
  status: 'active'
})

// 表单引用
const courseFormRef = ref()
const categoryFormRef = ref()

// 表单验证规则
const courseRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  courseDescription: [
    { required: true, message: '请输入课程简介', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  courseDuration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  courseCategory: [
    { required: true, message: '请选择课程类别', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const categoryRules = {
  categoryName: [
    { required: true, message: '请输入类别名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 方法
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const response: PageResponse<Course> = await getCourseList(params)
    courseList.value = response.content
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    ElMessage.error('获取课程类别失败')
  }
}

const loadTeachers = async () => {
  try {
    teachers.value = await getAllTeachers()
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  }
}

const loadCategoryList = async () => {
  categoryLoading.value = true
  try {
    const response: PageResponse<CourseCategory> = await getCourseCategoryList({
      page: 1,
      size: 100
    })
    categoryList.value = response.content
  } catch (error) {
    ElMessage.error('获取课程类别列表失败')
  } finally {
    categoryLoading.value = false
  }
}

const searchCourses = () => {
  pagination.page = 1
  loadCourses()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    courseName: '',
    teacherName: '',
    category: '',
    status: ''
  })
  searchCourses()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadCourses()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadCourses()
}

const showCreateDialog = () => {
  dialogTitle.value = '新增课程'
  isEdit.value = false
  dialogVisible.value = true
}

const editCourse = (course: Course) => {
  dialogTitle.value = '编辑课程'
  isEdit.value = true
  currentCourseId.value = course.id
  Object.assign(courseForm, course)
  dialogVisible.value = true
}

const handleTeacherChange = (teacherId: number) => {
  const selectedTeacher = teachers.value.find(t => t.id === teacherId)
  if (selectedTeacher) {
    courseForm.teacherName = selectedTeacher.realName || selectedTeacher.username
  }
}

const resetForm = () => {
  Object.assign(courseForm, {
    courseName: '',
    courseDescription: '',
    teacherId: undefined,
    teacherName: '',
    courseDuration: 1,
    courseCategory: '',
    status: 'active'
  })
  courseFormRef.value?.clearValidate()
}

const submitForm = async () => {
  try {
    await courseFormRef.value.validate()
    
    if (isEdit.value && currentCourseId.value) {
      await updateCourse(currentCourseId.value, courseForm)
      ElMessage.success('课程更新成功')
    } else {
      await createCourse(courseForm)
      ElMessage.success('课程创建成功')
    }
    
    dialogVisible.value = false
    loadCourses()
  } catch (error) {
    ElMessage.error(isEdit.value ? '课程更新失败' : '课程创建失败')
  }
}

const deleteCourseConfirm = (course: Course) => {
  ElMessageBox.confirm(
    `确定要删除课程"${course.courseName}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCourse(course.id!)
      ElMessage.success('课程删除成功')
      loadCourses()
    } catch (error) {
      ElMessage.error('课程删除失败')
    }
  })
}

// 类别管理相关方法
const showCategoryManagement = () => {
  categoryDialogVisible.value = true
  loadCategoryList()
}

const showCreateCategoryDialog = () => {
  categoryDialogTitle.value = '新增类别'
  isCategoryEdit.value = false
  categoryFormDialogVisible.value = true
}

const editCategory = (category: CourseCategory) => {
  categoryDialogTitle.value = '编辑类别'
  isCategoryEdit.value = true
  currentCategoryId.value = category.id
  Object.assign(categoryForm, category)
  categoryFormDialogVisible.value = true
}

const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    categoryName: '',
    description: '',
    status: 'active'
  })
  categoryFormRef.value?.clearValidate()
}

const submitCategoryForm = async () => {
  try {
    await categoryFormRef.value.validate()
    
    if (isCategoryEdit.value && currentCategoryId.value) {
      await updateCourseCategory(currentCategoryId.value, categoryForm)
      ElMessage.success('类别更新成功')
    } else {
      await createCourseCategory(categoryForm)
      ElMessage.success('类别创建成功')
    }
    
    categoryFormDialogVisible.value = false
    loadCategoryList()
    loadCategories() // 刷新类别下拉框
  } catch (error) {
    ElMessage.error(isCategoryEdit.value ? '类别更新失败' : '类别创建失败')
  }
}

const deleteCategoryConfirm = (category: CourseCategory) => {
  ElMessageBox.confirm(
    `确定要删除类别"${category.categoryName}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCourseCategory(category.id!)
      ElMessage.success('类别删除成功')
      loadCategoryList()
      loadCategories() // 刷新类别下拉框
    } catch (error) {
      ElMessage.error('类别删除失败')
    }
  })
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return format(new Date(dateTime), 'yyyy-MM-dd HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadCourses()
  loadCategories()
  loadTeachers()
})
</script>

<style scoped>
.course-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.search-section {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.category-management {
  padding: 10px 0;
}

.category-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 