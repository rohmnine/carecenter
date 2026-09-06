package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user")
public class Student {
    // 告诉Mybatis-plus, 属性对应表中的字段

    @TableId(value = "username")
    private String username;
    @TableField("password")
    private String password;
    @TableField("name")
    private String name;
    @TableField("age")
    private int age;
    @TableField("gender")
    private String gender;
    @TableField("phone_num")
    private String phoneNum;
    @TableField("email")
    private String email;
    @TableField("boarding_type")
    private String boardingType;
    @TableField("user_type")
    private String userType;
}

