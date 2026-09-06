package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.CenterRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CenterRoomMapper extends BaseMapper<CenterRoom> {

    @Select("SELECT * FROM center_room WHERE center_room_id = #{centerRoomId} FOR UPDATE")
    CenterRoom selectByIdForUpdate(@Param("centerRoomId") String centerRoomId);

    @Select("SELECT * FROM center_room WHERE first_bed = #{username} OR second_bed = #{username} OR third_bed = #{username} OR fourth_bed = #{username} LIMIT 1 FOR UPDATE")
    CenterRoom selectStudentBedRowForUpdate(@Param("username") String username);

    @Select("SELECT * FROM center_room WHERE first_bed = #{username} OR second_bed = #{username} OR third_bed = #{username} OR fourth_bed = #{username} LIMIT 1")
    CenterRoom selectStudentBedRowLegacy(@Param("username") String username);

    @Update({
            "<script>",
            "UPDATE center_room",
            "SET",
            "  current_capacity = current_capacity + 1,",
            "  ${bedColumn} = #{studentUsername}",
            "WHERE center_room_id = #{centerRoomId}",
            "  AND ${bedColumn} IS NULL",
            "  AND current_capacity != max_capacity",
            "</script>"
    })
    int assignBedIfEmpty(@Param("centerRoomId") String centerRoomId,
                         @Param("bedColumn") String bedColumn,
                         @Param("studentUsername") String studentUsername);

    @Update({
            "<script>",
            "UPDATE center_room",
            "SET",
            "  current_capacity = CASE WHEN current_capacity > 0 THEN current_capacity - 1 ELSE 0 END,",
            "  ${bedColumn} = NULL",
            "WHERE center_room_id = #{centerRoomId}",
            "  AND ${bedColumn} = #{studentUsername}",
            "</script>"
    })
    int releaseBedIfOwner(@Param("centerRoomId") String centerRoomId,
                          @Param("bedColumn") String bedColumn,
                          @Param("studentUsername") String studentUsername);

    @Update({
            "<script>",
            "UPDATE center_room",
            "SET",
            "  current_capacity = CASE",
            "    WHEN ${bedColumn} IS NULL THEN current_capacity",
            "    WHEN current_capacity > 0 THEN current_capacity - 1",
            "    ELSE 0",
            "  END,",
            "  ${bedColumn} = NULL",
            "WHERE center_room_id = #{centerRoomId}",
            "</script>"
    })
    int clearBedLegacy(@Param("centerRoomId") String centerRoomId,
                       @Param("bedColumn") String bedColumn);

    @Select("SELECT SUM(GREATEST(max_capacity - current_capacity, 0)) FROM center_room")
    Long countEmptyBedsLegacy();

    @Select("SELECT SUM(current_capacity) FROM center_room")
    Long countOccupiedBedsLegacy();

    @Select("SELECT SUM(current_capacity) FROM center_room WHERE center_building_id = #{centerBuildingId}")
    Long countOccupiedByBuildingLegacy(@Param("centerBuildingId") int centerBuildingId);

    @Select("SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'center_room_bed'")
    Long existsRoomBedTable();
}

