<template>
  <div class="notification-page">
    <el-row :gutter="20">
      <el-col :span="24" :md="12">
        <SendNotification />
      </el-col>
      
      <el-col :span="24" :md="12">
        <NotificationList type="sent" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import SendNotification from '../../components/notification/SendNotification.vue';
import NotificationList from '../../components/notification/NotificationList.vue';
</script>

<style scoped>
.notification-page {
  padding: 20px;
}

@media (max-width: 768px) {
  .notification-page {
    padding: 10px;
  }
}
</style> 