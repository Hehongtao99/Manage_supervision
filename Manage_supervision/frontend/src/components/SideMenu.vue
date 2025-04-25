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
        <el-menu-item index="/admin/classes" @click="handleRoute('/admin/classes')">
          <el-icon><List /></el-icon>
          <span>班级管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/students" @click="handleRoute('/admin/students')">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/parents" @click="handleRoute('/admin/parents')">
          <el-icon><Avatar /></el-icon>
          <span>家长管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/timetables" @click="handleRoute('/admin/timetables')">
          <el-icon><Calendar /></el-icon>
          <span>课程表管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notifications" @click="handleRoute('/admin/notifications')">
          <el-icon><Bell /></el-icon>
          <span>通知管理</span>
        </el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="bill-management">
        <template #title>
          <el-icon><Money /></el-icon>
          <span>账单管理</span>
        </template>
        <el-menu-item index="/admin/fee-standards" @click="handleRoute('/admin/fee-standards')">
          <el-icon><List /></el-icon>
          <span>费用标准管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/bills" @click="handleRoute('/admin/bills')">
          <el-icon><Tickets /></el-icon>
          <span>账单管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 教师/班主任菜单 -->
    <template v-else-if="userStore.isSupervisor">
      <el-menu-item index="/supervisor/dashboard" @click="handleRoute('/supervisor/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>教师控制台</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/supervisor/students" @click="handleRoute('/supervisor/students')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>学生管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/supervisor/class-management" @click="handleRoute('/supervisor/class-management')">
        <el-icon><List /></el-icon>
        <template #title>
          <span>班级管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/notifications" @click="handleRoute('/supervisor/notifications')">
        <el-icon><Bell /></el-icon>
        <template #title>
          <span>发送通知</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/messages" @click="handleRoute('/supervisor/messages')">
        <el-icon><Comment /></el-icon>
        <template #title>
          <span>家长留言</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/attendance" @click="handleRoute('/supervisor/attendance')">
        <el-icon><Timer /></el-icon>
        <template #title>
          <span>考勤管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/chat" @click="handleRoute('/supervisor/chat')">
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
    
    <!-- 家长菜单 -->
    <template v-else-if="userStore.isParent">
      <el-menu-item index="/parent/dashboard" @click="handleRoute('/parent/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>家长控制台</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/children" @click="handleRoute('/parent/children')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>子女管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/notifications" @click="handleRoute('/parent/notifications')">
        <el-icon><Bell /></el-icon>
        <template #title>
          <span>我的通知</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/chat" @click="handleRoute('/parent/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/teacher-messages" @click="handleRoute('/parent/teacher-messages')">
        <el-icon><Comment /></el-icon>
        <template #title>
          <span>与教师留言</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/attendance" @click="handleRoute('/parent/attendance')">
        <el-icon><Timer /></el-icon>
        <template #title>
          <span>子女考勤</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/parent/profile" @click="handleRoute('/parent/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 学生菜单 -->
    <template v-else>
      <el-menu-item index="/student/dashboard" @click="handleRoute('/student/dashboard')">
        <el-icon><Monitor /></el-icon>
        <template #title>
          <span>学生首页</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/chat" @click="handleRoute('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/student/teachers-classes" @click="handleRoute('/student/teachers-classes')">
        <el-icon><List /></el-icon>
        <template #title>
          <span>我的班级和老师</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/student/timetable" @click="handleRoute('/student/timetable')">
        <el-icon><Calendar /></el-icon>
        <template #title>
          <span>班级课表</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/student/notifications" @click="handleRoute('/student/notifications')">
        <el-icon><Bell /></el-icon>
        <template #title>
          <span>我的通知</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/student/parent-requests" @click="handleRoute('/student/parent-requests')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>家长绑定请求</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/student/bills" @click="handleRoute('/student/bills')">
        <el-icon><Money /></el-icon>
        <template #title>
          <span>我的账单</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/student/attendance" @click="handleRoute('/student/attendance')">
        <el-icon><Timer /></el-icon>
        <template #title>
          <span>考勤签到</span>
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
  Calendar,
  Bell,
  Money,
  Tickets,
  Message,
  Comment,
  Timer
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