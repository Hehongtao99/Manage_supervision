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

      <el-sub-menu index="companion-management">
        <template #title>
          <el-icon><Service /></el-icon>
          <span>陪玩管理</span>
        </template>
        <el-menu-item index="/admin/companions" @click="handleRoute('/admin/companions')">
          <el-icon><UserFilled /></el-icon>
          <span>陪玩人员</span>
        </el-menu-item>
        <el-menu-item index="/admin/players" @click="handleRoute('/admin/players')">
          <el-icon><User /></el-icon>
          <span>玩家管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/companion-services" @click="handleRoute('/admin/companion-services')">
          <el-icon><List /></el-icon>
          <span>陪玩服务</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders" @click="handleRoute('/admin/orders')">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews" @click="handleRoute('/admin/reviews')">
          <el-icon><Star /></el-icon>
          <span>评论审核</span>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 陪玩菜单 -->
    <template v-else-if="userStore.isSupervisor">
      <el-menu-item index="/companion/chat" @click="handleRoute('/companion/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/companion/profile" @click="handleRoute('/companion/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>陪玩信息</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/companion/services" @click="handleRoute('/companion/services')">
        <el-icon><Service /></el-icon>
        <template #title>
          <span>陪玩服务</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/companion/orders" @click="handleRoute('/companion/orders')">
        <el-icon><List /></el-icon>
        <template #title>
          <span>订单管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/companion/reviews" @click="handleRoute('/companion/reviews')">
        <el-icon><Star /></el-icon>
        <template #title>
          <span>评价管理</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 玩家菜单 -->
    <template v-else>
      <el-menu-item index="/chat" @click="handleRoute('/chat')">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>
          <span>聊天</span>
        </template>
      </el-menu-item>

      <el-menu-item index="/profile" @click="handleRoute('/profile')">
        <el-icon><UserFilled /></el-icon>
        <template #title>
          <span>个人信息</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/companion-market" @click="handleRoute('/companion-market')">
        <el-icon><Shop /></el-icon>
        <template #title>
          <span>陪玩大厅</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/orders" @click="handleRoute('/orders')">
        <el-icon><Tickets /></el-icon>
        <template #title>
          <span>我的订单</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/reviews" @click="handleRoute('/reviews')">
        <el-icon><Star /></el-icon>
        <template #title>
          <span>我的评价</span>
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
  Service,
  Shop,
  Tickets,
  Star
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