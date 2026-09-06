<template>
  <div class="discussion-group">
    <!-- Header -->
    <div class="chat-header">
      <h2><i class="el-icon-chat-dot-round"></i> 讨论组</h2>
      <span class="online-info">当前用户：{{ currentName }}（{{ currentRoleLabel }}）</span>
    </div>

    <!-- Message Container -->
    <div class="message-container" ref="messageContainer" v-loading="loading">
      <div v-if="messages.length === 0 && !loading" class="no-messages">
        <el-empty description="暂无消息，发送第一条消息吧！"></el-empty>
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message-item', isOwnMessage(msg) ? 'own-message' : 'other-message']"
      >
        <!-- Other's message -->
        <div v-if="!isOwnMessage(msg)" class="message-row">
          <div class="avatar-wrapper">
            <el-avatar :size="36" :style="{ backgroundColor: getAvatarColor(msg.senderRole) }">
              {{ msg.senderName ? msg.senderName.charAt(0) : '?' }}
            </el-avatar>
          </div>
          <div class="message-content-wrapper">
            <div class="message-sender">
              <span class="sender-name">{{ msg.senderName }}</span>
              <el-tag :type="getRoleTagType(msg.senderRole)" size="mini" effect="plain">
                {{ getRoleDisplayName(msg.senderRole) }}
              </el-tag>
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
              <el-button
                v-if="currentRole === 'admin'"
                type="text"
                size="mini"
                style="color: #f56c6c; margin-left: 8px;"
                @click="deleteMessage(msg.id)"
              >
                删除
              </el-button>
            </div>
            <div class="message-bubble other-bubble">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <!-- Own message -->
        <div v-else class="message-row own-row">
          <div class="message-content-wrapper own-content">
            <div class="message-sender own-sender">
              <span class="message-time">{{ formatTime(msg.createTime) }}</span>
              <el-tag :type="getRoleTagType(msg.senderRole)" size="mini" effect="plain">
                {{ getRoleDisplayName(msg.senderRole) }}
              </el-tag>
              <span class="sender-name">{{ msg.senderName }}</span>
              <el-button
                v-if="currentRole === 'admin'"
                type="text"
                size="mini"
                style="color: #f56c6c; margin-left: 8px;"
                @click="deleteMessage(msg.id)"
              >
                删除
              </el-button>
            </div>
            <div class="message-bubble own-bubble">
              {{ msg.content }}
            </div>
          </div>
          <div class="avatar-wrapper">
            <el-avatar :size="36" :style="{ backgroundColor: getAvatarColor(msg.senderRole) }">
              {{ msg.senderName ? msg.senderName.charAt(0) : '?' }}
            </el-avatar>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="input-area">
      <el-input
        v-model="newMessage"
        type="textarea"
        :rows="2"
        placeholder="输入消息，按 Enter 发送..."
        resize="none"
        @keydown.enter="handleKeyDown"
      ></el-input>
      <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">
        <i class="el-icon-s-promotion"></i> 发送
      </el-button>
    </div>
  </div>
</template>

<script>
import DiscussionGroup from '@/assets/js/DiscussionGroup.js'
export default DiscussionGroup
</script>

<style scoped>
.discussion-group {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #409eff, #53a8ff);
  color: white;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
}

.chat-header .online-info {
  font-size: 13px;
  opacity: 0.9;
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f0f2f5;
}

.no-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.message-item {
  margin-bottom: 16px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.own-row {
  justify-content: flex-end;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.message-content-wrapper {
  max-width: 60%;
}

.own-content {
  text-align: right;
}

.message-sender {
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.own-sender {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 6px;
}

.sender-name {
  font-weight: 500;
  color: #606266;
  margin-right: 6px;
}

.message-time {
  color: #c0c4cc;
  font-size: 11px;
}

.message-bubble {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  text-align: left;
}

.other-bubble {
  background-color: #ffffff;
  color: #303133;
  border-top-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.own-bubble {
  background-color: #409eff;
  color: white;
  border-top-right-radius: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 15px 20px;
  background-color: #ffffff;
  border-top: 1px solid #e4e7ed;
}

.input-area .el-textarea {
  flex: 1;
}

.input-area .el-button {
  height: 54px;
  min-width: 80px;
}

/* Scrollbar styling */
.message-container::-webkit-scrollbar {
  width: 6px;
}

.message-container::-webkit-scrollbar-track {
  background: transparent;
}

.message-container::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.message-container::-webkit-scrollbar-thumb:hover {
  background-color: #909399;
}
</style>