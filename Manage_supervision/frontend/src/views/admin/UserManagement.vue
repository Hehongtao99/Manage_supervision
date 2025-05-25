<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
          >
            <el-option
              v-for="role in roles"
              :key="role.name"
              :label="role.name"
              :value="role.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
          >
            <el-option label="活跃" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="用户编号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="collegeName" label="学院" width="120" />
        <el-table-column prop="majorName" label="专业" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role"
              :type="role === 'ADMIN' ? 'danger' : role === 'SUPERVISOR' ? 'warning' : 'success'"
              class="role-tag"
            >
              {{ role === 'ADMIN' ? '管理员' : role === 'SUPERVISOR' ? '教师' : role === 'USER' ? '学生' : role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'active' ? 'success' : 'danger'"
            >
              {{ row.status === 'active' ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
              :type="row.status === 'active' ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="用户名"
          prop="username"
          v-if="dialogType === 'add'"
        >
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item
          label="密码"
          prop="password"
          v-if="dialogType === 'add'"
        >
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="用户编号" v-if="dialogType === 'edit'">
          <el-input
            v-model="form.userNumber"
            placeholder="系统自动生成"
            disabled
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select
            v-model="form.roles"
            placeholder="请选择角色"
            @change="onRoleChange"
          >
            <el-option
              v-for="role in roles"
              :key="role.name"
              :label="role.name"
              :value="role.name"
            />
          </el-select>
        </el-form-item>
        
        <!-- 学院专业班级选择 -->
        <el-form-item label="学院" prop="collegeId">
          <el-select
            v-model="form.collegeId"
            placeholder="请选择学院"
            @change="onCollegeChange"
            clearable
          >
            <el-option
              v-for="college in colleges"
              :key="college.id"
              :label="college.collegeName"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="专业" prop="majorId">
          <el-select
            v-model="form.majorId"
            placeholder="请选择专业"
            @change="onMajorChange"
            clearable
          >
            <el-option
              v-for="major in filteredMajors"
              :key="major.id"
              :label="major.majorName"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="班级" prop="classId" v-if="showClassSelect">
          <!-- 学生单选班级 -->
          <el-select
            v-if="form.roles === 'USER'"
            v-model="form.classId"
            placeholder="请选择班级"
            clearable
          >
            <el-option
              v-for="classItem in filteredClasses"
              :key="classItem.id"
              :label="classItem.className"
              :value="classItem.id"
            />
          </el-select>
          
          <!-- 教师多选班级 -->
          <el-select
            v-else-if="form.roles === 'SUPERVISOR'"
            v-model="form.classIds"
            placeholder="请选择班级（可多选）"
            multiple
            clearable
          >
            <el-option
              v-for="classItem in filteredClasses"
              :key="classItem.id"
              :label="classItem.className"
              :value="classItem.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordVisible"
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="resetPasswordForm.password"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleResetPasswordSubmit"
            :loading="resetPasswordLoading"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Plus,
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type {
  UserProfile,
  Role,
  CreateUserRequest,
  UpdateUserRequest
} from '../../types/user'
import axios from '../../utils/axios'
import { useUserStore } from '../../stores/user'
import { getAllColleges, getAllMajors, getAllClasses } from '../../api/organization'
import type { College, Major, ClassEntity } from '../../api/organization'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const resetPasswordLoading = ref(false)
const dialogVisible = ref(false)
const resetPasswordVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userList = ref<UserProfile[]>([])
const roles = ref<Role[]>([])

// 学院专业班级数据
const colleges = ref<College[]>([])
const majors = ref<Major[]>([])
const classes = ref<ClassEntity[]>([])

// 表单
const formRef = ref<FormInstance>()
const resetPasswordFormRef = ref<FormInstance>()

const searchForm = reactive({
  username: '',
  role: '',
  status: ''
})

const form = reactive<Omit<CreateUserRequest & UpdateUserRequest, 'roles'> & { 
  roles: string | string[]
  collegeId?: number
  majorId?: number
  classId?: number
  classIds?: number[]  // 教师多班级选择
}>({
  username: '',
  password: '',
  realName: '',
  nickname: '',
  email: '',
  phone: '',
  roles: '',
  collegeId: undefined,
  majorId: undefined,
  classId: undefined,
  classIds: []
})

const resetPasswordForm = reactive({
  userId: 0,
  password: '',
  confirmPassword: ''
})

// 计算属性
const dialogTitle = computed(() => {
  return dialogType.value === 'add' ? '添加用户' : '编辑用户'
})

// 根据选择的学院过滤专业
const filteredMajors = computed(() => {
  if (!form.collegeId) return []
  return majors.value.filter(major => major.collegeId === form.collegeId)
})

// 根据选择的学院和专业过滤班级
const filteredClasses = computed(() => {
  if (!form.collegeId || !form.majorId) return []
  return classes.value.filter(classItem => 
    classItem.collegeId === form.collegeId && classItem.majorId === form.majorId
  )
})

// 是否显示班级选择（学生和教师都显示）
const showClassSelect = computed(() => {
  return form.roles === 'USER' || form.roles === 'SUPERVISOR'
})

// 表单验证规则
const rules = {
  username: [
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  realName: [
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roles: []
}

const resetPasswordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== resetPasswordForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const fetchUserList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/users', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        ...searchForm
      }
    })
    userList.value = response.data.content
    total.value = response.data.total
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const response = await axios.get('/api/admin/roles')
    roles.value = response.data
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取角色列表失败')
  }
}

// 获取学院列表
const fetchColleges = async () => {
  try {
    const response = await getAllColleges()
    colleges.value = response.data
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取学院列表失败')
  }
}

// 获取专业列表
const fetchMajors = async () => {
  try {
    const response = await getAllMajors()
    majors.value = response.data
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取专业列表失败')
  }
}

// 获取班级列表
const fetchClasses = async () => {
  try {
    const response = await getAllClasses()
    classes.value = response.data
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '获取班级列表失败')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.username = ''
  form.password = ''
  form.realName = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.roles = ''
  form.collegeId = undefined
  form.majorId = undefined
  form.classId = undefined
  form.classIds = []
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row: UserProfile) => {
  dialogType.value = 'edit'
  const userData = { ...row };
  
  // 确保只选择第一个角色
  if (userData.roles && userData.roles.length > 0) {
    userData.roles = userData.roles[0];
  } else {
    userData.roles = '';
  }
  
  // 设置学院专业班级信息
  userData.collegeId = row.collegeId
  userData.majorId = row.majorId
  userData.classId = row.classId
  userData.classIds = []
  
  // 如果是教师角色，获取班级关系
  if (userData.roles === 'SUPERVISOR') {
    try {
      const response = await axios.get(`/api/admin/users/${row.id}/classes`)
      userData.classIds = response.data || []
    } catch (error: any) {
      console.error('获取教师班级关系失败:', error)
      ElMessage.warning('获取教师班级关系失败')
      userData.classIds = []
    }
  }
  
  Object.assign(form, userData);
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 创建一个表单数据的副本
        const formData = { ...form };
        
        // 将单个角色值转换为数组
        if (formData.roles && typeof formData.roles === 'string') {
          formData.roles = [formData.roles];
        } else if (!formData.roles) {
          formData.roles = [];
        }
        
        // 添加班级关系数据
        if (form.roles === 'SUPERVISOR' && form.classIds) {
          formData.classIds = form.classIds
        }
        
        if (dialogType.value === 'add') {
          await axios.post('/api/admin/users', formData)
          ElMessage.success('添加用户成功')
        } else {
          await axios.put(`/api/admin/users/${formData.id}`, formData)
          ElMessage.success('编辑用户成功')
        }
        dialogVisible.value = false
        fetchUserList()
      } catch (error: any) {
        ElMessage.error(
          error.response?.data?.message ||
          (dialogType.value === 'add' ? '添加用户失败' : '编辑用户失败')
        )
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleResetPassword = (row: UserProfile) => {
  resetPasswordForm.userId = row.id
  resetPasswordForm.password = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return

  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      resetPasswordLoading.value = true
      try {
        await axios.post(`/api/admin/users/${resetPasswordForm.userId}/reset-password`, {
          password: resetPasswordForm.password
        })
        ElMessage.success('重置密码成功')
        resetPasswordVisible.value = false
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '重置密码失败')
      } finally {
        resetPasswordLoading.value = false
      }
    }
  })
}

const handleToggleStatus = async (row: UserProfile) => {
  try {
    const action = row.status === 'active' ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.post(`/api/admin/users/${row.id}/toggle-status`)
    ElMessage.success(`${action}用户成功`)
    fetchUserList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.role = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUserList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUserList()
}

const onRoleChange = () => {
  // 角色变化时清空班级选择
  if (form.roles === 'USER') {
    // 学生角色：清空多选班级，保留单选班级
    form.classIds = []
  } else if (form.roles === 'SUPERVISOR') {
    // 教师角色：清空单选班级，保留多选班级
    form.classId = undefined
  } else {
    // 管理员角色：清空所有班级选择
    form.classId = undefined
    form.classIds = []
  }
}

const onCollegeChange = () => {
  // 学院变化时清空专业和班级选择
  form.majorId = undefined
  form.classId = undefined
  form.classIds = []
}

const onMajorChange = () => {
  // 专业变化时清空班级选择
  form.classId = undefined
  form.classIds = []
}

// 生命周期钩子
onMounted(async () => {
  console.log('===== UserManagement组件挂载 =====')
  
  // 调试：检查用户登录和权限状态
  const token = localStorage.getItem('token')
  console.log('当前token存在状态:', !!token)
  if (token) {
    console.log('Token前10位:', token.substring(0, 10) + '...')
  }
  
  // 检查用户store状态
  const userStore = useUserStore()
  console.log('用户登录状态:', userStore.isLoggedIn)
  console.log('用户管理员状态:', userStore.isAdmin)
  console.log('用户角色:', userStore.user.roles)
  
  // 添加请求前的检查项
  console.log('开始请求用户列表和角色数据')
  
  try {
    // 分开请求以便能更明确地显示哪个请求成功或失败
    await fetchUserList()
    console.log('请求用户列表成功')
    
    await fetchRoles()
    console.log('请求角色列表成功')
    
    // 获取学院专业班级数据
    await fetchColleges()
    console.log('请求学院列表成功')
    
    await fetchMajors()
    console.log('请求专业列表成功')
    
    await fetchClasses()
    console.log('请求班级列表成功')
  } catch (error) {
    console.error('请求数据失败:', error)
  }
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.role-tag {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 