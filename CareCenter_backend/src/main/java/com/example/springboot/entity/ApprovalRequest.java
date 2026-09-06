package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一审批请求记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "approval_request", autoResultMap = true)
public class ApprovalRequest {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("request_type")
    private String requestType; // meal_reservation, visitor_appointment, leave_request
    
    @TableField("requester_username")
    private String requesterUsername;
    
    @TableField("requester_type")
    private String requesterType; // parent, student
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("status")
    private String status; // pending, approved, rejected, cancelled
    
    @TableField("reason")
    private String reason;
    
    @TableField("admin_reply")
    private String adminReply;
    
    // parentReply is an alias for adminReply (used for leave requests)
    @TableField(exist = false)
    private String parentReply;
    
    @TableField(value = "request_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> requestData;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // Transient fields for leave request data (not stored in DB, populated from requestData)
    @TableField(exist = false)
    private String leaveType;
    
    @TableField(exist = false)
    private String startTime;
    
    @TableField(exist = false)
    private String endTime;
    
    @TableField(exist = false)
    private String destination;
    
    @TableField(exist = false)
    private String contactPhone;

    @TableField(exist = false)
    private Boolean returnCancelled;

    @TableField(exist = false)
    private String returnTime;
}
