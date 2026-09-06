import request from "@/utils/request";
import { setCurrentUser } from "@/utils/sessionHelper";

const {ElMessage} = require("element-plus");
export default {
    name: "Login",
    data() {
        return {
            identity: "",
            form: {
                username: "",
                password: "",
                identity: "",
            },
            resetVisible: false,
            resetForm: {
                username: "",
                name: "",
                newPassword: "",
                identity: "",
            },
            rules: {
                username: [
                    {required: true, message: "请输入用户名/学号", trigger: "blur"},
                ],
                password: [{required: true, message: "请输入密码", trigger: "blur"}],
                identity: [{required: true, message: "请选择身份", trigger: "blur"}],
            },
        };
    },
    computed: {
        disabled() {
            const {username, password, identity} = this.form;
            return Boolean(username && password && identity);
        },
        resetDisabled() {
            const {username, name, newPassword, identity} = this.resetForm;
            return Boolean(username && name && newPassword && identity);
        },
    },
    methods: {
        login() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.identity = this.form.identity;
                    request.post("/" + this.identity + "/login", this.form).then((res) => {
                        if (res.code === "0") {
                            ElMessage({
                                message: "登录成功",
                                type: "success",
                            });
                            // 使用身份隔离的会话存储
                            setCurrentUser(res.data, this.form.identity);
                            this.$router.replace({path: "/home"});
                        } else {
                            ElMessage({
                                message: res.msg,
                                type: "error",
                            });
                        }
                    });
                }
            });
        },
        goToRegister() {
            this.$router.push('/register');
        },
        openResetDialog() {
            this.resetForm = {
                username: "",
                name: "",
                newPassword: "",
                identity: "",
            };
            this.resetVisible = true;
        },
        submitResetPassword() {
            if (!this.resetDisabled) {
                ElMessage({
                    message: "请完整填写重置信息",
                    type: "warning",
                });
                return;
            }

            request.post("/" + this.resetForm.identity + "/resetPassword", {
                username: this.resetForm.username,
                name: this.resetForm.name,
                newPassword: this.resetForm.newPassword,
            }).then((res) => {
                if (res.code === "0") {
                    ElMessage({
                        message: "密码修改成功，请使用新密码登录",
                        type: "success",
                    });
                    this.resetVisible = false;
                } else {
                    ElMessage({
                        message: res.msg || "密码修改失败",
                        type: "error",
                    });
                }
            });
        },
    },
};
