<template>
  <div>
    <div v-loading="pageLoading" element-loading-text="加载中...">
      <div style="margin-bottom: 20px;">
        <el-button type="primary" @click="showSubmitDialog" :loading="checkingParent">提交请假申请</el-button>
      </div>

      <!-- 请假申请列表 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
      <el-table-column prop="id" label="申请编号" width="100"></el-table-column>
      <el-table-column prop="leaveType" label="请假类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.leaveType === 'home' ? 'success' : 'warning'">
            {{ scope.row.leaveType === 'home' ? '回家' : '外出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="请假原因" width="200"></el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
      <el-table-column prop="status" label="审批状态" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="scope.row.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="parentReply" label="家长回复" width="200"></el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button size="mini" @click="viewDetail(scope.row)">详情</el-button>
          <el-button
            size="mini"
            type="success"
            @click="cancelReturn(scope.row)"
            v-if="scope.row.status === 'approved' && !scope.row.returnCancelled">
            返校销假
          </el-button>
          <el-button
            size="mini"
            type="danger"
            @click="deleteRequest(scope.row.id)"
            v-if="scope.row.status === 'pending'">
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
    </div>

    <!-- 提交请假申请对话框 - 移到 v-loading 外面 -->
    <el-dialog
      title="提交请假申请"
      v-model="submitDialogVisible"
      width="600px"
      :append-to-body="true"
      :close-on-click-modal="false"
      :destroy-on-close="false"
      @open="onDialogOpen"
      @close="onDialogClose">
      <el-form :model="leaveForm" :rules="rules" ref="leaveFormRef" label-width="100px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-radio-group v-model="leaveForm.leaveType">
            <el-radio label="home">回家</el-radio>
            <el-radio label="out">外出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-input
            v-model="leaveForm.startTime"
            placeholder="请输入开始时间，例如 2026-03-16 09:00:00"
            clearable>
          </el-input>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-input
            v-model="leaveForm.endTime"
            placeholder="请输入结束时间，例如 2026-03-16 18:00:00"
            clearable>
          </el-input>
        </el-form-item>
        <el-form-item label="目的地" prop="destination" v-if="leaveForm.leaveType === 'out'">
          <el-input v-model="leaveForm.destination" placeholder="请输入外出目的地"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone" v-if="leaveForm.leaveType === 'out'">
          <el-input v-model="leaveForm.contactPhone" placeholder="请输入外出期间的联系电话"></el-input>
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input type="textarea" v-model="leaveForm.reason" :rows="4" placeholder="请输入请假原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLeaveRequest">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="请假申请详情" v-model="detailDialogVisible" width="600px">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="申请编号">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ currentDetail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          {{ currentDetail.leaveType === 'home' ? '回家' : '外出' }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentDetail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="外出目的地" v-if="currentDetail.leaveType === 'out'">{{ currentDetail.destination || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话" v-if="currentDetail.leaveType === 'out'">{{ currentDetail.contactPhone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ currentDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag v-if="currentDetail.status === 'pending'" type="info">待审批</el-tag>
          <el-tag v-else-if="currentDetail.status === 'approved'" type="success">已批准</el-tag>
          <el-tag v-else-if="currentDetail.status === 'rejected'" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="家长回复">{{ currentDetail.parentReply || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="销假状态">
          <el-tag v-if="currentDetail.returnCancelled" type="success">已销假</el-tag>
          <el-tag v-else type="info">未销假</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="返校时间">{{ currentDetail.returnTime || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentUser } from '@/utils/sessionHelper'
import request from '@/utils/request'

export default {
  name: 'StudentLeaveRequest',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      submitDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: null,
      pageLoading: false,
      tableLoading: false,
      checkingParent: false,
      leaveForm: {
        leaveType: 'home',
        startTime: null,
        endTime: null,
        destination: '',
        contactPhone: '',
        reason: ''
      },
      rules: {
        leaveType: [
          { required: true, message: '请选择请假类型', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请输入开始时间', trigger: 'blur' },
          { pattern: /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/, message: '时间格式应为 YYYY-MM-DD HH:mm:ss', trigger: 'blur' }
        ],
        endTime: [
          { required: true, message: '请输入结束时间', trigger: 'blur' },
          { pattern: /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/, message: '时间格式应为 YYYY-MM-DD HH:mm:ss', trigger: 'blur' }
        ],
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
  mounted() {
    this.pageLoading = true;
    this.loadData();
  },
  unmounted() {
    // 清理所有加载状态，确保组件销毁时不会阻塞
    this.pageLoading = false;
    this.tableLoading = false;
    this.checkingParent = false;
  },
  methods: {
    loadData() {
      const user = getCurrentUser();
      if (!user) {
        this.$message.error('用户信息获取失败');
        this.pageLoading = false;
        this.tableLoading = false;
        return;
      }
      
      this.tableLoading = true;
      
      // 设置10秒超时
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时')), 10000);
      });
      
      const requestPromise = request.get(`/leaveRequest/student/${user.username}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      });
      
      Promise.race([requestPromise, timeoutPromise])
        .then(res => {
          if (res.code === '0' || res.code === 0) {
            this.tableData = res.data.records;
            this.total = res.data.total;
          } else {
            this.$message.error(res.msg);
          }
        })
        .catch(err => {
          console.error(err);
          if (err.message === '请求超时') {
            this.$message.error('请求超时，请检查网络连接或稍后重试');
          } else {
            this.$message.error('加载数据失败');
          }
        })
        .finally(() => {
          this.pageLoading = false;
          this.tableLoading = false;
        });
    },
    showSubmitDialog() {
      console.log('=== showSubmitDialog called ===');
      console.log('Current submitDialogVisible:', this.submitDialogVisible);
      
      // 检查是否绑定了家长
      const user = getCurrentUser();
      if (!user) {
        this.$message.error('用户信息获取失败');
        return;
      }
      
      console.log('User:', user.username);
      this.checkingParent = true;
      
      // 设置5秒超时
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时')), 5000);
      });
      
      const requestPromise = request.get(`/parent/existByStudent/${user.username}`);
      
      Promise.race([requestPromise, timeoutPromise])
        .then(res => {
          console.log('=== Parent binding check response ===');
          console.log('Full response:', JSON.stringify(res));
          console.log('res.code:', res.code, 'type:', typeof res.code);
          console.log('res.data:', res.data);
          console.log('Condition check: code match:', (res.code === '0' || res.code === 0), 'data exists:', !!res.data);
          
          // 响应拦截器已经返回了response.data，所以res就是{code, data, msg}
          // code 可能是字符串 '0' 或数字 0，使用宽松比较
          if ((res.code === '0' || res.code === 0) && res.data) {
            console.log('=== Parent binding check PASSED, opening dialog ===');
            
            // 重置表单
            this.leaveForm = {
              leaveType: 'home',
              startTime: null,
              endTime: null,
              destination: '',
              contactPhone: '',
              reason: ''
            };
            console.log('Form reset complete');
            
            // 直接设置，不使用 $nextTick
            this.submitDialogVisible = true;
            console.log('submitDialogVisible IMMEDIATELY set to:', this.submitDialogVisible);
            
            // 同时使用 $nextTick 再次确认
            this.$nextTick(() => {
              console.log('In $nextTick, submitDialogVisible:', this.submitDialogVisible);
              if (!this.submitDialogVisible) {
                console.error('ERROR: submitDialogVisible was reset to false!');
                this.submitDialogVisible = true;
              }
            });
          } else {
            console.log('=== Parent binding check FAILED ===');
            console.log('Reason: code:', res.code, 'data:', res.data);
            this.$message.warning('您还未绑定家长，无法提交请假申请。请先在个人中心绑定家长信息。');
          }
        })
        .catch((err) => {
          console.error('=== Parent check error ===', err);
          if (err.message === '请求超时') {
            this.$message.error('请求超时，请检查网络连接或稍后重试');
          } else {
            this.$message.warning('检查家长绑定状态失败，请稍后重试');
          }
        })
        .finally(() => {
          console.log('=== Request complete, setting checkingParent to false ===');
          this.checkingParent = false;
        });
    },
    onDialogOpen() {
      console.log('=== Dialog OPENED event fired ===');
    },
    onDialogClose() {
      console.log('=== Dialog CLOSED event fired ===');
      this.submitDialogVisible = false;
    },
    submitLeaveRequest() {
      this.$refs.leaveFormRef.validate((valid) => {
        if (valid) {
          const user = getCurrentUser();
          if (!user) {
            this.$message.error('用户信息获取失败');
            return;
          }

          const startText = (this.leaveForm.startTime || '').trim();
          const endText = (this.leaveForm.endTime || '').trim();
          if (startText && endText && startText >= endText) {
            this.$message.warning('开始时间必须早于结束时间');
            return;
          }
          
          // 获取家长信息
          request.get(`/parent/existByStudent/${user.username}`).then(parentRes => {
            console.log('Parent check response:', parentRes);
            // 响应拦截器已经返回了response.data，所以parentRes就是{code, data, msg}
            if ((parentRes.code === '0' || parentRes.code === 0) && parentRes.data) {
              const parent = parentRes.data;
              const requestData = {
                studentUsername: user.username,
                studentName: user.name,
                parentUsername: parent.username,
                leaveType: this.leaveForm.leaveType,
                startTime: startText,
                endTime: endText,
                destination: this.leaveForm.destination || '',
                contactPhone: this.leaveForm.contactPhone || '',
                reason: this.leaveForm.reason
              };
              
              request.post('/leaveRequest/submit', requestData).then(res => {
                if (res.code === '0' || res.code === 0) {
                  this.$message.success('请假申请已提交，等待家长审批');
                  this.submitDialogVisible = false;
                  this.loadData();
                } else {
                  this.$message.error(res.msg);
                }
              }).catch(err => {
                console.error(err);
                this.$message.error('提交失败');
              });
            }
          });
        }
      });
    },
    viewDetail(row) {
      this.currentDetail = row;
      this.detailDialogVisible = true;
    },
    deleteRequest(id) {
      this.$confirm('确定要删除这条请假申请吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.delete(`/leaveRequest/delete/${id}`).then(res => {
          if (res.code === '0' || res.code === 0) {
            this.$message.success('删除成功');
            this.loadData();
          } else {
            this.$message.error(res.msg);
          }
        }).catch(err => {
          console.error(err);
          this.$message.error('删除失败');
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
    },
    cancelReturn(row) {
      this.$confirm('确认学生已返校并执行销假吗？', '提示', {
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
:deep(.el-dialog__header) {
  background-color: #409EFF;
  color: white;
}
</style>