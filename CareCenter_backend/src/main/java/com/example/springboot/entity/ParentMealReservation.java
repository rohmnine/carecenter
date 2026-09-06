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
 * 家长用餐预约
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "parent_meal_reservation")
public class ParentMealReservation {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("parent_username")
    private String parentUsername;

    @TableField("student_username")
    private String studentUsername;

    @TableField("student_name")
    private String studentName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("reservation_date")
    private LocalDate reservationDate;

    /** lunch(午餐)、dinner(晚餐) */
    @TableField("meal_type")
    private String mealType;

    /** pending(待审核、approved(已通过)、rejected(已驳回、cancelled(已取消、reserved(兼容历史已预约  */
    @TableField("status")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
