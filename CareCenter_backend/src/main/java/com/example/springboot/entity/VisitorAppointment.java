package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家长访客预约
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "visitor_appointment")
public class VisitorAppointment {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("parent_username")
    private String parentUsername;

    @TableField("parent_name")
    private String parentName;

    @TableField("student_username")
    private String studentUsername;

    @TableField("student_name")
    private String studentName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("visit_date")
    private LocalDate visitDate;

    @TableField("visit_time_slot")
    private String visitTimeSlot;

    @TableField("visitor_count")
    private Integer visitorCount;

    @TableField("reason")
    private String reason;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("status")
    private String status;

    @TableField("admin_reply")
    private String adminReply;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
