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
 * 每日菜品图片
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "daily_menu_photo")
public class DailyMenuPhoto {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("photo_date")
    private String photoDate;

    @TableField("meal_type")
    private String mealType;

    @TableField("photo_url")
    private String photoUrl;

    @TableField("description")
    private String description;

    @TableField("uploader")
    private String uploader;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
