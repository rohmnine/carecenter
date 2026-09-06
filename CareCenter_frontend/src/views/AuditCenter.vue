<template>
  <div style="padding: 10px">
    <el-card shadow="never">
      <template #header>
        <div style="font-size: 16px; font-weight: bold;">审核中心</div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="请假审核" name="leave">
          <AdminLeaveRequestManagement />
        </el-tab-pane>
        <el-tab-pane label="访客审核" name="visitor">
          <AdminVisitorAppointment />
        </el-tab-pane>
        <el-tab-pane label="订餐审核" name="meal">
          <MealReservationAudit />
        </el-tab-pane>
        <el-tab-pane label="床位审核" name="bed">
          <AdminBedSelectionAudit />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import AdminLeaveRequestManagement from '@/views/AdminLeaveRequestManagement'
import AdminVisitorAppointment from '@/views/AdminVisitorAppointment'
import MealReservationAudit from '@/views/MealReservationAudit'
import AdminBedSelectionAudit from '@/views/AdminBedSelectionAudit'

export default {
  name: 'AuditCenter',
  components: {
    AdminLeaveRequestManagement,
    AdminVisitorAppointment,
    MealReservationAudit,
    AdminBedSelectionAudit
  },
  data() {
    return {
      activeTab: 'leave'
    }
  }
}
</script>