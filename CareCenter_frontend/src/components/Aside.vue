<template>
  <el-menu
      :default-active="this.path"
      router
      style="width: 200px; height:100%; min-height: calc(100vh - 40px)"
      unique-opened
  >
    <div style="display: flex;align-items: center;justify-content: center;padding: 11px 0;">
      <img alt="" src="@/assets/logo.png" style="width: 60px;">
    </div>
    <el-menu-item index="/home">
      <el-icon>
        <house/>
      </el-icon>
      <span>首页</span>
    </el-menu-item>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="2">
      <template #title>
        <el-icon>
          <user/>
        </el-icon>
        <span>用户管理</span>
      </template>
      <el-menu-item index="/stuInfo">学生信息</el-menu-item>
      <el-menu-item index="/parentInfo">家长信息</el-menu-item>
    </el-sub-menu>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="3">
      <template #title>
        <el-icon>
          <coin/>
        </el-icon>
        <span>门店管理</span>
      </template>
      <el-menu-item index="/buildingInfo">分店信息</el-menu-item>
      <el-menu-item index="/roomInfo">房间信息</el-menu-item>
    </el-sub-menu>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="4">
      <template #title>
        <el-icon>
          <message/>
        </el-icon>
        <span>信息管理</span>
      </template>
      <el-menu-item index="/noticeInfo">公告信息</el-menu-item>
    </el-sub-menu>
    <el-menu-item v-if="this.judgeIdentity()===2" index="/auditCenter">
      <el-icon>
        <pie-chart/>
      </el-icon>
      <span>审核中心</span>
    </el-menu-item>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="8">
      <template #title>
        <el-icon>
          <avatar/>
        </el-icon>
        <span>访客管理</span>
      </template>
      <el-menu-item index="/visitorInfo">来访登记</el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="6">
      <template #title>
        <el-icon>
          <food/>
        </el-icon>
        <span>菜谱管理</span>
      </template>
      <el-menu-item v-if="this.judgeIdentity()===2" index="/randomMenu">随机菜谱</el-menu-item>
      <el-menu-item index="/weeklyMenu">每周菜谱</el-menu-item>
      <el-menu-item v-if="this.judgeIdentity()===2" index="/dailyMenuPhotoAdmin">每日菜品上传</el-menu-item>
      <el-menu-item v-if="this.judgeIdentity()===3 || this.judgeIdentity()===0" index="/dailyMenuPhotoView">每日菜品查看</el-menu-item>
      <el-menu-item v-if="this.judgeIdentity()===3 || this.judgeIdentity()===0" index="/dishRatingFeedback">菜品建议</el-menu-item>
      <el-menu-item v-if="this.judgeIdentity()===2" index="/dishSuggestionManagement">建议管理</el-menu-item>
    </el-sub-menu>
    <el-menu-item v-if="this.judgeIdentity()===0" index="/myRoomInfo">
      <el-icon>
        <school/>
      </el-icon>
      <span>我的房间</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===0" index="/studentLeaveRequest">
      <el-icon>
        <document-checked/>
      </el-icon>
      <span>外出申请</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentNotifications">
      <el-icon>
        <bell/>
      </el-icon>
      <span>通知中心</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentDashboard">
      <el-icon>
        <data-line/>
      </el-icon>
      <span>学生仪表盘</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentLeaveRequest">
      <el-icon>
        <document-checked/>
      </el-icon>
      <span>请假管理</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentVisitorAppointment">
      <el-icon>
        <avatar/>
      </el-icon>
      <span>访客预约</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentBedSelection">
      <el-icon>
        <school/>
      </el-icon>
      <span>床位更换</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentMealBill">
      <el-icon>
        <money/>
      </el-icon>
      <span>餐费账单</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentMealReservation">
      <el-icon>
        <bowl/>
      </el-icon>
      <span>就餐预约</span>
    </el-menu-item>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="7">
      <template #title>
        <el-icon>
          <notification/>
        </el-icon>
        <span>外出管理</span>
      </template>
      <el-menu-item index="/adminUnauthorizedNotification">未授权外出通知</el-menu-item>
    </el-sub-menu>
    <el-sub-menu v-if="this.judgeIdentity()===2" index="9">
      <template #title>
        <el-icon>
          <money/>
        </el-icon>
        <span>缴费管理</span>
      </template>
      <el-menu-item index="/mealBillingAdmin">账单与缴费管理</el-menu-item>
    </el-sub-menu>
    <el-menu-item v-if="this.judgeIdentity()===2" index="/entryExitManagement">
      <el-icon>
        <clock/>
      </el-icon>
      <span>进出登记</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===0" index="/studentEntryExitView">
      <el-icon>
        <clock/>
      </el-icon>
      <span>进出记录</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===3" index="/parentEntryExitView">
      <el-icon>
        <clock/>
      </el-icon>
      <span>进出记录</span>
    </el-menu-item>
    <el-menu-item v-if="this.judgeIdentity()===2 || this.judgeIdentity()===3 || this.judgeIdentity()===0" index="/discussionGroup">
      <el-icon>
        <chat-dot-round/>
      </el-icon>
      <span>讨论组</span>
    </el-menu-item>
    <el-menu-item index="/selfInfo">
      <el-icon>
        <setting/>
      </el-icon>
      <span>个人信息</span>
    </el-menu-item>
  </el-menu>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import { setCurrentUser, getCurrentIdentity } from "@/utils/sessionHelper";

export default {
  name: "Aside",
  data() {
    return {
      user: {},
      identity: '',
      path: this.$route.path
    }
  },
  created(){
    this.init()
  },
  watch: {
    $route(to) {
      this.path = to.path
    }
  },
  methods: {
    init() {
      // Load identity first, then load user info to ensure proper sequencing
      request.get("/main/loadIdentity").then((res) => {
        if (res.code !== "0") {
          ElMessage({
            message: '用户会话过期',
            type: 'error',
          });
          sessionStorage.clear()
          request.get("/main/signOut");
          return;
        }
        window.sessionStorage.setItem("identity", JSON.stringify(res.data));
        this.identity = res.data

        // Ensure currentIdentity is set (may be missing after refresh)
        if (!window.sessionStorage.getItem('currentIdentity')) {
          window.sessionStorage.setItem('currentIdentity', res.data);
        }

        // Now load user info
        request.get("/main/loadUserInfo").then((result) => {
          if (result.code !== "0") {
            ElMessage({
              message: '用户会话过期',
              type: 'error',
            });
            request.get("/main/signOut");
            sessionStorage.clear()
            this.$router.replace({path: "/login"});
            return;
          }
          window.sessionStorage.setItem("user", JSON.stringify(result.data));
          this.user = result.data
          // Sync to sessionHelper storage so discussion group etc. can find user info
          const currentId = window.sessionStorage.getItem('currentIdentity');
          if (currentId) {
            setCurrentUser(result.data, currentId);
          }
        });
      });
    },
    judgeIdentity() {
      if (this.identity === 'stu') {
        return 0
      } else if (this.identity === 'parent') {
        return 3
      } else if (this.identity === 'admin') {
        return 2
      }
      return 2
    }
  },
}
</script>

<style scoped>
.icon {
  margin-right: 6px;
}

.el-sub-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 45px;
  min-width: 199px;
}
</style>