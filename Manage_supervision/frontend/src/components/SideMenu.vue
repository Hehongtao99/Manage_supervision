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

      <el-sub-menu index="user-management">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/admin/teachers" @click="handleRoute('/admin/teachers')">
          <el-icon><UserFilled /></el-icon>
          <span>安全分析师管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/students" @click="handleRoute('/admin/students')">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 督导员菜单 -->
    <template v-else-if="userStore.isSupervisor">
      <el-menu-item index="/supervisor/students" @click="handleRoute('/supervisor/students')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>用户管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/profile" @click="handleRoute('/supervisor/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>安全分析师信息</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/supervisor/feedback" @click="handleRoute('/supervisor/feedback')">
        <el-icon><Comment /></el-icon>
        <template #title>
          <span>问题反馈</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 学生菜单 -->
    <template v-else>
      <el-sub-menu index="resource-management">
        <template #title>
          <el-icon><Cpu /></el-icon>
          <span>资源管理</span>
        </template>
        <el-menu-item index="/resource/overview" @click="handleRoute('/resource/overview')">
          <el-icon><DataAnalysis /></el-icon>
          <span>资源总览</span>
        </el-menu-item>
        <el-menu-item index="/resource/hosts" @click="handleRoute('/resource/hosts')">
          <el-icon><Monitor /></el-icon>
          <span>主机管理</span>
        </el-menu-item>
        <el-menu-item index="/resource/processes" @click="handleRoute('/resource/processes')">
          <el-icon><List /></el-icon>
          <span>进程管理</span>
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/intrusion-detection" @click="handleRoute('/intrusion-detection')">
        <el-icon><AlarmClock /></el-icon>
        <template #title>
          <span>入侵检测</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/data-monitor" @click="handleRoute('/data-monitor')">
        <el-icon><Warning /></el-icon>
        <template #title>
          <span>数据监控分析</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/api-endpoints" @click="handleRoute('/api-endpoints')">
        <el-icon><Connection /></el-icon>
        <template #title>
          <span>服务接口检测</span>
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
  Cpu,
  Warning,
  AlarmClock,
  Connection,
  DataAnalysis,
  Comment
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