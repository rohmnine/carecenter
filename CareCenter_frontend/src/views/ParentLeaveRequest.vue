<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>请假管理</el-breadcrumb-item>
      <el-breadcrumb-item>请假申请</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <!-- 操作栏 -->
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <el-button type="primary" @click="showSubmitDialog" icon="Plus">代学生请假</el-button>
          <el-button type="info" @click="loadMonthlyStats" icon="DataAnalysis">月度统计</el-button>
        </div>
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-select
            v-model="selectedStudentUsername"
            placeholder="选择学生"
            style="width: 160px;"
            @change="onStudentChange"
          >
            <el-option
              v-for="stu in studentOptions"
              :key="stu"
              :label="stu"
              :value="stu"
            />
          </el-select>
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            format="YYYY-MM"
            placeholder="选择日期"
            value-format="YYYY-MM"
            @change="onMonthChange"
            style="width: 160px;"
            :teleported="true"
          />
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;" @change="loadData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待审批" value="pending"></el-option>
            <el-option label="已批准" value="approved"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
          </el-select>
        </div>
      </div>

      <!-- 月度统计卡片 -->
      <el-row :gutter="20" v-if="showStats" style="margin-bottom: 20px;">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #409EFF;">{{ monthlyStats.totalRequests || 0 }}</div>
            <div class="stat-label">本月总请假</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #67C23A;">{{ monthlyStats.approvedCount || 0 }}</div>
            <div class="stat-label">已批准</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #E6A23C;">{{ monthlyStats.pendingCount || 0 }}</div>
            <div class="stat-label">待审批</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #F56C6C;">{{ monthlyStats.rejectedCount || 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #67C23A;">{{ monthlyStats.homeLeaveCount || 0 }}</div>
            <div class="stat-label">回家请假</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number" style="color: #E6A23C;">{{ monthlyStats.outLeaveCount || 0 }}</div>
            <div class="stat-label">外出请假</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 请假记录表格 -->
      <el-table :data="tableData" stripe border style="width: 100%" v-loading="loading">
        <el-table-column label="#" type="index" width="50" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.leaveType === 'home' ? 'success' : 'warning'">
              {{ scope.row.leaveType === 'home' ? '回家' : '外出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="审批状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="info">待审批</el-tag>
            <el-tag v-else-if="scope.row.status === 'approved'" type="success">已批准</el-tag>
            <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentReply" label="家长回复" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              size="small"
              type="success"
              @click="approveRequest(scope.row, 'approved')"
              v-if="scope.row.status === 'pending'">
              批准
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="approveRequest(scope.row, 'rejected')"
              v-if="scope.row.status === 'pending'">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin: 10px 0">
        <el-pagination
          v-model:currentPage="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 提交请假申请对话框 -->
    <el-dialog
      title="代学生提交请假申请"
      v-model="submitDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      :append-to-body="true">
      <el-form :model="leaveForm" :rules="rules" ref="leaveFormRef" label-width="100px">
        <el-form-item label="学生账号">
          <el-select v-model="leaveForm.studentUsername" style="width: 100%">
            <el-option
              v-for="stu in studentOptions"
              :key="stu"
              :label="stu"
              :value="stu"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-radio-group v-model="leaveForm.leaveType">
            <el-radio label="home">回家</el-radio>
            <el-radio label="out">外出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="leaveForm.startTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
            :teleported="true"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="leaveForm.endTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
            :teleported="true"
          />
        </el-form-item>
        <el-form-item label="目的地" prop="destination" v-if="leaveForm.leaveType === 'out'">
          <el-input v-model="leaveForm.destination" placeholder="请输入外出目的地" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone" v-if="leaveForm.leaveType === 'out'">
          <el-input v-model="leaveForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input type="textarea" v-model="leaveForm.reason" :rows="4" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLeaveRequest">确定提交</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      :title="approveAction === 'approved' ? '批准请假申请' : '拒绝请假申请'"
      v-model="approveDialogVisible"
      width="500px"
      :close-on-click-modal="false"
      :append-to-body="true">
      <el-form label-width="80px">
        <el-form-item label="学生">{{ currentApproveRow ? currentApproveRow.studentName : '' }}</el-form-item>
        <el-form-item label="类型">{{ currentApproveRow ? (currentApproveRow.leaveType === 'home' ? '回家' : '外出') : '' }}</el-form-item>
        <el-form-item label="回复">
          <el-input type="textarea" v-model="parentReply" :rows="3" placeholder="请输入回复内容（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveAction === 'approved' ? 'success' : 'danger'" @click="confirmApprove">
          {{ approveAction === 'approved' ? '确认批准' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="请假申请详情" v-model="detailDialogVisible" width="600px" :append-to-body="true">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="学生姓名">{{ currentDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          {{ currentDetail.leaveType === 'home' ? '回家' : '外出' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="外出目的地" v-if="currentDetail.leaveType === 'out'">
          {{ currentDetail.destination || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话" v-if="currentDetail.leaveType === 'out'">
          {{ currentDetail.contactPhone || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ currentDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag v-if="currentDetail.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="currentDetail.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentDetail.status === 'rejected'" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="家长回复">{{ currentDetail.parentReply || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUser } from '@/utils/sessionHelper'
import request from '@/utils/request'

export default {
  name: 'ParentLeaveRequest',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      selectedMonth: '',
      statusFilter: '',
      showStats: false,
      monthlyStats: {},
      submitDialogVisible: false,
      approveDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: null,
      currentApproveRow: null,
      approveAction: '',
      parentReply: '',
      studentOptions: [],
      selectedStudentUsername: '',
      leaveForm: {
        studentUsername: '',
        leaveType: 'home',
        startTime: null,
        endTime: null,
        destination: '',
        contactPhone: '',
        reason: ''
      },
      rules: {
        leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        destination: [
          { required: true, message: '请输入外出目的地', trigger: 'blur' },
          { min: 2, max: 100, message: '目的地长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        reason: [
          { required: true, message: '请输入请假原因', trigger: 'blur' },
          { min: 5, max: 500, message: '请假原因长度在 5 到 500 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    const user = getCurrentUser()
    const bound = Array.isArray(user?.studentUsernames)
      ? user.studentUsernames
      : ((user?.studentUsername || '').split(',').map(v => v.trim()).filter(Boolean))
    this.studentOptions = bound
    this.selectedStudentUsername = bound.length ? bound[0] : ''
    this.loadData()
  },
  methods: {
    loadData() {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      this.loading = true
      request.get(`/parent/leave/list/${user.username}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          month: this.selectedMonth,
          status: this.statusFilter,
          studentUsername: this.selectedStudentUsername
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.tableData = res.data.records
          this.total = res.data.total
        } else {
          this.$message.error(res.msg)
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('加载数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    loadMonthlyStats() {
      const user = getCurrentUser()
      if (!user) return
      const month = this.selectedMonth || ''
      request.get(`/parent/leave/monthly-stats/${user.username}`, {
        params: {
          month,
          studentUsername: this.selectedStudentUsername
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.monthlyStats = res.data
          this.showStats = true
        } else {
          this.$message.error(res.msg)
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('加载统计数据失败')
      })
    },
    onMonthChange() {
      this.loadData()
      if (this.showStats) {
        this.loadMonthlyStats()
      }
    },
    onStudentChange() {
      this.pageNum = 1
      this.loadData()
      if (this.showStats) {
        this.loadMonthlyStats()
      }
    },
    showSubmitDialog() {
      this.leaveForm = {
        studentUsername: this.selectedStudentUsername || (this.studentOptions[0] || ''),
        leaveType: 'home',
        startTime: null,
        endTime: null,
        destination: '',
        contactPhone: '',
        reason: ''
      }
      this.submitDialogVisible = true
    },
    submitLeaveRequest() {
      this.$refs.leaveFormRef.validate((valid) => {
        if (valid) {
          const user = getCurrentUser()
          if (!user) {
            this.$message.error('用户信息获取失败')
            return
          }
          const requestData = {
            parentUsername: user.username,
            studentUsername: this.leaveForm.studentUsername || this.selectedStudentUsername || (this.studentOptions[0] || ''),
            studentName: this.leaveForm.studentUsername || this.selectedStudentUsername || (this.studentOptions[0] || ''),
            leaveType: this.leaveForm.leaveType,
            startTime: this.leaveForm.startTime,
            endTime: this.leaveForm.endTime,
            destination: this.leaveForm.destination || '',
            contactPhone: this.leaveForm.contactPhone || user.phoneNum || '',
            reason: this.leaveForm.reason
          }
          request.post('/parent/leave/submit', requestData).then(res => {
            if (res.code === '0' || res.code === 0) {
              this.$message.success('请假申请提交成功')
              this.submitDialogVisible = false
              this.loadData()
            } else {
              this.$message.error(res.msg)
            }
          }).catch(err => {
            console.error(err)
            this.$message.error('提交失败')
          })
        }
      })
    },
    approveRequest(row, action) {
      this.currentApproveRow = row
      this.approveAction = action
      this.parentReply = ''
      this.approveDialogVisible = true
    },
    confirmApprove() {
      if (!this.currentApproveRow) return
      request.put('/parent/leave/approve', {
        id: this.currentApproveRow.id,
        status: this.approveAction,
        parentReply: this.parentReply || (this.approveAction === 'approved' ? '同意请假' : '不同意请假')
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success('操作成功')
          this.approveDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.msg)
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('操作失败')
      })
    },
    viewDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadData()
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 10px 0;
}
.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
:deep(.el-dialog__header) {
  background-color: #409EFF;
  color: white;
}
</style>