<template>
    <div class="register-container">
        <div style="width: 400px; margin: 150px auto">
            <div style="color: black; font-size: 30px; text-align: left; padding: 30px 0">用户注册</div>
            <el-form ref="form" :model="form" :rules="rules" size="large">
                <el-form-item prop="username">
                    <el-input v-model="form.username" clearable placeholder="请输入用户名" prefix-icon="avatar"></el-input>
                </el-form-item>
                <el-form-item prop="name">
                    <el-input v-model="form.name" clearable placeholder="请输入昵称" prefix-icon="user"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" placeholder="请输入密码" prefix-icon="lock" show-password></el-input>
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input v-model="form.confirmPassword" placeholder="请确认密码" prefix-icon="lock" show-password></el-input>
                </el-form-item>
                <el-form-item prop="phoneNum">
                    <el-input v-model="form.phoneNum" clearable placeholder="请输入手机号" prefix-icon="phone"></el-input>
                </el-form-item>
                <el-form-item prop="gender">
                    <el-radio v-model="form.gender" label="男" style="color: grey">男</el-radio>
                    <el-radio v-model="form.gender" label="女" style="color: grey">女</el-radio>
                </el-form-item>
                <el-form-item prop="identity">
                    <el-radio v-model="form.identity" label="stu" style="color: grey">学生</el-radio>
                    <el-radio v-model="form.identity" label="parent" style="color: grey">家长</el-radio>
                </el-form-item>
                <el-form-item>
                    <el-button :disabled="!disabled" style="width: 48%" type="primary" @click="register">注 册</el-button>
                    <el-button style="width: 48%; margin-left: 4%" @click="goToLogin">返回登录</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
    <div class="footer">
        ©Copyright 南京工业大学浦江学院
    </div>
</template>
<script src="@/assets/js/Register.js"></script>
<style scoped>
.register-container {
    position: fixed;
    height: 100%;
    width: 100%;
    top: 0;
    left: 0;
    background: hsl(190deg, 80%, 70%);
}

.footer {
    left: 0;
    bottom: 0;
    color: #fff;
    width: 100%;
    position: absolute;
    text-align: center;
    line-height: 30px;
    padding-bottom: 10px;
    text-shadow: #000 0.1em 0.1em 0.1em;
    font-size: 14px;
}

.footer a,
.footer span {
    color: #fff;
}
</style>