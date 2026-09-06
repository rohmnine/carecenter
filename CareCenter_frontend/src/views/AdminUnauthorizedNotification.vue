<template>
  <div>
    <el-card class="box-card" style="margin-bottom: 20px;">
      <div slot="header" class="clearfix">
        <span>学生未授权外出通知</span>
        <span style="float: right; color: #999; font-size: 12px;">
          当发现学生未经家长同意擅自外出时，可通过此功能通知家长
        </span>
      </div>
    </el-card>

    <el-button type="primary" @click="showNotificationDialog" style="margin-bottom: 20px;">
      发送未授权外出通知
    </el-button>

    <!-- 学生列表 -->
    <el-table :data="tableData" stripe style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="username" label="学号" width="150"></el-table-column>
      <el-table-column prop="name" label="姓名" width="150"></el-table-column>
      <el-table-column prop="gender" label="性别" width="100"></el-table-column>
      <el-table-column prop="phoneNum" label="电话" width="150"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button 
            size="mini" 
            type="warning"
            @click="sendNotification(scope.row)">
            发送未授权外出通知
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

    <!-- 发送通知对话框 -->
    <el-dialog title="发送未授权外出通知" :visible.sync="notificationDialogVisible" width="600px">
      <el-form :model="notificationForm" :rules="rules" ref="notificationForm" label-width="120px">
        <el-form-item label="学生学号" prop="studentUsername">
          <el-input v-model="notificationForm.studentUsername" placeholder="请输入学生学号"></el-input>
        </el-form-item>
        <el-form-item label="详细情况" prop="details">
          <el-input 
            type="textarea" 
            v-model="notificationForm.details" 
            :rows="5" 
            placeholder="请详细描述学生未授权外出的情况，如：时间、地点、发现经过等">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="notificationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotification">发送通知</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AdminUnauthorizedNotification',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      selectedRows: [], // 选中的行
      notificationDialogVisible: false,
      notificationForm: {
        studentUsername: '',
        details: ''
      },
      rules: {
        studentUsername: [
          { required: true, message: '请输入学生学号', trigger: 'blur' }
        ],
        details: [
          { required: true, message: '请输入详细情况', trigger: 'blur' },
          { min: 10, max: 1000, message: '详细情况长度在 10 到 1000 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadStudents();
  },
  methods: {
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    loadStudents() {
      request.get('/stu/find', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          search: ''
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
        this.$message.error('加载学生列表失败');
      });
    },
    showNotificationDialog() {
      this.notificationForm = {
        studentUsername: '',
        details: ''
      };
      this.notificationDialogVisible = true;
    },
    sendNotification(student) {
      this.notificationForm = {
        studentUsername: student.username,
        details: ''
      };
      this.notificationDialogVisible = true;
      if (this.$refs.notificationForm) {
        this.$refs.notificationForm.clearValidate();
      }
    },
    submitNotification() {
      this.$refs.notificationForm.validate((valid) => {
        if (valid) {
          // 先查询学生信息和对应的家长
          request.get(`/stu/exist/${this.notificationForm.studentUsername}`).then(studentRes => {
            if (studentRes.code === '0' || studentRes.code === 0) {
              const student = studentRes.data;
              
              // 查询学生对应的家长
              request.get(`/parent/find`, {
                params: {
                  pageNum: 1,
                  pageSize: 100,
                  search: ''
                }
              }).then(parentRes => {
                if (parentRes.code === '0' || parentRes.code === 0) {
                  const parents = parentRes.data.records;
                  const parent = parents.find(p => p.studentUsername === student.username);
                  
                  if (parent) {
                    // 发送通知
                    const notificationData = {
                      studentUsername: student.username,
                      studentName: student.name,
                      parentUsername: parent.username,
                      details: this.notificationForm.details
                    };
                    
                    request.post('/parent/notifications/unauthorized', notificationData).then(res => {
                      if (res.code === '0' || res.code === 0) {
                        this.$message.success('通知已发送给家长');
                        this.notificationDialogVisible = false;
                      } else {
                        this.$message.error(res.msg);
                      }
                    }).catch(err => {
                      console.error(err);
                      this.$message.error('发送通知失败');
                    });
                  } else {
                    this.$message.warning('该学生未绑定家长，无法发送通知');
                  }
                }
              }).catch(err => {
                console.error(err);
                this.$message.error('查询家长信息失败');
              });
            } else {
              this.$message.error('学生不存在');
            }
          }).catch(err => {
            console.error(err);
            this.$message.error('查询学生信息失败');
          });
        }
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadStudents();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.loadStudents();
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