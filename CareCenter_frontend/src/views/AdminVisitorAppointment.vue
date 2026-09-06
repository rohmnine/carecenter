<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>访客管理</el-breadcrumb-item>
      <el-breadcrumb-item>访客预约审批</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <!-- 筛选栏 -->
      <div style="margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 130px;" @change="loadData">
          <el-option label="全部" value=""></el-option>
          <el-option label="待审批" value="pending"></el-option>
          <el-option label="已批准" value="approved"></el-option>
          <el-option label="已拒绝" value="rejected"></el-option>
          <el-option label="已取消" value="cancelled"></el-option>
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索家长/学生姓名"
          clearable
          style="width: 200px;"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-button type="primary" @click="loadData" icon="Search">搜索</el-button>
      </div>

      <!-- 预约列表 -->
      <el-table :data="tableData" stripe border style="width: 100%" v-loading="loading">
        <el-table-column label="#" type="index" width="50" />
        <el-table-column prop="requesterUsername" label="家长账号" width="130" />
        <el-table-column prop="requesterName" label="家长姓名" width="120" />
        <el-table-column prop="requesterPhone" label="联系电话" width="140" />
        <el-table-column prop="studentName" label="探访学生" width="100" />
        <el-table-column prop="visitDate" label="预约日期" width="120" />
        <el-table-column prop="visitTimeSlot" label="时间段" width="130" />
        <el-table-column prop="visitorCount" label="来访人数" width="90" />
        <el-table-column prop="purpose" label="来访目的" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="info">待审批</el-tag>
            <el-tag v-else-if="scope.row.status === 'approved'" type="success">已批准</el-tag>
            <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="scope.row.status === 'cancelled'" type="warning">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="success"
              @click="approveDialog(scope.row, 'approved')"
              v-if="scope.row.status === 'pending'">
              批准
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="approveDialog(scope.row, 'rejected')"
              v-if="scope.row.status === 'pending'">
              拒绝
            </el-button>
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
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

    <!-- 审批对话框 -->
    <el-dialog
      :title="approveAction === 'approved' ? '批准预约' : '拒绝预约'"
      v-model="approveDialogVisible"
      width="500px"
      :close-on-click-modal="false"
      :append-to-body="true">
      <div v-if="currentRow" style="margin-bottom: 15px;">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="家长账号">{{ currentRow.requesterUsername }}</el-descriptions-item>
          <el-descriptions-item label="家长姓名">{{ currentRow.requesterName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.requesterPhone }}</el-descriptions-item>
          <el-descriptions-item label="探访学生">{{ currentRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ currentRow.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ currentRow.visitTimeSlot }}</el-descriptions-item>
          <el-descriptions-item label="来访目的">{{ currentRow.purpose }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            type="textarea"
            v-model="approveForm.adminReply"
            :rows="3"
            :placeholder="approveAction === 'approved' ? '可选：添加备注信息' : '请填写拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
          :type="approveAction === 'approved' ? 'success' : 'danger'"
          @click="submitApprove">
          {{ approveAction === 'approved' ? '确认批准' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="预约详情" v-model="detailDialogVisible" width="600px" :append-to-body="true">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="预约编号">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="家长账号">{{ currentDetail.requesterUsername }}</el-descriptions-item>
        <el-descriptions-item label="家长姓名">{{ currentDetail.requesterName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.requesterPhone }}</el-descriptions-item>
        <el-descriptions-item label="学生账号">{{ currentDetail.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="探访学生">{{ currentDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ currentDetail.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ currentDetail.visitTimeSlot }}</el-descriptions-item>
        <el-descriptions-item label="来访人数">{{ currentDetail.visitorCount }}人</el-descriptions-item>
        <el-descriptions-item label="来访目的">{{ currentDetail.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentDetail.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="currentDetail.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentDetail.status === 'rejected'" type="danger">已拒绝</el-tag>
          <el-tag v-else-if="currentDetail.status === 'cancelled'" type="warning">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="管理员回复">{{ currentDetail.adminReply || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentDetail.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AdminVisitorAppointment',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      statusFilter: '',
      searchKeyword: '',
      approveDialogVisible: false,
      detailDialogVisible: false,
      approveAction: '',
      currentRow: null,
      currentDetail: null,
      approveForm: {
        adminReply: ''
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      request.get('/parent/visitor/all', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.statusFilter,
          keyword: this.searchKeyword
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          const records = (res.data && res.data.records) ? res.data.records : []
          this.tableData = records.map(item => {
            const requestData = item.requestData || {}
            return {
              ...item,
              requesterUsername: item.requesterUsername || '-',
              requesterName: requestData.parent_name || '-',
              requesterPhone: requestData.parent_phone || '-',
              visitDate: requestData.visit_date || '-',
              visitTimeSlot: requestData.visit_time || '-',
              visitorCount: requestData.visitor_count || '-',
              purpose: requestData.visit_purpose || item.reason || '-'
            }
          })
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
    approveDialog(row, action) {
      this.currentRow = row
      this.approveAction = action
      this.approveForm.adminReply = ''
      this.approveDialogVisible = true
    },
    submitApprove() {
      if (this.approveAction === 'rejected' && !this.approveForm.adminReply) {
        this.$message.warning('拒绝时请填写原因')
        return
      }
      request.put('/parent/visitor/approve', {
        id: this.currentRow.id,
        status: this.approveAction,
        adminReply: this.approveForm.adminReply
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success(this.approveAction === 'approved' ? '已批准该预约' : '已拒绝该预约')
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
:deep(.el-dialog__header) {
  background-color: #409EFF;
  color: white;
}
</style>