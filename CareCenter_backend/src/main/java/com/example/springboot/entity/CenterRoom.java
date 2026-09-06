package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@TableName(value = "center_room")
public class CenterRoom {

    @TableId(value = "center_room_id")
    private String centerRoomId;
    @TableField("center_building_id")
    private int centerBuildingId;
    @TableField("floor_num")
    private int floorNum;
    @TableField("max_capacity")
    private int maxCapacity;
    @TableField("current_capacity")
    private int currentCapacity;
    @TableField("first_bed")
    private String firstBed;
    @TableField("second_bed")
    private String secondBed;
    @TableField("third_bed")
    private String thirdBed;
    @TableField("fourth_bed")
    private String fourthBed;

    /**
     * 数据库脚本中 center_room 暂无 version 字段，避免分页查询时 MyBatis-Plus 查询不存在的 version 列。
     */
    @TableField(exist = false)
    private Integer version;

}

