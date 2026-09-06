<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>进出管理</el-breadcrumb-item>
      <el-breadcrumb-item>进出登记</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <el-tabs v-model="activeTab" @tab-click="onTabClick">
        <!-- Tab 1: Quick Registration -->
        <el-tab-pane label="快捷登记" name="register">
          <div style="margin: 10px 0">
            <el-input v-model="stuSearch" clearable placeholder="搜索学号或姓名" prefix-icon="Search" style="width: 200px" @clear="onStuSearchClear" />
            <el-button type="primary" style="margin-left: 5px" icon="Search" @click="onStuSearch">搜索</el-button>
            <el-button type="warning" style="margin-left: 10px" @click="batchCheckIn">批量签到(进入)</el-button>
            <el-button type="success" style="margin-left: 5px" @click="batchCheckOut">批量签退(离开)</el-button>
            <el-button type="info" style="margin-left: 10px" :disabled="selectedStudents.length === 0" @click="openMessageDialog">发送家长消息({{ selectedStudents.length }})</el-button>
            <el-button v-if="!isAllStudentsSelected" type="primary" style="margin-left: 10px" @click="selectAllStudentsAcrossPages">全选所有({{ stuTotal }}人)</el-button>
            <el-button v-else type="warning" style="margin-left: 10px" @click="clearAllSelection">取消全选</el-button>
            <span v-if="isAllStudentsSelected" style="margin-left: 10px; color: #E6A23C; font-size: 13px;">已全选所有 {{ stuTotal }} 名学生（跨页）</span>
          </div>
          <el-table ref="studentTableRef" :data="studentList" border stripe style="width: 100%" v-loading="stuLoading" row-key="username" @selection-change="handleSelectionChange" @select="handleStuSelect" @select-all="handleStuSelectAll">
            <el-table-column type="selection" width="50" :reserve-selection="true" />
            <el-table-column label="#" type="index" width="50" />
            <el-table-column prop="username" label="学号" width="120" sortable />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="gender" label="性别" width="70" />
            <el-table-column prop="phoneNum" label="手机号" width="130" />
            <el-table-column label="当前状态" width="100">
              <template #default="scope">
                <el-tag v-if="todayStatusMap[scope.row.username] === 'entry'" type="success" size="small">已签到</el-tag>
                <el-tag v-else-if="todayStatusMap[scope.row.username] === 'exit'" type="warning" size="small">已签退</el-tag>
                <el-tag v-else type="info" size="small">未签</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" :disabled="todayStatusMap[scope.row.username] === 'entry'" @click="quickRegister(scope.row, 'entry')">签到</el-button>
                <el-button type="warning" size="small" :disabled="todayStatusMap[scope.row.username] === 'exit'" @click="quickRegister(scope.row, 'exit')">签退</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin: 10px 0">
            <el-pagination
              v-model:currentPage="stuPageNum"
              :page-size="stuPageSize"
              :page-sizes="[10, 20, 50]"
              :total="stuTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleStuSizeChange"
              @current-change="handleStuCurrentChange"
            />
          </div>
        </el-tab-pane>

        <!-- Tab 2: Records View -->
        <el-tab-pane label="进出记录" name="records">
          <div style="margin: 10px 0">
            <el-input v-model="search" clearable placeholder="搜索学号或姓名" prefix-icon="Search" style="width: 200px" @clear="loadRecords" />
            <el-button type="primary" style="margin-left: 5px" icon="Search" @click="loadRecords">查询</el-button>
            <el-date-picker v-model="dateRange" type="daterange" format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="margin-left: 10px" @change="loadRecords" :teleported="true" />
            <el-button type="warning" style="margin-left: 10px" :disabled="selectedRecords.length === 0" @click="handleBatchSendAlert">批量发送提醒({{ selectedRecords.length }})</el-button>
            <el-button type="danger" style="margin-left: 5px" @click="handleSendAllAlert">一键发送所有异常提醒</el-button>
          </div>
          <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading" @selection-change="handleRecordSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="studentUsername" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="recordType" label="类型" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.recordType === 'entry' ? 'success' : 'warning'">
                  {{ scope.row.recordType === 'entry' ? '进入' : '离开' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="记录时间" width="180" />
            <el-table-column prop="adminUsername" label="登记人" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'normal' ? 'success' : 'danger'">
                  {{ scope.row.status === 'normal' ? '正常' : scope.row.status === 'late' ? '晚归' : '未归' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertSent" label="已提醒" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.alertSent === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.alertSent === 1 ? '已提醒' : '未提醒' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="appealStatus" label="申诉状态" width="100">
              <template #default="scope">
                <span v-if="scope.row.appealStatus">
                  <el-tag :type="scope.row.appealStatus === 'pending' ? 'warning' : scope.row.appealStatus === 'approved' ? 'success' : 'danger'" size="small">
                    {{ scope.row.appealStatus === 'pending' ? '待处理' : scope.row.appealStatus === 'approved' ? '已通过' : '已驳回' }}
                  </el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="scope">
                <el-button type="info" size="small" @click="openEditStatus(scope.row)">修改状态</el-button>
                <el-button v-if="scope.row.appealStatus === 'pending'" type="primary" size="small" @click="handleAppeal(scope.row, 'approved')">通过</el-button>
                <el-button v-if="scope.row.appealStatus === 'pending'" type="danger" size="small" @click="handleAppeal(scope.row, 'rejected')">驳回</el-button>
                <el-button v-if="scope.row.status !== 'normal' && scope.row.alertSent === 0" type="warning" size="small" @click="sendSingleAlert(scope.row)">发送提醒</el-button>
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
        </el-tab-pane>

        <!-- Tab 3: Manual Registration -->
        <el-tab-pane label="手动登记" name="manual">
          <div style="margin: 10px 0">
            <el-button type="primary" @click="handleAdd">单个登记</el-button>
            <el-button type="success" @click="handleBatchAdd">批量登记</el-button>
          </div>
          <p style="color: #909399; font-size: 14px; margin-top: 10px">
            提示：手动登记适用于需要补录记录的情况。时间将自动获取当前服务器时间，不可修改。<br/>
            异常记录（晚归、未归）将自动通知家长。
          </p>
        </el-tab-pane>

      </el-tabs>

      <!-- Single Add Dialog -->
      <el-dialog v-model="dialogVisible" title="单个登记" width="40%">
        <el-form :model="form" label-width="100px">
          <el-form-item label="学号">
            <el-input v-model="form.studentUsername" @blur="getStudentInfo" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.studentName" disabled />
          </el-form-item>
          <el-form-item label="记录类型">
            <el-select v-model="form.recordType" placeholder="请选择">
              <el-option label="进入" value="entry" />
              <el-option label="离开" value="exit" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" placeholder="请选择">
              <el-option label="正常" value="normal" />
              <el-option label="晚归" value="late" />
              <el-option label="未归" value="absent" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="save">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Batch Add Dialog -->
      <el-dialog v-model="batchDialogVisible" title="批量登记" width="60%">
        <el-button type="primary" size="small" @click="addBatchRow">添加行</el-button>
        <el-table :data="batchData" border style="margin-top: 10px">
          <el-table-column label="学号" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.studentUsername" @blur="getBatchStudentInfo(scope.$index)" />
            </template>
          </el-table-column>
          <el-table-column label="姓名" width="120">
            <template #default="scope">
              <el-input v-model="scope.row.studentName" disabled />
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.recordType">
                <el-option label="进入" value="entry" />
                <el-option label="离开" value="exit" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.status">
                <el-option label="正常" value="normal" />
                <el-option label="晚归" value="late" />
                <el-option label="未归" value="absent" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeBatchRow(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <template #footer>
          <span>
            <el-button @click="batchDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveBatch">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Edit Status Dialog -->
      <el-dialog v-model="editStatusDialogVisible" title="修改记录状态" width="30%">
        <el-form label-width="100px">
          <el-form-item label="学号">
            <el-input :model-value="editStatusForm.studentUsername" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input :model-value="editStatusForm.studentName" disabled />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-tag :type="editStatusForm.oldStatus === 'normal' ? 'success' : 'danger'">
              {{ editStatusForm.oldStatus === 'normal' ? '正常' : editStatusForm.oldStatus === 'late' ? '晚归' : '未归' }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新状态">
            <el-select v-model="editStatusForm.status" placeholder="请选择">
              <el-option label="正常" value="normal" />
              <el-option label="晚归" value="late" />
              <el-option label="未归" value="absent" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span>
            <el-button @click="editStatusDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveEditStatus">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Send Message to Parents Dialog -->
      <el-dialog v-model="messageDialogVisible" title="发送家长消息" width="50%">
        <p style="margin-bottom: 10px; color: #606266;">将向以下 {{ messageStudents.length }} 位学生的家长发送消息：</p>
        <el-tag v-for="s in messageStudents" :key="s.username" style="margin: 2px;">{{ s.name }}({{ s.username }})</el-tag>
        <el-form style="margin-top: 15px;" label-width="100px">
          <el-form-item label="消息标题">
            <el-input v-model="messageForm.title" placeholder="请输入消息标题" />
          </el-form-item>
          <el-form-item label="消息内容">
            <el-input type="textarea" v-model="messageForm.content" :rows="4" placeholder="请输入消息内容" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span>
            <el-button @click="messageDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="sendParentMessage">发送</el-button>
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
  name: 'EntryExitManagement',
  data() {
    return {
      activeTab: 'register',
      // Student list for quick registration
      studentList: [],
      stuPageNum: 1,
      stuPageSize: 10,
      stuTotal: 0,
      stuSearch: '',
      stuLoading: false,
      selectedStudents: [],
      isAllStudentsSelected: false,
      todayStatusMap: {},
      // Records list
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      search: '',
      dateRange: [],
      loading: false,
      selectedRecords: [],
      // Dialogs
      dialogVisible: false,
      batchDialogVisible: false,
      editStatusDialogVisible: false,
      messageDialogVisible: false,
      form: {},
      batchData: [],
      adminUsername: '',
      editStatusForm: { id: null, studentUsername: '', studentName: '', oldStatus: '', status: '' },
      messageStudents: [],
      messageForm: { title: '', content: '' }
    }
  },
  created() {
    let user = getCurrentUser()
    this.adminUsername = user ? user.username : ''
    this.loadStudents()
    this.loadTodayStatus()
    this.loadRecords()
  },
  methods: {
    onTabClick(tab) {
      if (tab.props.name === 'register') {
        this.loadTodayStatus()
      } else if (tab.props.name === 'records') {
        this.loadRecords()
      }
    },
    // Load today's status for all students
    loadTodayStatus() {
      request.get('/entryExit/todayStatus').then(res => {
        if (res.code === '0') {
          this.todayStatusMap = res.data || {}
        }
      })
    },
    // Search button click - reset cross-page selection before loading
    onStuSearch() {
      this.isAllStudentsSelected = false
      this.selectedStudents = []
      if (this.$refs.studentTableRef) {
        this.$refs.studentTableRef.clearSelection()
      }
      this.loadStudents()
    },
    // Clear search and reset cross-page selection
    onStuSearchClear() {
      this.isAllStudentsSelected = false
      this.selectedStudents = []
      if (this.$refs.studentTableRef) {
        this.$refs.studentTableRef.clearSelection()
      }
      this.loadStudents()
    },
    // Load all students for quick check-in/out
    loadStudents() {
      this.stuLoading = true
      request.get('/stu/find', {
        params: {
          pageNum: this.stuPageNum,
          pageSize: this.stuPageSize,
          search: this.stuSearch || ''
        }
      }).then(res => {
        if (res.code === '0') {
          this.studentList = res.data.records
          this.stuTotal = res.data.total
          // If all-selected mode is on, check all rows on the new page
          if (this.isAllStudentsSelected) {
            this.$nextTick(() => {
              if (this.$refs.studentTableRef) {
                this.studentList.forEach(row => {
                  this.$refs.studentTableRef.toggleRowSelection(row, true)
                })
              }
            })
          }
        }
        this.stuLoading = false
      }).catch(() => {
        this.stuLoading = false
      })
    },
    handleSelectionChange(val) {
      this.selectedStudents = val
      // If user manually deselected all on current page, turn off all-selected flag
      if (this.isAllStudentsSelected && val.length < this.studentList.length) {
        this.isAllStudentsSelected = false
      }
    },
    handleStuSelect(selection, row) {
      // Individual row select/deselect - no special handling needed, reserve-selection handles it
    },
    handleStuSelectAll(selection) {
      // When current page select-all is toggled, check if all pages were selected
      if (this.isAllStudentsSelected && selection.length < this.studentList.length) {
        // User unchecked the "select all" checkbox on current page
        this.isAllStudentsSelected = false
      }
    },
    // Select all students across all pages
    selectAllStudentsAcrossPages() {
      this.stuLoading = true
      request.get('/stu/findAll', {
        params: { search: this.stuSearch || '' }
      }).then(res => {
        if (res.code === '0') {
          this.selectedStudents = res.data || []
          this.isAllStudentsSelected = true
          // Sync table checkbox state for current page
          this.$nextTick(() => {
            if (this.$refs.studentTableRef) {
              this.studentList.forEach(row => {
                this.$refs.studentTableRef.toggleRowSelection(row, true)
              })
            }
          })
        }
        this.stuLoading = false
      }).catch(() => {
        this.stuLoading = false
      })
    },
    // Clear all selection across pages
    clearAllSelection() {
      this.isAllStudentsSelected = false
      this.selectedStudents = []
      if (this.$refs.studentTableRef) {
        this.$refs.studentTableRef.clearSelection()
      }
    },
    handleRecordSelectionChange(val) {
      this.selectedRecords = val
    },
    // Quick single register
    quickRegister(student, recordType) {
      let record = {
        studentUsername: student.username,
        studentName: student.name,
        recordType: recordType,
        status: 'normal',
        adminUsername: this.adminUsername
      }
      request.post('/entryExit/add', record).then(res => {
        if (res.code === '0') {
          this.$message.success(student.name + (recordType === 'entry' ? ' 签到成功' : ' 签退成功'))
          // Update local status map immediately
          this.todayStatusMap[student.username] = recordType
          // Force reactivity
          this.todayStatusMap = { ...this.todayStatusMap }
          this.loadRecords()
        } else {
          this.$message.error('登记失败')
        }
      })
    },
    // Batch check-in
    batchCheckIn() {
      if (this.selectedStudents.length === 0) {
        this.$message.warning('请先选择学生')
        return
      }
      let records = this.selectedStudents.map(s => ({
        studentUsername: s.username,
        studentName: s.name,
        recordType: 'entry',
        status: 'normal',
        adminUsername: this.adminUsername
      }))
      request.post('/entryExit/batchAdd', records).then(res => {
        if (res.code === '0') {
          this.$message.success('批量签到成功，共' + records.length + '人')
          // Update local status map
          this.selectedStudents.forEach(s => {
            this.todayStatusMap[s.username] = 'entry'
          })
          this.todayStatusMap = { ...this.todayStatusMap }
          this.isAllStudentsSelected = false
          this.selectedStudents = []
          if (this.$refs.studentTableRef) {
            this.$refs.studentTableRef.clearSelection()
          }
          this.loadRecords()
        } else {
          this.$message.error('批量签到失败')
        }
      })
    },
    // Batch check-out
    batchCheckOut() {
      if (this.selectedStudents.length === 0) {
        this.$message.warning('请先选择学生')
        return
      }
      let records = this.selectedStudents.map(s => ({
        studentUsername: s.username,
        studentName: s.name,
        recordType: 'exit',
        status: 'normal',
        adminUsername: this.adminUsername
      }))
      request.post('/entryExit/batchAdd', records).then(res => {
        if (res.code === '0') {
          this.$message.success('批量签退成功，共' + records.length + '人')
          // Update local status map
          this.selectedStudents.forEach(s => {
            this.todayStatusMap[s.username] = 'exit'
          })
          this.todayStatusMap = { ...this.todayStatusMap }
          this.isAllStudentsSelected = false
          this.selectedStudents = []
          if (this.$refs.studentTableRef) {
            this.$refs.studentTableRef.clearSelection()
          }
          this.loadRecords()
        } else {
          this.$message.error('批量签退失败')
        }
      })
    },
    // Load entry/exit records
    loadRecords() {
      this.loading = true
      let params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        search: this.search || ''
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }
      request.get('/entryExit/find', { params }).then(res => {
        if (res.code === '0') {
          this.tableData = res.data.records
          this.total = res.data.total
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // Manual single add
    handleAdd() {
      this.form = { recordType: 'entry', status: 'normal', adminUsername: this.adminUsername }
      this.dialogVisible = true
    },
    handleBatchAdd() {
      this.batchData = [{ recordType: 'entry', status: 'normal', adminUsername: this.adminUsername }]
      this.batchDialogVisible = true
    },
    addBatchRow() {
      this.batchData.push({ recordType: 'entry', status: 'normal', adminUsername: this.adminUsername })
    },
    removeBatchRow(index) {
      this.batchData.splice(index, 1)
    },
    getStudentInfo() {
      if (this.form.studentUsername) {
        request.get('/stu/exist/' + this.form.studentUsername).then(res => {
          if (res.code === '0') {
            this.form.studentName = res.data.name
          } else {
            this.$message.error('未找到该学生')
          }
        })
      }
    },
    getBatchStudentInfo(index) {
      if (this.batchData[index].studentUsername) {
        request.get('/stu/exist/' + this.batchData[index].studentUsername).then(res => {
          if (res.code === '0') {
            this.batchData[index].studentName = res.data.name
          }
        })
      }
    },
    save() {
      if (!this.form.studentUsername || !this.form.studentName) {
        this.$message.warning('请输入有效的学号')
        return
      }
      request.post('/entryExit/add', this.form).then(res => {
        if (res.code === '0') {
          this.$message.success('登记成功')
          this.dialogVisible = false
          this.loadTodayStatus()
          this.loadRecords()
        }
      })
    },
    saveBatch() {
      let valid = this.batchData.every(item => item.studentUsername && item.studentName)
      if (!valid) {
        this.$message.warning('请确保所有行都填写了有效学号')
        return
      }
      request.post('/entryExit/batchAdd', this.batchData).then(res => {
        if (res.code === '0') {
          this.$message.success('批量登记成功')
          this.batchDialogVisible = false
          this.loadTodayStatus()
          this.loadRecords()
        }
      })
    },
    // Appeal handling
    handleAppeal(row, status) {
      request.put('/entryExit/appealStatus', { id: row.id, appealStatus: status }).then(res => {
        if (res.code === '0') {
          this.$message.success('处理成功')
          this.loadRecords()
        }
      })
    },
    // Send batch alerts for selected records
    handleBatchSendAlert() {
      if (this.selectedRecords.length === 0) {
        this.$message.warning('请先选择要提醒的记录')
        return
      }
      // Filter only abnormal records that haven't been alerted yet
      let alertableRecords = this.selectedRecords.filter(r => r.status !== 'normal' && r.alertSent === 0)
      if (alertableRecords.length === 0) {
        this.$message.warning('所选记录中没有需要提醒的异常记录（仅状态为晚归/未归且未提醒的记录可发送提醒）')
        return
      }
      let promises = alertableRecords.map(record => {
        let alertType = record.status === 'late' ? '晚归' : '未归'
        return request.post('/entryExit/sendAlert', { id: record.id, alertType })
      })
      Promise.all(promises).then(() => {
        this.$message.success('批量提醒已发送，共' + alertableRecords.length + '条')
        this.loadRecords()
      })
    },
    // Send all alerts at once
    handleSendAllAlert() {
      request.get('/entryExit/abnormal').then(res => {
        if (res.code === '0' && res.data.length > 0) {
          let promises = res.data.map(record => {
            let alertType = record.status === 'late' ? '晚归' : '未归'
            return request.post('/entryExit/sendAlert', { id: record.id, alertType })
          })
          Promise.all(promises).then(() => {
            this.$message.success('异常提醒已全部发送，共' + res.data.length + '条')
            this.loadRecords()
          })
        } else {
          this.$message.info('暂无未提醒的异常记录')
        }
      })
    },
    sendSingleAlert(row) {
      let alertType = row.status === 'late' ? '晚归' : '未归'
      request.post('/entryExit/sendAlert', { id: row.id, alertType }).then(res => {
        if (res.code === '0') {
          this.$message.success('提醒已发送')
          this.loadRecords()
        }
      })
    },
    // Edit status
    openEditStatus(row) {
      this.editStatusForm = {
        id: row.id,
        studentUsername: row.studentUsername,
        studentName: row.studentName,
        oldStatus: row.status,
        status: row.status
      }
      this.editStatusDialogVisible = true
    },
    saveEditStatus() {
      if (this.editStatusForm.status === this.editStatusForm.oldStatus) {
        this.$message.info('状态未改变')
        this.editStatusDialogVisible = false
        return
      }
      request.put('/entryExit/updateStatus', {
        id: this.editStatusForm.id,
        status: this.editStatusForm.status
      }).then(res => {
        if (res.code === '0') {
          this.$message.success('状态修改成功')
          this.editStatusDialogVisible = false
          this.loadRecords()
        } else {
          this.$message.error('修改失败')
        }
      })
    },
    // Send message to parents
    openMessageDialog() {
      if (this.selectedStudents.length === 0) {
        this.$message.warning('请先选择学生')
        return
      }
      this.messageStudents = [...this.selectedStudents]
      this.messageForm = { title: '', content: '' }
      this.messageDialogVisible = true
    },
    sendParentMessage() {
      if (!this.messageForm.title || !this.messageForm.content) {
        this.$message.warning('请填写标题和内容')
        return
      }
      let promises = this.messageStudents.map(student => {
        return request.post('/parent/notifications/send', {
          studentUsername: student.username,
          studentName: student.name,
          title: this.messageForm.title,
          content: this.messageForm.content,
          notificationType: 'admin_message'
        })
      })
      Promise.all(promises).then(results => {
        let successCount = results.filter(r => r.code === '0').length
        this.$message.success('消息已发送给 ' + successCount + ' 位家长')
        this.messageDialogVisible = false
      }).catch(() => {
        this.$message.error('发送失败')
      })
    },
    // Pagination - Students
    handleStuSizeChange(val) {
      this.stuPageSize = val
      this.loadStudents()
    },
    handleStuCurrentChange(val) {
      this.stuPageNum = val
      this.loadStudents()
    },
    // Pagination - Records
    handleSizeChange(val) {
      this.pageSize = val
      this.loadRecords()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadRecords()
    }
  }
}
</script>