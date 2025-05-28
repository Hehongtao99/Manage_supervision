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
        <el-table-column label="权限" width="400">
          <template #default="{ row }">
            <div class="permissions-container">
              <el-tag
                v-for="permission in row.permissions"
                :key="permission"
                class="permission-tag"
                type="primary"
                size="small"
              >
                {{ getPermissionLabel(permission) }}
              </el-tag>
              <el-tag v-if="!row.permissions || row.permissions.length === 0" type="info" size="small">
                无特殊权限
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleView(row)"
              v-permission="'ROLE_VIEW'"
            >
              查看
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
            :data="filterPermissionTree(permissionTree, viewForm.permissions)"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :default-expanded-keys="['user_management', 'role_management', 'system_management']"
            :render-after-expand="false"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { Role } from '../../types/user'
import axios from '../../utils/axios'
import { hasPermission } from '../../utils/permission'
import { useUserStore } from '../../stores/user'

// 获取用户权限
const userStore = useUserStore()

// 状态
const loading = ref(false)
const dialogVisible = ref(false)
const roleList = ref<Role[]>([])

// 可用权限列表 - 适合跑步管理系统
const availablePermissions = [
  'USER_VIEW',
  'USER_EDIT', 
  'USER_DELETE',
  'USER_DISABLE',
  'ROLE_VIEW',
  'ROLE_EDIT',
  'ROLE_DELETE',
  'RUNNER_MANAGEMENT',
  'RUN_DATA_VIEW',
  'RUN_DATA_EDIT',
  'SOCIAL_MANAGEMENT',
  'LOG_VIEW',
  'SYSTEM_SETTINGS'
]

// 权限名称映射（英文到中文）- 跑步管理系统专用，完整覆盖所有可能的权限
const permissionLabels = {
  'USER_VIEW': '查看用户',
  'USER_EDIT': '编辑用户', 
  'USER_DELETE': '删除用户',
  'USER_DISABLE': '禁用用户',
  'ROLE_VIEW': '查看角色',
  'ROLE_EDIT': '编辑角色',
  'ROLE_DELETE': '删除角色',
  'RUNNER_MANAGEMENT': '跑步者管理',
  'RUN_DATA_VIEW': '查看跑步数据',
  'RUN_DATA_EDIT': '编辑跑步数据',
  'SOCIAL_MANAGEMENT': '社交管理',
  'LOG_VIEW': '查看日志',
  'SYSTEM_SETTINGS': '系统设置',
  // 兼容旧版本权限名称
  'RUNNING_MANAGEMENT': '跑步管理',
  'RUNNING_RECORD': '跑步记录',
  'SOCIAL_INTERACT': '社交互动',
  'RUNNING_DATA': '跑步数据',
  'USER_MANAGEMENT': '用户管理',
  'ROLE_MANAGEMENT': '角色管理',
  'SYSTEM_MANAGEMENT': '系统管理',
  // 其他可能的权限
  'PROFILE_EDIT': '编辑资料',
  'FRIEND_MANAGEMENT': '好友管理',
  'POST_MANAGEMENT': '动态管理',
  'COMMENT_MANAGEMENT': '评论管理',
  'DATA_EXPORT': '数据导出',
  'DATA_IMPORT': '数据导入'
}

// 获取权限标签的安全方法
const getPermissionLabel = (permission: string): string => {
  // 首先尝试从映射表获取
  if (permissionLabels[permission]) {
    return permissionLabels[permission]
  }
  
  // 如果找不到映射，尝试转换为友好的中文显示
  if (permission && typeof permission === 'string') {
    // 移除下划线并转换为中文描述
    const parts = permission.toLowerCase().split('_')
    const translations = {
      'user': '用户',
      'role': '角色', 
      'system': '系统',
      'running': '跑步',
      'runner': '跑步者',
      'run': '跑步',
      'data': '数据',
      'social': '社交',
      'management': '管理',
      'view': '查看',
      'edit': '编辑',
      'delete': '删除',
      'create': '创建',
      'settings': '设置',
      'log': '日志',
      'interact': '互动',
      'record': '记录',
      'disable': '禁用'
    }
    
    const translatedParts = parts.map(part => translations[part] || part)
    return translatedParts.join('')
  }
  
  return '未知权限'
}

// 权限树形结构 - 跑步管理系统
const permissionTree = [
  {
    id: 'user_management',
    label: '用户管理',
    children: [
      { id: 'USER_VIEW', label: '查看用户' },
      { id: 'USER_EDIT', label: '编辑用户' },
      { id: 'USER_DELETE', label: '删除用户' },
      { id: 'USER_DISABLE', label: '禁用用户' }
    ]
  },
  {
    id: 'runner_management', 
    label: '跑步者管理',
    children: [
      { id: 'RUNNER_MANAGEMENT', label: '跑步者管理' },
      { id: 'RUN_DATA_VIEW', label: '查看跑步数据' },
      { id: 'RUN_DATA_EDIT', label: '编辑跑步数据' }
    ]
  },
  {
    id: 'social_management',
    label: '社交管理', 
    children: [
      { id: 'SOCIAL_MANAGEMENT', label: '社交管理' }
    ]
  },
  {
    id: 'role_management',
    label: '角色管理',
    children: [
      { id: 'ROLE_VIEW', label: '查看角色' },
      { id: 'ROLE_EDIT', label: '编辑角色' },
      { id: 'ROLE_DELETE', label: '删除角色' }
    ]
  },
  {
    id: 'system_management',
    label: '系统管理',
    children: [
      { id: 'LOG_VIEW', label: '查看日志' },
      { id: 'SYSTEM_SETTINGS', label: '系统设置' }
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
    
    // 过滤掉SUPERVISOR角色
    roleList.value = response.data.filter((role: Role) => role.name !== 'SUPERVISOR')
  } catch (error: any) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleView = (row: Role) => {
  // 检查权限
  if (!hasPermission('ROLE_VIEW')) {
    ElMessage.error('您没有查看角色的权限')
    return
  }
  
  // 复制角色数据到查看表单
  Object.assign(viewForm, row)
  dialogVisible.value = true
}

// 过滤权限树，只显示已授权的权限
const filterPermissionTree = (tree: any[], permissions: string[]) => {
  return tree.map(node => {
    // 创建节点的副本，避免修改原始数据
    const newNode = { ...node }
    
    if (newNode.children && newNode.children.length) {
      // 递归处理子节点
      const filteredChildren = filterPermissionTree(newNode.children, permissions)
      // 只保留有权限的子节点
      newNode.children = filteredChildren.filter(child => {
        // 如果是叶子节点，检查是否有授权
        if (!child.children || child.children.length === 0) {
          return permissions.includes(child.id)
        }
        // 如果是分类节点，检查是否有子节点
        return child.children && child.children.length > 0
      })
      
      // 如果分类节点没有子节点，则不显示该分类
      if (newNode.children.length === 0) {
        return null
      }
    } else if (availablePermissions.includes(newNode.id)) {
      // 叶子节点，检查是否有授权
      if (!permissions.includes(newNode.id)) {
        return null
      }
    }
    
    return newNode
  }).filter(Boolean) // 移除空节点
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

.permissions-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-width: 100%;
  align-items: flex-start;
}

.permission-tag {
  margin: 0;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
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