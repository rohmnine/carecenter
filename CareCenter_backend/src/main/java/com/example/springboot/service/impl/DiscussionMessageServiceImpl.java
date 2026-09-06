package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.DiscussionMessage;
import com.example.springboot.mapper.DiscussionMessageMapper;
import com.example.springboot.service.DiscussionMessageService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class DiscussionMessageServiceImpl extends ServiceImpl<DiscussionMessageMapper, DiscussionMessage>
        implements DiscussionMessageService {

    @Override
    public DiscussionMessage sendMessage(String senderUsername, String senderName, String senderRole, String content) {
        DiscussionMessage message = new DiscussionMessage();
        message.setSenderUsername(senderUsername);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setCreateTime(new Date());
        this.save(message);
        return message;
    }

    @Override
    public List<DiscussionMessage> getRecentMessages(int limit) {
        QueryWrapper<DiscussionMessage> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + limit);
        List<DiscussionMessage> messages = this.list(wrapper);
        // Reverse to get chronological order
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<DiscussionMessage> getMessagesAfterId(int lastId) {
        QueryWrapper<DiscussionMessage> wrapper = new QueryWrapper<>();
        wrapper.gt("id", lastId);
        wrapper.orderByAsc("id");
        return this.list(wrapper);
    }

    @Override
    public boolean deleteMessage(int id) {
        return this.removeById(id);
    }
}
