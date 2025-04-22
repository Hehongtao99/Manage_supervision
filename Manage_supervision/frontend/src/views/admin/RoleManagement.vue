<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加角色
      </el-button>
    </div>

    <!-- 角色列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="权限" width="300">
          <template #default="{ row }">
            <el-tag
              v-for="permission in row.permissions"
              :key="permission"
              class="permission-tag"
            >
              {{ permissionLabels[permission] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.name === 'ADMIN' || row.name === 'USER'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入角色名称"
            :disabled="form.name === 'ADMIN' || form.name === 'USER'"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="权限" prop="permissions">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox
              v-for="permission in availablePermissions"
              :key="permission"
              :label="permission"
            >
              {{ permissionLabels[permission] }}
            </el-checkbox>
          </el-checkbox-group>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Role, CreateRoleRequest, UpdateRoleRequest } from '../../types/user'
import axios from '../../utils/axios'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const roleList = ref<Role[]>([])

// 可用权限列表
const availablePermissions = [
  'USER_VIEW',
  'USER_EDIT',
  'USER_DELETE',
  'ROLE_VIEW',
  'ROLE_EDIT',
  'ROLE_DELETE',
  'LOG_VIEW',
  'SYSTEM_SETTINGS'
]

// 权限名称映射（英文到中文）
const permissionLabels = {
  'USER_VIEW': '查看用户',
  'USER_EDIT': '编辑用户',
  'USER_DELETE': '删除用户',
  'ROLE_VIEW': '查看角色',
  'ROLE_EDIT': '编辑角色',
  'ROLE_DELETE': '删除角色',
  'LOG_VIEW': '查看日志',
  'SYSTEM_SETTINGS': '系统设置'
}

// 表单
const formRef = ref<FormInstance>()

const form = reactive<CreateRoleRequest & UpdateRoleRequest>({
  name: '',
  description: '',
  permissions: []
})

// 计算属性
const dialogTitle = computed(() => {
  return dialogType.value === 'add' ? '添加角色' : '编辑角色'
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' },
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  permissions: [
    { required: true, message: '请选择权限', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一个权限', trigger: 'change' }
  ]
}

// 方法
const fetchRoleList = async () => {
  loading.value = true
  console.log('===== 正在获取角色列表 =====')
  
  // 检查token和授权头
  const token = localStorage.getItem('token')
  console.log('当前token存在状态:', !!token)
  
  try {
    // 使用axios替代fetch，以便利用全局拦截器
    const response = await axios.get('/api/admin/roles')
    console.log('获取角色列表成功:', response.data.length, '个角色')
    roleList.value = response.data
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.name = ''
  form.description = ''
  form.permissions = []
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Role) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await axios.post('/api/admin/roles', form)
          ElMessage.success('添加角色成功')
        } else {
          await axios.put(`/api/admin/roles/${form.id}`, form)
          ElMessage.success('编辑角色成功')
        }
        dialogVisible.value = false
        fetchRoleList()
      } catch (error: any) {
        ElMessage.error(
          error.response?.data?.message ||
          (dialogType.value === 'add' ? '添加角色失败' : '编辑角色失败')
        )
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该角色吗？删除后无法恢复。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.delete(`/api/admin/roles/${row.id}`)
    ElMessage.success('删除角色成功')
    fetchRoleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除角色失败')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-management {
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

.list-card {
  margin-bottom: 20px;
}

.permission-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style> 