<template>
  <div class="dish-suggestion-management">
    <el-card>
      <div slot="header">
        <span>菜品建议管理</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待处理" name="pending"></el-tab-pane>
        <el-tab-pane label="已处理" name="reviewed"></el-tab-pane>
      </el-tabs>

      <el-table :data="suggestions" style="width: 100%" v-loading="loading">
        <el-table-column prop="userUsername" label="提交人" width="120"></el-table-column>
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="scope">
            {{ scope.row.userType === 'student' ? '学生' : '家长' }}
          </template>
        </el-table-column>
        <el-table-column prop="suggestionType" label="建议类型" width="120">
          <template #default="scope">
            {{ scope.row.suggestionType === 'improvement' ? '改进建议' : '新菜品' }}
          </template>
        </el-table-column>
        <el-table-column prop="dishName" label="菜品名称" width="150"></el-table-column>
        <el-table-column prop="content" label="建议内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="info">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 'reviewed'" type="warning">已处理</el-tag>
            <el-tag v-else-if="scope.row.status === 'adopted'" type="success">已采纳</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'pending'" size="small" @click="handleReply(scope.row)">回复</el-button>
            <el-button v-if="scope.row.status === 'pending'" size="small" type="success" @click="handleAdopt(scope.row)">采纳</el-button>
            <el-button v-if="scope.row.adminReply" size="small" type="info" @click="viewReply(scope.row)">查看回复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total">
      </el-pagination>
    </el-card>

    <el-dialog title="回复建议" v-model="replyDialogVisible" width="500px" :close-on-click-modal="false">
      <el-form>
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="replyContent" :rows="4"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="replyStatus">
            <el-radio label="reviewed">已查看</el-radio>
            <el-radio label="adopted">已采纳</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'DishSuggestionManagement',
  data() {
    return {
      activeTab: 'pending',
      suggestions: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      replyDialogVisible: false,
      replyContent: '',
      replyStatus: 'reviewed',
      currentSuggestion: null
    }
  },
  mounted() {
    this.loadSuggestions()
  },
  methods: {
    async loadSuggestions() {
      this.loading = true
      try {
        const res = await request.get('/dishSuggestion/find', {
          params: {
            status: this.activeTab,
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            _t: new Date().getTime()
          }
        })
        if (res.code === '0') {
          this.suggestions = res.data.records || []
          this.total = res.data.total || 0
        } else {
          this.suggestions = []
          this.total = 0
        }
      } catch (error) {
        this.$message.error('加载建议列表失败')
        this.suggestions = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    handleTabClick() {
      this.pageNum = 1
      this.loadSuggestions()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadSuggestions()
    },
    handleReply(row) {
      this.currentSuggestion = row
      this.replyContent = row.adminReply || ''
      this.replyStatus = 'reviewed'
      this.replyDialogVisible = true
    },
    handleAdopt(row) {
      this.currentSuggestion = row
      this.replyContent = ''
      this.replyStatus = 'adopted'
      this.replyDialogVisible = true
    },
    async submitReply() {
      if (!this.replyContent) {
        this.$message.warning('请填写回复内容')
        return
      }
      try {
        const res = await request.put('/dishSuggestion/update', {
          id: this.currentSuggestion.id,
          status: this.replyStatus,
          adminReply: this.replyContent
        })
        if (res.code === '0') {
          this.$message.success('回复成功')
          this.replyDialogVisible = false
          await this.loadSuggestions()
        } else {
          this.$message.error(res.msg || '回复失败')
        }
      } catch (error) {
        this.$message.error('回复失败: ' + (error.response?.data?.msg || error.message))
      }
    },
    viewReply(row) {
      this.$alert(row.adminReply, '管理员回复', {
        confirmButtonText: '确定'
      })
    }
  }
}
</script>

<style scoped>
.dish-suggestion-management {
  padding: 20px;
}
</style>