<template>
  <div>
    <el-card class="box-card" style="margin-bottom: 20px;">
      <template #header>
        <div class="clearfix">
        <span>通知中心</span>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" style="margin-left: 20px;">
          <el-button size="small" @click="loadData">刷新</el-button>
        </el-badge>
        </div>
      </template>
    </el-card>

    <!-- 通知列表 -->
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column label="通知编号" width="100">
        <template #default="scope">
          {{ (pageNum - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="200"></el-table-column>
      <el-table-column prop="content" label="内容" min-width="300"></el-table-column>
      <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
      <el-table-column prop="notificationType" label="通知类型" width="150">
        <template #default="scope">
          <el-tag v-if="scope.row.notificationType === 'leave_request'" type="primary">请假申请</el-tag>
          <el-tag v-else-if="scope.row.notificationType === 'unauthorized_out'" type="danger">未授权外出</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isRead" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isRead === 0 ? 'warning' : 'success'">
            {{ scope.row.isRead === 0 ? '未读' : '已读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="通知时间" width="180"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            size="mini" 
            @click="markAsRead(scope.row.id)"
            v-if="scope.row.isRead === 0">
            标记已读
          </el-button>
          <el-button 
            size="mini" 
            type="primary"
            @click="viewLeaveRequest(scope.row)"
            v-if="scope.row.notificationType === 'leave_request' && scope.row.relatedRequestId">
            查看申请
          </el-button>
          <el-button 
            size="mini" 
            type="danger" 
            @click="deleteNotification(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pageNum"
      :page-sizes="[5, 10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      style="margin-top: 20px;">
    </el-pagination>

    <!-- 请假申请审批对话框 -->
    <el-dialog title="请假申请详情" v-model="leaveRequestDialogVisible" width="700px">
      <el-descriptions :column="1" border v-if="currentLeaveRequest">
        <el-descriptions-item label="学生姓名">{{ currentLeaveRequest.studentName }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          {{ currentLeaveRequest.leaveType === 'home' ? '回家' : '外出' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentLeaveRequest.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentLeaveRequest.endTime }}</el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ currentLeaveRequest.reason }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag v-if="currentLeaveRequest.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="currentLeaveRequest.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentLeaveRequest.status === 'rejected'" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentLeaveRequest.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentLeaveRequest && currentLeaveRequest.status === 'pending'" style="margin-top: 20px;">
        <el-form :model="approvalForm" ref="approvalForm" label-width="100px">
          <el-form-item label="审批意见">
            <el-input 
              type="textarea" 
              v-model="approvalForm.parentReply" 
              :rows="3" 
              placeholder="请输入审批意见（可选）">
            </el-input>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <template v-if="currentLeaveRequest && currentLeaveRequest.status === 'pending'">
            <el-button @click="leaveRequestDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="approveLeaveRequest('rejected')">拒绝</el-button>
            <el-button type="success" @click="approveLeaveRequest('approved')">批准</el-button>
          </template>
          <template v-else>
            <el-button @click="leaveRequestDialogVisible = false">关闭</el-button>
          </template>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUser } from '@/utils/sessionHelper'
import request from '@/utils/request'

export default {
  name: 'ParentNotifications',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      unreadCount: 0,
      leaveRequestDialogVisible: false,
      currentLeaveRequest: null,
      approvalForm: {
        parentReply: ''
      }
    }
  },
  mounted() {
    this.loadData();
    this.loadUnreadCount();
    // 定时刷新未读数量
    this.timer = setInterval(() => {
      this.loadUnreadCount();
    }, 30000); // 每30秒刷新一次
  },
  beforeUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    loadData() {
      const user = getCurrentUser();
      if (!user) {
        this.$message.error('用户信息获取失败');
        return;
      }
      request.get(`/parent/notifications/${user.username}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        } else {
          this.$message.error(res.msg);
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('加载数据失败');
      });
    },
    loadUnreadCount() {
      const user = getCurrentUser();
      if (!user) {
        return;
      }
      request.get(`/parent/notifications/unread/${user.username}`).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.unreadCount = res.data;
        }
      }).catch(err => {
        console.error(err);
      });
    },
    markAsRead(id) {
      request.put(`/parent/notifications/read/${id}`).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.$message.success('已标记为已读');
          this.loadData();
          this.loadUnreadCount();
        } else {
          this.$message.error(res.msg);
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('操作失败');
      });
    },
    deleteNotification(id) {
      this.$confirm('确定要删除这条通知吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.delete(`/parent/notifications/delete/${id}`).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('删除成功');
            this.loadData();
            this.loadUnreadCount();
          } else {
            this.$message.error(res.msg);
          }
        }).catch(err => {
          console.error(err);
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },
    viewLeaveRequest(notification) {
      // 标记通知为已读
      if (notification.isRead === 0) {
        this.markAsRead(notification.id);
      }
      
      // 获取请假申请详情
      request.get(`/leaveRequest/detail/${notification.relatedRequestId}`).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.currentLeaveRequest = res.data;
          this.approvalForm.parentReply = '';
          this.leaveRequestDialogVisible = true;
        } else {
          this.$message.error('获取请假申请详情失败');
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('获取请假申请详情失败');
      });
    },
    approveLeaveRequest(status) {
      const statusText = status === 'approved' ? '批准' : '拒绝';
      this.$confirm(`确定要${statusText}这个请假申请吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const requestData = {
          id: this.currentLeaveRequest.id,
          status: status,
          parentReply: this.approvalForm.parentReply || (status === 'approved' ? '同意' : '不同意')
        };
        
        request.put('/leaveRequest/approve', requestData).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success(`已${statusText}该请假申请`);
            this.leaveRequestDialogVisible = false;
            this.loadData();
          } else {
            this.$message.error(res.msg);
          }
        }).catch(err => {
          console.error(err);
          this.$message.error('操作失败');
        });
      }).catch(() => {});
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadData();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadData();
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