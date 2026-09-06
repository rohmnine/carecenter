package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一账单记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "billing_record")
public class BillingRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("bill_month")
    private String billMonth;
    
    @TableField("bill_type")
    private String billType; // meal, payment
    
    // 餐费账单特有字段
    @TableField("boarding_type")
    private String boardingType;
    
    @TableField("breakfast_days")
    private Integer breakfastDays;
    
    @TableField("breakfast_price")
    private BigDecimal breakfastPrice;
    
    @TableField("lunch_days")
    private Integer lunchDays;
    
    @TableField("dinner_days")
    private Integer dinnerDays;
    
    @TableField("lunch_price")
    private BigDecimal lunchPrice;
    
    @TableField("dinner_price")
    private BigDecimal dinnerPrice;
    
    @TableField("boarding_fee")
    private BigDecimal boardingFee;
    
    @TableField("leave_deduct_days")
    private Integer leaveDeductDays;
    
    @TableField("meal_total")
    private BigDecimal mealTotal;
    
    // 通用字段
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("status")
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    
    @TableField("remark")
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
