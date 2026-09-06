<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>进出管理</el-breadcrumb-item>
      <el-breadcrumb-item>孩子进出记录</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div style="margin: 10px 0; display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
        <el-select
          v-model="selectedStudentUsername"
          placeholder="选择学生"
          style="width: 180px"
          @change="onStudentChange"
        >
          <el-option
            v-for="stu in studentOptions"
            :key="stu"
            :label="stu"
            :value="stu"
          />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="load" :teleported="true" />
        <el-button type="primary" icon="Search" @click="load">查询</el-button>
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
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/sessionHelper'

export default {
  name: 'ParentEntryExitView',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dateRange: [],
      studentOptions: [],
      selectedStudentUsername: '',
      loading: false
    }
  },
  created() {
    const user = getCurrentUser()
    const bound = Array.isArray(user?.studentUsernames)
      ? user.studentUsernames
      : ((user?.studentUsername || '').split(',').map(v => v.trim()).filter(Boolean))
    this.studentOptions = bound
    this.selectedStudentUsername = bound.length ? bound[0] : ''
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      let params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        studentUsername: this.selectedStudentUsername
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
    handleSizeChange(val) {
      this.pageSize = val
      this.load()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.load()
    },
    onStudentChange() {
      this.pageNum = 1
      this.load()
    }
  }
}
</script>