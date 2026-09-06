<template>
  <div class="parent-meal-reservation">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">
            <el-icon><Calendar /></el-icon> 就餐预约
          </span>
          <div>
            <el-select
              v-model="selectedStudentUsername"
              placeholder="选择学生"
              style="width: 180px; margin-right: 10px;"
              @change="studentChanged"
            >
              <el-option
                v-for="stu in studentOptions"
                :key="stu"
                :label="stu"
                :value="stu"
              />
            </el-select>
            <el-date-picker
              v-model="currentMonth"
              type="month"
              value-format="YYYY-MM"
              format="YYYY-MM"
              placeholder="选择日期"
              style="width: 180px; margin-right: 10px;"
              :teleported="true"
              @change="monthChanged"
            />
            <el-button type="primary" @click="openReserveDialog">
              <el-icon><Plus /></el-icon> 新增预约
            </el-button>
          </div>
        </div>
      </template>

      <!-- Statistics Cards -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #409EFF;">{{ monthStats.totalCount }}</div>
            <div class="summary-label">本月预约总数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #E6A23C;">{{ monthStats.lunchCount }}</div>
            <div class="summary-label">午餐预约</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #67C23A;">{{ monthStats.dinnerCount }}</div>
            <div class="summary-label">晚餐预约</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Reservations Table -->
      <el-table :data="reservations" border stripe v-loading="loading" style="width: 100%;">
        <el-table-column prop="reservationDate" label="预约日期" width="130" align="center">
          <template #default="scope">
            <el-tag>{{ formatDate(scope.row.reservationDate) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mealType" label="餐次" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getMealTypeTag(scope.row.mealType)" size="small">
              {{ getMealTypeLabel(scope.row.mealType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否就餐" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="isMealConfirmed(scope.row)" type="success" size="small">
              已就餐
            </el-tag>
            <el-tag v-else-if="scope.row.status === 'approved' || scope.row.status === 'reserved'" type="info" size="small">
              未确认
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" width="180" align="center">
          <template #default="scope">
            {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString('zh-CN') : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="canCancel(scope.row)"
              type="danger"
              size="small"
              link
              @click="cancelReservation(scope.row)"
            >
              <el-icon><Close /></el-icon> 取消
            </el-button>
            <span v-else style="color: #909399; font-size: 12px;">
              {{ scope.row.status === 'cancelled' ? '已取消' : '已过期' }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <el-empty description="本月暂无预约记录" v-if="reservations.length === 0 && !loading" />

      <!-- Info Alert -->
      <el-alert
        style="margin-top: 20px;"
        type="info"
        :closable="false"
        show-icon
      >
        <template #title>预约说明</template>
        <template #default>
          <div style="line-height: 1.8;">
            <p>1. 家长可以为孩子预约额外的午餐或晚餐。</p>
            <p>2. 预约的餐次费用将自动计入当月餐费账单。</p>
            <p>3. 不能预约过去的日期，已过期的预约无法取消。</p>
            <p>4. 相同日期相同餐次不可重复预约。</p>
          </div>
        </template>
      </el-alert>
    </el-card>

    <!-- New Reservation Dialog -->
    <el-dialog v-model="reserveDialogVisible" title="新增就餐预约" width="500px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="学生账号">
          <el-select v-model="reserveForm.studentUsername" style="width: 100%">
            <el-option
              v-for="stu in studentOptions"
              :key="stu"
              :label="stu"
              :value="stu"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="reserveForm.mealType" style="width: 100%">
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="reserveForm.dateInput"
            type="date"
            format="YYYY-MM-DD"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :teleported="true"
            :disabled-date="disablePastDates"
            @change="addReserveDate"
            clearable
          />
        </el-form-item>
        <el-form-item label="已选日期">
          <div style="display: flex; flex-wrap: wrap; gap: 6px;">
            <el-tag
              v-for="date in reserveForm.dates"
              :key="date"
              closable
              @close="removeReserveDate(date)"
            >
              {{ date }}
            </el-tag>
            <span v-if="reserveForm.dates.length === 0" style="color: #909399; font-size: 12px;">
              暂无已选日期
            </span>
          </div>
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 12px;">
            提示: 点击日期添加到列表，点击标签右侧可取消
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import ParentMealReservation from "@/assets/js/ParentMealReservation";
export default ParentMealReservation;
</script>

<style scoped>
.parent-meal-reservation {
  padding: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.summary-card {
  text-align: center;
  padding: 10px 0;
}
.summary-value {
  font-size: 28px;
  font-weight: bold;
}
.summary-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}
</style>