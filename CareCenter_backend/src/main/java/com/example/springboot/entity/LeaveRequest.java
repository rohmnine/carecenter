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
 * 学生请假/外出申请
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "student_leave_request")
public class LeaveRequest {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("parent_username")
    private String parentUsername;
    
    @TableField("leave_type")
    private String leaveType; // home-回家, out-外出
    
    @TableField("reason")
    private String reason;
    
    @TableField("destination")
    private String destination; // 外出目的地（仅外出类型需要）
    
    @TableField("contact_phone")
    private String contactPhone; // 外出联系电话（仅外出类型需要）
    
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    @TableField("status")
    private String status; // pending-待审核? approved-已批准? rejected-已拒绝?
    
    @TableField("parent_reply")
    private String parentReply;
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
