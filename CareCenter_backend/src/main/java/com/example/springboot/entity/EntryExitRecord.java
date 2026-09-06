package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生进出记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "entry_exit_record")
public class EntryExitRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("record_type")
    private String recordType;
    
    @TableField("record_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recordTime;
    
    @TableField("admin_username")
    private String adminUsername;
    
    @TableField("status")
    private String status;
    
    @TableField("alert_sent")
    private Integer alertSent;
    
    @TableField("appeal_reason")
    private String appealReason;
    
    @TableField("appeal_status")
    private String appealStatus;
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
