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
      
      <el-sub-menu index="social-management">
        <template #title>
          <el-icon><ChatDotRound /></el-icon>
          <span>社交管理</span>
        </template>
        <el-menu-item index="/admin/social/posts" @click="handleRoute('/admin/social/posts')">
          <el-icon><PictureFilled /></el-icon>
          <span>朋友圈管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/social/comments" @click="handleRoute('/admin/social/comments')">
          <el-icon><ChatLineRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="running-management">
        <template #title>
          <el-icon><Timer /></el-icon>
          <span>跑步管理</span>
        </template>
        <el-menu-item index="/admin/running/report" @click="handleRoute('/admin/running/report')">
          <el-icon><DataAnalysis /></el-icon>
          <span>跑步报表</span>
        </el-menu-item>
        <el-menu-item index="/admin/running/ranking" @click="handleRoute('/admin/running/ranking')">
          <el-icon><Medal /></el-icon>
          <span>跑步排名</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 跑步爱好者菜单 -->
    <template v-else-if="userStore.isRunner">
      <el-menu-item index="/chat" @click="handleRoute('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/friends" @click="handleRoute('/friends')">
        <el-icon><User /></el-icon>
        <template #title>
          <span>好友管理</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/profile" @click="handleRoute('/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
      
      <el-sub-menu index="running-management">
        <template #title>
          <el-icon><Timer /></el-icon>
          <span>跑步管理</span>
        </template>
        <el-menu-item index="/running-record" @click="handleRoute('/running-record')">
          <el-icon><EditPen /></el-icon>
          <span>跑步记录</span>
        </el-menu-item>
        <el-menu-item index="/running-dashboard" @click="handleRoute('/running-dashboard')">
          <el-icon><DataAnalysis /></el-icon>
          <span>跑步看板</span>
        </el-menu-item>
      </el-sub-menu>
      
      <!-- 社交系统菜单 - 跑步爱好者 -->
      <el-sub-menu index="social-management">
        <template #title>
          <el-icon><Connection /></el-icon>
          <span>社交系统</span>
        </template>
        <el-menu-item index="/social" @click="handleRoute('/social')">
          <el-icon><ChatLineRound /></el-icon>
          <span>朋友圈</span>
        </el-menu-item>
        <el-menu-item index="/social/my-posts" @click="handleRoute('/social/my-posts')">
          <el-icon><Collection /></el-icon>
          <span>我的发布</span>
        </el-menu-item>
        <el-menu-item index="/social/create-post" @click="handleRoute('/social/create-post')">
          <el-icon><Edit /></el-icon>
          <span>发布动态</span>
        </el-menu-item>
      </el-sub-menu>
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
  Timer,
  EditPen,
  DataAnalysis,
  Connection,
  ChatLineRound,
  Collection,
  Edit,
  PictureFilled,
  Medal
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