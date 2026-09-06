package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分店
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@TableName(value = "center_building")
public class CenterBuilding {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("center_building_id")
    private int centerBuildingId;
    @TableField("center_building_name")
    private String centerBuildingName;
    @TableField("center_building_detail")
    private String centerBuildingDetail;

}

