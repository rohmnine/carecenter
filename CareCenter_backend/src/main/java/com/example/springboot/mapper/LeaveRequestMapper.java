package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.LeaveRequest;
import com.example.springboot.entity.LeaveSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生请假申请Mapper
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    @Select("SELECT student_username AS username, " +
            "SUM(CASE " +
            "WHEN DATE(end_time) < #{startDate} OR DATE(start_time) > #{endDate} THEN 0 " +
            "ELSE DATEDIFF(LEAST(DATE(end_time), #{endDate}), GREATEST(DATE(start_time), #{startDate})) + 1 " +
            "END) AS totalDays " +
            "FROM student_leave_request " +
            "WHERE status = 'approved' " +
            "AND DATE(end_time) >= #{startDate} " +
            "AND DATE(start_time) <= #{endDate} " +
            "GROUP BY student_username")
    List<LeaveSummary> selectLeaveSummaryByDate(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);
}
