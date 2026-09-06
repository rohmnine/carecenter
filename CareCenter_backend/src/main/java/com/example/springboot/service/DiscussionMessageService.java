package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.DiscussionMessage;

import java.util.List;

public interface DiscussionMessageService extends IService<DiscussionMessage> {

    /**
     * Send a new message
     */
    DiscussionMessage sendMessage(String senderUsername, String senderName, String senderRole, String content);

    /**
     * Get recent messages (last N messages)
     */
    List<DiscussionMessage> getRecentMessages(int limit);

    /**
     * Get messages after a specific message ID (for polling new messages)
     */
    List<DiscussionMessage> getMessagesAfterId(int lastId);

    /**
     * Delete a message (admin only)
     */
    boolean deleteMessage(int id);
}
