import request from "@/utils/request";

export default {
  name: 'DishRatingFeedback',
  data() {
    return {
      mySuggestions: [],
      suggestionPage: 1,
      suggestionPageSize: 10,
      suggestionTotal: 0,
      suggestionDialogVisible: false,
      suggestionForm: {
        suggestionType: 'improvement',
        dishName: '',
        content: ''
      }
    }
  },
  mounted() {
    this.loadMySuggestions()
  },
  methods: {
    async loadMySuggestions() {
      const user = JSON.parse(sessionStorage.getItem('user') || '{}')
      let identity = sessionStorage.getItem('identity')
      try {
        identity = JSON.parse(identity)
      } catch (e) {}
      const userType = (identity === '家长' || identity === 'parent') ? 'parent' : 'student'
      
      console.log('加载我的建议列表:', { username: user.username, userType, identity })
      
      try {
        const res = await request.get('/dishSuggestion/myList', {
          params: {
            userUsername: user.username,
            userType: userType,
            pageNum: this.suggestionPage,
            pageSize: this.suggestionPageSize
          }
        })
        console.log('查询结果:', res)
        if (res.code === '0') {
          this.mySuggestions = res.data.records || []
          this.suggestionTotal = res.data.total || 0
          console.log('建议列表:', this.mySuggestions)
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      } catch (error) {
        console.error('加载错误:', error)
        this.$message.error('加载建议列表失败')
      }
    },
    openSuggestionDialog() {
      this.suggestionForm = {
        suggestionType: 'improvement',
        dishName: '',
        content: ''
      }
      this.suggestionDialogVisible = true
    },
    async submitSuggestion() {
      if (!this.suggestionForm.content) {
        this.$message.warning('请填写建议内容')
        return
      }
      
      const user = JSON.parse(sessionStorage.getItem('user') || '{}')
      let identity = sessionStorage.getItem('identity')
      try {
        identity = JSON.parse(identity)
      } catch (e) {}
      const userType = (identity === '家长' || identity === 'parent') ? 'parent' : 'student'
      
      const data = {
        userUsername: user.username,
        userType: userType,
        suggestionType: this.suggestionForm.suggestionType,
        dishName: this.suggestionForm.dishName,
        content: this.suggestionForm.content
      }
      
      console.log('提交建议数据:', data)
      console.log('用户信息:', user)
      console.log('身份类型:', sessionStorage.getItem('identity'))
      
      try {
        const res = await request.post('/dishSuggestion/add', data)
        console.log('提交响应:', res)
        if (res && res.code === '0') {
          this.$message.success('建议已提交')
          this.suggestionDialogVisible = false
          await this.loadMySuggestions()
        } else {
          this.$message.error(res?.msg || '提交失败')
        }
      } catch (error) {
        this.$message.error('提交建议失败: ' + (error.response?.data?.msg || error.message))
        console.error('提交错误:', error)
      }
    },
    handlePageChange(page) {
      this.suggestionPage = page
      this.loadMySuggestions()
    },
    viewReply(row) {
      this.$alert(row.adminReply, '管理员回复', {
        confirmButtonText: '确定'
      })
    }
  }
}