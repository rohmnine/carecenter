package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.DiscussionMessage;
import com.example.springboot.service.DiscussionMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discussion")
public class DiscussionController {

    @Autowired
    private DiscussionMessageService discussionMessageService;

    /**
     * Send a new message
     */
    @PostMapping("/send")
    public Result<?> sendMessage(@RequestBody Map<String, String> params) {
        String senderUsername = params.get("senderUsername");
        String senderName = params.get("senderName");
        String senderRole = params.get("senderRole");
        String content = params.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("-1", "消息内容不能为空");
        }

        DiscussionMessage message = discussionMessageService.sendMessage(
                senderUsername, senderName, senderRole, content.trim());
        return Result.success(message);
    }

    /**
     * Get recent messages (default last 50)
     */
    @GetMapping("/recent")
    public Result<?> getRecentMessages(@RequestParam(defaultValue = "50") int limit) {
        List<DiscussionMessage> messages = discussionMessageService.getRecentMessages(limit);
        return Result.success(messages);
    }

    /**
     * Get new messages after a specific ID (for polling)
     */
    @GetMapping("/poll")
    public Result<?> pollMessages(@RequestParam int lastId) {
        List<DiscussionMessage> messages = discussionMessageService.getMessagesAfterId(lastId);
        return Result.success(messages);
    }

    /**
     * Delete a message (admin only)
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteMessage(@PathVariable int id) {
        boolean success = discussionMessageService.deleteMessage(id);
        if (success) {
            return Result.success();
        }
        return Result.error("-1", "删除失败");
    }
}
