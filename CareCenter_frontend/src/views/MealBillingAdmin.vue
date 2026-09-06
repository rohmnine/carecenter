<template>
  <div style="padding: 10px">
    <!-- Statistics Cards -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div><span>总账单数</span></div>
          <div style="font-size: 24px; color: #409EFF; text-align: center">{{ statistics.totalBills || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div><span>已缴费</span></div>
          <div style="font-size: 24px; color: #67C23A; text-align: center">{{ statistics.paidCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div><span>未缴费</span></div>
          <div style="font-size: 24px; color: #F56C6C; text-align: center">{{ statistics.unpaidCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div><span>总金额(元)</span></div>
          <div style="font-size: 24px; color: #E6A23C; text-align: center">{{ statistics.totalAmount || '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap">
        <el-input v-model="billSearch" placeholder="搜索学生姓名/学号" style="width: 200px" clearable />
        <el-input v-model="billMonth" placeholder="月份 (如: 2026-02)" style="width: 180px" clearable />
        <el-select v-model="billStatus" placeholder="缴费状态" clearable style="width: 140px">
          <el-option label="已缴费" value="paid" />
          <el-option label="未缴费" value="unpaid" />
        </el-select>
        <el-button type="primary" @click="searchBills">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="showGenerateDialog">生成账单</el-button>

        <!-- Added buttons after generate -->
        <el-button type="primary" @click="batchMarkPaid" :disabled="!isAllSelected && selectedBillRows.length === 0">批量标记已缴费</el-button>
        <el-button type="warning" @click="batchSendReminder" :disabled="!isAllSelected && selectedBillRows.length === 0">批量发送提醒</el-button>
        <el-button type="success" @click="exportMealExcel">导出Excel</el-button>
        <el-upload
          action=""
          :auto-upload="false"
          :on-change="handleMealFileChange"
          :show-file-list="false"
          accept=".xlsx,.xls"
        >
          <el-button type="primary">导入Excel</el-button>
        </el-upload>
      </div>

      <el-table ref="billTable" :data="bills" border stripe row-key="id" @selection-change="handleBillSelectionChange">
        <el-table-column type="selection" width="55" :reserve-selection="true" />
        <el-table-column prop="studentUsername" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="billMonth" label="账单月份" width="110" />
        <el-table-column label="就餐类型" width="170">
          <template #default="scope">
            <el-tag size="small" :type="getBoardingTypeTag(scope.row.boardingType)">
              {{ getBoardingTypeLabel(scope.row.boardingType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lunchDays" label="午餐次数" width="90" />
        <el-table-column prop="dinnerDays" label="晚餐次数" width="90" />
        <el-table-column prop="leaveDeductDays" label="请假天数" width="90" />
        <el-table-column prop="totalAmount" label="总金额(元)" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="info" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 'unpaid'" type="success" size="small" @click="markAsPaid(scope.row)">确认缴费</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Cross-page selection notification bar -->
      <div v-if="isAllSelected" style="margin-top: 10px; padding: 8px 12px; background: #ecf5ff; border: 1px solid #b3d8ff; border-radius: 4px; display: flex; align-items: center; justify-content: space-between;">
        <span style="color: #409EFF; font-size: 13px;">
          已勾选全部 <b>{{ total }}</b> 条账单（跨所有页面）
        </span>
        <el-button type="text" size="small" @click="clearAllSelection" style="color: #F56C6C;">取消全选</el-button>
      </div>

      <el-pagination
        style="margin-top: 15px; text-align: center"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        :total="total"
      />
    </el-card>

    <!-- Generate Bills Dialog -->
    <el-dialog v-model="generateDialogVisible" title="生成月度账单" width="400px">
      <el-form label-width="100px">
        <el-form-item label="账单月份">
          <el-input v-model="generateMonth" placeholder="格式: 2026-02" style="width: 100%" />
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 12px">
            新规则：午餐+午休固定500元/月，晚餐20元/次，请假20元/天退费。
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateBills">生成</el-button>
      </template>
    </el-dialog>

    <!-- Bill Detail Dialog -->
    <el-dialog v-model="billDetailVisible" title="账单详情" width="600px">
      <div v-if="billDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ billDetail.studentUsername }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ billDetail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="账单月份">{{ billDetail.billMonth }}</el-descriptions-item>
          <el-descriptions-item label="就餐类型">{{ getBoardingTypeLabel(billDetail.boardingType) }}</el-descriptions-item>
          <el-descriptions-item label="午餐预约次数">{{ billDetail.lunchDays }} 次</el-descriptions-item>
          <el-descriptions-item label="午餐单价">{{ billDetail.lunchPrice }} 元/次</el-descriptions-item>
          <el-descriptions-item label="晚餐次数">{{ billDetail.dinnerDays }} 次</el-descriptions-item>
          <el-descriptions-item label="晚餐单价">{{ billDetail.dinnerPrice }} 元/次</el-descriptions-item>
          <el-descriptions-item label="午餐+午休基础费">{{ billDetail.boardingFee }} 元</el-descriptions-item>
          <el-descriptions-item label="请假退费天数">{{ billDetail.leaveDeductDays }} 天</el-descriptions-item>
          <el-descriptions-item label="餐费小计(预约餐次)">{{ billDetail.mealTotal }} 元</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span style="color: #F56C6C; font-size: 18px; font-weight: bold">{{ billDetail.totalAmount }} 元</span>
          </el-descriptions-item>
          <el-descriptions-item label="缴费状态">
            <el-tag :type="getStatusType(billDetail.status)">{{ getStatusLabel(billDetail.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="billLeaveRecords.length > 0" style="margin-top: 20px">
          <h4>请假记录</h4>
          <el-table :data="billLeaveRecords" border size="small">
            <el-table-column prop="leaveType" label="类型" width="80">
              <template #default="scope">
                {{ scope.row.leaveType === 'home' ? '回家' : '外出' }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" />
            <el-table-column label="开始时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="结束时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.endTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else style="margin-top: 20px; color: #909399">
          本月无请假记录
        </div>
      </div>
    </el-dialog>

    <!-- Edit Bill Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑月度消费记录" width="760px">
      <el-form :model="editForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="editForm.studentUsername" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="editForm.studentName" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账单月份">
              <el-input v-model="editForm.billMonth" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="就餐类型">
              <el-select v-model="editForm.boardingType" style="width: 100%">
                <el-option label="仅午餐(走读)" value="meal_only" />
                <el-option label="午餐+午休(月度固定)" value="meal_and_rest" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">消费记录与费用</el-divider>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="午餐次数">
              <el-input-number v-model="editForm.lunchDays" :min="0" :step="1" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="晚餐次数">
              <el-input-number v-model="editForm.dinnerDays" :min="0" :step="1" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="请假天数">
              <el-input-number v-model="editForm.leaveDeductDays" :min="0" :step="1" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="午餐单价(元)">
              <el-input-number v-model="editForm.lunchPrice" :min="0" :step="1" :precision="2" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="晚餐单价(元)">
              <el-input-number v-model="editForm.dinnerPrice" :min="0" :step="1" :precision="2" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="基础费(元)">
              <el-input-number v-model="editForm.boardingFee" :min="0" :step="1" :precision="2" style="width: 100%" @change="recalculateEditTotalAmount" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="总金额(元)">
          <el-input-number
            v-model="editForm.totalAmount"
            :min="0"
            :step="1"
            :precision="2"
            style="width: 220px"
            :controls="false"
            disabled
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">
            根据“基础费 + 餐次费用 - 请假退费”自动计算
          </span>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="可选，记录人工调整原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MealBillingAdmin from '@/assets/js/MealBillingAdmin.js'
export default MealBillingAdmin
</script>