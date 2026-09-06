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
 * 学生状况报告
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "student_status_report")
public class StudentStatusReport {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("student_username")
    private String studentUsername;

    @TableField("student_name")
    private String studentName;

    @TableField("parent_username")
    private String parentUsername;

    @TableField("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate reportDate;

    /**
     * 休息状况：normal/good/poor
     */
    @TableField("rest_status")
    private String restStatus;

    /**
     * 就餐状况：normal/good/poor
     */
    @TableField("meal_status")
    private String mealStatus;

    /**
     * 情绪状况：normal/good/poor
     */
    @TableField("mood_status")
    private String moodStatus;

    /**
     * 健康状况：normal/good/poor
     */
    @TableField("health_status")
    private String healthStatus;

    @TableField("remark")
    private String remark;

    @TableField("reporter_username")
    private String reporterUsername;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
