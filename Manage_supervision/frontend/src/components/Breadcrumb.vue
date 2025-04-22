<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span 
          v-if="index === breadcrumbs.length - 1" 
          class="no-redirect"
        >{{ item.meta?.title }}</span>
        <a 
          v-else 
          class="redirect" 
          @click.prevent="handleLink(item.path)"
        >{{ item.meta?.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { RouteLocationMatched } from 'vue-router'

const route = useRoute()
const router = useRouter()

const breadcrumbs = computed(() => {
  // 过滤掉没有 meta.title 的路由
  return route.matched.filter(item => item.meta?.title)
})

const handleLink = (path: string) => {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 60px;
  margin-left: 8px;
  
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
  
  .redirect {
    color: #666;
    font-weight: 600;
    cursor: pointer;
    
    &:hover {
      color: #409EFF;
    }
  }
}

/* 面包屑动画 */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-active {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}

:deep(.el-breadcrumb__separator) {
  margin: 0 9px;
  font-weight: 400;
  color: #97a8be;
}

:deep(.el-breadcrumb__inner) {
  color: #97a8be;
}
</style> 