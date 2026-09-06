<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>住宿管理</el-breadcrumb-item>
      <el-breadcrumb-item>床位更换</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <el-button type="primary" icon="Plus" @click="showSubmitDialog">新建床位更换申请</el-button>
        <div style="display: flex; gap: 10px; align-items: center;">
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
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="待审批" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </div>
      </div>

      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="centerBuildingName" label="分店" width="120" />
        <el-table-column prop="centerRoomId" label="房间号" width="100" />
        <el-table-column prop="bedNum" label="床位号" width="90">
          <template #default="scope">
            {{ bedNumLabel(scope.row.bedNum) }}
          </template>
        </el-table-column>
        <el-table-column label="原床位" width="160">
          <template #default="scope">
            <span v-if="scope.row.originRoomId && scope.row.originBedNum">
              {{ scope.row.originRoomId }} / {{ bedNumLabel(scope.row.originBedNum) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请说明" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminReply" label="管理员回复" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="warning"
              @click="cancelSelection(scope.row)"
            >
              取消
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
      title="新建床位更换申请"
      v-model="submitDialogVisible"
      width="800px"
      :close-on-click-modal="false"
      :append-to-body="true"
    >
      <div style="display: flex; gap: 10px; margin-bottom: 10px;">
        <el-select
          v-model="submitForm.studentUsername"
          placeholder="请选择学生"
          style="width: 200px"
          @change="onSubmitStudentChange"
        >
          <el-option
            v-for="stu in studentOptions"
            :key="stu"
            :label="stu"
            :value="stu"
          />
        </el-select>
        <el-input
          v-model="submitForm.reason"
          placeholder="请输入申请说明（可选）"
          maxlength="200"
          show-word-limit
        />
        <el-button type="primary" @click="loadAvailableBeds">刷新可选床位</el-button>
      </div>

      <el-table
        :data="availableBeds"
        stripe
        border
        height="320"
        style="width: 100%"
        v-loading="availableLoading"
        @row-click="selectAvailableBed"
        :row-class-name="availableRowClassName"
      >
        <el-table-column label="选择" width="80">
          <template #default="scope">
            <el-radio
              :model-value="selectedBedKey"
              :label="bedKey(scope.row)"
              @change="() => selectAvailableBed(scope.row)"
            >
              选中
            </el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="centerBuildingName" label="分店" width="130" />
        <el-table-column prop="centerRoomId" label="房间号" width="120" />
        <el-table-column prop="floorNum" label="楼层" width="90" />
        <el-table-column prop="bedNum" label="床位号" width="100">
          <template #default="scope">
            {{ bedNumLabel(scope.row.bedNum) }}
          </template>
        </el-table-column>
        <el-table-column label="当前入住/上限" width="130">
          <template #default="scope">
            {{ scope.row.currentCapacity }}/{{ scope.row.maxCapacity }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!availableLoading && availableBeds.length === 0" description="暂无可申请床位" />

      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSelection">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog title="床位更换申请详情" v-model="detailDialogVisible" width="600px" :append-to-body="true">
      <el-descriptions v-if="currentDetail" :column="1" border>
        <el-descriptions-item label="家长账号">{{ currentDetail.requesterUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生账号">{{ currentDetail.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="分店">{{ currentDetail.centerBuildingName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentDetail.centerRoomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="床位号">{{ bedNumLabel(currentDetail.bedNum) }}</el-descriptions-item>
        <el-descriptions-item label="原房间号">{{ currentDetail.originRoomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原床位号">{{ bedNumLabel(currentDetail.originBedNum) }}</el-descriptions-item>
        <el-descriptions-item label="申请说明">{{ currentDetail.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(currentDetail.status)">{{ statusLabel(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
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
  name: 'ParentBedSelection',
  data() {
    return {
      loading: false,
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      statusFilter: '',
      submitDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: null,
      availableLoading: false,
      availableBeds: [],
      selectedBed: null,
      selectedBedKey: '',
      studentOptions: [],
      selectedStudentUsername: '',
      submitForm: {
        studentUsername: '',
        reason: ''
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
      request.get(`/parent/bed/list/${user.username}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.statusFilter,
          studentUsername: this.selectedStudentUsername
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          const records = (res.data && res.data.records) ? res.data.records : []
          this.tableData = records.map(this.normalizeRecord)
          this.total = res.data.total || 0
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
    showSubmitDialog() {
      this.submitForm = {
        studentUsername: this.selectedStudentUsername || (this.studentOptions[0] || ''),
        reason: ''
      }
      this.selectedBed = null
      this.selectedBedKey = ''
      this.submitDialogVisible = true
      this.loadAvailableBeds()
    },
    loadAvailableBeds() {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      this.availableLoading = true
      request.get(`/parent/bed/available/${user.username}`, {
        params: {
          studentUsername: this.submitForm.studentUsername || this.selectedStudentUsername || (this.studentOptions[0] || '')
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.availableBeds = Array.isArray(res.data) ? res.data : []
          if (this.availableBeds.length === 0) {
            this.selectedBed = null
            this.selectedBedKey = ''
          }
        } else {
          this.$message.error(res.msg || '加载可选床位失败')
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('加载可选床位失败')
      }).finally(() => {
        this.availableLoading = false
      })
    },
    bedKey(row) {
      return `${row.centerRoomId}_${row.bedNum}`
    },
    selectAvailableBed(row) {
      this.selectedBed = row
      this.selectedBedKey = this.bedKey(row)
    },
    availableRowClassName({ row }) {
      return this.selectedBedKey && this.selectedBedKey === this.bedKey(row) ? 'selected-row' : ''
    },
    submitSelection() {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      if (!this.selectedBed) {
        this.$message.warning('请先选择目标床位')
        return
      }

      const payload = {
        parentUsername: user.username,
        studentUsername: this.submitForm.studentUsername || this.selectedStudentUsername || (this.studentOptions[0] || ''),
        centerRoomId: this.selectedBed.centerRoomId,
        bedNum: this.selectedBed.bedNum,
        bedName: this.selectedBed.bedName,
        reason: this.submitForm.reason || '家长申请床位更换',
        idemKey: `bed_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
      }

      request.post('/parent/bed/submit', payload).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success((res.data && res.data.message) || '提交成功')
          this.submitDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      }).catch(err => {
        console.error(err)
        this.$message.error('提交失败')
      })
    },
    cancelSelection(row) {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      this.$confirm('确定取消该床位更换申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.put(`/parent/bed/cancel/${row.id}`, null, {
          params: { parentUsername: user.username }
        }).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('取消成功')
            this.loadData()
          } else {
            this.$message.error(res.msg || '取消失败')
          }
        }).catch(err => {
          console.error(err)
          this.$message.error('取消失败')
        })
      }).catch(() => {})
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
    onStudentChange() {
      this.pageNum = 1
      this.loadData()
    },
    onSubmitStudentChange() {
      this.selectedBed = null
      this.selectedBedKey = ''
      this.loadAvailableBeds()
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
:deep(.selected-row) {
  background: #ecf5ff !important;
}
</style>