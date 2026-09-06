package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.entity.MealReservationSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ParentMealReservationMapper extends BaseMapper<ApprovalRequest> {

    /**
     * Count effective lunch reservations for a student in a given month
     */
    @Select("SELECT COUNT(*) FROM approval_request WHERE student_username = #{studentUsername} " +
            "AND request_type = 'meal_reservation' AND status = 'approved' " +
            "AND JSON_EXTRACT(request_data, '$.meal_type') = 'lunch' " +
            "AND JSON_EXTRACT(request_data, '$.reservation_date') >= #{startDate} " +
            "AND JSON_EXTRACT(request_data, '$.reservation_date') <= #{endDate}")
    int countLunchReservations(@Param("studentUsername") String studentUsername,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    /**
     * Count effective dinner reservations for a student in a given month
     */
    @Select("SELECT COUNT(*) FROM approval_request WHERE student_username = #{studentUsername} " +
            "AND request_type = 'meal_reservation' AND status = 'approved' " +
            "AND JSON_EXTRACT(request_data, '$.meal_type') = 'dinner' " +
            "AND JSON_EXTRACT(request_data, '$.reservation_date') >= #{startDate} " +
            "AND JSON_EXTRACT(request_data, '$.reservation_date') <= #{endDate}")
    int countDinnerReservations(@Param("studentUsername") String studentUsername,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate);

    @Select("SELECT student_username AS username, COUNT(*) AS totalCount " +
            "FROM approval_request " +
            "WHERE request_type = 'meal_reservation' " +
            "AND status = 'approved' " +
            "AND student_username IS NOT NULL " +
            "AND student_username <> '' " +
            "AND JSON_UNQUOTE(JSON_EXTRACT(request_data, '$.meal_type')) = #{mealType} " +
            "AND JSON_UNQUOTE(JSON_EXTRACT(request_data, '$.reservation_date')) >= #{startDate} " +
            "AND JSON_UNQUOTE(JSON_EXTRACT(request_data, '$.reservation_date')) <= #{endDate} " +
            "GROUP BY student_username")
    List<MealReservationSummary> selectStudentMealSummaryByDate(@Param("startDate") String startDate,
                                                                @Param("endDate") String endDate,
                                                                @Param("mealType") String mealType);

    /**
     * Count today's parent meal reservations (for admin dashboard)
     */
    @Select("SELECT COUNT(DISTINCT requester_username) FROM approval_request " +
            "WHERE request_type = 'meal_reservation' AND status = 'approved' " +
            "AND JSON_EXTRACT(request_data, '$.reservation_date') = #{date}")
    int countTodayParentReservations(@Param("date") String date);
}
