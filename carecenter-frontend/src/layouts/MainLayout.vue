<template>
  <div class="main-layout">
    <Sidebar />
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '../components/Sidebar.vue';
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
}

.content {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>