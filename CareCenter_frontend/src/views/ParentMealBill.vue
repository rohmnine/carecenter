<template>
  <div class="parent-meal-bill">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">
            <el-icon><Money /></el-icon> {{ getPageTitle() }}
          </span>
        </div>
      </template>

      <!-- Bill Summary Cards -->
      <el-row :gutter="20" style="margin-bottom: 20px;" v-if="bills.length > 0">
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #409EFF;">{{ bills.length }}</div>
            <div class="summary-label">总账单数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #67C23A;">{{ paidCount }}</div>
            <div class="summary-label">已缴费</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value" style="color: #F56C6C;">{{ unpaidCount }}</div>
            <div class="summary-label">待缴费</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Bills Table -->
      <el-table :data="bills" border stripe v-loading="loading" style="width: 100%;">
        <el-table-column prop="billMonth" label="账单月份" width="120" align="center">
          <template #default="scope">
            <el-tag>{{ scope.row.billMonth }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="100" align="center" />
        <el-table-column prop="boardingType" label="就餐类型" width="160" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.boardingType === 'meal_and_rest' ? 'warning' : 'info'" size="small">
              {{ getBoardingTypeLabel(scope.row.boardingType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="午餐" align="center">
          <el-table-column prop="lunchDays" label="次数" width="70" align="center" />
          <el-table-column prop="lunchPrice" label="单价(元)" width="90" align="center" />
        </el-table-column>
        <el-table-column label="晚餐" align="center">
          <el-table-column prop="dinnerDays" label="次数" width="70" align="center" />
          <el-table-column prop="dinnerPrice" label="单价(元)" width="90" align="center" />
        </el-table-column>
        <el-table-column prop="boardingFee" label="午休费(元)" width="100" align="center" />
        <el-table-column prop="leaveDeductDays" label="请假扣减天数" width="120" align="center">
          <template #default="scope">
            <el-tag type="success" size="small" v-if="scope.row.leaveDeductDays > 0">
              -{{ scope.row.leaveDeductDays }}天
            </el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额(元)" width="110" align="center">
          <template #default="scope">
            <span style="font-weight: bold; color: #E6A23C; font-size: 16px;">
              ¥{{ scope.row.totalAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="缴费状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="default">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <el-empty description="暂无账单记录" v-if="bills.length === 0 && !loading" />
    </el-card>

    <!-- Bill Detail Dialog -->
    <el-dialog v-model="billDetailVisible" title="账单详情" width="650px">
      <div v-if="billDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账单月份">{{ billDetail.billMonth }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ billDetail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ billDetail.studentUsername }}</el-descriptions-item>
          <el-descriptions-item label="就餐类型">
            <el-tag :type="billDetail.boardingType === 'meal_and_rest' ? 'warning' : 'info'" size="small">
              {{ getBoardingTypeLabel(billDetail.boardingType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="午餐预约次数">{{ billDetail.lunchDays }} 次</el-descriptions-item>
          <el-descriptions-item label="午餐单价">¥{{ billDetail.lunchPrice }}</el-descriptions-item>
          <el-descriptions-item label="晚餐预约次数">{{ billDetail.dinnerDays }} 次</el-descriptions-item>
          <el-descriptions-item label="晚餐单价">¥{{ billDetail.dinnerPrice }}</el-descriptions-item>
          <el-descriptions-item label="午餐+午休基础费">¥{{ billDetail.boardingFee }}</el-descriptions-item>
          <el-descriptions-item label="餐费小计(预约餐次)">¥{{ billDetail.mealTotal }}</el-descriptions-item>
          <el-descriptions-item label="请假扣减天数">
            <span style="color: #67C23A;">{{ billDetail.leaveDeductDays }} 天</span>
          </el-descriptions-item>
          <el-descriptions-item label="缴费状态">
            <el-tag :type="getStatusType(billDetail.status)">{{ getStatusLabel(billDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="应缴总金额" :span="2">
            <span style="font-size: 20px; font-weight: bold; color: #E6A23C;">¥{{ billDetail.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- Leave Records -->
        <div style="margin-top: 20px;" v-if="billLeaveRecords.length > 0">
          <h4 style="margin-bottom: 10px;">
            <el-icon><Calendar /></el-icon> 本月请假记录（已审批通过）
          </h4>
          <el-table :data="billLeaveRecords" border stripe size="small">
            <el-table-column prop="id" label="ID" width="60" align="center" />
            <el-table-column prop="startDate" label="开始日期" align="center">
              <template #default="scope">{{ formatDate(scope.row.startDate) }}</template>
            </el-table-column>
            <el-table-column prop="endDate" label="结束日期" align="center">
              <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="请假原因" />
          </el-table>
        </div>
        <el-empty description="本月无请假记录" v-else style="margin-top: 10px;" :image-size="60" />

        <!-- Fee Calculation Explanation -->
        <el-alert
          style="margin-top: 20px;"
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>费用计算说明</template>
          <template #default>
            <div style="line-height: 1.8;">
              <p>
                餐费小计 = 午餐预约次数 × 午餐单价 + 晚餐预约次数 × 晚餐单价<br/>
                应缴总金额 = 午餐+午休基础费 + 餐费小计 - 请假退费（20元/天）
              </p>
            </div>
          </template>
        </el-alert>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ParentMealBill from "@/assets/js/ParentMealBill";
export default ParentMealBill;
</script>

<style scoped>
.parent-meal-bill {
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