<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>访客管理</el-breadcrumb-item>
      <el-breadcrumb-item>访客预约</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <!-- 操作栏 -->
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <el-button type="primary" @click="showSubmitDialog" icon="Plus">新建预约</el-button>
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px;" @change="loadData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待审批" value="pending"></el-option>
            <el-option label="已批准" value="approved"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </div>
      </div>

      <!-- 预约列表 -->
      <el-table :data="tableData" stripe border style="width: 100%" v-loading="loading">
        <el-table-column label="#" type="index" width="50" />
        <el-table-column prop="studentName" label="探访学生" width="100" />
        <el-table-column prop="visitDate" label="预约日期" width="120" />
        <el-table-column prop="visitTimeSlot" label="时间段" width="120" />
        <el-table-column prop="visitorCount" label="来访人数" width="90" />
        <el-table-column prop="purpose" label="来访目的" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="info">待审批</el-tag>
            <el-tag v-else-if="scope.row.status === 'approved'" type="success">已批准</el-tag>
            <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="scope.row.status === 'cancelled'" type="warning">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminReply" label="管理员回复" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              size="small"
              type="warning"
              @click="cancelAppointment(scope.row)"
              v-if="scope.row.status === 'pending'">
              取消
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteAppointment(scope.row)"
              v-if="scope.row.status === 'cancelled' || scope.row.status === 'rejected'">
              删除
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

    <!-- 新建预约对话框 -->
    <el-dialog
      title="新建访客预约"
      v-model="submitDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      :append-to-body="true">
      <el-form :model="appointmentForm" :rules="rules" ref="appointmentFormRef" label-width="100px">
        <el-form-item label="预约日期" prop="visitDate">
          <el-date-picker
            v-model="appointmentForm.visitDate"
            type="date"
            format="YYYY-MM-DD"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
            :teleported="true"
            :disabled-date="disablePastDates"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="visitTimeSlot">
          <el-select v-model="appointmentForm.visitTimeSlot" placeholder="选择时间段" style="width: 100%;">
            <el-option label="上午 (8:00-12:00)" value="上午 (8:00-12:00)" />
            <el-option label="下午 (14:00-17:00)" value="下午 (14:00-17:00)" />
            <el-option label="晚上 (18:00-20:00)" value="晚上 (18:00-20:00)" />
          </el-select>
        </el-form-item>
        <el-form-item label="来访人数" prop="visitorCount">
          <el-input-number v-model="appointmentForm.visitorCount" :min="1" :max="5" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="联系电话" prop="parentPhone">
          <el-input v-model="appointmentForm.parentPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="来访目的" prop="purpose">
          <el-input type="textarea" v-model="appointmentForm.purpose" :rows="3" placeholder="请描述来访目的" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">提交预约</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="预约详情" v-model="detailDialogVisible" width="600px" :append-to-body="true">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="家长姓名">{{ currentDetail.parentName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDetail.parentPhone }}</el-descriptions-item>
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
import { getCurrentUser } from '@/utils/sessionHelper'
import request from '@/utils/request'

export default {
  name: 'ParentVisitorAppointment',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      statusFilter: '',
      submitDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: null,
      appointmentForm: {
        visitDate: '',
        visitTimeSlot: '',
        visitorCount: 1,
        parentPhone: '',
        purpose: ''
      },
      rules: {
        visitDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
        visitTimeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
        visitorCount: [{ required: true, message: '请输入来访人数', trigger: 'change' }],
        parentPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        purpose: [
          { required: true, message: '请描述来访目的', trigger: 'blur' },
          { min: 5, max: 200, message: '来访目的长度在 5 到 200 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.initPhone()
    this.loadData()
  },
  methods: {
    initPhone() {
      const user = getCurrentUser()
      if (user && user.phoneNum) {
        this.appointmentForm.parentPhone = user.phoneNum
      }
    },
    loadData() {
      const user = getCurrentUser()
      if (!user) {
        this.$message.error('用户信息获取失败')
        return
      }
      this.loading = true
      request.get(`/parent/visitor/list/${user.username}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.statusFilter
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          const records = (res.data && res.data.records) ? res.data.records : []
          this.tableData = records.map(item => {
            const requestData = item.requestData || {}
            return {
              ...item,
              parentName: requestData.parent_name || '-',
              parentPhone: requestData.parent_phone || '-',
              visitDate: requestData.visit_date || '-',
              visitTimeSlot: requestData.visit_time || '-',
              visitorCount: requestData.visitor_count || '-',
              purpose: requestData.visit_purpose || item.reason || '-',
              adminReply: item.adminReply || '-'
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
    disablePastDates(date) {
      const todayStart = new Date()
      todayStart.setHours(0, 0, 0, 0)
      return date.getTime() < todayStart.getTime()
    },
    showSubmitDialog() {
      this.appointmentForm = {
        visitDate: '',
        visitTimeSlot: '',
        visitorCount: 1,
        parentPhone: '',
        purpose: ''
      }
      this.initPhone()
      this.submitDialogVisible = true
    },
    submitAppointment() {
      this.$refs.appointmentFormRef.validate((valid) => {
        if (valid) {
          const user = getCurrentUser()
          if (!user) {
            this.$message.error('用户信息获取失败')
            return
          }
          const requestData = {
            parentUsername: user.username,
            parentName: user.name || user.username,
            parentPhone: this.appointmentForm.parentPhone,
            studentUsername: user.studentUsername,
            studentName: user.studentUsername,
            visitDate: this.appointmentForm.visitDate,
            visitTimeSlot: this.appointmentForm.visitTimeSlot,
            visitorCount: this.appointmentForm.visitorCount,
            purpose: this.appointmentForm.purpose
          }
          request.post('/parent/visitor/submit', requestData).then(res => {
            if (res.code === '0' || res.code === 0) {
              this.$message.success('预约提交成功，等待管理员审批')
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
    cancelAppointment(row) {
      this.$confirm('确定要取消该预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.put(`/parent/visitor/cancel/${row.id}`).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('预约已取消')
            this.loadData()
          } else {
            this.$message.error(res.msg)
          }
        }).catch(err => {
          console.error(err)
          this.$message.error('取消失败')
        })
      }).catch(() => {})
    },
    deleteAppointment(row) {
      this.$confirm('确定要删除该预约记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.delete(`/parent/visitor/delete/${row.id}`).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.msg)
          }
        }).catch(err => {
          console.error(err)
          this.$message.error('删除失败')
        })
      }).catch(() => {})
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