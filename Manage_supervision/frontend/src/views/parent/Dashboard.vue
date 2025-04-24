<template>
  <div class="parent-dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <h2>欢迎，{{ username }}</h2>
              <el-tag type="success">家长</el-tag>
            </div>
          </template>
          <div class="card-content">
            <p>欢迎访问家长控制台，您可以在这里查看孩子的学习进度、课程安排和成绩情况。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>子女信息</h3>
            </div>
          </template>
          <div class="card-content">
            <el-empty description="暂无子女信息" v-if="!hasChildren"></el-empty>
            <div v-else>
              <!-- 子女信息展示 -->
              <p>暂无子女信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>课程与任务</h3>
            </div>
          </template>
          <div class="card-content">
            <el-empty description="暂无课程信息" v-if="!hasCourses"></el-empty>
            <div v-else>
              <!-- 课程信息展示 -->
              <p>暂无课程信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <h3>通知公告</h3>
            </div>
          </template>
          <div class="card-content">
            <el-empty description="暂无通知" v-if="!hasNotifications"></el-empty>
            <div v-else>
              <!-- 通知信息展示 -->
              <p>暂无通知信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import type { AxiosResponse } from '../../types/axios'

const userStore = useUserStore()
const username = ref(userStore.user.realName || userStore.user.username || '家长用户')

// 数据状态
const hasChildren = ref(false)
const hasCourses = ref(false)
const hasNotifications = ref(false)

onMounted(async () => {
  // 初始化数据
  console.log('家长控制台初始化')
})
</script>

<style scoped>
.parent-dashboard {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.dashboard-card {
  height: 280px;
  overflow: auto;
}

.card-content {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
</style> 