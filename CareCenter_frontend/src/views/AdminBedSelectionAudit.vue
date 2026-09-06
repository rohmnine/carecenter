<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>审核中心</el-breadcrumb-item>
      <el-breadcrumb-item>床位更换审核</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div style="margin-bottom: 20px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 130px;" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待审批" value="pending" />
          <el-option label="已批准" value="approved" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="已取消" value="cancelled" />
        </el-select>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索家长账号/学生账号/学生姓名"
          clearable
          style="width: 260px;"
          @clear="loadData"
          @keyup.enter="loadData"
        />

        <el-input-number
          v-model="centerBuildingIdFilter"
          :min="1"
          :step="1"
          :precision="0"
          controls-position="right"
          style="width: 160px;"
          placeholder="分店ID"
        />

        <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="id" label="申请编号" width="90" />
        <el-table-column prop="requesterUsername" label="家长账号" width="120" />
        <el-table-column prop="studentUsername" label="学生账号" width="120" />
        <el-table-column prop="studentName" label="学生姓名" width="110" />
        <el-table-column prop="centerBuildingName" label="分店" width="130" />
        <el-table-column prop="centerRoomId" label="房间号" width="100" />
        <el-table-column prop="bedNum" label="床位号" width="90">
          <template #default="scope">
            {{ bedNumLabel(scope.row.bedNum) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请说明" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="success"
              @click="openApproveDialog(scope.row, 'approved')"
            >
              批准
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="danger"
              @click="openApproveDialog(scope.row, 'rejected')"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px">
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

    <el-dialog
      :title="approveAction === 'approved' ? '批准床位更换申请' : '拒绝床位更换申请'"
      v-model="approveDialogVisible"
      width="560px"
      :close-on-click-modal="false"
      :append-to-body="true"
    >
      <div v-if="currentApproveRow" style="margin-bottom: 12px;">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="申请编号">{{ currentApproveRow.id }}</el-descriptions-item>
          <el-descriptions-item label="家长账号">{{ currentApproveRow.requesterUsername }}</el-descriptions-item>
          <el-descriptions-item label="学生账号">{{ currentApproveRow.studentUsername }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ currentApproveRow.studentName }}</el-descriptions-item>
          <el-descriptions-item label="分店/房间/床位">
            {{ currentApproveRow.centerBuildingName || '-' }} / {{ currentApproveRow.centerRoomId || '-' }} / {{ bedNumLabel(currentApproveRow.bedNum) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请说明">{{ currentApproveRow.reason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form :model="approveForm" label-width="90px">
        <el-form-item label="审批回复">
          <el-input
            type="textarea"
            v-model="approveForm.adminReply"
            :rows="3"
            :placeholder="approveAction === 'approved' ? '可选：填写审批备注' : '请填写拒绝原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveAction === 'approved' ? 'success' : 'danger'" @click="submitApprove">
          {{ approveAction === 'approved' ? '确认批准' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog title="床位更换申请详情" v-model="detailDialogVisible" width="620px" :append-to-body="true">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="申请编号">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="家长账号">{{ currentDetail.requesterUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生账号">{{ currentDetail.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="分店">{{ currentDetail.centerBuildingName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分店ID">{{ currentDetail.centerBuildingId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentDetail.centerRoomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="床位号">{{ bedNumLabel(currentDetail.bedNum) }}</el-descriptions-item>
        <el-descriptions-item label="原房间号">{{ currentDetail.originRoomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原床位号">{{ bedNumLabel(currentDetail.originBedNum) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(currentDetail.status)">{{ statusLabel(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请说明">{{ currentDetail.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员回复">{{ currentDetail.adminReply || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentDetail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getCurrentUser } from '@/utils/sessionHelper'

export default {
  name: 'AdminBedSelectionAudit',
  data() {
    return {
      tableData: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      statusFilter: '',
      searchKeyword: '',
      centerBuildingIdFilter: null,
      approveDialogVisible: false,
      detailDialogVisible: false,
      currentApproveRow: null,
      currentDetail: null,
      approveAction: '',
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
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        status: this.statusFilter || undefined,
        search: this.searchKeyword || undefined,
        centerBuildingId: this.centerBuildingIdFilter || undefined
      }
      request.get('/parent/bed/all', { params }).then(res => {
        if (res.code === '0' || res.code === 0) {
          const records = (res.data && res.data.records) ? res.data.records : []
          this.tableData = records.map(this.normalizeRecord)
          this.total = res.data.total || this.tableData.length
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    normalizeRecord(item) {
      const requestData = item.requestData || {}
      return {
        ...item,
        centerBuildingId: requestData.center_building_id || requestData.center_building_id || null,
        centerBuildingName: requestData.center_building_name || requestData.center_building_name || '-',
        centerRoomId: requestData.center_room_id || requestData.center_room_id || '-',
        bedNum: requestData.bed_num || null,
        bedName: requestData.bed_name || '',
        originRoomId: requestData.origin_center_room_id || requestData.origin_center_room_id || '',
        originBedNum: requestData.origin_bed_num || null,
        originBedName: requestData.origin_bed_name || '',
        adminReply: item.adminReply || '-'
      }
    },
    openApproveDialog(row, action) {
      this.currentApproveRow = row
      this.approveAction = action
      this.approveForm.adminReply = ''
      this.approveDialogVisible = true
    },
    submitApprove() {
      if (!this.currentApproveRow) return
      if (this.approveAction === 'rejected' && !this.approveForm.adminReply) {
        this.$message.warning('拒绝时请填写原因')
        return
      }
      const admin = getCurrentUser()
      const payload = {
        id: this.currentApproveRow.id,
        status: this.approveAction,
        adminReply: this.approveForm.adminReply,
        adminUsername: admin ? admin.username : undefined
      }
      request.put('/parent/bed/approve', payload).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success(this.approveAction === 'approved' ? '审批通过，已完成床位更换' : '已拒绝该申请')
          this.approveDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.msg || '审批失败')
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('审批失败')
      })
    },
    viewDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
    },
    statusTag(status) {
      if (status === 'approved') return 'success'
      if (status === 'rejected') return 'danger'
      if (status === 'cancelled') return 'warning'
      return 'info'
    },
    statusLabel(status) {
      if (status === 'approved') return '已批准'
      if (status === 'rejected') return '已拒绝'
      if (status === 'cancelled') return '已取消'
      return '待审批'
    },
    bedNumLabel(bedNum) {
      if (!bedNum) return '-'
      return `${bedNum}号床`
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