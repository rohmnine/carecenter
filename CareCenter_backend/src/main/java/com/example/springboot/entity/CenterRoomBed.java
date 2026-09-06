package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.Version;
/**
 * 床位明细表（支持动态床位数量）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("center_room_bed")
public class CenterRoomBed {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("center_room_id")
    private String centerRoomId;

    @TableField("bed_no")
    private Integer bedNo;

    @TableField("occupant_username")
    private String occupantUsername;

    /**
     * 床位占用学生姓名，仅用于接口展示，不落库。
     */
    @TableField(exist = false)
    private String occupantName;

    @Version
    private Integer version;
}
