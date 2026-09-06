package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 菜品建议实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "dish_suggestion")
public class DishSuggestion {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("user_username")
    private String userUsername;
    
    @TableField("user_type")
    private String userType;
    
    @TableField("suggestion_type")
    private String suggestionType;
    
    @TableField("dish_name")
    private String dishName;
    
    @TableField("content")
    private String content;
    
    @TableField("status")
    private String status;
    
    @TableField("admin_reply")
    private String adminReply;
    
    @TableField("create_time")
    private Date createTime;
    
    @TableField("update_time")
    private Date updateTime;
}
