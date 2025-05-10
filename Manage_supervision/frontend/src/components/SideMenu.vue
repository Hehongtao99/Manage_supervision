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
    <!-- 管理员菜单 -->
    <template v-if="userStore.isAdmin">
      <el-menu-item index="/admin/dashboard" @click="handleRoute('/admin/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>管理控制台</span>
        </template>
      </el-menu-item>

      <el-sub-menu index="admin-management">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/users" @click="handleRoute('/admin/users')">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/roles" @click="handleRoute('/admin/roles')">
          <el-icon><Lock /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="teacher-student-management">
        <template #title>
          <el-icon><User /></el-icon>
          <span>教学管理</span>
        </template>
        <el-menu-item index="/admin/teachers" @click="handleRoute('/admin/teachers')">
          <el-icon><UserFilled /></el-icon>
          <span>教师管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/students" @click="handleRoute('/admin/students')">
          <el-icon><Avatar /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/course-applications" @click="handleRoute('/admin/course-applications')">
          <el-icon><Document /></el-icon>
          <span>课程申请审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/subjects" @click="handleRoute('/admin/subjects')">
          <el-icon><Reading /></el-icon>
          <span>科目管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/appeals" @click="handleRoute('/admin/appeals')">
          <el-icon><Service /></el-icon>
          <span>申诉管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 督导员菜单 -->
    <template v-else-if="userStore.isSupervisor">
      <el-menu-item index="/supervisor/students" @click="handleRoute('/supervisor/students')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>学生管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/course-applications" @click="handleRoute('/supervisor/course-applications')">
        <el-icon><Document /></el-icon>
        <template #title>
          <span>课程申请</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/chat" @click="handleRoute('/supervisor/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/orders" @click="handleRoute('/supervisor/orders')">
        <el-icon><ShoppingCart /></el-icon>
        <template #title>
          <span>订单管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/after-sales" @click="handleRoute('/supervisor/after-sales')">
        <el-icon><Service /></el-icon>
        <template #title>
          <span>售后管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/profile" @click="handleRoute('/supervisor/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>教师信息</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/supervisor/income-report" @click="handleRoute('/supervisor/income-report')">
        <el-icon><TrendCharts /></el-icon>
        <template #title>
          <span>收入报表</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/supervisor/course-students" @click="handleRoute('/supervisor/course-students')">
        <el-icon><Postcard /></el-icon>
        <template #title>
          <span>课程学生管理</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 学生菜单 -->
    <template v-else>
      <el-menu-item index="/chat" @click="handleRoute('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/courses" @click="handleRoute('/courses')">
        <el-icon><Reading /></el-icon>
        <template #title>
          <span>课程浏览</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/orders" @click="handleRoute('/orders')">
        <el-icon><ShoppingCart /></el-icon>
        <template #title>
          <span>我的订单</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/after-sales" @click="handleRoute('/after-sales')">
        <el-icon><Service /></el-icon>
        <template #title>
          <span>售后管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/learning-report" @click="handleRoute('/learning-report')">
        <el-icon><TrendCharts /></el-icon>
        <template #title>
          <span>学习记录报表</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/profile" @click="handleRoute('/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
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
  ChatDotRound,
  Reading,
  ShoppingCart,
  Service,
  TrendCharts,
  Postcard
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