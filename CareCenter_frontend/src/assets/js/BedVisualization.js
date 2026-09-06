import request from "@/utils/request";

const { ElMessage } = require("element-plus");

export default {
  name: "BedVisualization",
  data() {
    const checkStuNum = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入学号"));
        return;
      }
      request.get("/stu/exist/" + value).then((res) => {
        if (res.code === "0") {
          request.get("/room/judgeHadBed/" + value).then((result) => {
            if (result.code === "0") {
              callback();
            } else {
              callback(new Error("该学生已有床位"));
            }
          });
        } else {
          callback(new Error("学生不存在"));
        }
      });
    };

    return {
      searchRoomId: "",
      currentRoom: null,
      bedDialog: false,
      dialogTitle: "",
      isViewMode: false,
      currentBedNum: 0,
      currentBedPosition: "",
      currentBedStudent: null,
      studentInfo: null,
      // 下铺学生信息（模拟数据，实际应从数据库获取）
      lowerBeds: [null, null, null, null],
      bedForm: {
        centerRoomId: "",
        bedNumber: "",
        bedDisplay: "",
        studentId: "",
      },
      bedRules: {
        studentId: [{ validator: checkStuNum, trigger: "blur" }],
      },
    };
  },
  methods: {
    // 加载房间信息
    async loadRoom() {
      if (!this.searchRoomId) {
        ElMessage.warning("请输入房间号");
        return;
      }

      try {
        const res = await request.get("/room/checkRoomExist/" + this.searchRoomId);
        if (res.code === "0") {
          this.currentRoom = res.data;
          ElMessage.success("房间加载成功");
        } else {
          ElMessage.error("房间不存在");
          this.currentRoom = null;
        }
      } catch (error) {
        ElMessage.error("加载房间信息失败");
        this.currentRoom = null;
      }
    },

    // 重置
    reset() {
      this.searchRoomId = "";
      this.currentRoom = null;
      this.lowerBeds = [null, null, null, null];
    },

    // 获取床位状态样式
    getBedStatus(bedNum, position) {
      if (!this.currentRoom) return "empty";

      let hasStudent = false;
      if (position === "upper") {
        // 上铺
        if (bedNum === 1) hasStudent = !!this.currentRoom.firstBed;
        else if (bedNum === 2) hasStudent = !!this.currentRoom.secondBed;
        else if (bedNum === 3) hasStudent = !!this.currentRoom.thirdBed;
        else if (bedNum === 4) hasStudent = !!this.currentRoom.fourthBed;
      } else {
        // 下铺
        hasStudent = !!this.lowerBeds[bedNum - 1];
      }

      return hasStudent ? "occupied" : "empty";
    },

    // 显示床位提示
    showBedTooltip(bedNum, position) {
      // 可以在这里实现悬停提示功能
    },

    // 隐藏提示
    hideTooltip() {
      // 隐藏提示
    },

    // 处理床位点击
    async handleBedClick(bedNum, position) {
      if (!this.currentRoom) return;

      this.currentBedNum = bedNum;
      this.currentBedPosition = position;

      // 获取床位学生信息
      let studentId = null;
      if (position === "upper") {
        if (bedNum === 1) studentId = this.currentRoom.firstBed;
        else if (bedNum === 2) studentId = this.currentRoom.secondBed;
        else if (bedNum === 3) studentId = this.currentRoom.thirdBed;
        else if (bedNum === 4) studentId = this.currentRoom.fourthBed;
      } else {
        studentId = this.lowerBeds[bedNum - 1];
      }

      this.currentBedStudent = studentId;

      // 设置表单数据
      this.bedForm.centerRoomId = this.currentRoom.centerRoomId;
      this.bedForm.bedNumber = bedNum;
      this.bedForm.bedDisplay = `${bedNum}号床 (${position === "upper" ? "上铺" : "下铺"})`;
      this.bedForm.studentId = studentId || "";

      if (studentId) {
        // 查看学生信息
        await this.loadStudentInfo(studentId);
        this.dialogTitle = "床位信息";
        this.isViewMode = true;
      } else {
        // 分配床位
        this.dialogTitle = "分配床位";
        this.isViewMode = false;
        this.studentInfo = null;
      }

      this.bedDialog = true;
    },

    // 加载学生信息
    async loadStudentInfo(studentId) {
      try {
        const res = await request.get("/stu/exist/" + studentId);
        if (res.code === "0") {
          this.studentInfo = res.data;
        } else {
          this.studentInfo = null;
        }
      } catch (error) {
        this.studentInfo = null;
      }
    },

    // 分配床位
    async assignBed() {
      this.$refs.bedForm.validate(async (valid) => {
        if (valid) {
          try {
            // 检查房间是否满员
            const roomCheck = await request.get("/room/checkRoomState/" + this.currentRoom.centerRoomId);
            if (roomCheck.code !== "0") {
              ElMessage.error("该房间已满员");
              return;
            }

            // 检查床位是否有人
            const bedCheck = await request.get(
              "/room/checkBedState/" + this.currentRoom.centerRoomId + "/" + this.currentBedNum
            );
            if (bedCheck.code !== "0") {
              ElMessage.error("该床位已有人");
              return;
            }

            // 更新房间信息
            const updateData = { ...this.currentRoom };
            updateData.currentCapacity = this.currentRoom.currentCapacity + 1;

            if (this.currentBedPosition === "upper") {
              if (this.currentBedNum === 1) updateData.firstBed = this.bedForm.studentId;
              else if (this.currentBedNum === 2) updateData.secondBed = this.bedForm.studentId;
              else if (this.currentBedNum === 3) updateData.thirdBed = this.bedForm.studentId;
              else if (this.currentBedNum === 4) updateData.fourthBed = this.bedForm.studentId;
            } else {
              // 下铺需要额外处理（如果系统支持）
              this.lowerBeds[this.currentBedNum - 1] = this.bedForm.studentId;
            }

            const res = await request.put("/room/update", updateData);
            if (res.code === "0") {
              ElMessage.success("床位分配成功");
              this.bedDialog = false;
              await this.loadRoom();
            } else {
              ElMessage.error(res.msg || "分配失败");
            }
          } catch (error) {
            ElMessage.error("分配床位失败");
          }
        }
      });
    },

    // 更新床位
    async updateBed() {
      this.$refs.bedForm.validate(async (valid) => {
        if (valid) {
          try {
            const updateData = { ...this.currentRoom };

            if (this.currentBedPosition === "upper") {
              if (this.currentBedNum === 1) updateData.firstBed = this.bedForm.studentId;
              else if (this.currentBedNum === 2) updateData.secondBed = this.bedForm.studentId;
              else if (this.currentBedNum === 3) updateData.thirdBed = this.bedForm.studentId;
              else if (this.currentBedNum === 4) updateData.fourthBed = this.bedForm.studentId;
            } else {
              this.lowerBeds[this.currentBedNum - 1] = this.bedForm.studentId;
            }

            const res = await request.put("/room/update", updateData);
            if (res.code === "0") {
              ElMessage.success("床位更新成功");
              this.bedDialog = false;
              await this.loadRoom();
            } else {
              ElMessage.error(res.msg || "更新失败");
            }
          } catch (error) {
            ElMessage.error("更新床位失败");
          }
        }
      });
    },

    // 移除学生
    async removeBed() {
      try {
        let bedName = "";
        if (this.currentBedPosition === "upper") {
          if (this.currentBedNum === 1) bedName = "first_bed";
          else if (this.currentBedNum === 2) bedName = "second_bed";
          else if (this.currentBedNum === 3) bedName = "third_bed";
          else if (this.currentBedNum === 4) bedName = "fourth_bed";

          const currentCapacity = this.currentRoom.currentCapacity - 1;
          const res = await request.delete(
            "/room/delete/" + bedName + "/" + this.currentRoom.centerRoomId + "/" + currentCapacity
          );

          if (res.code === "0") {
            ElMessage.success("移除成功");
            this.bedDialog = false;
            await this.loadRoom();
          } else {
            ElMessage.error(res.msg || "移除失败");
          }
        } else {
          // 下铺移除
          this.lowerBeds[this.currentBedNum - 1] = null;
          ElMessage.success("移除成功");
          this.bedDialog = false;
        }
      } catch (error) {
        ElMessage.error("移除学生失败");
      }
    },

    // 取消对话框
    cancelBedDialog() {
      this.bedDialog = false;
      this.bedForm = {
        centerRoomId: "",
        bedNumber: "",
        bedDisplay: "",
        studentId: "",
      };
      this.studentInfo = null;
      this.isViewMode = false;
      this.$refs.bedForm && this.$refs.bedForm.resetFields();
    },
  },
};