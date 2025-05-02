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
            :default-expanded-keys="['user_management', 'role_management', 'system_management', 'student_management']"
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
  'STUDENT_MANAGEMENT'
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
  'STUDENT_MANAGEMENT': '学生管理'
}

// 权限树形结构
const permissionTree = [
  {
    id: 'user_management',
    label: '用户管理',
    children: [
      { id: 'USER_VIEW', label: '查看用户' },
      { id: 'USER_EDIT', label: '编辑用户' },
      { id: 'USER_DISABLE', label: '禁用用户' }
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
  },
  {
    id: 'student_management',
    label: '学生管理',
    children: [
      { id: 'STUDENT_MANAGEMENT', label: '学生管理' }
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
    roleList.value = response.data
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