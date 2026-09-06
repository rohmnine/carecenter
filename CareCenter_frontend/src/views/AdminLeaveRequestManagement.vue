<template>
  <div>
    <el-card class="box-card" style="margin-bottom: 20px;">
      <div slot="header" class="clearfix">
        <span>外出申请管理</span>
        <span style="float: right; color: #999; font-size: 12px;">
          当家长来不及处理时，管理员可以代为审批学生的外出申请
        </span>
      </div>
    </el-card>

    <!-- 搜索栏 -->
    <div style="margin-bottom: 20px;">
      <el-input
        v-model="searchText"
        placeholder="搜索学生姓名或学号"
        style="width: 300px; margin-right: 10px;"
        clearable
        @clear="loadData">
      </el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button @click="searchText=''; loadData()">重置</el-button>
    </div>

    <!-- 筛选标签 -->
    <div style="margin-bottom: 20px;">
      <el-radio-group v-model="statusFilter" @change="loadData">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待审批</el-radio-button>
        <el-radio-button label="approved">已批准</el-radio-button>
        <el-radio-button label="rejected">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 申请列表 -->
    <el-table :data="tableData" stripe style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="申请编号" width="100"></el-table-column>
      <el-table-column prop="studentUsername" label="学号" width="120"></el-table-column>
      <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
      <el-table-column prop="leaveType" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.leaveType === 'home' ? 'success' : 'warning'" size="small">
            {{ scope.row.leaveType === 'home' ? '回家' : '外出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="160"></el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="160"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="info" size="small">待审批</el-tag>
          <el-tag v-else-if="scope.row.status === 'approved'" type="success" size="small">已批准</el-tag>
          <el-tag v-else-if="scope.row.status === 'rejected'" type="danger" size="small">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="parentReply" label="审批意见" width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="160"></el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="scope">
          <div style="display: flex; gap: 5px; align-items: center;">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="success"
              @click="showApproveDialog(scope.row, 'approved')">
              批准
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="danger"
              @click="showApproveDialog(scope.row, 'rejected')">
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.status !== 'pending'"
              size="small"
              type="warning"
              @click="showModifyDialog(scope.row)">
              修改状态
            </el-button>
            <el-button
              v-if="scope.row.status === 'approved' && !scope.row.returnCancelled"
              size="small"
              type="primary"
              @click="cancelReturnByAdmin(scope.row)">
              销假
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pageNum"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      style="margin-top: 20px;">
    </el-pagination>

    <!-- 详情对话框 -->
    <el-dialog title="申请详情" v-model="detailDialogVisible" width="700px">
      <el-descriptions :column="1" border v-if="currentRequest">
        <el-descriptions-item label="申请编号">{{ currentRequest.id }}</el-descriptions-item>
        <el-descriptions-item label="学生学号">{{ currentRequest.studentUsername }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentRequest.studentName }}</el-descriptions-item>
        <el-descriptions-item label="家长账号">{{ currentRequest.parentUsername }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">
          <el-tag :type="currentRequest.leaveType === 'home' ? 'success' : 'warning'">
            {{ currentRequest.leaveType === 'home' ? '回家' : '外出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentRequest.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentRequest.endTime }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ currentRequest.reason }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag v-if="currentRequest.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="currentRequest.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentRequest.status === 'rejected'" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批意见">{{ currentRequest.parentReply || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="销假状态">
          <el-tag v-if="currentRequest.returnCancelled" type="success">已销假</el-tag>
          <el-tag v-else type="info">未销假</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="返校时间">{{ currentRequest.returnTime || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRequest.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRequest.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      :title="approvalAction === 'approved' ? '批准申请' : '拒绝申请'"
      v-model="approveDialogVisible"
      width="500px">
      <el-alert
        :title="approvalAction === 'approved' ? '您即将批准此外出申请' : '您即将拒绝此外出申请'"
        :type="approvalAction === 'approved' ? 'success' : 'warning'"
        :description="'学生：' + (currentRequest ? currentRequest.studentName : '') + '，申请' + 
                     (currentRequest && currentRequest.leaveType === 'home' ? '回家' : '外出')"
        show-icon
        style="margin-bottom: 20px;">
      </el-alert>
      
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input 
            type="textarea" 
            v-model="approvalForm.reply" 
            :rows="4" 
            :placeholder="approvalAction === 'approved' ? '请输入批准意见（可选）' : '请输入拒绝原因'">
          </el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button
            :type="approvalAction === 'approved' ? 'success' : 'danger'"
            @click="confirmApproval">
            确认{{ approvalAction === 'approved' ? '批准' : '拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog title="修改审批状态" v-model="modifyDialogVisible" width="500px">
      <el-alert
        title="管理员可以修改已审批的申请状态"
        type="info"
        :description="'学生：' + (currentRequest ? currentRequest.studentName : '')"
        show-icon
        style="margin-bottom: 20px;">
      </el-alert>
      
      <el-form :model="modifyForm" label-width="100px">
        <el-form-item label="当前状态">
          <el-tag v-if="currentRequest && currentRequest.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentRequest && currentRequest.status === 'rejected'" type="danger">已拒绝</el-tag>
        </el-form-item>
        <el-form-item label="修改为">
          <el-radio-group v-model="modifyForm.newStatus">
            <el-radio label="pending">待审批</el-radio>
            <el-radio label="approved">已批准</el-radio>
            <el-radio label="rejected">已拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="修改原因">
          <el-input
            type="textarea"
            v-model="modifyForm.reason"
            :rows="3"
            placeholder="请输入修改原因">
          </el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="modifyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmModify">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AdminLeaveRequestManagement',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchText: '',
      statusFilter: 'pending', // 默认显示待审批
      selectedRows: [], // 选中的行
      detailDialogVisible: false,
      approveDialogVisible: false,
      modifyDialogVisible: false,
      currentRequest: null,
      approvalAction: '', // 'approved' or 'rejected'
      approvalForm: {
        reply: ''
      },
      modifyForm: {
        newStatus: '',
        reason: ''
      }
    }
  },
  mounted() {
    this.loadData();
  },
  methods: {
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    loadData() {
      request.get('/leaveRequest/all', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          search: this.searchText
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          let records = res.data.records;
          
          // 根据状态筛选
          if (this.statusFilter) {
            records = records.filter(item => item.status === this.statusFilter);
          }
          
          this.tableData = records;
          this.total = this.statusFilter ? records.length : res.data.total;
        } else {
          this.$message.error(res.msg);
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('加载数据失败');
      });
    },
    viewDetail(row) {
      this.currentRequest = row;
      this.detailDialogVisible = true;
    },
    showApproveDialog(row, action) {
      this.currentRequest = row;
      this.approvalAction = action;
      this.approvalForm.reply = action === 'approved' ? '管理员批准外出' : '';
      this.approveDialogVisible = true;
    },
    confirmApproval() {
      if (this.approvalAction === 'rejected' && !this.approvalForm.reply.trim()) {
        this.$message.warning('拒绝申请时请填写拒绝原因');
        return;
      }

      const requestData = {
        id: this.currentRequest.id,
        status: this.approvalAction,
        parentReply: this.approvalForm.reply || (this.approvalAction === 'approved' ? '管理员批准' : '管理员拒绝')
      };

      request.put('/leaveRequest/adminApprove', requestData).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success(this.approvalAction === 'approved' ? '批准成功' : '拒绝成功');
          this.approveDialogVisible = false;
          this.loadData();
        } else {
          this.$message.error(res.msg);
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('操作失败');
      });
    },
    showModifyDialog(row) {
      this.currentRequest = row;
      this.modifyForm.newStatus = row.status;
      this.modifyForm.reason = '';
      this.modifyDialogVisible = true;
    },
    confirmModify() {
      if (!this.modifyForm.reason.trim()) {
        this.$message.warning('请填写修改原因');
        return;
      }

      const requestData = {
        id: this.currentRequest.id,
        status: this.modifyForm.newStatus,
        parentReply: `管理员修改状态: ${this.modifyForm.reason}`
      };

      request.put('/leaveRequest/adminApprove', requestData).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success('状态修改成功');
          this.modifyDialogVisible = false;
          this.loadData();
        } else {
          this.$message.error(res.msg);
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('修改失败');
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadData();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadData();
    },
    cancelReturnByAdmin(row) {
      this.$confirm('确认该学生已返校并执行销假吗？', '提示', {
        confirmButtonText: '确认销假',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.put('/leaveRequest/cancelReturn', {
          id: row.id,
          returnTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
        }).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('销假成功，考勤已自动回填');
            this.loadData();
            if (this.currentRequest && this.currentRequest.id === row.id) {
              this.currentRequest = { ...this.currentRequest, ...((res.data && res.data.request) || {}) }
            }
          } else {
            this.$message.error(res.msg || '销假失败');
          }
        }).catch(err => {
          console.error(err);
          this.$message.error('销假失败');
        });
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>