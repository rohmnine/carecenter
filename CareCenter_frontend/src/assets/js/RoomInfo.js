import request from "@/utils/request";

const {ElMessage} = require("element-plus");

export default {
    name: "BuildingInfo",
    components: {},
    data() {
        const checkStuNum = (rule, value, callback) => {
            if (!value) {
                callback();
                return;
            }
            request.get("/stu/exist/" + value).then((res) => {
                request.get("/room/judgeHadBed/" + value).then((result) => {
                    if (res.code === "0" && result.code === "0") {
                        callback();
                    } else if (res.code === "-1" && result.code === "0") {
                        callback(new Error(res.msg));
                    } else if (res.code === "0" && result.code === "-1") {
                        callback(new Error(result.msg));
                    } else {
                        callback(new Error("请输入正确的数据"));
                    }
                });
            });
        };
        return {
            bedNum: 0,
            havePeopleNum: 0,
            loading: true,
            disabled: false,
            judge: false,
            dialogVisible: false,
            bedDialog: false,
            stuInfoDialog: false,
            bedName: "",
            search: "",
            currentPage: 1,
            pageSize: 10,
            total: 0,
            tableData: [],
            buildingList: [],
            expandedRowKeys: [],
            form: {
                centerRoomId: "",
                centerBuildingId: "",
                floorNum: "",
                maxCapacity: "",
                currentCapacity: "",
                bedNo: "",
                bedStudentUsername: "",
            },
            rules: {
                centerRoomId: [
                    {required: true, message: "请输入房间号", trigger: "blur"},
                ],
                floorNum: [
                    {required: true, message: "请输入楼层数", trigger: "blur"},
                    {pattern: /^[1-3]$/, message: "范围：1-3", trigger: "blur"},
                ],
                centerBuildingId: [
                    {required: true, message: "请输入楼宇号数", trigger: "blur"},
                    {pattern: /^[1-4]$/, message: "范围：1-4", trigger: "blur"},
                ],
                maxCapacity: [
                    {required: true, message: "请输入房间可住人数", trigger: "blur"},
                    {pattern: /^([0-9]|1[0-9]|20)$/, message: "范围：0-20", trigger: "blur"},
                ],
                currentCapacity: [
                    {required: true, message: "请输入当前已住人数", trigger: "blur"},
                    {pattern: /^([0-9]|1[0-9]|20)$/, message: "范围：0-20", trigger: "blur"},
                ],
                bedStudentUsername: [{validator: checkStuNum, trigger: "blur"}],
            },
            recommendDialog: false,
            recommendLoading: false,
            recommendList: [],
            recommendForm: {
                studentUsername: "",
                centerBuildingId: "",
                gender: "",
                grade: "",
                scheduleType: "",
                allergyPreference: "",
                dietPreference: "",
            },
        };
    },
    created() {
        this.load();
        this.loadBuildings();
        this.loading = true;
        setTimeout(() => {
            //设置延迟执行
            this.loading = false;
        }, 1000);
    },
    computed: {
        capacityFilters() {
            const max = Math.max(4, ...this.tableData.map(r => Number(r.maxCapacity || 0)));
            return Array.from({length: max + 1}, (_, i) => ({text: i, value: i}));
        },
    },
    methods: {
        formatBuildingName(row, column, cellValue) {
            const building = this.buildingList.find(b => b.centerBuildingId === cellValue);
            return building ? building.centerBuildingName : cellValue;
        },
        formatOccupantLabel(bed) {
            if (!bed || !bed.occupantUsername) {
                return "";
            }
            return bed.occupantName ? `${bed.occupantName}（${bed.occupantUsername}）` : bed.occupantUsername;
        },
        loadBuildings() {
            request.get("/building/find", {
                params: {
                    pageNum: 1,
                    pageSize: 100,
                    search: "",
                },
            }).then((res) => {
                this.buildingList = res.data.records;
            });
        },
        async load() {
            request.get("/room/find", {
                params: {
                    pageNum: this.currentPage,
                    pageSize: this.pageSize,
                    search: this.search,
                },
            }).then((res) => {
                const records = Array.isArray(res.data.records) ? res.data.records : [];
                this.tableData = records.map(item => ({
                    ...item,
                    beds: Array.isArray(item.beds) ? item.beds : [],
                    bedLoading: false,
                }));
                this.total = res.data.total;
                this.loading = false;
            });
        },
        reset() {
            this.search = ''
            request.get("/room/find", {
                params: {
                    pageNum: 1,
                    pageSize: this.pageSize,
                    search: this.search,
                },
            }).then((res) => {
                console.log(res);
                const records = Array.isArray(res.data.records) ? res.data.records : [];
                this.tableData = records.map(item => ({
                    ...item,
                    beds: Array.isArray(item.beds) ? item.beds : [],
                    bedLoading: false,
                }));
                this.total = res.data.total;
                this.loading = false;
            });
        },
        filterTag(value, row) {
            return row.currentCapacity === value;
        },
        add() {
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                this.disabled = false;
                this.form = {};
                this.judge = false;
            });
        },
        save() {
            this.$refs.form.validate(async (valid) => {
                if (valid) {
                    if (this.judge === false) {
                        //新增
                        request.post("/room/add", this.form).then((res) => {
                            if (res.code === "0") {
                                ElMessage({
                                    message: "新增成功",
                                    type: "success",
                                });
                                this.search = "";
                                this.loading = true;
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage({
                                    message: res.msg,
                                    type: "error",
                                });
                            }
                        });
                    } else {
                        //修改
                        request.put("/room/update", this.form).then((res) => {
                            if (res.code === "0") {
                                ElMessage({
                                    message: "修改成功",
                                    type: "success",
                                });
                                this.search = "";
                                this.load();
                                this.dialogVisible = false;
                            } else {
                                ElMessage({
                                    message: res.msg,
                                    type: "error",
                                });
                            }
                        });
                    }
                }
            });
        },
        cancel() {
            if (this.$refs.form && typeof this.$refs.form.resetFields === "function") {
                this.$refs.form.resetFields();
            }
            this.dialogVisible = false;
            this.bedDialog = false;
            this.stuInfoDialog = false;
            this.recommendDialog = false;
        },
        handleEdit(row) {
            //修改
            this.judge = true;
            this.dialogVisible = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                // 生拷贝
                this.form = JSON.parse(JSON.stringify(row));
                this.disabled = true;
            });
        },
        handleDelete(centerRoomId) {
            //删除
            request.delete("/room/delete/" + centerRoomId).then((res) => {
                if (res.code === "0") {
                    ElMessage({
                        message: "删除成功",
                        type: "success",
                    });
                    this.search = "";
                    this.load();
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        calCurrentNum(info) {
            this.havePeopleNum = 0;
            const beds = Array.isArray(info.beds) ? info.beds : [];
            this.havePeopleNum = beds.filter(b => b && b.occupantUsername).length;
        },
        onExpandChange(row, expandedRows) {
            const keys = Array.isArray(expandedRows)
                ? expandedRows.map(item => item && item.centerRoomId).filter(Boolean)
                : [];
            this.expandedRowKeys = keys;
            if (!row || !row.centerRoomId) {
                return;
            }
            if (keys.includes(row.centerRoomId)) {
                this.fetchRoomBeds(row);
            }
        },
        fetchRoomBeds(row) {
            row.bedLoading = true;
            request.get(`/room/beds/${row.centerRoomId}`).then((res) => {
                if (res.code === "0") {
                    const beds = Array.isArray(res.data) ? res.data : [];
                    row.beds = beds.length > 0 ? beds : this.buildEmptyBeds(row.maxCapacity);
                } else {
                    row.beds = this.buildEmptyBeds(row.maxCapacity);
                }
            }).finally(() => {
                row.bedLoading = false;
            });
        },
        plusIcon(bed, info) {
            //添加图标
            this.judge = false;
            this.bedNum = bed.bedNo;
            this.calCurrentNum(info);
            this.bedDialog = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                // 生拷贝
                this.form = {
                    centerRoomId: info.centerRoomId,
                    centerBuildingId: info.centerBuildingId,
                    floorNum: info.floorNum,
                    bedNo: bed.bedNo,
                    bedStudentUsername: "",
                };
            });
        },
        editIcon(bed, info) {
            //修改图标
            this.judge = true;
            this.bedNum = bed.bedNo;
            this.bedDialog = true;
            this.$nextTick(() => {
                this.$refs.form.resetFields();
                // 生拷贝
                this.form = {
                    centerRoomId: info.centerRoomId,
                    centerBuildingId: info.centerBuildingId,
                    floorNum: info.floorNum,
                    bedNo: bed.bedNo,
                    bedStudentUsername: bed.occupantUsername || "",
                };
            });
        },
        detailIcon(bed, info) {
            //查看床位所住的学生
            const stu = bed && bed.occupantUsername ? bed.occupantUsername : "";
            if (!stu) {
                return;
            }
            request.get("/stu/exist/" + stu).then((res) => {
                if (res.code === "0") {
                    this.stuInfoDialog = true;
                    this.$nextTick(() => {
                        this.$refs.form.resetFields();
                        // 生拷贝
                        this.form = JSON.parse(JSON.stringify(res.data));
                    });
                }
            });
        },
        addStuBed() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    const payload = {
                        centerRoomId: this.form.centerRoomId,
                        bedNo: this.form.bedNo,
                        studentUsername: this.form.bedStudentUsername,
                    };
                    request.put("/room/beds/assign", null, {params: payload}).then((res) => {
                        if (res.code === "0") {
                            ElMessage({
                                message: "新增成功",
                                type: "success",
                            });
                            this.search = "";
                            this.loading = true;
                            this.load();
                            this.bedDialog = false;
                        } else {
                            ElMessage({
                                message: res.msg,
                                type: "error",
                            });
                        }
                    }).catch((err) => {
                        ElMessage({
                            message: (err && err.message) ? err.message : "网络异常，分配失败",
                            type: "error",
                        });
                    });
                }
            });
        },
        editStuBed() {
            //修改
            this.$refs.form.validate((valid) => {
                if (valid) {
                    const payload = {
                        centerRoomId: this.form.centerRoomId,
                        bedNo: this.form.bedNo,
                        studentUsername: this.form.bedStudentUsername,
                    };
                    request.put("/room/beds/assign", null, {params: payload}).then((res) => {
                        if (res.code === "0") {
                            ElMessage({
                                message: "修改成功",
                                type: "success",
                            });
                            this.search = "";
                            this.loading = true;
                            this.load();
                            this.bedDialog = false;
                        } else {
                            ElMessage({
                                message: res.msg,
                                type: "error",
                            });
                        }
                    }).catch((err) => {
                        ElMessage({
                            message: (err && err.message) ? err.message : "网络异常，分配失败",
                            type: "error",
                        });
                    });
                }
            });
        },
        async deleteStuBed(bed, info) {
            const bedNo = bed && bed.bedNo ? bed.bedNo : null;
            if (!bedNo || !info || !info.centerRoomId) {
                return;
            }
            request.put("/room/beds/clear", null, {params: {centerRoomId: info.centerRoomId, bedNo}}).then((res) => {
                if (res.code === "0") {
                    ElMessage({
                        message: "删除成功",
                        type: "success",
                    });
                    this.search = "";
                    this.loading = true;
                    this.load();
                    this.bedDialog = false;
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        buildEmptyBeds(maxCapacity) {
            const max = Math.max(0, Number(maxCapacity || 0));
            const list = [];
            for (let i = 1; i <= max; i += 1) {
                list.push({bedNo: i, occupantUsername: null});
            }
            return list;
        },
        handleSizeChange(pageSize) {
            //改变每页个数
            this.pageSize = pageSize;
            this.load();
        },
        handleCurrentChange(pageNum) {
            //改变页码
            this.currentPage = pageNum;
            this.load();
        },
        openRecommendDialog() {
            this.recommendDialog = true;
            this.recommendList = [];
            this.recommendForm = {
                studentUsername: "",
                centerBuildingId: "",
                gender: "",
                grade: "",
                scheduleType: "",
                allergyPreference: "",
                dietPreference: "",
            };
        },
        fetchRecommendBeds() {
            if (!this.recommendForm.studentUsername) {
                ElMessage({
                    message: "请先输入学生学号",
                    type: "warning",
                });
                return;
            }
            if (!this.recommendForm.centerBuildingId) {
                ElMessage({
                    message: "请选择分店",
                    type: "warning",
                });
                return;
            }
            this.recommendLoading = true;
            const payload = {
                ...this.recommendForm,
                centerBuildingId: Number(this.recommendForm.centerBuildingId),
            };
            request.post("/room/recommendBed", payload).then((res) => {
                if (res.code === "0") {
                    this.recommendList = Array.isArray(res.data) ? res.data : [];
                } else {
                    ElMessage({
                        message: res.msg || "推荐失败",
                        type: "error",
                    });
                }
            }).catch((err) => {
                ElMessage({
                    message: (err && err.message) ? err.message : "推荐失败",
                    type: "error",
                });
            }).finally(() => {
                this.recommendLoading = false;
            });
        },
        applyRecommendBed(item) {
            if (!item || !item.centerRoomId || !item.bedNum) {
                ElMessage({
                    message: "推荐数据不完整，无法套用",
                    type: "warning",
                });
                return;
            }
            if (!this.recommendForm.studentUsername) {
                ElMessage({
                    message: "请先输入学生学号",
                    type: "warning",
                });
                return;
            }

            const payload = {
                centerRoomId: item.centerRoomId,
                bedNo: item.bedNum,
                studentUsername: this.recommendForm.studentUsername,
            };

            request.put("/room/beds/assign", null, {params: payload}).then((res) => {
                if (res.code === "0") {
                    ElMessage({
                        message: "套用推荐成功",
                        type: "success",
                    });
                    this.recommendDialog = false;
                    this.load();
                } else {
                    ElMessage({
                        message: res.msg || "套用失败",
                        type: "error",
                    });
                }
            }).catch((err) => {
                ElMessage({
                    message: (err && err.message) ? err.message : "套用失败",
                    type: "error",
                });
            });
        },
    },
};