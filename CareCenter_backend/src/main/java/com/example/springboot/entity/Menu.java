package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 菜谱
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "menu")
public class Menu {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("dish_name")
    private String dishName;
    
    @TableField("category")
    private String category;
    
    @TableField("description")
    private String description;
    
    @TableField("price")
    private BigDecimal price;
    
    @TableField("image_url")
    private String imageUrl;
    
    @TableField("nutrition_info")
    private String nutritionInfo;
    
    @TableField("create_time")
    private Date createTime;
}
