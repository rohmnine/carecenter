import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
    name: 'Register',
    data() {
        // 验证手机号
        const validatePhone = (rule, value, callback) => {
            const phoneReg = /^1[3-9]\d{9}$/;
            if (!value) {
                callback(new Error('请输入手机号'));
            } else if (!phoneReg.test(value)) {
                callback(new Error('请输入正确的手机号'));
            } else {
                callback();
            }
        };

        // 验证确认密码
        const validateConfirmPassword = (rule, value, callback) => {
            if (!value) {
                callback(new Error('请再次输入密码'));
            } else if (value !== this.form.password) {
                callback(new Error('两次输入密码不一致'));
            } else {
                callback();
            }
        };

        return {
            form: {
                username: '',
                name: '',
                password: '',
                confirmPassword: '',
                phoneNum: '',
                gender: '男',
                identity: 'stu'
            },
            rules: {
                username: [
                    {required: true, message: '请输入用户名', trigger: 'blur'},
                    {pattern: /^[a-zA-Z0-9]{4,9}$/, message: '用户名必须由 4 到 9 个字母或数字组成', trigger: 'blur'}
                ],
                name: [
                    {required: true, message: '请输入昵称', trigger: 'blur'},
                    {min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur'}
                ],
                password: [
                    {required: true, message: '请输入密码', trigger: 'blur'},
                    {min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur'}
                ],
                confirmPassword: [
                    {required: true, validator: validateConfirmPassword, trigger: 'blur'}
                ],
                phoneNum: [
                    {required: true, validator: validatePhone, trigger: 'blur'}
                ],
                gender: [
                    {required: true, message: '请选择性别', trigger: 'change'}
                ],
                identity: [
                    {required: true, message: '请选择角色', trigger: 'change'}
                ]
            }
        }
    },
    computed: {
        disabled() {
            return this.form.username && this.form.name && this.form.password 
                && this.form.confirmPassword && this.form.phoneNum 
                && this.form.gender && this.form.identity;
        }
    },
    methods: {
        register() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    // 根据身份选择注册接口
                    let url = '';
                    let registerData = {
                        username: this.form.username,
                        name: this.form.name,
                        password: this.form.password,
                        phoneNum: this.form.phoneNum,
                        gender: this.form.gender,
                        age: 18 // 默认年龄
                    };

                    if (this.form.identity === 'stu') {
                        url = '/stu/register';
                    } else if (this.form.identity === 'parent') {
                        url = '/parent/register';
                    }

                    request.post(url, registerData).then(res => {
                        if (res.code === '0') {
                            ElMessage({
                                message: '注册成功，请登录',
                                type: 'success',
                            });
                            // 跳转到登录页面
                            this.$router.push('/login');
                        } else {
                            ElMessage({
                                message: res.msg || '注册失败',
                                type: 'error',
                            });
                        }
                    }).catch(err => {
                        ElMessage({
                            message: '注册失败，请稍后重试',
                            type: 'error',
                        });
                        console.error(err);
                    });
                } else {
                    ElMessage({
                        message: '请填写完整信息',
                        type: 'warning',
                    });
                    return false;
                }
            });
        },
        goToLogin() {
            this.$router.push('/login');
        }
    }
}