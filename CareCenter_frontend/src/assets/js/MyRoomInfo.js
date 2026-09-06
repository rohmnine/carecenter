import request from "@/utils/request";
import { getCurrentUser } from "@/utils/sessionHelper";

const {ElMessage} = require("element-plus");

export default {
    name: "MyRoomInfo",
    data() {
        return {
            name: "",
            form: {
                username: "",
            },
            room: {
                centerRoomId: "",
                centerBuildingId: "",
                floorNum: "",
                maxCapacity: "",
                currentCapacity: "",
                firstBed: "",
                secondBed: "",
                thirdBed: "",
                fourthBed: "",
            },
            bedList: [],
        };
    },
    created() {
        this.init();
        this.getInfo();
    },
    methods: {
        init() {
            this.form = getCurrentUser();
            this.name = this.form.username;
        },
        getInfo() {
            request.get("/room/getMyRoom/" + this.name).then((res) => {
                if (res.code === "0") {
                    this.room = res.data;
                    this.loadBeds();
                } else {
                    ElMessage({
                        message: res.msg,
                        type: "error",
                    });
                }
            });
        },
        loadBeds() {
            if (!this.room || !this.room.centerRoomId) {
                this.bedList = [];
                return;
            }
            request.get("/room/beds/" + this.room.centerRoomId).then((res) => {
                if (res.code === "0") {
                    const apiBeds = Array.isArray(res.data) ? res.data : [];
                    if (apiBeds.length > 0) {
                        this.bedList = apiBeds.map(item => ({
                            bedNo: item.bedNo,
                            occupantUsername: item.occupantUsername,
                        }));
                    } else {
                        this.buildLegacyBeds();
                    }
                } else {
                    this.buildLegacyBeds();
                }
            }).catch(() => {
                this.buildLegacyBeds();
            });
        },
        buildLegacyBeds() {
            const max = Math.max(0, Number(this.room.maxCapacity || 0));
            const legacyMap = {
                1: this.room.firstBed || null,
                2: this.room.secondBed || null,
                3: this.room.thirdBed || null,
                4: this.room.fourthBed || null,
            };
            this.bedList = Array.from({length: max}, (_, index) => {
                const bedNo = index + 1;
                return {
                    bedNo,
                    occupantUsername: legacyMap[bedNo] || null,
                };
            });
        },
    },
};
