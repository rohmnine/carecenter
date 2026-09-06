<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>数据中心</el-breadcrumb-item>
      <el-breadcrumb-item>学生仪表盘</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)" v-loading="loading">
      <!-- 月份选择 -->
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 18px; font-weight: bold; color: #303133;">
            {{ studentName }} 的月度报告
          </span>
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
            @change="loadDashboard"
            style="width: 160px;"
            :teleported="true"
          />
        </div>
      </div>

      <!-- 概览统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card" style="border-left: 4px solid #409EFF;">
            <div class="overview-icon" style="background: #ECF5FF;">
              <el-icon :size="28" color="#409EFF"><Calendar /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ dashboard.daysElapsed || 0 }} / {{ dashboard.daysInMonth || 0 }}</div>
              <div class="overview-label">已过天数 / 本月天数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card" style="border-left: 4px solid #67C23A;">
            <div class="overview-icon" style="background: #F0F9EB;">
              <el-icon :size="28" color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ dashboard.attendanceRate || 0 }}%</div>
              <div class="overview-label">出勤率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card" style="border-left: 4px solid #E6A23C;">
            <div class="overview-icon" style="background: #FDF6EC;">
              <el-icon :size="28" color="#E6A23C"><Document /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ leaveStats.totalRequests || 0 }}</div>
              <div class="overview-label">本月请假次数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card" style="border-left: 4px solid #909399;">
            <div class="overview-icon" style="background: #F4F4F5;">
              <el-icon :size="28" color="#909399"><Switch /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-number">{{ entryExitStats.totalRecords || 0 }}</div>
              <div class="overview-label">进出记录数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 就餐预约统计 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center;">
                <el-icon :size="20" color="#67C23A" style="margin-right: 8px;"><Calendar /></el-icon>
                <span style="font-weight: bold;">就餐预约统计</span>
              </div>
            </template>
            <el-row :gutter="10">
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">本月预约总数</div>
                  <div class="detail-value" style="color: #409EFF;">{{ mealReservationStats.totalCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">午餐预约</div>
                  <div class="detail-value" style="color: #E6A23C;">{{ mealReservationStats.lunchCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">晚餐预约</div>
                  <div class="detail-value" style="color: #67C23A;">{{ mealReservationStats.dinnerCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">已批准</div>
                  <div class="detail-value" style="color: #67C23A;">{{ mealReservationStats.approvedCount || 0 }}</div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细统计 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <!-- 进出统计 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center;">
                <el-icon :size="20" color="#409EFF" style="margin-right: 8px;"><Switch /></el-icon>
                <span style="font-weight: bold;">进出统计</span>
              </div>
            </template>
            <el-row :gutter="10">
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">进入次数</div>
                  <div class="detail-value" style="color: #67C23A;">{{ entryExitStats.entryCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">离开次数</div>
                  <div class="detail-value" style="color: #E6A23C;">{{ entryExitStats.exitCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">正常记录</div>
                  <div class="detail-value" style="color: #67C23A;">{{ entryExitStats.normalCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">异常记录</div>
                  <div class="detail-value" style="color: #F56C6C;">{{ entryExitStats.abnormalCount || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <!-- 进出状态进度条 -->
            <div style="margin-top: 15px;" v-if="entryExitStats.totalRecords > 0">
              <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                <span style="font-size: 13px; color: #606266;">正常率</span>
                <span style="font-size: 13px; color: #606266;">
                  {{ Math.round((entryExitStats.normalCount / entryExitStats.totalRecords) * 100) }}%
                </span>
              </div>
              <el-progress
                :percentage="Math.round((entryExitStats.normalCount / entryExitStats.totalRecords) * 100)"
                :color="entryExitStats.abnormalCount > 0 ? '#E6A23C' : '#67C23A'"
              />
            </div>
          </el-card>
        </el-col>

        <!-- 请假统计 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center;">
                <el-icon :size="20" color="#E6A23C" style="margin-right: 8px;"><Document /></el-icon>
                <span style="font-weight: bold;">请假统计</span>
              </div>
            </template>
            <el-row :gutter="10">
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-label">已批准</div>
                  <div class="detail-value" style="color: #67C23A;">{{ leaveStats.approvedCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-label">待审批</div>
                  <div class="detail-value" style="color: #E6A23C;">{{ leaveStats.pendingCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-label">已拒绝</div>
                  <div class="detail-value" style="color: #F56C6C;">{{ leaveStats.rejectedCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">回家请假</div>
                  <div class="detail-value" style="color: #67C23A;">{{ leaveStats.homeLeaveCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-stat">
                  <div class="detail-label">外出请假</div>
                  <div class="detail-value" style="color: #E6A23C;">{{ leaveStats.outLeaveCount || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <!-- 请假类型分布 -->
            <div style="margin-top: 15px;" v-if="leaveStats.totalRequests > 0">
              <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                <span style="font-size: 13px; color: #606266;">批准率</span>
                <span style="font-size: 13px; color: #606266;">
                  {{ Math.round((leaveStats.approvedCount / leaveStats.totalRequests) * 100) }}%
                </span>
              </div>
              <el-progress
                :percentage="Math.round((leaveStats.approvedCount / leaveStats.totalRequests) * 100)"
                color="#67C23A"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 学生状况报告 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: bold;">学生状况报告</span>
                <span style="font-size: 12px; color: #909399;">
                  最新日期：{{ dashboard.latestStatusReport?.reportDate || '暂无' }}
                </span>
              </div>
            </template>

            <el-row :gutter="10" style="margin-bottom: 12px;">
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">休息状况</div>
                  <el-tag :type="formatStatusType(dashboard.latestStatusReport?.restStatus)">
                    {{ formatStatusText(dashboard.latestStatusReport?.restStatus) }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">就餐状况</div>
                  <el-tag :type="formatStatusType(dashboard.latestStatusReport?.mealStatus)">
                    {{ formatStatusText(dashboard.latestStatusReport?.mealStatus) }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">情绪状况</div>
                  <el-tag :type="formatStatusType(dashboard.latestStatusReport?.moodStatus)">
                    {{ formatStatusText(dashboard.latestStatusReport?.moodStatus) }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="detail-stat">
                  <div class="detail-label">健康状况</div>
                  <el-tag :type="formatStatusType(dashboard.latestStatusReport?.healthStatus)">
                    {{ formatStatusText(dashboard.latestStatusReport?.healthStatus) }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>

            <div style="font-size: 13px; color: #606266; margin-bottom: 10px;" v-if="dashboard.latestStatusReport?.remark">
              备注：{{ dashboard.latestStatusReport.remark }}
            </div>

            <el-table :data="recentStatusReports" stripe size="small" style="width: 100%">
              <el-table-column prop="reportDate" label="日期" width="120" />
              <el-table-column label="休息" width="100">
                <template #default="scope">
                  <el-tag :type="formatStatusType(scope.row.restStatus)" size="small">
                    {{ formatStatusText(scope.row.restStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="就餐" width="100">
                <template #default="scope">
                  <el-tag :type="formatStatusType(scope.row.mealStatus)" size="small">
                    {{ formatStatusText(scope.row.mealStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="情绪" width="100">
                <template #default="scope">
                  <el-tag :type="formatStatusType(scope.row.moodStatus)" size="small">
                    {{ formatStatusText(scope.row.moodStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="健康" width="100">
                <template #default="scope">
                  <el-tag :type="formatStatusType(scope.row.healthStatus)" size="small">
                    {{ formatStatusText(scope.row.healthStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            </el-table>
            <el-empty v-if="!recentStatusReports || recentStatusReports.length === 0" description="暂无状况报告" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近记录 -->
      <el-row :gutter="20">
        <!-- 最近进出记录 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: bold;">最近进出记录</span>
                <el-button type="primary" link @click="$router.push('/parentEntryExitView')">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentEntryExit" stripe size="small" style="width: 100%">
              <el-table-column prop="recordType" label="类型" width="70">
                <template #default="scope">
                  <el-tag :type="scope.row.recordType === 'entry' ? 'success' : 'warning'" size="small">
                    {{ scope.row.recordType === 'entry' ? '进入' : '离开' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="recordTime" label="时间" />
              <el-table-column prop="status" label="状态" width="70">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'normal' ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 'normal' ? '正常' : '异常' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!recentEntryExit || recentEntryExit.length === 0" description="暂无记录" :image-size="60" />
          </el-card>
        </el-col>

        <!-- 最近请假记录 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: bold;">最近请假记录</span>
                <el-button type="primary" link @click="$router.push('/parentLeaveRequest')">查看全部</el-button>
              </div>
            </template>
            <el-table :data="recentLeave" stripe size="small" style="width: 100%">
              <el-table-column prop="leaveType" label="类型" width="70">
                <template #default="scope">
                  <el-tag :type="scope.row.leaveType === 'home' ? 'success' : 'warning'" size="small">
                    {{ scope.row.leaveType === 'home' ? '回家' : '外出' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="原因" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag v-if="scope.row.status === 'pending'" type="info" size="small">待审批</el-tag>
                  <el-tag v-else-if="scope.row.status === 'approved'" type="success" size="small">已批准</el-tag>
                  <el-tag v-else-if="scope.row.status === 'rejected'" type="danger" size="small">已拒绝</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!recentLeave || recentLeave.length === 0" description="暂无记录" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getCurrentUser } from '@/utils/sessionHelper'
import request from '@/utils/request'
import { Calendar, CircleCheck, Document, Switch } from '@element-plus/icons-vue'

export default {
  name: 'ParentDashboard',
  components: { Calendar, CircleCheck, Document, Switch },
  data() {
    return {
      loading: false,
      selectedMonth: '',
      studentName: '',
      studentOptions: [],
      selectedStudentUsername: '',
      dashboard: {},
      entryExitStats: {},
      leaveStats: {},
      mealReservationStats: {},
      recentEntryExit: [],
      recentLeave: [],
      recentStatusReports: []
    }
  },
  created() {
    const user = getCurrentUser()
    const bound = Array.isArray(user?.studentUsernames)
      ? user.studentUsernames
      : ((user?.studentUsername || '').split(',').map(v => v.trim()).filter(Boolean))
    this.studentOptions = bound
    this.selectedStudentUsername = bound.length ? bound[0] : ''
    this.loadDashboard()
  },
  methods: {
    loadDashboard() {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      this.loading = true
      const params = {}
      if (this.selectedMonth) {
        params.month = this.selectedMonth
      }
      if (this.selectedStudentUsername) {
        params.studentUsername = this.selectedStudentUsername
      }
      request.get(`/parent/dashboard/${user.username}`, { params }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.dashboard = res.data
          this.studentName = res.data.studentName || this.selectedStudentUsername || '学生'
          this.entryExitStats = res.data.entryExitStats || {}
          this.leaveStats = res.data.leaveStats || {}
          this.mealReservationStats = res.data.mealReservationStats || {}
          this.recentEntryExit = res.data.recentEntryExit || []
          this.recentLeave = res.data.recentLeave || []
          this.recentStatusReports = res.data.recentStatusReports || []
        } else {
          this.$message.error(res.msg)
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('加载仪表盘数据失败')
      }).finally(() => {
        this.loading = false
      })
    },
    onStudentChange() {
      this.loadDashboard()
    },
    formatStatusText(status) {
      if (status === 'good') return '良好'
      if (status === 'poor') return '较差'
      return '正常'
    },
    formatStatusType(status) {
      if (status === 'good') return 'success'
      if (status === 'poor') return 'danger'
      return 'info'
    }
  }
}
</script>

<style scoped>
.overview-card {
  display: flex;
  align-items: center;
  padding: 15px;
}
.overview-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 15px;
  width: 100%;
}
.overview-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}
.overview-info {
  flex: 1;
}
.overview-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}
.overview-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.detail-stat {
  text-align: center;
  padding: 12px 0;
}
.detail-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}
.detail-value {
  font-size: 22px;
  font-weight: bold;
}
</style>