package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一用户实例
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user")
public class UserEntity {
    
    @TableId(value = "username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("name")
    private String name;
    
    @TableField("age")
    private Integer age;
    
    @TableField("gender")
    private String gender;
    
    @TableField("phone_num")
    private String phoneNum;
    
    @TableField("email")
    private String email;
    
    @TableField("user_type")
    private String userType; // student, parent, admin
    
    // 学生特有字段
    @TableField("boarding_type")
    private String boardingType;

    // 家长特有字段（数据库中逗号分隔存储）
    @TableField("student_username")
    private String studentUsername;

    // 家长特有扩展字段（仅用于接口传输，不落库）
    @TableField(exist = false)
    private java.util.List<String> studentUsernames;

    // 管理员特有字段
    @TableField("center_building_id")
    private Integer centerBuildingId;

    // 床位信息扩展字段（仅用于接口传输，不落库）
    @TableField(exist = false)
    private String centerRoomId;

    @TableField(exist = false)
    private Integer bedNo;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
