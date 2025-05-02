<template>
  <div class="create-post-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>发布动态</h2>
      </div>
      
      <!-- 发布表单 -->
      <post-form @success="handleSuccess" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PostForm from '@/components/social/PostForm.vue'

const router = useRouter()

// 发布成功处理
const handleSuccess = () => {
  router.push('/social')
}
</script>

<style scoped>
.create-post-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
</style> 