<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-form" v-if="isLogin">
        <h1>登录</h1>
        <form @submit.prevent="login">
          <div class="input-group">
            <input type="text" v-model="username" placeholder="用户名" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="password" placeholder="密码" required>
          </div>
          <button type="submit" class="btn">登录</button>
        </form>
        <p class="toggle-form">
          还没有账户？ <a href="#" @click.prevent="toggleForm">立即注册</a>
        </p>
      </div>
      <div class="register-form" v-else>
        <h1>注册</h1>
        <form @submit.prevent="register">
          <div class="input-group">
            <input type="text" v-model="username" placeholder="用户名" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="password" placeholder="密码" required>
          </div>
          <div class="input-group">
            <select v-model="role" required>
              <option disabled value="">请选择角色</option>
              <option value="ADMIN">管理员</option>
              <option value="TEACHER">教师</option>
              <option value="PARENT">家长</option>
            </select>
          </div>
          <button type="submit" class="btn">注册</button>
        </form>
        <p class="toggle-form">
          已有账户？ <a href="#" @click.prevent="toggleForm">立即登录</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import api from '../services/api';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const router = useRouter();
const isLogin = ref(true);
const role = ref('');

const login = async () => {
  try {
    const response = await api.login({
      username: username.value,
      password: password.value
    });
    // 处理登录成功, 例如存储token并重定向
    const token = response.data;
    localStorage.setItem('token', token);
    const payload = JSON.parse(atob(token.split('.')[1]));
    switch (payload.role) {
      case 'ADMIN':
        router.push('/admin');
        break;
      case 'TEACHER':
        router.push('/teacher');
        break;
      case 'PARENT':
        router.push('/parent');
        break;
      default:
        router.push('/');
    }
  } catch (error) {
    console.error(error);
    alert('登录失败，请检查用户名和密码。');
  }
};

const register = async () => {
  try {
    const response = await api.register({ // 假设api.ts中有register方法
      username: username.value,
      password: password.value,
      role: role.value
    });
    console.log(response.data);
    alert('注册成功！请登录。');
    toggleForm(); // 切换回登录表单
  } catch (error) {
    console.error(error);
    alert('注册失败，用户名可能已被占用。');
  }
};

const toggleForm = () => {
  isLogin.value = !isLogin.value;
  username.value = '';
  password.value = '';
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-box {
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

h1 {
  margin-bottom: 1.5rem;
  color: #333;
}

.input-group {
  margin-bottom: 1rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #1890ff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #40a9ff;
}

.toggle-form {
  margin-top: 1rem;
  font-size: 0.9rem;
}

.toggle-form a {
  color: #1890ff;
  text-decoration: none;
}

.toggle-form a:hover {
  text-decoration: underline;
}
</style>