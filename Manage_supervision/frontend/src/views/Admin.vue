<template>
  <div class="admin-container">
    <el-card class="admin-card">
      <template #header>
        <div class="header-content">
          <h2>管理员控制台</h2>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>
      </template>
      
      <div class="admin-content">
        <h3>管理员专属内容</h3>
        <p>这里是只有管理员才能访问的页面。</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.admin-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.admin-card {
  margin-top: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
}

.admin-content {
  margin-top: 20px;
}
</style> 