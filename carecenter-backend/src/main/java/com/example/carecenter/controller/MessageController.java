package com.example.carecenter.controller;

import com.example.carecenter.entity.Message;
import com.example.carecenter.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/{id}")
    public Optional<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id);
    }

    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return messageService.createMessage(message);
    }

    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }
}