package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("discussion_message")
public class DiscussionMessage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("sender_username")
    private String senderUsername;

    @TableField("sender_name")
    private String senderName;

    @TableField("sender_role")
    private String senderRole;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private String messageType;

    @TableField("image_url")
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;
}
