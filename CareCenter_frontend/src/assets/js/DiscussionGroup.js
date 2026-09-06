import request from '@/utils/request'
import { getCurrentUser, getCurrentIdentity } from '@/utils/sessionHelper'

export default {
    name: 'DiscussionGroup',
    data() {
        return {
            messages: [],
            newMessage: '',
            lastMessageId: 0,
            pollTimer: null,
            loading: false,
            cachedUser: null,
            cachedIdentity: null,
            userReady: false
        }
    },
    computed: {
        currentUsername() {
            const user = this.cachedUser
            return user ? (user.username || '') : ''
        },
        currentName() {
            const user = this.cachedUser
            if (!user) return '未知用户'
            return user.name || user.username || '未知用户'
        },
        currentRole() {
            const identity = this.cachedIdentity
            if (!identity) return 'unknown'
            if (identity === 'admin') return 'admin'
            if (identity === 'parent') return 'parent'
            if (identity === 'stu') return 'student'
            return 'unknown'
        },
        currentRoleLabel() {
            if (this.currentRole === 'admin') return '管理员'
            if (this.currentRole === 'parent') return '家长'
            if (this.currentRole === 'student') return '学生'
            return '用户'
        }
    },
    methods: {
        // Resolve user info, retrying if not yet available (race condition with Aside.vue init)
        resolveUserInfo() {
            return new Promise((resolve) => {
                const tryResolve = (attempts) => {
                    // Try sessionHelper first
                    let user = getCurrentUser()
                    let identity = getCurrentIdentity()

                    // Fallback: try legacy session keys
                    if (!user) {
                        const userStr = window.sessionStorage.getItem('user')
                        if (userStr) {
                            try { user = JSON.parse(userStr) } catch (e) { user = null }
                        }
                    }
                    if (!identity) {
                        const identityStr = window.sessionStorage.getItem('identity')
                        if (identityStr) {
                            try { identity = JSON.parse(identityStr) } catch (e) { identity = null }
                        }
                    }
                    // Also try currentIdentity directly as identity value
                    if (!identity) {
                        identity = window.sessionStorage.getItem('currentIdentity') || null
                    }

                    if (user && identity) {
                        this.cachedUser = user
                        this.cachedIdentity = identity
                        this.userReady = true
                        resolve(true)
                    } else if (attempts < 10) {
                        // Retry after 300ms - Aside.vue may still be loading
                        setTimeout(() => tryResolve(attempts + 1), 300)
                    } else {
                        // Give up after 3 seconds, use whatever we have
                        this.cachedUser = user
                        this.cachedIdentity = identity
                        this.userReady = true
                        resolve(false)
                    }
                }
                tryResolve(0)
            })
        },

        // Load recent messages (chat history)
        loadRecentMessages() {
            this.loading = true
            request.get('/discussion/recent', { params: { limit: 100 } }).then(res => {
                if (res.code === '0') {
                    this.messages = res.data || []
                    if (this.messages.length > 0) {
                        this.lastMessageId = this.messages[this.messages.length - 1].id
                    }
                    this.$nextTick(() => {
                        this.scrollToBottom()
                    })
                }
                this.loading = false
            }).catch(() => {
                this.loading = false
            })
        },

        // Poll for new messages
        pollNewMessages() {
            request.get('/discussion/poll', { params: { lastId: this.lastMessageId } }).then(res => {
                if (res.code === '0' && res.data && res.data.length > 0) {
                    // Filter out messages already in the list (avoid duplicates from own sent messages)
                    const existingIds = new Set(this.messages.map(m => m.id))
                    const newMsgs = res.data.filter(m => !existingIds.has(m.id))
                    if (newMsgs.length > 0) {
                        this.messages.push(...newMsgs)
                        this.lastMessageId = res.data[res.data.length - 1].id
                        this.$nextTick(() => {
                            this.scrollToBottom()
                        })
                    }
                }
            })
        },

        // Send a message
        sendMessage() {
            if (!this.newMessage.trim()) {
                return
            }
            if (!this.cachedUser) {
                this.$message.error('用户信息未加载，请刷新页面重试')
                return
            }
            const params = {
                senderUsername: this.currentUsername,
                senderName: this.currentName,
                senderRole: this.currentRole,
                content: this.newMessage.trim()
            }
            request.post('/discussion/send', params).then(res => {
                if (res.code === '0') {
                    this.newMessage = ''
                    // Add the sent message immediately
                    if (res.data) {
                        this.messages.push(res.data)
                        this.lastMessageId = res.data.id
                        this.$nextTick(() => {
                            this.scrollToBottom()
                        })
                    }
                } else {
                    this.$message.error(res.msg || '发送失败')
                }
            })
        },

        // Delete a message (admin only)
        deleteMessage(id) {
            this.$confirm('确定要删除这条消息吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                request.delete('/discussion/delete/' + id).then(res => {
                    if (res.code === '0') {
                        this.messages = this.messages.filter(m => m.id !== id)
                        this.$message.success('删除成功')
                    } else {
                        this.$message.error(res.msg || '删除失败')
                    }
                })
            }).catch(() => {})
        },

        // Scroll chat container to bottom
        scrollToBottom() {
            const container = this.$refs.messageContainer
            if (container) {
                container.scrollTop = container.scrollHeight
            }
        },

        // Check if message is from current user
        isOwnMessage(message) {
            if (!this.currentUsername) return false
            return message.senderUsername === this.currentUsername &&
                   message.senderRole === this.currentRole
        },

        // Get role tag type for display
        getRoleTagType(role) {
            if (role === 'admin') return 'danger'
            if (role === 'parent') return 'success'
            if (role === 'student') return 'primary'
            return 'info'
        },

        // Get role display name
        getRoleDisplayName(role) {
            if (role === 'admin') return '管理员'
            if (role === 'parent') return '家长'
            if (role === 'student') return '学生'
            return '用户'
        },

        // Get avatar color based on role
        getAvatarColor(role) {
            if (role === 'admin') return '#f56c6c'
            if (role === 'parent') return '#67c23a'
            if (role === 'student') return '#409eff'
            return '#909399'
        },

        // Format time
        formatTime(timeStr) {
            if (!timeStr) return ''
            const date = new Date(timeStr)
            const now = new Date()
            const isToday = date.toDateString() === now.toDateString()
            const hours = String(date.getHours()).padStart(2, '0')
            const minutes = String(date.getMinutes()).padStart(2, '0')
            if (isToday) {
                return hours + ':' + minutes
            }
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            return month + '-' + day + ' ' + hours + ':' + minutes
        },

        // Handle Enter key to send message
        handleKeyDown(event) {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault()
                this.sendMessage()
            }
        },

        // Start polling
        startPolling() {
            this.pollTimer = setInterval(() => {
                this.pollNewMessages()
            }, 3000) // Poll every 3 seconds
        },

        // Stop polling
        stopPolling() {
            if (this.pollTimer) {
                clearInterval(this.pollTimer)
                this.pollTimer = null
            }
        }
    },
    mounted() {
        // First resolve user info (with retry for race condition), then load messages
        this.resolveUserInfo().then(() => {
            this.loadRecentMessages()
            this.startPolling()
        })
    },
    beforeUnmount() {
        this.stopPolling()
    }
}