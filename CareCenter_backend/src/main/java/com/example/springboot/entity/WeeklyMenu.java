package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 每周菜谱
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "weekly_menu")
public class WeeklyMenu {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("menu_id")
    private Integer menuId;
    
    @TableField("week_day")
    private String weekDay;
    
    @TableField("meal_type")
    private String mealType;
    
    @TableField("week_start_date")
    private Date weekStartDate;
    
    @TableField("week_end_date")
    private Date weekEndDate;
    
    @TableField("create_time")
    private Date createTime;
}
