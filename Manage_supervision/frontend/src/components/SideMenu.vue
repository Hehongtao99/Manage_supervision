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
      <el-sub-menu index="admin-management">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/regions" @click="handleRoute('/admin/regions')">
          <el-icon><Location /></el-icon>
          <span>地区管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/scenic-spots" @click="handleRoute('/admin/scenic-spots')">
          <el-icon><Picture /></el-icon>
          <span>景区管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/hotels" @click="handleRoute('/admin/hotels')">
          <el-icon><House /></el-icon>
          <span>酒店管理</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 督导员菜单 -->
    <template v-else-if="userStore.isSupervisor">
      <el-menu-item index="/supervisor/profile" @click="handleRoute('/supervisor/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>教师信息</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 用户菜单 -->
    <template v-else>
      <el-menu-item index="/travel-recommendation" @click="handleRoute('/travel-recommendation')">
        <el-icon><Place /></el-icon>
        <template #title>
          <span>旅游推荐</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { 
  House, 
  Menu as IconMenu, 
  Location, 
  Briefcase, 
  User, 
  UserFilled, 
  ChatDotRound, 
  Avatar, 
  Setting, 
  Histogram, 
  Grid, 
  Notebook,
  Picture,
  Place
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