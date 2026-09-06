<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>进出管理</el-breadcrumb-item>
      <el-breadcrumb-item>我的进出记录</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div style="margin: 10px 0">
        <el-date-picker v-model="dateRange" type="daterange" format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="load" :teleported="true" />
        <el-button type="primary" style="margin-left: 10px" icon="Search" @click="load">查询</el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
        <el-table-column label="#" type="index" width="50" />
        <el-table-column prop="recordType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.recordType === 'entry' ? 'success' : 'warning'">
              {{ scope.row.recordType === 'entry' ? '进入' : '离开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'normal' ? 'success' : 'danger'">
              {{ scope.row.status === 'normal' ? '正常' : scope.row.status === 'late' ? '晚归' : '未归' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminUsername" label="登记人" width="120" />
        <el-table-column prop="appealReason" label="申诉说明" />
        <el-table-column prop="appealStatus" label="申诉状态" width="100">
          <template #default="scope">
            <span v-if="scope.row.appealStatus">
              <el-tag :type="scope.row.appealStatus === 'pending' ? 'warning' : scope.row.appealStatus === 'approved' ? 'success' : 'danger'" size="small">
                {{ scope.row.appealStatus === 'pending' ? '待处理' : scope.row.appealStatus === 'approved' ? '已通过' : '已驳回' }}
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.status !== 'normal' && !scope.row.appealStatus" type="primary" size="small" @click="handleAppeal(scope.row)">申诉</el-button>
          </template>
        </el-table-column>
      </el-table>

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

      <el-dialog v-model="dialogVisible" title="申诉说明" width="40%">
        <el-input type="textarea" :rows="5" v-model="appealReason" placeholder="请输入申诉原因" />
        <template #footer>
          <span>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAppeal">提交</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/sessionHelper'

export default {
  name: 'StudentEntryExitView',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dateRange: [],
      studentUsername: '',
      dialogVisible: false,
      appealReason: '',
      currentRecordId: null,
      loading: false
    }
  },
  created() {
    let user = getCurrentUser()
    this.studentUsername = user ? user.username : ''
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      let params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        studentUsername: this.studentUsername
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }
      request.get('/entryExit/findByStudent', { params }).then(res => {
        if (res.code === '0') {
          this.tableData = res.data.records
          this.total = res.data.total
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAppeal(row) {
      this.currentRecordId = row.id
      this.appealReason = ''
      this.dialogVisible = true
    },
    submitAppeal() {
      if (!this.appealReason) {
        this.$message.warning('请输入申诉原因')
        return
      }
      request.put('/entryExit/appeal', { id: this.currentRecordId, appealReason: this.appealReason }).then(res => {
        if (res.code === '0') {
          this.$message.success('申诉提交成功')
          this.dialogVisible = false
          this.load()
        }
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.load()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.load()
    }
  }
}
</script>