package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 家长通知
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "parent_notification")
public class ParentNotification {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("parent_username")
    private String parentUsername;
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("notification_type")
    private String notificationType; // leave_request-请假申请, unauthorized_out-未授权外出
    
    @TableField("title")
    private String title;
    
    @TableField("content")
    private String content;
    
    @TableField("related_request_id")
    private Integer relatedRequestId;
    
    @TableField("is_read")
    private Integer isRead; // 0-未读, 1-已读
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
