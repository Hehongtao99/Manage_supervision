<template>
  <div class="organization-management">
    <el-card class="header-card">
      <h2>组织架构管理</h2>
      <p>管理学院、专业和班级信息</p>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" type="card" class="organization-tabs">
      <!-- 学院管理 -->
      <el-tab-pane label="学院管理" name="colleges">
        <el-card>
          <div class="content-header">
            <div class="search-bar">
              <el-input 
                v-model="collegeSearch" 
                placeholder="搜索学院名称..."
                style="width: 300px; margin-right: 10px;"
                @keyup.enter="loadColleges"
                clearable
              >
                <template #append>
                  <el-button @click="loadColleges" :icon="Search">搜索</el-button>
                </template>
              </el-input>
            </div>
            <el-button type="primary" @click="showCollegeModal = true" :icon="Plus">添加学院</el-button>
          </div>

          <el-table :data="colleges" border stripe v-loading="loading">
            <el-table-column prop="collegeName" label="学院名称" />
            <el-table-column prop="collegeCode" label="学院代码" />
            <el-table-column prop="description" label="描述">
              <template #default="{ row }">
                {{ row.description || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editCollege(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteCollegeConfirm(row.id!)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="collegePage"
            v-model:page-size="pageSize"
            :total="collegeTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadColleges"
            @current-change="loadColleges"
            style="margin-top: 20px; text-align: center;"
          />
        </el-card>
      </el-tab-pane>

      <!-- 专业管理 -->
      <el-tab-pane label="专业管理" name="majors">
        <el-card>
          <div class="content-header">
            <div class="search-bar">
              <el-select v-model="majorCollegeFilter" placeholder="选择学院" @change="loadMajors" style="width: 200px; margin-right: 10px;" clearable>
                <el-option label="所有学院" value="" />
                <el-option 
                  v-for="college in allColleges" 
                  :key="college.id" 
                  :label="college.collegeName"
                  :value="college.id"
                />
              </el-select>
              <el-input 
                v-model="majorSearch" 
                placeholder="搜索专业名称..."
                style="width: 300px; margin-right: 10px;"
                @keyup.enter="loadMajors"
                clearable
              >
                <template #append>
                  <el-button @click="loadMajors" :icon="Search">搜索</el-button>
                </template>
              </el-input>
            </div>
            <el-button type="primary" @click="showMajorModal = true" :icon="Plus">添加专业</el-button>
          </div>

          <el-table :data="majors" border stripe v-loading="loading">
            <el-table-column prop="majorName" label="专业名称" />
            <el-table-column prop="majorCode" label="专业代码" />
            <el-table-column prop="collegeName" label="所属学院" />
            <el-table-column prop="description" label="描述">
              <template #default="{ row }">
                {{ row.description || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editMajor(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteMajorConfirm(row.id!)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="majorPage"
            v-model:page-size="pageSize"
            :total="majorTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadMajors"
            @current-change="loadMajors"
            style="margin-top: 20px; text-align: center;"
          />
        </el-card>
      </el-tab-pane>

      <!-- 班级管理 -->
      <el-tab-pane label="班级管理" name="classes">
        <el-card>
          <div class="content-header">
            <div class="search-bar">
              <el-select v-model="classCollegeFilter" placeholder="选择学院" @change="onCollegeChange" style="width: 200px; margin-right: 10px;" clearable>
                <el-option label="所有学院" value="" />
                <el-option 
                  v-for="college in allColleges" 
                  :key="college.id" 
                  :label="college.collegeName"
                  :value="college.id"
                />
              </el-select>
              <el-select v-model="classMajorFilter" placeholder="选择专业" @change="loadClasses" style="width: 200px; margin-right: 10px;" clearable>
                <el-option label="所有专业" value="" />
                <el-option 
                  v-for="major in filteredMajors" 
                  :key="major.id" 
                  :label="major.majorName"
                  :value="major.id"
                />
              </el-select>
              <el-input 
                v-model="classSearch" 
                placeholder="搜索班级名称..."
                style="width: 300px; margin-right: 10px;"
                @keyup.enter="loadClasses"
                clearable
              >
                <template #append>
                  <el-button @click="loadClasses" :icon="Search">搜索</el-button>
                </template>
              </el-input>
            </div>
            <el-button type="primary" @click="showClassModal = true" :icon="Plus">添加班级</el-button>
          </div>

          <el-table :data="classes" border stripe v-loading="loading">
            <el-table-column prop="className" label="班级名称" />
            <el-table-column prop="classCode" label="班级代码" />
            <el-table-column prop="collegeName" label="所属学院" />
            <el-table-column prop="majorName" label="所属专业" />
            <el-table-column prop="grade" label="年级" />
            <el-table-column prop="studentCount" label="学生人数">
              <template #default="{ row }">
                {{ row.studentCount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editClass(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteClassConfirm(row.id!)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="classPage"
            v-model:page-size="pageSize"
            :total="classTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadClasses"
            @current-change="loadClasses"
            style="margin-top: 20px; text-align: center;"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 学院模态框 -->
    <el-dialog v-model="showCollegeModal" :title="editingCollege ? '编辑学院' : '添加学院'" width="500px">
      <el-form :model="collegeForm" label-width="100px">
        <el-form-item label="学院名称" required>
          <el-input v-model="collegeForm.collegeName" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="学院代码" required>
          <el-input v-model="collegeForm.collegeCode" placeholder="请输入学院代码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="collegeForm.description" type="textarea" placeholder="请输入学院描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCollegeModal = false">取消</el-button>
        <el-button type="primary" @click="saveCollege" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 专业模态框 -->
    <el-dialog v-model="showMajorModal" :title="editingMajor ? '编辑专业' : '添加专业'" width="500px">
      <el-form :model="majorForm" label-width="100px">
        <el-form-item label="专业名称" required>
          <el-input v-model="majorForm.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="专业代码" required>
          <el-input v-model="majorForm.majorCode" placeholder="请输入专业代码" />
        </el-form-item>
        <el-form-item label="所属学院" required>
          <el-select v-model="majorForm.collegeId" placeholder="请选择学院" style="width: 100%;">
            <el-option 
              v-for="college in allColleges" 
              :key="college.id" 
              :label="college.collegeName"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="majorForm.description" type="textarea" placeholder="请输入专业描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMajorModal = false">取消</el-button>
        <el-button type="primary" @click="saveMajor" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班级模态框 -->
    <el-dialog v-model="showClassModal" :title="editingClass ? '编辑班级' : '添加班级'" width="500px">
      <el-form :model="classForm" label-width="100px">
        <el-form-item label="班级名称" required>
          <el-input v-model="classForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班级代码" required>
          <el-input v-model="classForm.classCode" placeholder="请输入班级代码" />
        </el-form-item>
        <el-form-item label="所属学院" required>
          <el-select v-model="classForm.collegeId" placeholder="请选择学院" @change="onClassCollegeChange" style="width: 100%;">
            <el-option 
              v-for="college in allColleges" 
              :key="college.id" 
              :label="college.collegeName"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业" required>
          <el-select v-model="classForm.majorId" placeholder="请选择专业" style="width: 100%;">
            <el-option 
              v-for="major in classFormMajors" 
              :key="major.id" 
              :label="major.majorName"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" required>
          <el-input v-model="classForm.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="学生人数">
          <el-input-number v-model="classForm.studentCount" :min="0" placeholder="学生人数" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="classForm.description" type="textarea" placeholder="请输入班级描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClassModal = false">取消</el-button>
        <el-button type="primary" @click="saveClass" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import {
  getColleges,
  createCollege,
  updateCollege,
  deleteCollege,
  getMajors,
  createMajor,
  updateMajor,
  deleteMajor,
  getClasses,
  createClass,
  updateClass,
  deleteClass,
  getAllColleges,
  getMajorsByCollege,
  type College,
  type Major,
  type ClassEntity
} from '../../api/organization'

// 响应式数据
const activeTab = ref('colleges')
const loading = ref(false)
const saving = ref(false)

// 分页
const pageSize = ref(10)
const collegePage = ref(1)
const majorPage = ref(1)
const classPage = ref(1)
const collegeTotal = ref(0)
const majorTotal = ref(0)
const classTotal = ref(0)

// 搜索
const collegeSearch = ref('')
const majorSearch = ref('')
const classSearch = ref('')
const majorCollegeFilter = ref('')
const classCollegeFilter = ref('')
const classMajorFilter = ref('')

// 数据列表
const colleges = ref<College[]>([])
const majors = ref<Major[]>([])
const classes = ref<ClassEntity[]>([])
const allColleges = ref<College[]>([])
const allMajors = ref<Major[]>([])

// 模态框
const showCollegeModal = ref(false)
const showMajorModal = ref(false)
const showClassModal = ref(false)
const editingCollege = ref(false)
const editingMajor = ref(false)
const editingClass = ref(false)

// 表单数据
const collegeForm = reactive<Partial<College>>({
  collegeName: '',
  collegeCode: '',
  description: ''
})

const majorForm = reactive<Partial<Major>>({
  majorName: '',
  majorCode: '',
  collegeId: undefined,
  description: ''
})

const classForm = reactive<Partial<ClassEntity>>({
  className: '',
  classCode: '',
  collegeId: undefined,
  majorId: undefined,
  grade: '',
  studentCount: 0,
  description: ''
})

// 计算属性
const filteredMajors = computed(() => {
  if (!classCollegeFilter.value) return allMajors.value
  return allMajors.value.filter(major => major.collegeId === classCollegeFilter.value)
})

const classFormMajors = computed(() => {
  if (!classForm.collegeId) return []
  return allMajors.value.filter(major => major.collegeId === classForm.collegeId)
})

// 方法
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 学院相关方法
const loadColleges = async () => {
  loading.value = true
  try {
    const response = await getColleges({
      current: collegePage.value,
      size: pageSize.value,
      collegeName: collegeSearch.value
    })
    colleges.value = response.data.content
    collegeTotal.value = response.data.total
  } catch (error) {
    ElMessage.error('加载学院列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllColleges = async () => {
  try {
    const response = await getAllColleges()
    allColleges.value = response.data
  } catch (error) {
    ElMessage.error('加载学院列表失败')
  }
}

const editCollege = (college: College) => {
  editingCollege.value = true
  Object.assign(collegeForm, college)
  showCollegeModal.value = true
}

const saveCollege = async () => {
  if (!collegeForm.collegeName || !collegeForm.collegeCode) {
    ElMessage.warning('请填写必填字段')
    return
  }

  saving.value = true
  try {
    if (editingCollege.value) {
      await updateCollege(collegeForm.id!, collegeForm as College)
      ElMessage.success('学院更新成功')
    } else {
      await createCollege(collegeForm as College)
      ElMessage.success('学院创建成功')
    }
    showCollegeModal.value = false
    resetCollegeForm()
    loadColleges()
    loadAllColleges()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteCollegeConfirm = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个学院吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCollege(id)
    ElMessage.success('删除成功')
    loadColleges()
    loadAllColleges()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetCollegeForm = () => {
  Object.assign(collegeForm, {
    id: undefined,
    collegeName: '',
    collegeCode: '',
    description: ''
  })
  editingCollege.value = false
}

// 专业相关方法
const loadMajors = async () => {
  loading.value = true
  try {
    const response = await getMajors({
      current: majorPage.value,
      size: pageSize.value,
      majorName: majorSearch.value,
      collegeId: majorCollegeFilter.value
    })
    majors.value = response.data.content
    majorTotal.value = response.data.total
  } catch (error) {
    ElMessage.error('加载专业列表失败')
  } finally {
    loading.value = false
  }
}

const loadAllMajors = async () => {
  try {
    const response = await getMajors({
      current: 1,
      size: 1000
    })
    allMajors.value = response.data.content
  } catch (error) {
    ElMessage.error('加载专业列表失败')
  }
}

const editMajor = (major: Major) => {
  editingMajor.value = true
  Object.assign(majorForm, major)
  showMajorModal.value = true
}

const saveMajor = async () => {
  if (!majorForm.majorName || !majorForm.majorCode || !majorForm.collegeId) {
    ElMessage.warning('请填写必填字段')
    return
  }

  saving.value = true
  try {
    if (editingMajor.value) {
      await updateMajor(majorForm.id!, majorForm as Major)
      ElMessage.success('专业更新成功')
    } else {
      await createMajor(majorForm as Major)
      ElMessage.success('专业创建成功')
    }
    showMajorModal.value = false
    resetMajorForm()
    loadMajors()
    loadAllMajors()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteMajorConfirm = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个专业吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMajor(id)
    ElMessage.success('删除成功')
    loadMajors()
    loadAllMajors()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetMajorForm = () => {
  Object.assign(majorForm, {
    id: undefined,
    majorName: '',
    majorCode: '',
    collegeId: undefined,
    description: ''
  })
  editingMajor.value = false
}

// 班级相关方法
const loadClasses = async () => {
  loading.value = true
  try {
    const response = await getClasses({
      current: classPage.value,
      size: pageSize.value,
      className: classSearch.value,
      collegeId: classCollegeFilter.value,
      majorId: classMajorFilter.value
    })
    classes.value = response.data.content
    classTotal.value = response.data.total
  } catch (error) {
    ElMessage.error('加载班级列表失败')
  } finally {
    loading.value = false
  }
}

const editClass = (classItem: ClassEntity) => {
  editingClass.value = true
  Object.assign(classForm, classItem)
  showClassModal.value = true
}

const saveClass = async () => {
  if (!classForm.className || !classForm.classCode || !classForm.collegeId || !classForm.majorId || !classForm.grade) {
    ElMessage.warning('请填写必填字段')
    return
  }

  saving.value = true
  try {
    if (editingClass.value) {
      await updateClass(classForm.id!, classForm as ClassEntity)
      ElMessage.success('班级更新成功')
    } else {
      await createClass(classForm as ClassEntity)
      ElMessage.success('班级创建成功')
    }
    showClassModal.value = false
    resetClassForm()
    loadClasses()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteClassConfirm = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个班级吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteClass(id)
    ElMessage.success('删除成功')
    loadClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetClassForm = () => {
  Object.assign(classForm, {
    id: undefined,
    className: '',
    classCode: '',
    collegeId: undefined,
    majorId: undefined,
    grade: '',
    studentCount: 0,
    description: ''
  })
  editingClass.value = false
}

const onCollegeChange = () => {
  classMajorFilter.value = ''
  loadClasses()
}

const onClassCollegeChange = () => {
  classForm.majorId = undefined
}

// 监听标签页切换
const handleTabChange = () => {
  if (activeTab.value === 'colleges') {
    loadColleges()
  } else if (activeTab.value === 'majors') {
    loadMajors()
  } else if (activeTab.value === 'classes') {
    loadClasses()
  }
}

// 监听模态框打开
const handleCollegeModalOpen = () => {
  if (!editingCollege.value) {
    resetCollegeForm()
  }
}

const handleMajorModalOpen = () => {
  if (!editingMajor.value) {
    resetMajorForm()
  }
}

const handleClassModalOpen = () => {
  if (!editingClass.value) {
    resetClassForm()
  }
}

// 生命周期
onMounted(() => {
  loadColleges()
  loadAllColleges()
  loadAllMajors()
})
</script>

<style scoped>
.organization-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.header-card p {
  margin: 0;
  color: #606266;
}

.organization-tabs {
  margin-top: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
}

.search-bar > * {
  margin-right: 10px;
}

.search-bar > *:last-child {
  margin-right: 0;
}
</style>