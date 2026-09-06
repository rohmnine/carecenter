package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.CenterRoomBed;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CenterRoomBedMapper extends BaseMapper<CenterRoomBed> {

    @Select("SELECT * FROM center_room_bed WHERE center_room_id = #{centerRoomId} ORDER BY bed_no ASC")
    List<CenterRoomBed> selectByRoomId(@Param("centerRoomId") String centerRoomId);

    @Select("SELECT * FROM center_room_bed WHERE center_room_id = #{centerRoomId} AND bed_no = #{bedNo} LIMIT 1")
    CenterRoomBed selectByRoomAndBedNo(@Param("centerRoomId") String centerRoomId, @Param("bedNo") Integer bedNo);

    @Select("SELECT * FROM center_room_bed WHERE occupant_username = #{username} LIMIT 1")
    CenterRoomBed selectByOccupant(@Param("username") String username);

    @Select("SELECT * FROM center_room_bed WHERE occupant_username = #{username} LIMIT 1 FOR UPDATE")
    CenterRoomBed selectByOccupantForUpdate(@Param("username") String username);

    @Select("SELECT * FROM center_room_bed WHERE center_room_id = #{centerRoomId} AND bed_no = #{bedNo} LIMIT 1 FOR UPDATE")
    CenterRoomBed selectByRoomAndBedNoForUpdate(@Param("centerRoomId") String centerRoomId, @Param("bedNo") Integer bedNo);

    @Update("UPDATE center_room_bed SET occupant_username = #{username} WHERE center_room_id = #{centerRoomId} AND bed_no = #{bedNo} AND occupant_username IS NULL")
    int assignIfEmpty(@Param("centerRoomId") String centerRoomId, @Param("bedNo") Integer bedNo, @Param("username") String username);

    @Update("UPDATE center_room_bed SET occupant_username = NULL WHERE center_room_id = #{centerRoomId} AND bed_no = #{bedNo} AND occupant_username = #{username}")
    int releaseIfOwner(@Param("centerRoomId") String centerRoomId, @Param("bedNo") Integer bedNo, @Param("username") String username);

    @Update("UPDATE center_room_bed SET occupant_username = NULL WHERE center_room_id = #{centerRoomId} AND bed_no = #{bedNo}")
    int clearBed(@Param("centerRoomId") String centerRoomId, @Param("bedNo") Integer bedNo);

    @Delete("DELETE FROM center_room_bed WHERE center_room_id = #{centerRoomId} AND bed_no > #{maxBedNo} AND occupant_username IS NULL")
    int deleteEmptyBedsAbove(@Param("centerRoomId") String centerRoomId, @Param("maxBedNo") Integer maxBedNo);

    @Select("SELECT COUNT(1) FROM center_room_bed WHERE center_room_id = #{centerRoomId} AND bed_no > #{maxBedNo} AND occupant_username IS NOT NULL")
    Long countOccupiedBedsAbove(@Param("centerRoomId") String centerRoomId, @Param("maxBedNo") Integer maxBedNo);

    @Select("SELECT COUNT(1) FROM center_room_bed WHERE occupant_username IS NOT NULL")
    Long countAllOccupied();

    @Select("SELECT COUNT(1) FROM center_room_bed WHERE occupant_username IS NULL")
    Long countAllEmptyBeds();

    @Select("SELECT COUNT(1) FROM center_room_bed WHERE center_room_id = #{centerRoomId} AND occupant_username IS NOT NULL")
    Long countOccupiedByRoom(@Param("centerRoomId") String centerRoomId);

    @Select("SELECT COUNT(1) FROM center_room_bed b INNER JOIN center_room r ON r.center_room_id = b.center_room_id WHERE r.center_building_id = #{centerBuildingId} AND b.occupant_username IS NOT NULL")
    Long countOccupiedByBuilding(@Param("centerBuildingId") int centerBuildingId);
}
