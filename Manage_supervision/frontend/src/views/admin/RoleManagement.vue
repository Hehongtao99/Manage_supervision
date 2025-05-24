<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
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
              {{ permissionLabels[permission] || permission }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleView(row)"
              v-permission="'role:view'"
            >
              查看
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              v-permission="'role:edit'"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色查看对话框 -->
    <el-dialog
      title="查看角色权限"
      v-model="dialogVisible"
      width="500px"
    >
      <el-descriptions border :column="1" size="large">
        <el-descriptions-item label="角色名称">{{ viewForm.name }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ viewForm.description }}</el-descriptions-item>
        <el-descriptions-item label="权限">
          <el-tree
            :data="filterPermissionTree(permissionTree, viewForm.permissions || [])"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :default-expanded-keys="['system', 'user', 'role', 'permission', 'teacher', 'student', 'chat']"
            :render-after-expand="false"
            show-checkbox
            :default-checked-keys="viewForm.permissions || []"
            :check-strictly="true"
            disabled
          >
          </el-tree>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色编辑对话框 -->
    <el-dialog
      title="编辑角色权限"
      v-model="editDialogVisible"
      width="500px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="editForm.name" :disabled="editForm.name === 'ADMIN'" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTree"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :default-expanded-keys="['system', 'user', 'role', 'permission', 'teacher', 'student', 'chat']"
            show-checkbox
            :default-checked-keys="editForm.permissions || []"
            :check-strictly="true"
            @check="handlePermissionCheck"
          >
          </el-tree>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRole" :loading="saveLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElTree } from 'element-plus'
import type { Role } from '../../types/user'
import axios from '../../utils/axios'
import { hasPermission } from '../../utils/permission'
import { useUserStore } from '../../stores/user'

// 获取用户权限
const userStore = useUserStore()

// 状态
const loading = ref(false)
const saveLoading = ref(false)
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const roleList = ref<Role[]>([])
const permissionTreeRef = ref<InstanceType<typeof ElTree>>()

// 可用权限列表
const availablePermissions = [
  'USER_VIEW',
  'USER_EDIT',
  'USER_DISABLE',
  'ROLE_VIEW',
  'ROLE_EDIT',
  'ROLE_DELETE',
  'LOG_VIEW',
  'SYSTEM_SETTINGS',
  'STUDENT_MANAGEMENT',
  'user:view',
  'user:edit',
  'user:add',
  'user:delete',
  'role:view',
  'role:edit',
  'role:add',
  'role:delete',
  'permission:view',
  'permission:edit',
  'permission:add',
  'permission:delete',
  'permission:assign',
  'teacher:view',
  'teacher:edit',
  'teacher:add',
  'teacher:delete',
  'teacher:assign',
  'student:view',
  'student:edit',
  'student:add',
  'student:delete',
  'student:assign',
  'chat:view',
  'chat:send',
  'chat:delete',
  'system',
  'user',
  'role',
  'permission',
  'teacher',
  'student',
  'chat'
]

// 权限名称映射（英文到中文）
const permissionLabels = {
  'USER_VIEW': '查看用户',
  'USER_EDIT': '编辑用户',
  'USER_DISABLE': '禁用用户',
  'ROLE_VIEW': '查看角色',
  'ROLE_EDIT': '编辑角色',
  'ROLE_DELETE': '删除角色',
  'LOG_VIEW': '查看日志',
  'SYSTEM_SETTINGS': '系统设置',
  'STUDENT_MANAGEMENT': '学生管理',
  'user:view': '查看用户',
  'user:edit': '编辑用户',
  'user:add': '添加用户',
  'user:delete': '删除用户',
  'role:view': '查看角色',
  'role:edit': '编辑角色',
  'role:add': '添加角色',
  'role:delete': '删除角色',
  'permission:view': '查看权限',
  'permission:edit': '编辑权限',
  'permission:add': '添加权限',
  'permission:delete': '删除权限',
  'permission:assign': '分配权限',
  'teacher:view': '查看教师',
  'teacher:edit': '编辑教师',
  'teacher:add': '添加教师',
  'teacher:delete': '删除教师',
  'teacher:assign': '分配学生',
  'student:view': '查看学生',
  'student:edit': '编辑学生',
  'student:add': '添加学生',
  'student:delete': '删除学生',
  'student:assign': '分配导师',
  'chat:view': '查看消息',
  'chat:send': '发送消息',
  'chat:delete': '删除消息',
  'system': '系统管理',
  'user': '用户管理',
  'role': '角色管理',
  'permission': '权限管理',
  'teacher': '教师管理',
  'student': '学生管理',
  'chat': '对话管理'
}

// 权限树形结构
const permissionTree = [
  {
    id: 'system',
    label: '系统管理',
    children: [
      {
        id: 'user',
        label: '用户管理',
        children: [
          { id: 'user:view', label: '查看用户' },
          { id: 'user:add', label: '添加用户' },
          { id: 'user:edit', label: '编辑用户' },
          { id: 'user:delete', label: '删除用户' }
        ]
      },
      {
        id: 'role',
        label: '角色管理',
        children: [
          { id: 'role:view', label: '查看角色' },
          { id: 'role:add', label: '添加角色' },
          { id: 'role:edit', label: '编辑角色' },
          { id: 'role:delete', label: '删除角色' }
        ]
      },
      {
        id: 'permission',
        label: '权限管理',
        children: [
          { id: 'permission:view', label: '查看权限' },
          { id: 'permission:add', label: '添加权限' },
          { id: 'permission:edit', label: '编辑权限' },
          { id: 'permission:delete', label: '删除权限' },
          { id: 'permission:assign', label: '分配权限' }
        ]
      }
    ]
  },
  {
    id: 'teacher',
    label: '教师管理',
    children: [
      { id: 'teacher:view', label: '查看教师' },
      { id: 'teacher:add', label: '添加教师' },
      { id: 'teacher:edit', label: '编辑教师' },
      { id: 'teacher:delete', label: '删除教师' },
      { id: 'teacher:assign', label: '分配学生' }
    ]
  },
  {
    id: 'student',
    label: '学生管理',
    children: [
      { id: 'student:view', label: '查看学生' },
      { id: 'student:add', label: '添加学生' },
      { id: 'student:edit', label: '编辑学生' },
      { id: 'student:delete', label: '删除学生' },
      { id: 'student:assign', label: '分配导师' }
    ]
  },
  {
    id: 'chat',
    label: '对话管理',
    children: [
      { id: 'chat:send', label: '发送消息' },
      { id: 'chat:view', label: '查看消息' },
      { id: 'chat:delete', label: '删除消息' }
    ]
  }
]

// 查看表单
const viewForm = reactive({
  id: '',
  name: '',
  description: '',
  permissions: [] as string[]
})

// 编辑表单
const editForm = reactive({
  id: '',
  name: '',
  description: '',
  permissions: [] as string[],
  selectedPermissions: [] as string[]
})

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

const handleView = (row: Role) => {
  // 复制角色数据到查看表单
  Object.assign(viewForm, row)
  console.log('查看角色权限:', viewForm.permissions)
  dialogVisible.value = true
}

const handleEdit = (row: Role) => {
  // 复制角色数据到编辑表单
  editForm.id = row.id
  editForm.name = row.name
  editForm.description = row.description
  editForm.permissions = Array.isArray(row.permissions) ? [...row.permissions] : []
  editForm.selectedPermissions = Array.isArray(row.permissions) ? [...row.permissions] : []
  
  console.log('编辑角色权限:', editForm.permissions)
  editDialogVisible.value = true
}

// 处理权限选择
const handlePermissionCheck = (node: any, checkedStatus: any) => {
  console.log('权限选择变更:', node, checkedStatus)
  
  // 获取当前选中的所有节点
  const checkedNodes = permissionTreeRef.value?.getCheckedNodes() || []
  const checkedKeys = checkedNodes.map(node => node.id)
  
  console.log('当前选中的权限:', checkedKeys)
  editForm.selectedPermissions = checkedKeys
}

// 保存角色权限
const handleSaveRole = async () => {
  if (!editForm.id) {
    ElMessage.error('角色ID不能为空')
    return
  }
  
  saveLoading.value = true
  try {
    console.log('保存角色权限:', editForm.id, editForm.selectedPermissions)
    
    // 先更新角色基本信息
    await axios.put(`/api/roles/${editForm.id}`, {
      name: editForm.name,
      description: editForm.description,
      permissions: editForm.selectedPermissions
    })
    
    // 更新角色权限
    await axios.post(`/api/roles/${editForm.id}/permissions`, {
      permissions: editForm.selectedPermissions
    })
    
    ElMessage.success('角色权限更新成功')
    editDialogVisible.value = false
    
    // 刷新角色列表
    fetchRoleList()
  } catch (error: any) {
    console.error('更新角色权限失败:', error)
    ElMessage.error('更新角色权限失败')
  } finally {
    saveLoading.value = false
  }
}

// 过滤权限树，只显示已授权的权限
const filterPermissionTree = (tree: any[], permissions: string[]) => {
  // 如果权限为空，返回完整的树
  if (!permissions || permissions.length === 0) {
    return tree
  }
  
  return tree.map(node => {
    // 创建节点的副本，避免修改原始数据
    const newNode = { ...node }
    
    if (newNode.children && newNode.children.length) {
      // 递归处理子节点
      const filteredChildren = filterPermissionTree(newNode.children, permissions)
      newNode.children = filteredChildren
      
      // 如果分类节点没有子节点，则不显示该分类
      if (newNode.children.length === 0) {
        return null
      }
    }
    
    return newNode
  }).filter(Boolean) // 移除空节点
}

// 监听对话框显示状态
watch(dialogVisible, (newVal) => {
  if (newVal) {
    console.log('查看对话框打开，当前权限:', viewForm.permissions)
  }
})

watch(editDialogVisible, (newVal) => {
  if (newVal) {
    console.log('编辑对话框打开，当前权限:', editForm.permissions)
  }
})

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

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  min-height: 60px;
  height: auto;
  padding-top: 5px;
  padding-bottom: 5px;
}

:deep(.el-select__tags) {
  max-height: 60px;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
}

:deep(.permission-select-dropdown) {
  max-height: 300px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}
</style> 