<template>
  <div style="padding: 10px">
    <el-card shadow="never">
      <template #header>
        <div style="font-size: 16px; font-weight: bold;">家长就餐预约审核</div>
      </template>

      <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap">
        <el-input v-model="search" placeholder="搜索学生姓名/家长账号" style="width: 220px" clearable />
        <el-input v-model="month" placeholder="月份 (如: 2026-03)" style="width: 180px" clearable />
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-button type="primary" @click="searchReservations">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" :disabled="selectedIds.length === 0" @click="batchApprove">
          批量通过
        </el-button>
        <span style="color: #909399; font-size: 12px;">已选 {{ selectedIds.length }} 条</span>
      </div>

      <el-table :data="reservations" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="parentUsername" label="家长账号" width="120" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="110" align="center" />
        <el-table-column prop="reservationDate" label="预约日期" width="130" align="center" />
        <el-table-column prop="mealType" label="餐次" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getMealTypeTag(scope.row.mealType)" size="small">
              {{ getMealTypeLabel(scope.row.mealType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
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
            <el-tag v-else-if="canConfirmMeal(scope.row)" type="info" size="small">
              未确认
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'pending'"
              type="success"
              size="small"
              @click="approveReservation(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              type="danger"
              size="small"
              @click="rejectReservation(scope.row)"
            >
              驳回
            </el-button>
            <!-- Confirm meal buttons (only for approved/reserved status) -->
            <el-button
              v-if="canConfirmMeal(scope.row) && !isMealConfirmed(scope.row)"
              type="primary"
              size="small"
              @click="confirmMeal(scope.row)"
            >
              确认就餐
            </el-button>
            <el-button
              v-if="canConfirmMeal(scope.row) && isMealConfirmed(scope.row)"
              type="warning"
              size="small"
              @click="unconfirmMeal(scope.row)"
            >
              取消确认
            </el-button>
            <span v-if="scope.row.status !== 'pending' && !canConfirmMeal(scope.row)" style="color: #909399;">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 15px; text-align: center"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
      />

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #409EFF; font-weight: bold;">{{ stats.total || 0 }}</div>
            <div style="color: #909399; margin-top: 5px;">当前页总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #E6A23C; font-weight: bold;">{{ stats.pending || 0 }}</div>
            <div style="color: #909399; margin-top: 5px;">待审核</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #67C23A; font-weight: bold;">{{ stats.approved || 0 }}</div>
            <div style="color: #909399; margin-top: 5px;">已通过</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" style="text-align: center;">
            <div style="font-size: 24px; color: #F56C6C; font-weight: bold;">{{ stats.rejected || 0 }}</div>
            <div style="color: #909399; margin-top: 5px;">已驳回</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import MealReservationAudit from "@/assets/js/MealReservationAudit";
export default MealReservationAudit;
</script>