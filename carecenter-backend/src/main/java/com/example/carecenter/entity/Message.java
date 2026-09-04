package com.example.carecenter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String content;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Message parentMessage;
}