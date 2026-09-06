<template>
  <div class="dish-suggestion-page">
    <el-card>
      <div slot="header">
        <span>菜品建议</span>
      </div>
      
      <el-button type="primary" @click="openSuggestionDialog">提交建议</el-button>

      <el-table :data="mySuggestions" style="width: 100%; margin-top: 20px">
        <el-table-column prop="suggestionType" label="建议类型" width="120">
          <template #default="scope">
            {{ scope.row.suggestionType === 'improvement' ? '改进建议' : '新菜品' }}
          </template>
        </el-table-column>
        <el-table-column prop="dishName" label="菜品名称" width="150"></el-table-column>
        <el-table-column prop="content" label="建议内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 'reviewed'" type="info">已查看</el-tag>
            <el-tag v-else-if="scope.row.status === 'adopted'" type="success">已采纳</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.adminReply" size="small" type="text" @click="viewReply(scope.row)">查看回复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @current-change="handlePageChange"
        :current-page="suggestionPage"
        :page-size="suggestionPageSize"
        layout="total, prev, pager, next"
        :total="suggestionTotal">
      </el-pagination>
    </el-card>

    <el-dialog title="提交建议" v-model="suggestionDialogVisible" width="500px">
      <el-form>
        <el-form-item label="建议类型">
          <el-radio-group v-model="suggestionForm.suggestionType">
            <el-radio label="improvement">改进建议</el-radio>
            <el-radio label="new_dish">新菜品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜品名称">
          <el-input v-model="suggestionForm.dishName" placeholder="选填"></el-input>
        </el-form-item>
        <el-form-item label="建议内容">
          <el-input type="textarea" v-model="suggestionForm.content" :rows="4" placeholder="请输入您的建议"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="suggestionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSuggestion">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script src="@/assets/js/DishRatingFeedback.js"></script>

<style scoped>
.dish-suggestion-page {
  padding: 20px;
}
</style>