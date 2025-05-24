<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapsed"
    :collapse-transition="false"
    class="side-menu"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
  >
    <!-- 管理员菜单 - 或拥有管理权限的用户 -->
    <template v-if="userStore.isAdmin || hasAdminPermissions">
      <!-- 管理控制台 - 管理员或有dashboard权限 -->
      <el-menu-item v-if="userStore.isAdmin || hasPermission('dashboard')" 
        index="/admin/dashboard" @click="handleRoute('/admin/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>管理控制台</span>
        </template>
      </el-menu-item>

      <!-- 系统管理菜单 - 管理员或有系统管理权限 -->
      <el-sub-menu index="admin-management" v-if="userStore.isAdmin || hasSystemManagementPermission">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>系统管理</span>
        </template>
        
        <!-- 用户管理 - 管理员或有用户查看权限 -->
        <el-menu-item v-if="userStore.isAdmin || hasPermission('user:view') || hasPermission('USER_VIEW')" 
          index="/admin/users" @click="handleRoute('/admin/users')">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <!-- 角色管理 - 管理员或有角色查看权限 -->
        <el-menu-item v-if="userStore.isAdmin || hasPermission('role:view') || hasPermission('ROLE_VIEW')" 
          index="/admin/roles" @click="handleRoute('/admin/roles')">
          <el-icon><Lock /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 教学管理菜单 - 管理员或有教学管理权限 -->
      <el-sub-menu index="teacher-student-management" 
        v-if="userStore.isAdmin || hasTeacherStudentManagementPermission">
        <template #title>
          <el-icon><User /></el-icon>
          <span>教学管理</span>
        </template>
        
        <!-- 教师管理 - 管理员或有教师查看权限 -->
        <el-menu-item v-if="userStore.isAdmin || hasPermission('teacher:view') || hasPermission('TEACHER_VIEW')" 
          index="/admin/teachers" @click="handleRoute('/admin/teachers')">
          <el-icon><UserFilled /></el-icon>
          <span>教师管理</span>
        </el-menu-item>
        
        <!-- 学生管理 - 管理员或有学生查看权限 -->
        <el-menu-item v-if="userStore.isAdmin || hasPermission('student:view') || hasPermission('STUDENT_VIEW')" 
          index="/admin/students" @click="handleRoute('/admin/students')">
          <el-icon><Avatar /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 督导员菜单 - 或拥有督导员权限的用户 -->
    <template v-else-if="userStore.isSupervisor || hasStudentViewPermission">
      <el-menu-item index="/supervisor/students" @click="handleRoute('/supervisor/students')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>学生管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/chat" @click="handleRoute('/supervisor/chat')" v-permission="'chat:view'">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/profile" @click="handleRoute('/supervisor/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>教师信息</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 学生菜单 - 普通用户 -->
    <template v-else>
      <!-- 聊天菜单 - 所有用户都可以访问 -->
      <el-menu-item index="/chat" @click="handleRoute('/chat')" v-permission="'chat:view'">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <!-- 个人信息菜单 - 所有用户都可以访问 -->
      <el-menu-item index="/profile" @click="handleRoute('/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
    </template>
    
    <!-- 聊天菜单 - 所有用户都可以访问(除了管理员和督导员已经在上面显示了) -->
    <el-menu-item v-if="!userStore.isAdmin && !userStore.isSupervisor && 
      !hasStudentViewPermission && !hasAdminPermissions" 
      index="/chat" @click="handleRoute('/chat')" v-permission="'chat:view'">
      <el-icon><ChatDotRound /></el-icon>
      <template #title>
        <span>聊天</span>
      </template>
    </el-menu-item>

    <!-- 个人信息菜单 - 所有用户都可以访问(除了管理员和督导员已经在上面显示了) -->
    <el-menu-item v-if="!userStore.isAdmin && !userStore.isSupervisor && 
      !hasStudentViewPermission && !hasAdminPermissions" 
      index="/profile" @click="handleRoute('/profile')">
      <el-icon><UserFilled /></el-icon>
      <template #title>
        <span>个人信息</span>
      </template>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { hasPermission } from '../utils/permission'
import {
  Monitor,
  User,
  UserFilled,
  Setting,
  Management,
  Avatar,
  Lock,
  Document,
  List,
  Folder,
  ChatDotRound
} from '@element-plus/icons-vue'

const props = defineProps<{
  isCollapsed: boolean
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

// 检查用户是否有系统管理相关的权限
const hasSystemManagementPermission = computed(() => {
  return hasPermission('user:view') || 
         hasPermission('USER_VIEW') || 
         hasPermission('role:view') || 
         hasPermission('ROLE_VIEW') ||
         hasPermission('system');
})

// 检查用户是否有教学管理相关的权限
const hasTeacherStudentManagementPermission = computed(() => {
  return hasPermission('teacher:view') || 
         hasPermission('TEACHER_VIEW') || 
         hasPermission('student:view') || 
         hasPermission('STUDENT_VIEW');
})

// 检查用户是否有学生查看权限
const hasStudentViewPermission = computed(() => {
  return hasPermission('student:view') || hasPermission('STUDENT_VIEW');
})

// 检查用户是否有任何管理员权限
const hasAdminPermissions = computed(() => {
  return hasSystemManagementPermission.value || 
         hasTeacherStudentManagementPermission.value || 
         hasPermission('dashboard') || 
         hasPermission('DASHBOARD');
})

// 检查用户是否有指定权限
function hasPermission(permission: string): boolean {
  // 如果是管理员，直接返回true
  if (userStore.isAdmin) {
    return true
  }
  
  // 检查用户是否有该权限
  return userStore.permissions.includes(permission)
}

const handleRoute = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.side-menu {
  border-right: none;
  user-select: none;
}

.side-menu:not(.el-menu--collapse) {
  width: 210px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item) {
  &:hover {
    background-color: #263445 !important;
  }
  
  &.is-active {
    background-color: #1890ff !important;
    color: #fff !important;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: #fff;
    }
  }
}

:deep(.el-sub-menu__title) {
  &:hover {
    background-color: #263445 !important;
  }
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  width: 16px;
  height: 16px;
  margin-right: 16px;
  font-size: 16px;
}

/* 折叠时的图标样式 */
.el-menu--collapse {
  :deep(.el-sub-menu__title) {
    span {
      height: 0;
      width: 0;
      overflow: hidden;
      visibility: hidden;
      display: inline-block;
    }
  }
}
</style> 