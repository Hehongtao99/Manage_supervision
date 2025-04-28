<template>
  <div class="notification-page">
    <NotificationList type="received" />
  </div>
</template>

<script setup lang="ts">
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