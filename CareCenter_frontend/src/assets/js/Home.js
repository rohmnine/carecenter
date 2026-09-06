import Calender from "@/components/Calendar";
import request from "@/utils/request";
import home_echarts from "@/components/home_echarts";
import { ElMessage } from "element-plus";
import { clearCurrentSession } from "@/utils/sessionHelper";

export default {
    name: "Home",
    components: {
        Calender,
        home_echarts,
    },
    data() {
        return {
            studentNum: "",
            haveRoomStudentNum: "",
            detailDialog: false,
            noFullRoomNum: "",
            activities: [],
            todayMealReservation: "0",
            todayMealConfirmed: "0",
        };
    },
    created() {
        this.getHomePageNotice();
        this.getStuNum();
        this.getHaveRoomNum();
        this.getNoFullRoom();
        this.getTodayMealReservation();
    },
    methods: {
        async getStuNum() {
            request.get("/stu/stuNum").then((res) => {
                if (res.code === "0") {
                    this.studentNum = res.data;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            }).catch((err) => {
                const status = err && err.response ? err.response.status : null;
                if (status === 401) {
                    clearCurrentSession();
                    this.$router.replace({ path: "/login" });
                    ElMessage({ message: "登录已过期，请重新登录", type: "warning" });
                } else if (status === 403) {
                    ElMessage({ message: "无权限获取学生统计", type: "warning" });
                } else {
                    ElMessage({ message: "学生统计获取失败", type: "error" });
                }
            });
        },
        async getHaveRoomNum() {
            request.get("/room/selectHaveRoomStuNum").then((res) => {
                if (res.code === "0") {
                    this.haveRoomStudentNum = res.data;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            }).catch((err) => {
                const status = err && err.response ? err.response.status : null;
                if (status === 401) {
                    clearCurrentSession();
                    this.$router.replace({ path: "/login" });
                    ElMessage({ message: "登录已过期，请重新登录", type: "warning" });
                } else {
                    ElMessage({ message: "住宿人数获取失败", type: "error" });
                }
            });
        },
        async getNoFullRoom() {
            request.get("/room/noFullRoom").then((res) => {
                if (res.code === "0") {
                    this.noFullRoomNum = res.data;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            }).catch((err) => {
                const status = err && err.response ? err.response.status : null;
                if (status === 401) {
                    clearCurrentSession();
                    this.$router.replace({ path: "/login" });
                    ElMessage({ message: "登录已过期，请重新登录", type: "warning" });
                } else {
                    ElMessage({ message: "空床位统计获取失败", type: "error" });
                }
            });
        },
        async getHomePageNotice() {
            request.get("/notice/homePageNotice").then((res) => {
                if (res.code === "0") {
                    this.activities = res.data;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            }).catch((err) => {
                const status = err && err.response ? err.response.status : null;
                if (status === 401) {
                    clearCurrentSession();
                    this.$router.replace({ path: "/login" });
                    ElMessage({ message: "登录已过期，请重新登录", type: "warning" });
                } else {
                    ElMessage({ message: "首页公告获取失败", type: "error" });
                }
            });
        },
        async getTodayMealReservation() {
            request.get("/mealReservation/todayWithConfirm").then((res) => {
                if (res.code === "0") {
                    const list = res.data || [];
                    this.todayMealReservation = list.length;
                    // Count confirmed meals
                    const confirmed = list.filter(item => {
                        // Handle both object and JSON string formats
                        let requestData = item.requestData;
                        if (typeof requestData === 'string') {
                            try {
                                requestData = JSON.parse(requestData);
                            } catch (e) {
                                requestData = null;
                            }
                        }
                        return requestData && requestData.mealConfirmed === true;
                    });
                    this.todayMealConfirmed = confirmed.length;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            }).catch((err) => {
                const status = err && err.response ? err.response.status : null;
                if (status === 401) {
                    clearCurrentSession();
                    this.$router.replace({ path: "/login" });
                    ElMessage({ message: "登录已过期，请重新登录", type: "warning" });
                } else {
                    ElMessage({ message: "今日订餐统计获取失败", type: "error" });
                }
            });
        },
    },
};