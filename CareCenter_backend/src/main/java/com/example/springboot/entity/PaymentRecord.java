package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "payment_record")
public class PaymentRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("student_username")
    private String studentUsername;
    
    @TableField("student_name")
    private String studentName;
    
    @TableField("payment_month")
    private String paymentMonth;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("status")
    private String status;
    
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("remark")
    private String remark;
}
