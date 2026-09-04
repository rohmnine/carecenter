<template>
  <div class="sidebar">
    <h2>导航</h2>
    <ul>
      <li><router-link to="/admin">管理员面板</router-link></li>
      <li><router-link to="/teacher">教师面板</router-link></li>
      <li><router-link to="/parent">家长面板</router-link></li>
      <li><router-link to="/messages">消息板</router-link></li>
    </ul>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.sidebar {
  width: 200px;
  background-color: #f0f2f5;
  padding: 20px;
  height: 100%;
}

h2 {
  margin-top: 0;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin-bottom: 10px;
}

a {
  text-decoration: none;
  color: #333;
  font-weight: bold;
}

a.router-link-exact-active {
  color: #42b983;
}
</style>