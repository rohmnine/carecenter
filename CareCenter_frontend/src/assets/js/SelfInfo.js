import request from "@/utils/request";
import { getCurrentUser, getCurrentIdentity, updateCurrentUser } from "@/utils/sessionHelper";

const {ElMessage} = require("element-plus");
export default {
    name: "selfInfo",
    data() {
        // 手机号验证
        const checkPhone = (rule, value, callback) => {
            const phoneReg = /^1[3|4|5|6|7|8][0-9]{9}$/;
            if (!value) {
                return callback(new Error("电话号码不能为空"));
            }
            setTimeout(() => {
                if (!Number.isInteger(+value)) {
                    callback(new Error("请输入数字值"));
                } else {
                    if (phoneReg.test(value)) {
                        callback();
                    } else {
                        callback(new Error("电话号码格式不正确"));
                    }
                }
            }, 100);
        };
        const checkPass = (rule, value, callback) => {
            if (!this.editJudge) {
                console.log("验证");
                if (value == "") {
                    callback(new Error("请再次输入密码"));
                } else if (value !== this.form.password) {
                    callback(new Error("两次输入密码不一致!"));
                } else {
                    callback();
                }
            } else {
                console.log("不验证");
                callback();
            }
        };
        return {
            showpassword: true,
            editJudge: true,
            disabled: true,
            dialogVisible: false,
            identity: "",
            username: "",
            name: "",
            gender: "",
            age: "",
            phoneNum: "",
            email: "",
            studentUsername: "",
            studentUsernames: [],
            studentList: [],
            searchLoading: false,
            form: {
                username: "",
                name: "",
                gender: "",
                age: "",
                phoneNum: "",
                email: "",
                studentUsername: "",
                studentUsernames: [],
            },
            rules: {
                username: [
                    {required: true, message: "请输入账号", trigger: "blur"},
                    {
                        pattern: /^[a-zA-Z0-9]{4,9}$/,
                        message: "必须由 4 到 9 个字母或数字组成",
                        trigger: "blur",
                    },
                ],
                name: [
                    {required: true, message: "请输入姓名", trigger: "blur"},
                    {
                        pattern: /^(?:[\u4E00-\u9FA5·]{2,10})$/,
                        message: "必须由 2 到 10 个汉字组成",
                        trigger: "blur",
                    },
                ],
                gender: [{required: true, message: "请选择性别", trigger: "change"}],
                age: [
                    {required: true, message: "请输入年龄", trigger: "blur"},
                    {type: "number", message: "年龄必须为数字值", trigger: "blur"},
                    {
                        pattern: /^(1|[1-9]\d?|100)$/,
                        message: "范围：1-100",
                        trigger: "blur",
                    },
                ],
                phoneNum: [{required: true, validator: checkPhone, trigger: "blur"}],
                email: [
                    {type: "email", message: "请输入正确的邮箱地址", trigger: "blur"},
                ],
                password: [
                    {required: true, message: "请输入密码", trigger: "blur"}
                ],
                checkPass: [{validator: checkPass, trigger: "blur"}],
                studentUsernames: [],
            },
            display: {
                display: "none",
            },
        };
    },
    created() {
        this.load();
        this.find();
    },
    methods: {
        //获取个人信息页面信息
        load() {
            this.form = getCurrentUser();
            this.identity = getCurrentIdentity();
            this.username = this.form.username;
            this.name = this.form.name;
            this.gender = this.form.gender;
            this.age = this.form.age;
            this.phoneNum = this.form.phoneNum;
            this.email = this.form.email;
            const list = Array.isArray(this.form.studentUsernames)
                ? this.form.studentUsernames
                : ((this.form.studentUsername || '')
                    .split(',')
                    .map(item => item.trim())
                    .filter(Boolean));
            this.form.studentUsernames = list;
            this.studentUsernames = list;
            this.studentUsername = list.join(', ');
        },
        //查询数据，更新session
        find() {
            this.form = getCurrentUser();
            request.post("/" + this.identity + "/login", this.form).then((res) => {
                //更新sessionStorage
                updateCurrentUser(res.data);
                //更新页面数据
                this.load();
            });
        },
        Edit() {
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                this.form = getCurrentUser();
                // 如果是家长且已绑定学生，加载该学生信息
                if (this.identity === 'parent' && this.form.studentUsernames && this.form.studentUsernames.length > 0) {
                    this.loadCurrentStudents();
                }
            });
        },
        // 加载当前绑定的学生信息
        loadCurrentStudents() {
            const usernames = Array.isArray(this.form.studentUsernames) ? this.form.studentUsernames : [];
            if (!usernames.length) {
                this.studentList = [];
                return;
            }
            Promise.all(usernames.map(username => request.get("/stu/exist/" + username)))
                .then(results => {
                    this.studentList = results
                        .filter(res => (res.code === '200' || res.code === 200) && res.data)
                        .map(res => res.data);
                })
                .catch(err => {
                    console.error("加载学生信息失败", err);
                });
        },
        // 搜索学生
        searchStudents(query) {
            if (query !== '') {
                this.searchLoading = true;
                console.log("搜索学生，关键词：", query);
                request.get("/stu/find", {
                    params: {
                        pageNum: 1,
                        pageSize: 20,
                        search: query
                    }
                }).then((res) => {
                    console.log("搜索学生响应：", res);
                    this.searchLoading = false;
                    if (res.code === '0' && res.data && res.data.records) {
                        this.studentList = res.data.records;
                        console.log("找到学生数量：", this.studentList.length);
                    } else {
                        console.log("搜索结果为空或响应码不正确：", res);
                        this.studentList = [];
                    }
                }).catch((err) => {
                    console.error("搜索学生失败", err);
                    this.searchLoading = false;
                    this.studentList = [];
                    ElMessage({
                        message: "搜索失败：" + (err.message || "网络错误"),
                        type: "error",
                    });
                });
            } else {
                this.studentList = [];
            }
        },
        cancel() {
            this.$refs.form.resetFields();
            this.display = {display: "none"};
            this.showpassword = true;
            this.editJudge = true;
            this.disabled = true;
            this.dialogVisible = false;
        },
        async save() {
            this.$refs.form.validate(async (valid) => {
                if (valid) {
                    if (this.identity === 'parent') {
                        const list = Array.isArray(this.form.studentUsernames)
                            ? this.form.studentUsernames.filter(Boolean)
                            : [];
                        this.form.studentUsernames = list;
                        this.form.studentUsername = list.join(',');
                    }
                    //修改
                    await request.put("/" + this.identity + "/update", this.form).then((res) => {
                        if (res.code === "0") {
                            ElMessage({
                                message: "修改成功",
                                type: "success",
                            });
                            //更新sessionStorage
                            updateCurrentUser(this.form);
                            this.find();
                            this.dialogVisible = false;
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
        EditPass() {
            if (this.editJudge) {
                this.display = {display: "flex"};
                this.showpassword = false;
                this.disabled = false;
                this.editJudge = false;
            } else {
                this.display = {display: "none"};
                this.showpassword = true;
                this.editJudge = true;
                this.disabled = true;
            }
        },
    },
};