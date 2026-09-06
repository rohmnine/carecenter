<template>
    <div class="login-container">
        <!-- <div style="margin-right: 150px; min-width: 300px">
            <img alt="" src="../../public/login_Team.png" style="width: 600px"/>
        </div> -->
        <div style="width: 400px; margin: 150px auto">
            <div style="color: black; font-size: 30px; text-align: left; padding: 30px 0">登录</div>
            <el-form ref="form" :model="form" :rules="rules" size="large">
                <el-form-item prop="username">
                    <el-input v-model="form.username" clearable prefix-icon="avatar" placeholder="请输入用户名/学号"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" prefix-icon="lock" show-password></el-input>
                </el-form-item>
                <el-form-item :model="form" prop="identity">
                    <el-radio v-model="form.identity" label="stu" style="color: grey">学生</el-radio>
                    <el-radio v-model="form.identity" label="parent" style="color: grey">家长</el-radio>
                    <el-radio v-model="form.identity" label="admin" style="color: grey">管理员</el-radio>
                </el-form-item>
                <el-form-item>
                    <el-button :disabled="!disabled" style="width: 48%" type="primary" @click="login">登 录</el-button>
                    <el-button style="width: 48%; margin-left: 4%" @click="goToRegister">注 册</el-button>
                </el-form-item>
                <el-form-item style="margin-top: -8px; margin-bottom: 0">
                    <el-link type="primary" :underline="false" @click="openResetDialog">忘记密码？</el-link>
                </el-form-item>
            </el-form>
        </div>
    </div>

    <el-dialog v-model="resetVisible" title="找回密码" width="420px" destroy-on-close>
        <el-form :model="resetForm" label-width="80px">
            <el-form-item label="身份">
                <el-radio-group v-model="resetForm.identity">
                    <el-radio label="stu">学生</el-radio>
                    <el-radio label="parent">家长</el-radio>
                    <el-radio label="admin">管理员</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="用户名">
                <el-input v-model="resetForm.username" clearable placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="姓名">
                <el-input v-model="resetForm.name" clearable placeholder="请输入姓名"></el-input>
            </el-form-item>
            <el-form-item label="新密码">
                <el-input v-model="resetForm.newPassword" show-password placeholder="请输入新密码"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="resetVisible = false">取 消</el-button>
            <el-button type="primary" :disabled="!resetDisabled" @click="submitResetPassword">确 定</el-button>
        </template>
    </el-dialog>

    <div class="footer">
        ©Copyright 南京工业大学浦江学院
    </div>
</template>
<script src="@/assets/js/Login.js"></script>
<style scoped>
.login-container {
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
