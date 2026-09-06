package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.ApprovalRequestMapper;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.ApprovalRequestService;
import com.example.springboot.service.UserEntityService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mealReservation")
public class ParentMealReservationController {

    @Resource
    private ApprovalRequestService approvalRequestService;

    @Resource
    private ApprovalRequestMapper approvalRequestMapper;

    @Resource
    private UserEntityMapper userEntityMapper;

    @Resource
    private UserEntityService userEntityService;

    /**
     * Parent: create a meal reservation
     * POST /mealReservation/reserve
     */
    @PostMapping("/reserve")
    public Result reserve(@RequestBody Map<String, Object> params) {
        String parentUsername = (String) params.get("parentUsername");
        String reservationDate = (String) params.get("reservationDate");
        String mealType = (String) params.get("mealType");
        String studentUsername = (String) params.get("studentUsername");

        if (parentUsername == null || reservationDate == null || mealType == null) {
            return Result.error("-1", "缺少必要参数");
        }

        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("username", parentUsername).eq("user_type", "parent");
        UserEntity parent = userEntityMapper.selectOne(parentQuery);
        if (parent == null) {
            return Result.error("-1", "家长信息不存在");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername) || !boundStudents.contains(studentUsername)) {
            return Result.error("-1", "请选择已绑定学生");
        }

        LocalDate date = LocalDate.parse(reservationDate);
        if (date.isBefore(LocalDate.now())) {
            return Result.error("-1", "不能预约过去的日期");
        }

        // Check for existing reservation (use requester_username so parents without child are also supported)
        QueryWrapper<ApprovalRequest> existingQuery = new QueryWrapper<>();
        existingQuery.eq("request_type", "meal_reservation")
                .eq("requester_username", parentUsername)
                .eq("requester_type", "parent")
                .in("status", "pending", "approved", "reserved");

        List<ApprovalRequest> existing = approvalRequestService.list(existingQuery);
        for (ApprovalRequest req : existing) {
            if (req.getRequestData() != null) {
                Object dateObj = req.getRequestData().get("reservation_date");
                Object typeObj = req.getRequestData().get("meal_type");
                String reqStudent = req.getStudentUsername();
                if (reservationDate.equals(String.valueOf(dateObj))
                        && mealType.equals(String.valueOf(typeObj))
                        && studentUsername.equals(reqStudent)) {
                    return Result.error("-1", "该学生在该日期该餐次已预约，请勿重复预约");
                }
            }
        }

        // Get student name
        String studentName = null;
        QueryWrapper<UserEntity> studentQuery = new QueryWrapper<>();
        studentQuery.eq("username", studentUsername).eq("user_type", "student");
        UserEntity student = userEntityMapper.selectOne(studentQuery);
        if (student != null) {
            studentName = student.getName();
        }
        
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType("meal_reservation");
        request.setRequesterUsername(parentUsername);
        request.setRequesterType("parent");
        request.setStudentUsername(studentUsername);
        request.setStudentName(studentName);
        request.setStatus("pending");
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("reservation_date", reservationDate);
        requestData.put("meal_type", mealType);
        request.setRequestData(requestData);

        int result = approvalRequestService.createRequest(request);
        if (result > 0) {
            return Result.success();
        }
        return Result.error("-1", "预约失败");
    }

    /**
     * Parent: batch create meal reservations for multiple dates
     * POST /mealReservation/batchReserve
     */
    @PostMapping("/batchReserve")
    public Result batchReserve(@RequestBody Map<String, Object> params) {
        String parentUsername = (String) params.get("parentUsername");
        String mealType = (String) params.get("mealType");
        @SuppressWarnings("unchecked")
        List<String> dates = (List<String>) params.get("dates");
        String studentUsername = (String) params.get("studentUsername");

        if (parentUsername == null || mealType == null || dates == null || dates.isEmpty()) {
            return Result.error("-1", "缺少必要参数");
        }

        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("username", parentUsername).eq("user_type", "parent");
        UserEntity parent = userEntityMapper.selectOne(parentQuery);
        if (parent == null) {
            return Result.error("-1", "家长信息不存在");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername) || !boundStudents.contains(studentUsername)) {
            return Result.error("-1", "请选择已绑定学生");
        }

        QueryWrapper<UserEntity> studentQuery = new QueryWrapper<>();
        studentQuery.eq("username", studentUsername).eq("user_type", "student");
        UserEntity student = userEntityMapper.selectOne(studentQuery);
        String studentName = student == null ? null : student.getName();

        int successCount = 0;
        int skipCount = 0;

        for (String dateStr : dates) {
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.now())) {
                skipCount++;
                continue;
            }

            // Check existing (use requester_username so parents without child are also supported)
            QueryWrapper<ApprovalRequest> existingQuery = new QueryWrapper<>();
            existingQuery.eq("request_type", "meal_reservation")
                    .eq("requester_username", parentUsername)
                    .eq("requester_type", "parent")
                    .in("status", "pending", "approved", "reserved");

            List<ApprovalRequest> existing = approvalRequestService.list(existingQuery);
            boolean alreadyExists = false;
            for (ApprovalRequest req : existing) {
                if (req.getRequestData() != null) {
                    Object dateObj = req.getRequestData().get("reservation_date");
                    Object typeObj = req.getRequestData().get("meal_type");
                    String reqStudent = req.getStudentUsername();
                    if (dateStr.equals(String.valueOf(dateObj))
                            && mealType.equals(String.valueOf(typeObj))
                            && studentUsername.equals(reqStudent)) {
                        alreadyExists = true;
                        break;
                    }
                }
            }
            
            if (alreadyExists) {
                skipCount++;
                continue;
            }

            ApprovalRequest request = new ApprovalRequest();
            request.setRequestType("meal_reservation");
            request.setRequesterUsername(parentUsername);
            request.setRequesterType("parent");
            request.setStudentUsername(studentUsername);
            request.setStudentName(studentName);
            request.setStatus("pending");
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("reservation_date", dateStr);
            requestData.put("meal_type", mealType);
            request.setRequestData(requestData);

            int result = approvalRequestService.createRequest(request);
            if (result > 0) {
                successCount++;
            } else {
                skipCount++;
            }
        }

        return Result.success("成功预约 " + successCount + " 天" + (skipCount > 0 ? "，跳过 " + skipCount + " 天（已预约或过期）" : ""));
    }

    /**
     * Parent: cancel a reservation
     * PUT /mealReservation/cancel/{id}
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Integer id, @RequestParam String parentUsername) {
        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null) {
            return Result.error("-1", "预约记录不存在");
        }
        if (!request.getRequesterUsername().equals(parentUsername)) {
            return Result.error("-1", "无权取消此预约");
        }
        if ("cancelled".equals(request.getStatus()) || "rejected".equals(request.getStatus())) {
            return Result.error("-1", "该预约已取消");
        }
        
        // Check if meal has been confirmed - cannot cancel if already confirmed
        if (request.getRequestData() != null) {
            Object confirmedObj = request.getRequestData().get("mealConfirmed");
            if ("true".equals(String.valueOf(confirmedObj))) {
                return Result.error("-1", "该预约已确认就餐，无法取消");
            }
        }
        
        // Check if past date
        if (request.getRequestData() != null) {
            Object dateObj = request.getRequestData().get("reservation_date");
            if (dateObj != null) {
                LocalDate reservationDate = LocalDate.parse(String.valueOf(dateObj));
                if (reservationDate.isBefore(LocalDate.now())) {
                    return Result.error("-1", "不能取消过去的预约");
                }
            }
        }
        
        int result = approvalRequestService.cancel(id);
        if (result > 0) {
            return Result.success();
        }
        return Result.error("-1", "取消失败");
    }

    /**
     * Parent: get my reservations for a month
     * GET /mealReservation/parent/{parentUsername}?month=2026-03
     */
    @GetMapping("/parent/{parentUsername}")
    public Result getByParent(@PathVariable String parentUsername,
                              @RequestParam(required = false) String month,
                              @RequestParam(required = false) String studentUsername) {
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername) || !boundStudents.contains(studentUsername)) {
            return Result.success(java.util.Collections.emptyList());
        }

        List<ApprovalRequest> list = approvalRequestService.getByRequester(parentUsername);

        final String filterMonth = month;
        final String selectedStudent = studentUsername;
        list = list.stream()
                .filter(req -> "meal_reservation".equals(req.getRequestType()))
                .filter(req -> selectedStudent.equals(req.getStudentUsername()))
                .filter(req -> {
                    if (req.getRequestData() != null) {
                        Object dateObj = req.getRequestData().get("reservation_date");
                        if (dateObj != null) {
                            String dateStr = String.valueOf(dateObj);
                            return dateStr.startsWith(filterMonth);
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        return Result.success(list);
    }

    /**
     * Admin: get all reservations with pagination
     * GET /mealReservation/page?pageNum=1&pageSize=10&search=&date=&mealType=
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String date,
                           @RequestParam(required = false) String mealType,
                           @RequestParam(required = false) String status) {
        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "meal_reservation", status, search);
        
        // Additional filtering by date and mealType if needed
        if (date != null || mealType != null) {
            List<ApprovalRequest> filtered = page.getRecords().stream()
                    .filter(req -> {
                        if (req.getRequestData() == null) return false;
                        boolean matchDate = date == null || date.equals(String.valueOf(req.getRequestData().get("reservation_date")));
                        boolean matchType = mealType == null || mealType.equals(String.valueOf(req.getRequestData().get("meal_type")));
                        return matchDate && matchType;
                    })
                    .collect(Collectors.toList());
            page.setRecords(filtered);
        }
        
        return Result.success(page);
    }

    /**
     * Admin: get today's reservations
     * GET /mealReservation/today
     */
    @GetMapping("/today")
    public Result getTodayReservations() {
        String today = LocalDate.now().toString();
        QueryWrapper<ApprovalRequest> query = new QueryWrapper<>();
        query.eq("request_type", "meal_reservation")
                .in("status", "approved", "reserved");
        
        List<ApprovalRequest> all = approvalRequestService.list(query);
        List<ApprovalRequest> todayList = all.stream()
                .filter(req -> {
                    if (req.getRequestData() != null) {
                        Object dateObj = req.getRequestData().get("reservation_date");
                        return today.equals(String.valueOf(dateObj));
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        return Result.success(todayList);
    }

    /**
     * Admin: get reservation statistics
     * GET /mealReservation/statistics?startDate=&endDate=
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<ApprovalRequest> query = new QueryWrapper<>();
        query.eq("request_type", "meal_reservation")
                .in("status", "approved", "reserved");
        
        List<ApprovalRequest> all = approvalRequestService.list(query);
        
        // Filter by date range
        List<ApprovalRequest> filtered = all.stream()
                .filter(req -> {
                    if (req.getRequestData() == null) return false;
                    Object dateObj = req.getRequestData().get("reservation_date");
                    if (dateObj == null) return false;
                    String dateStr = String.valueOf(dateObj);
                    boolean afterStart = startDate == null || dateStr.compareTo(startDate) >= 0;
                    boolean beforeEnd = endDate == null || dateStr.compareTo(endDate) <= 0;
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
        
        long totalReservations = filtered.size();
        long lunchCount = filtered.stream()
                .filter(req -> "lunch".equals(String.valueOf(req.getRequestData().get("meal_type"))))
                .count();
        long dinnerCount = filtered.stream()
                .filter(req -> "dinner".equals(String.valueOf(req.getRequestData().get("meal_type"))))
                .count();
        
        // Today's count
        String today = LocalDate.now().toString();
        long todayParentCount = all.stream()
                .filter(req -> {
                    if (req.getRequestData() != null) {
                        Object dateObj = req.getRequestData().get("reservation_date");
                        return today.equals(String.valueOf(dateObj));
                    }
                    return false;
                })
                .count();
        
        stats.put("totalReservations", totalReservations);
        stats.put("lunchCount", lunchCount);
        stats.put("dinnerCount", dinnerCount);
        stats.put("todayParentCount", todayParentCount);
        
        return Result.success(stats);
    }

    /**
     * Admin: approve reservation
     * PUT /mealReservation/approve/{id}
     */
    @PutMapping("/approve/{id}")
    public Result approve(@PathVariable Integer id) {
        int result = approvalRequestService.approve(id, "管理员已批准");
        if (result == 0) {
            return Result.error("-1", "预约记录不存在");
        }
        if (result == -1) {
            return Result.error("-1", "当前状态不允许通过");
        }
        return Result.success();
    }

    /**
     * Admin: reject reservation
     * PUT /mealReservation/reject/{id}
     */
    @PutMapping("/reject/{id}")
    public Result reject(@PathVariable Integer id) {
        int result = approvalRequestService.reject(id, "管理员已驳回");
        if (result == 0) {
            return Result.error("-1", "预约记录不存在");
        }
        if (result == -1) {
            return Result.error("-1", "当前状态不允许驳回");
        }
        return Result.success();
    }

    /**
     * Admin: batch approve reservations
     * POST /mealReservation/batchApprove
     */
    @PostMapping("/batchApprove")
    public Result batchApprove(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "缺少必要参数");
        }

        int successCount = 0;
        int skipCount = 0;
        int notFoundCount = 0;

        for (Integer id : ids) {
            if (id == null) {
                skipCount++;
                continue;
            }
            ApprovalRequest request = approvalRequestService.getById(id);
            if (request == null) {
                notFoundCount++;
                continue;
            }
            if (!"meal_reservation".equals(request.getRequestType()) || !"pending".equals(request.getStatus())) {
                skipCount++;
                continue;
            }
            int result = approvalRequestService.approve(id, "管理员已批准");
            if (result > 0) {
                successCount++;
            } else {
                skipCount++;
            }
        }

        String msg = "批量通过成功 " + successCount + " 条" +
                (skipCount > 0 ? "，跳过 " + skipCount + " 条" : "") +
                (notFoundCount > 0 ? "，未找到 " + notFoundCount + " 条" : "");
        return Result.success(msg);
    }

    /**
     * Admin: confirm meal (record actual dining)
     * PUT /mealReservation/confirmMeal/{id}
     */
    @PutMapping("/confirmMeal/{id}")
    public Result confirmMeal(@PathVariable Integer id) {
        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null) {
            return Result.error("-1", "预约记录不存在");
        }
        if (!"approved".equals(request.getStatus()) && !"reserved".equals(request.getStatus())) {
            return Result.error("-1", "只有已通过的预约才能确认就餐");
        }
        
        // Check if already confirmed
        if (request.getRequestData() != null && "true".equals(String.valueOf(request.getRequestData().get("mealConfirmed")))) {
            return Result.error("-1", "该预约已确认就餐");
        }
        
        // Update requestData to mark meal as confirmed
        Map<String, Object> requestData = request.getRequestData();
        if (requestData == null) {
            requestData = new HashMap<>();
        }
        requestData.put("mealConfirmed", true);
        requestData.put("mealConfirmedTime", LocalDateTime.now().toString());
        
        request.setRequestData(requestData);
        request.setUpdateTime(LocalDateTime.now());
        
        int result = approvalRequestMapper.updateById(request);
        if (result > 0) {
            return Result.success("确认就餐成功");
        }
        return Result.error("-1", "确认就餐失败");
    }

    /**
     * Admin: unconfirm meal (cancel dining confirmation)
     * PUT /mealReservation/unconfirmMeal/{id}
     */
    @PutMapping("/unconfirmMeal/{id}")
    public Result unconfirmMeal(@PathVariable Integer id) {
        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null) {
            return Result.error("-1", "预约记录不存在");
        }
        
        // Check if confirmed
        if (request.getRequestData() == null || !"true".equals(String.valueOf(request.getRequestData().get("mealConfirmed")))) {
            return Result.error("-1", "该预约尚未确认就餐");
        }
        
        // Update requestData to mark meal as unconfirmed
        Map<String, Object> requestData = request.getRequestData();
        requestData.put("mealConfirmed", false);
        requestData.remove("mealConfirmedTime");
        
        request.setRequestData(requestData);
        request.setUpdateTime(LocalDateTime.now());
        
        int result = approvalRequestMapper.updateById(request);
        if (result > 0) {
            return Result.success("取消确认就餐成功");
        }
        return Result.error("-1", "取消确认就餐失败");
    }

    /**
     * Admin: get today's reservations with meal confirmation status
     * GET /mealReservation/todayWithConfirm
     */
    @GetMapping("/todayWithConfirm")
    public Result getTodayReservationsWithConfirm() {
        String today = LocalDate.now().toString();
        QueryWrapper<ApprovalRequest> query = new QueryWrapper<>();
        query.eq("request_type", "meal_reservation")
                .in("status", "approved", "reserved");
        
        List<ApprovalRequest> all = approvalRequestService.list(query);
        List<ApprovalRequest> todayList = all.stream()
                .filter(req -> {
                    if (req.getRequestData() != null) {
                        Object dateObj = req.getRequestData().get("reservation_date");
                        return today.equals(String.valueOf(dateObj));
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        return Result.success(todayList);
    }
}
