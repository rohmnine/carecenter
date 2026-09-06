package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.AuthUtil;
import com.example.springboot.common.Result;
import com.example.springboot.entity.*;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.ApprovalRequestMapper;
import com.example.springboot.mapper.CenterRoomMapper;
import com.example.springboot.mapper.CenterRoomBedMapper;
import com.example.springboot.mapper.EntryExitRecordMapper;
import com.example.springboot.service.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parent")
public class ParentController {

    @Resource
    private ParentService parentService;
    
    @Resource
    private UserEntityService userEntityService;

    @Resource
    private ParentNotificationService parentNotificationService;

    @Resource
    private EntryExitRecordService entryExitRecordService;
    
    @Resource
    private ApprovalRequestService approvalRequestService;

    @Resource
    private ApprovalRequestMapper approvalRequestMapper;

    @Resource
    private CenterRoomService centerRoomService;

    @Resource
    private CenterRoomMapper centerRoomMapper;

    @Resource
    private CenterBuildingService centerBuildingService;

    @Resource
    private CenterRoomBedMapper centerRoomBedMapper;

    @Resource
    private EntryExitRecordMapper entryExitRecordMapper;

    @Resource
    private StudentStatusReportService studentStatusReportService;

    /**
     * 家长注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody UserEntity parent) {
        // Check if username already exists
        UserEntity existUser = userEntityService.getUserInfo(parent.getUsername());
        if (existUser != null) {
            return Result.error("-1", "用户名已存在");
        }
        parent.setUserType("parent");
        int i = userEntityService.addUser(parent);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "注册失败");
        }
    }

    /**
     * 添加家长信息
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody UserEntity parent) {
        parent.setUserType("parent");
        int i = userEntityService.addUser(parent);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "添加失败");
        }
    }

    /**
     * 更新家长信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody UserEntity parent) {
        int i = userEntityService.updateUser(parent);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    /**
     * 删除家长信息
     */
    @DeleteMapping("/delete/{username}")
    public Result<?> delete(@PathVariable String username) {
        int i = userEntityService.deleteUser(username);
        if (i == 1) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 查找家长信息
     */
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        Page<UserEntity> page = userEntityService.findByUserType(pageNum, pageSize, "parent", search);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 家长登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user, HttpSession session) {
        UserEntity userEntity = userEntityService.login(user.getUsername(), user.getPassword(), "parent");
        if (userEntity != null) {
            //存入session
            session.setAttribute("Identity", "parent");
            session.setAttribute("User", userEntity);
            return Result.success(userEntity);
        } else {
            return Result.error("-1", "用户名或密码错误");
        }
    }

    /**
     * 忘记密码（家长）：通过用户名+姓名重置
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String name = params.get("name");
        String newPassword = params.get("newPassword");

        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(name) || !StringUtils.hasLength(newPassword)) {
            return Result.error("-1", "参数不完整");
        }

        int result = userEntityService.resetPasswordByUsernameAndName(username, name, newPassword, "parent");
        if (result > 0) {
            return Result.success("密码修改成功");
        }
        return Result.error("-1", "用户名或姓名不匹配");
    }

    /**
     * 查询家长信息
     */
    @GetMapping("/exist/{value}")
    public Result<?> exist(@PathVariable String value) {
        UserEntity parent = userEntityService.getUserInfo(value);
        if (parent != null && "parent".equals(parent.getUserType())) {
            return Result.success(parent);
        } else {
            return Result.error("-1", "不存在该家长");
        }
    }

    /**
     * 根据学生用户名查询绑定的家长（用于学生请假申请前检查）
     */
    @GetMapping("/existByStudent/{studentUsername}")
    public Result<?> existByStudent(@PathVariable String studentUsername) {
        UserEntity parent = userEntityService.getParentByStudentUsername(studentUsername);
        if (parent != null) {
            return Result.success(parent);
        } else {
            return Result.error("-1", "未绑定家长");
        }
    }

    /**
     * 获取家长通知列表
     */
    @GetMapping("/notifications/{parentUsername}")
    public Result<?> getNotifications(@PathVariable String parentUsername,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长通知");
        }
        Page<ParentNotification> page = parentNotificationService.findByParent(pageNum, pageSize, parentUsername);
        if (page != null) {
            return Result.success(page);
        } else {
            return Result.error("-1", "查询失败");
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread/{parentUsername}")
    public Result<?> getUnreadCount(@PathVariable String parentUsername, HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长通知");
        }
        Long count = parentNotificationService.getUnreadCount(parentUsername);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/notifications/read/{id}")
    public Result<?> markAsRead(@PathVariable Integer id) {
        int result = parentNotificationService.markAsRead(id);
        if (result > 0) {
            return Result.success("标记成功");
        } else {
            return Result.error("-1", "标记失败");
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/notifications/delete/{id}")
    public Result<?> deleteNotification(@PathVariable Integer id) {
        int result = parentNotificationService.deleteNotification(id);
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 管理员发送未授权外出通知
     */
    @PostMapping("/notifications/unauthorized")
    public Result<?> sendUnauthorizedNotification(@RequestBody Map<String, String> params) {
        String studentUsername = params.get("studentUsername");
        String studentName = params.get("studentName");
        String parentUsername = params.get("parentUsername");
        String details = params.get("details");
        
        int result = parentNotificationService.sendUnauthorizedOutNotification(
            studentUsername, studentName, parentUsername, details
        );
        
        if (result > 0) {
            return Result.success("通知已发送");
        } else {
            return Result.error("-1", "发送失败");
        }
    }

    /**
     * 管理员发送自定义消息给家长（通过学生用户名自动查找家长）
     */
    @PostMapping("/notifications/send")
    public Result<?> sendMessageToParent(@RequestBody Map<String, String> params) {
        String studentUsername = params.get("studentUsername");
        String studentName = params.get("studentName");
        String title = params.get("title");
        String content = params.get("content");
        String notificationType = params.get("notificationType");

        if (studentUsername == null || title == null || content == null) {
            return Result.error("-1", "参数不完整 ");
        }

        // Look up the parent by student username
        UserEntity parent = userEntityService.getParentByStudentUsername(studentUsername);
        if (parent == null) {
            return Result.error("-1", "该学生未绑定家长，无法发送消息 ");
        }

        if (notificationType == null) {
            notificationType = "admin_message";
        }

        int result = parentNotificationService.sendCustomNotification(
            parent.getUsername(), studentUsername, studentName,
            notificationType, title, content
        );

        if (result > 0) {
            return Result.success("消息已发送");
        } else {
            return Result.error("-1", "发送失败");
        }
    }

    // ==================== 请假申请相关接口 ====================
    
    /**
     * 家长代学生提交请假申�?
     */
    @PostMapping("/leave/submit")
    public Result<?> submitLeaveRequest(@RequestBody Map<String, Object> params) {
        String parentUsername = (String) params.get("parentUsername");
        String studentUsername = (String) params.get("studentUsername");
        String leaveType = (String) params.get("leaveType");
        String reason = (String) params.get("reason");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        String destination = (String) params.get("destination");
        String contactPhone = (String) params.get("contactPhone");

        if (parentUsername == null || reason == null || startTime == null || endTime == null) {
            return Result.error("-1", "缺少必要参数");
        }

        LocalDateTime startDateTime = parseDateTimeFlexible(startTime);
        LocalDateTime endDateTime = parseDateTimeFlexible(endTime);
        if (startDateTime == null || endDateTime == null) {
            return Result.error("-1", "时间格式应为 YYYY-MM-DD HH:mm:ss");
        }
        if (!startDateTime.isBefore(endDateTime)) {
            return Result.error("-1", "开始时间必须早于结束时间");
        }

        // Verify parent
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在 ");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername)) {
            return Result.error("-1", "请先绑定学生");
        }
        if (!boundStudents.isEmpty() && !boundStudents.contains(studentUsername)) {
            return Result.error("-1", "仅可为已绑定学生提交请假");
        }

        // Get student info
        UserEntity student = userEntityService.getUserInfo(studentUsername);
        if (student == null || !"student".equals(student.getUserType())) {
            return Result.error("-1", "学生信息不存在 ");
        }

        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType("leave_request");
        request.setRequesterUsername(parentUsername);
        request.setRequesterType("parent");
        request.setStudentUsername(studentUsername);
        request.setStudentName(student.getName());
        request.setReason(reason);
        request.setStatus("pending"); // 等待管理员审核

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("leave_type", leaveType != null ? leaveType : "home");
        requestData.put("start_time", startTime);
        requestData.put("end_time", endTime);
        requestData.put("parent_username", parentUsername);
        if (destination != null && !destination.isEmpty()) {
            requestData.put("destination", destination);
        }
        if (contactPhone != null && !contactPhone.isEmpty()) {
            requestData.put("contact_phone", contactPhone);
        }
        request.setRequestData(requestData);

        int result = approvalRequestService.createRequest(request);

        if (result > 0) {
            return Result.success("请假申请已提交，等待管理员审核");
        } else {
            return Result.error("-1", "提交失败");
        }
    }

    /**
     * 家长查看孩子的请假申请列表
     */
    @GetMapping("/leave/list/{parentUsername}")
    public Result<?> getLeaveList(@PathVariable String parentUsername,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String month,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String studentUsername,
                                   HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长请假记录");
        }
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (boundStudents.isEmpty()) {
            return Result.success(new Page<ApprovalRequest>(pageNum, pageSize));
        }
        if (!StringUtils.hasLength(studentUsername)) {
            studentUsername = boundStudents.get(0);
        }
        if (!boundStudents.contains(studentUsername)) {
            return Result.error("-1", "只能查询已绑定学生的请假记录");
        }

        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "leave_request")
               .eq("student_username", studentUsername)
               .orderByDesc("create_time");
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        if (month != null && !month.isEmpty()) {
            wrapper.likeRight("create_time", month);
        }

        Page<ApprovalRequest> page = new Page<>(pageNum, pageSize);
        page = approvalRequestService.page(page, wrapper);
        
        // Transform data for frontend
        List<ApprovalRequest> records = page.getRecords();
        for (ApprovalRequest record : records) {
            Map<String, Object> data = record.getRequestData();
            if (data != null) {
                record.setLeaveType((String) data.get("leave_type"));
                record.setStartTime((String) data.get("start_time"));
                record.setEndTime((String) data.get("end_time"));
                record.setDestination((String) data.get("destination"));
                record.setContactPhone((String) data.get("contact_phone"));
            }
            // Set parentReply as alias for adminReply for frontend compatibility
            record.setParentReply(record.getAdminReply());
        }
        
        return Result.success(page);
    }

    /**
     * 家长审批请假申请
     */
    @PutMapping("/leave/approve")
    public Result<?> approveLeaveRequest(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        String parentReply = (String) params.get("parentReply");

        if (id == null || status == null) {
            return Result.error("-1", "缺少必要参数");
        }

        int result;
        if ("approved".equals(status)) {
            result = approvalRequestService.approve(id, parentReply != null ? parentReply : "家长已批准");
        } else if ("rejected".equals(status)) {
            result = approvalRequestService.reject(id, parentReply != null ? parentReply : "家长已拒绝");
        } else {
            return Result.error("-1", "无效的状态");
        }

        if (result > 0) {
            return Result.success("审批成功");
        } else {
            return Result.error("-1", "审批失败");
        }
    }

    /**
     * 家长查看月度请假统计
     */
    @GetMapping("/leave/monthly-stats/{parentUsername}")
    public Result<?> getMonthlyLeaveStats(@PathVariable String parentUsername,
                                          @RequestParam(required = false) String month,
                                          @RequestParam(required = false) String studentUsername,
                                          HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长请假统计");
        }
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在");
        }
        
        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (boundStudents.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        if (!StringUtils.hasLength(studentUsername)) {
            studentUsername = boundStudents.get(0);
        }
        if (!boundStudents.contains(studentUsername)) {
            return Result.error("-1", "只能查询已绑定学生的请假统计");
        }

        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "leave_request")
               .eq("student_username", studentUsername);
        
        if (month != null && !month.isEmpty()) {
            wrapper.likeRight("create_time", month);
        }

        List<ApprovalRequest> records = approvalRequestService.list(wrapper);
        
        long totalRequests = records.size();
        long approvedCount = records.stream().filter(r -> "approved".equals(r.getStatus())).count();
        long pendingCount = records.stream().filter(r -> "pending".equals(r.getStatus())).count();
        long rejectedCount = records.stream().filter(r -> "rejected".equals(r.getStatus())).count();
        
        long homeLeaveCount = records.stream()
            .filter(r -> r.getRequestData() != null && "home".equals(r.getRequestData().get("leave_type")))
            .count();
        long outLeaveCount = records.stream()
            .filter(r -> r.getRequestData() != null && "out".equals(r.getRequestData().get("leave_type")))
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", totalRequests);
        stats.put("approvedCount", approvedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("homeLeaveCount", homeLeaveCount);
        stats.put("outLeaveCount", outLeaveCount);
        
        return Result.success(stats);
    }


    /**
     * 获取家长仪表盘数据（出勤情况 + 请假统计信息
     */
    @GetMapping("/dashboard/{parentUsername}")
    public Result<?> getDashboardData(@PathVariable String parentUsername,
                                       @RequestParam(required = false) String month,
                                       @RequestParam(required = false) String studentUsername,
                                       HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长仪表盘");
        }
        // Find parent to get student info
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长不存在");
        }
        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (boundStudents.isEmpty()) {
            return Result.error("-1", "请先绑定学生");
        }
        if (!StringUtils.hasLength(studentUsername)) {
            studentUsername = boundStudents.get(0);
        }
        if (!boundStudents.contains(studentUsername)) {
            return Result.error("-1", "只能查看已绑定学生的仪表盘");
        }

        UserEntity studentUser = userEntityService.getUserInfo(studentUsername);

        // Parse month or use current month（兼容 "2026-6" 等未补零格式，非法输入回退当前月）
        YearMonth yearMonth;
        if (month != null && !month.isEmpty()) {
            try {
                yearMonth = YearMonth.parse(month.trim());
            } catch (DateTimeParseException e) {
                try {
                    String[] parts = month.trim().split("-");
                    yearMonth = YearMonth.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                } catch (Exception ex) {
                    yearMonth = YearMonth.now();
                }
            }
        } else {
            yearMonth = YearMonth.now();
        }
        
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        String monthStr = yearMonth.toString();
        
        // --- Entry/Exit stats (EntryExitRecord uses LocalDateTime, so direct comparison is fine) ---
        QueryWrapper<EntryExitRecord> entryWrapper = new QueryWrapper<>();
        entryWrapper.eq("student_username", studentUsername)
                    .ge("record_time", startOfMonth)
                    .le("record_time", endOfMonth);
        List<EntryExitRecord> entryRecords = entryExitRecordMapper.selectList(entryWrapper);
        
        long totalEntryExit = entryRecords.size();
        long entryCount = entryRecords.stream().filter(r -> "entry".equals(r.getRecordType())).count();
        long exitCount = entryRecords.stream().filter(r -> "exit".equals(r.getRecordType())).count();
        long normalCount = entryRecords.stream().filter(r -> "normal".equals(r.getStatus())).count();
        long abnormalCount = entryRecords.stream().filter(r -> "abnormal".equals(r.getStatus()) || "unauthorized".equals(r.getStatus())).count();
        
        Map<String, Object> entryExitStats = new HashMap<>();
        entryExitStats.put("totalRecords", totalEntryExit);
        entryExitStats.put("entryCount", entryCount);
        entryExitStats.put("exitCount", exitCount);
        entryExitStats.put("normalCount", normalCount);
        entryExitStats.put("abnormalCount", abnormalCount);
        
        // --- Leave stats (使用ApprovalRequestService查询请假记录) ---
        QueryWrapper<ApprovalRequest> leaveWrapper = new QueryWrapper<>();
        leaveWrapper.eq("student_username", studentUsername)
                    .eq("request_type", "leave_request")
                    .ge("create_time", startOfMonth)
                    .le("create_time", endOfMonth);
        List<ApprovalRequest> leaveRecords = approvalRequestService.list(leaveWrapper);
        
        long totalLeave = leaveRecords.size();
        long leaveApproved = leaveRecords.stream().filter(r -> "approved".equals(r.getStatus())).count();
        long leavePending = leaveRecords.stream().filter(r -> "pending".equals(r.getStatus())).count();
        long leaveRejected = leaveRecords.stream().filter(r -> "rejected".equals(r.getStatus())).count();
        
        // 从JSON数据中提取leave_type统计
        long homeLeave = leaveRecords.stream()
            .filter(r -> r.getRequestData() != null && "home".equals(r.getRequestData().get("leave_type")))
            .count();
        long outLeave = leaveRecords.stream()
            .filter(r -> r.getRequestData() != null && "out".equals(r.getRequestData().get("leave_type")))
            .count();
        
        Map<String, Object> leaveStats = new HashMap<>();
        leaveStats.put("totalRequests", totalLeave);
        leaveStats.put("approvedCount", leaveApproved);
        leaveStats.put("pendingCount", leavePending);
        leaveStats.put("rejectedCount", leaveRejected);
        leaveStats.put("homeLeaveCount", homeLeave);
        leaveStats.put("outLeaveCount", outLeave);
        
        // --- Meal Reservation stats ---
        QueryWrapper<ApprovalRequest> mealWrapper = new QueryWrapper<>();
        mealWrapper.eq("student_username", studentUsername)
                   .eq("request_type", "meal_reservation");
        List<ApprovalRequest> allMealReservations = approvalRequestService.list(mealWrapper);
        
        // Filter by month from reservation_date in request_data
        List<ApprovalRequest> monthMealReservations = allMealReservations.stream()
            .filter(r -> {
                if (r.getRequestData() != null) {
                    Object dateObj = r.getRequestData().get("reservation_date");
                    if (dateObj != null) {
                        String dateStr = String.valueOf(dateObj);
                        return dateStr.startsWith(monthStr);
                    }
                }
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
        
        long totalMealReservations = monthMealReservations.size();
        long lunchReservations = monthMealReservations.stream()
            .filter(r -> "lunch".equals(String.valueOf(r.getRequestData().get("meal_type"))))
            .count();
        long dinnerReservations = monthMealReservations.stream()
            .filter(r -> "dinner".equals(String.valueOf(r.getRequestData().get("meal_type"))))
            .count();
        long pendingMealReservations = monthMealReservations.stream()
            .filter(r -> "pending".equals(r.getStatus()))
            .count();
        long approvedMealReservations = monthMealReservations.stream()
            .filter(r -> "approved".equals(r.getStatus()) || "reserved".equals(r.getStatus()))
            .count();
        
        Map<String, Object> mealReservationStats = new HashMap<>();
        mealReservationStats.put("totalCount", totalMealReservations);
        mealReservationStats.put("lunchCount", lunchReservations);
        mealReservationStats.put("dinnerCount", dinnerReservations);
        mealReservationStats.put("pendingCount", pendingMealReservations);
        mealReservationStats.put("approvedCount", approvedMealReservations);
        
        // --- Attendance rate ---
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        int daysElapsed = yearMonth.equals(YearMonth.from(today))
            ? today.getDayOfMonth()
            : daysInMonth;
        double attendanceRate = daysElapsed > 0
            ? Math.round((double)(daysElapsed - leaveApproved) / daysElapsed * 10000) / 100.0
            : 100.0;
        
        // Combine all data
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("month", yearMonth.toString());
        dashboard.put("studentUsername", studentUsername);
        dashboard.put("studentName", studentUser == null ? studentUsername : studentUser.getName());
        dashboard.put("entryExitStats", entryExitStats);
        dashboard.put("leaveStats", leaveStats);
        dashboard.put("mealReservationStats", mealReservationStats);
        dashboard.put("attendanceRate", attendanceRate);
        dashboard.put("daysInMonth", daysInMonth);
        dashboard.put("daysElapsed", daysElapsed);
        dashboard.put("recentEntryExit", entryRecords.size() > 5 ? entryRecords.subList(0, 5) : entryRecords);
        dashboard.put("recentLeave", leaveRecords.size() > 5 ? leaveRecords.subList(0, 5) : leaveRecords);

        // --- Student status report ---
        StudentStatusReport latestStatusReport = studentStatusReportService.getLatestByStudent(studentUsername);
        List<StudentStatusReport> recentStatusReports = studentStatusReportService.listRecentByStudent(studentUsername, 5);
        dashboard.put("latestStatusReport", latestStatusReport);
        dashboard.put("recentStatusReports", recentStatusReports);

        return Result.success(dashboard);
    }

    // ==================== 访客预约相关接口 ====================

    /**
     * 家长提交访客预约
     */
    @PostMapping("/visitor/submit")
    public Result<?> submitVisitorAppointment(@RequestBody Map<String, Object> params) {
        String parentUsername = (String) params.get("parentUsername");
        if (parentUsername == null || parentUsername.isEmpty()) {
            return Result.error("-1", "家长用户名不能为空");
        }
        
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在");
        }
        
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType("visitor_appointment");
        request.setRequesterUsername(parentUsername);
        request.setRequesterType("parent");
        request.setStudentUsername(parent.getStudentUsername());
        // 获取学生姓名 - 需要查询学生信息
        UserEntity student = userEntityService.getUserInfo(parent.getStudentUsername());
        request.setStudentName(student != null ? student.getName() : "");
        request.setStatus("pending");
        request.setReason((String) params.get("reason"));
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("parent_name", params.get("parentName"));
        requestData.put("parent_phone", params.get("parentPhone"));
        requestData.put("visitor_name", params.get("visitorName"));
        requestData.put("visitor_phone", params.get("visitorPhone"));
        requestData.put("visitor_id_card", params.get("visitorIdCard"));
        requestData.put("visit_date", params.get("visitDate"));
        Object visitTime = params.get("visitTime");
        if (visitTime == null) {
            visitTime = params.get("visitTimeSlot");
        }
        requestData.put("visit_time", visitTime);
        Object visitorCount = params.get("visitorCount");
        if (visitorCount != null) {
            requestData.put("visitor_count", visitorCount);
        }
        Object visitPurpose = params.get("visitPurpose");
        if (visitPurpose == null) {
            visitPurpose = params.get("purpose");
        }
        requestData.put("visit_purpose", visitPurpose);
        request.setRequestData(requestData);
        
        int result = approvalRequestService.createRequest(request);
        if (result > 0) {
            return Result.success("预约提交成功");
        } else {
            return Result.error("-1", "提交失败");
        }
    }

    /**
     * 获取家长的访客预约列表
     */
    @GetMapping("/visitor/list/{parentUsername}")
    public Result<?> getVisitorAppointments(@PathVariable String parentUsername,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String status,
                                             HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长访客预约");
        }
        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "visitor_appointment", status, parentUsername);
        return Result.success(page);
    }

    /**
     * 获取访客预约详情
     */
    @GetMapping("/visitor/detail/{id}")
    public Result<?> getVisitorAppointmentDetail(@PathVariable Integer id) {
        ApprovalRequest appointment = approvalRequestService.getById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("-1", "预约不存在");
        }
    }

    /**
     * 取消访客预约
     */
    @PutMapping("/visitor/cancel/{id}")
    public Result<?> cancelVisitorAppointment(@PathVariable Integer id) {
        int result = approvalRequestService.cancel(id);
        if (result > 0) {
            return Result.success("取消成功");
        } else {
            return Result.error("-1", "取消失败");
        }
    }

    /**
     * 删除访客预约记录
     */
    @DeleteMapping("/visitor/delete/{id}")
    public Result<?> deleteVisitorAppointment(@PathVariable Integer id) {
        boolean result = approvalRequestService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 获取所有访客预约列表
     */
    @GetMapping("/visitor/all")
    public Result<?> getAllVisitorAppointments(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String search) {
        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "visitor_appointment", status, search);
        return Result.success(page);
    }

    /**
     * 管理员审批访客预约
     */
    @PutMapping("/visitor/approve")
    public Result<?> approveVisitorAppointment(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        String adminReply = (String) params.get("adminReply");

        if (id == null || status == null) {
            return Result.error("-1", "参数不完整");
        }

        int result;
        if ("approved".equals(status)) {
            result = approvalRequestService.approve(id, adminReply != null ? adminReply : "管理员已批准");
        } else if ("rejected".equals(status)) {
            result = approvalRequestService.reject(id, adminReply != null ? adminReply : "管理员已拒绝");
        } else {
            return Result.error("-1", "无效的状态");
        }

        if (result > 0) {
            return Result.success("审批成功");
        } else {
            return Result.error("-1", "审批失败");
        }
    }

    // ==================== 选床申请相关接口 ====================

    /**
     * 家长提交选床申请（锁位）
     */
    @PostMapping("/bed/submit")
    public Result<?> submitBedSelection(@RequestBody Map<String, Object> params) {
        String parentUsername = valueToString(params.get("parentUsername"));
        String studentUsername = valueToString(params.get("studentUsername"));
        String centerRoomId = valueToString(params.get("centerRoomId"));
        Integer bedNum = toInteger(params.get("bedNum"));
        String bedName = normalizeBedColumn(valueToString(params.get("bedName")), bedNum);
        String idemKey = valueToString(params.get("idemKey"));
        String reason = valueToString(params.get("reason"));

        if (!StringUtils.hasLength(parentUsername) || !StringUtils.hasLength(centerRoomId) || bedNum == null || !StringUtils.hasLength(bedName)) {
            return Result.error("-1", "缺少必要参数");
        }

        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername) || !boundStudents.contains(studentUsername)) {
            return Result.error("-1", "仅支持为绑定学生申请床位");
        }

        UserEntity student = userEntityService.getUserInfo(studentUsername);
        if (student == null || !"student".equals(student.getUserType())) {
            return Result.error("-1", "学生信息不存在");
        }

        // 幂等：同一请求键重复提交直接返回历史记录
        if (StringUtils.hasLength(idemKey)) {
            ApprovalRequest existed = findBedSelectionByIdemKey(parentUsername, studentUsername, idemKey);
            if (existed != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", existed.getId());
                result.put("status", existed.getStatus());
                result.put("message", "重复提交已忽略");
                return Result.success(result);
            }
        }

        // 允许已分配床位学生发起调床申请（审批通过后自动迁移床位）
        CenterRoom studentRoom = centerRoomService.judgeHadBed(studentUsername);

        CenterRoom room = centerRoomService.checkRoomExist(centerRoomId);
        if (room == null) {
            return Result.error("-1", "房间不存在");
        }

        if (bedNum <= 0 || bedNum > room.getMaxCapacity()) {
            return Result.error("-1", "床位号超出房间容量范围");
        }

        if (hasRoomBedTable()) {
            CenterRoomBed targetBed = centerRoomBedMapper.selectByRoomAndBedNo(centerRoomId, bedNum);
            if (targetBed == null) {
                return Result.error("-1", "目标床位不存在");
            }
            if (StringUtils.hasLength(targetBed.getOccupantUsername())) {
                return Result.error("-1", "目标床位已有人");
            }
        } else {
            if (bedNum > 4) {
                return Result.error("-1", "当前床位表未升级，仅支持1-4号床位");
            }
            String bedColumn = normalizeBedColumn(bedName, bedNum);
            String holder = getBedValue(room, bedColumn);
            if (StringUtils.hasLength(holder)) {
                return Result.error("-1", "目标床位已有人");
            }
        }

        // pending 锁位：同学生只能有1条待审，同床位只能被1条待审锁定
        Integer pendingByStudent = countPendingBedByStudentLocal(studentUsername, null);
        if (pendingByStudent != null && pendingByStudent > 0) {
            return Result.error("-1", "该学生已有待审批的选床申请");
        }
        Integer pendingByBed = countPendingBedLockLocal(centerRoomId, bedNum, null);
        if (pendingByBed != null && pendingByBed > 0) {
            return Result.error("-1", "该床位已被其他待审批申请锁定");
        }

        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType("bed_selection");
        request.setRequesterUsername(parentUsername);
        request.setRequesterType("parent");
        request.setStudentUsername(studentUsername);
        request.setStudentName(student.getName());
        request.setStatus("pending");
        request.setReason(StringUtils.hasLength(reason) ? reason : "家长申请选床");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("center_room_id", centerRoomId);
        requestData.put("center_building_id", room.getCenterBuildingId());
        requestData.put("bed_num", bedNum);
        requestData.put("bed_name", bedName);
        requestData.put("idem_key", idemKey);
        if (studentRoom != null) {
            requestData.put("origin_center_room_id", studentRoom.getCenterRoomId());
            String originBedName = findStudentBedColumn(studentUsername);
            Integer originBedNum = bedNumFromColumn(originBedName);
            requestData.put("origin_bed_name", originBedName);
            requestData.put("origin_bed_num", originBedNum);
        }
        requestData.put("schedule_type", params.get("scheduleType"));
        requestData.put("allergy_preference", params.get("allergyPreference"));
        requestData.put("diet_preference", params.get("dietPreference"));
        requestData.put("grade", params.get("grade"));
        requestData.put("requested_at", LocalDateTime.now().toString());
        request.setRequestData(requestData);

        int created = approvalRequestService.createRequest(request);
        if (created > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", request.getId());
            result.put("status", "pending");
            result.put("message", "选床申请已提交，等待管理员审核");
            return Result.success(result);
        }
        return Result.error("-1", "提交失败");
    }

    /**
     * 家长查看选床申请列表
     */
    @GetMapping("/bed/list/{parentUsername}")
    public Result<?> getBedSelectionList(@PathVariable String parentUsername,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String studentUsername,
                                         HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长选床申请");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (StringUtils.hasLength(studentUsername) && !boundStudents.contains(studentUsername)) {
            return Result.error("-1", "只能查询已绑定学生的选床申请");
        }

        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "bed_selection")
                .eq("requester_username", parentUsername)
                .orderByDesc("create_time");
        if (StringUtils.hasLength(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasLength(studentUsername)) {
            wrapper.eq("student_username", studentUsername);
        }

        Page<ApprovalRequest> page = new Page<>(pageNum, pageSize);
        page = approvalRequestService.page(page, wrapper);
        hydrateBedSelectionRecords(page.getRecords());
        return Result.success(page);
    }

    /**
     * 家长查看可申请床位（按性别/年级约束过滤，排除pending锁位
     */
    @GetMapping("/bed/available/{parentUsername}")
    public Result<?> getAvailableBeds(@PathVariable String parentUsername,
                                      @RequestParam(required = false) Integer centerBuildingId,
                                      @RequestParam(required = false) String studentUsername,
                                      HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限查看该家长可选床位");
        }
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长信息不存在");
        }

        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (!StringUtils.hasLength(studentUsername) && !boundStudents.isEmpty()) {
            studentUsername = boundStudents.get(0);
        }
        if (!StringUtils.hasLength(studentUsername)) {
            return Result.error("-1", "未绑定学生信息");
        }
        if (!boundStudents.contains(studentUsername)) {
            return Result.error("-1", "仅可查看已绑定学生的可选床位");
        }

        UserEntity student = userEntityService.getUserInfo(studentUsername);
        if (student == null || !"student".equals(student.getUserType())) {
            return Result.error("-1", "学生信息不存在");
        }

        Map<Integer, String> buildNameMap = buildNameMap();
        List<CenterRoom> roomList = centerRoomService.list();
        List<Map<String, Object>> availableBeds = new ArrayList<>();

        for (CenterRoom room : roomList) {
            if (room == null) {
                continue;
            }
            if (centerBuildingId != null && centerBuildingId > 0 && room.getCenterBuildingId() != centerBuildingId) {
                continue;
            }

            int maxBed = room.getMaxCapacity();
            if (!hasRoomBedTable()) {
                maxBed = Math.min(maxBed, 4);
            }
            for (int bedNo = 1; bedNo <= maxBed; bedNo++) {
                addAvailableBed(availableBeds, room, student, bedNo, buildNameMap);
            }
        }

        return Result.success(availableBeds);
    }

    /**
     * 家长取消选床申请
     */
    @PutMapping("/bed/cancel/{id}")
    public Result<?> cancelBedSelection(@PathVariable Integer id,
                                        @RequestParam String parentUsername,
                                        HttpSession session) {
        if (!AuthUtil.isParentSelfOrAdmin(session, parentUsername)) {
            return Result.error("403", "无权限取消该家长申请");
        }
        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null || !"bed_selection".equals(request.getRequestType())) {
            return Result.error("-1", "选床申请不存在");
        }
        if (!parentUsername.equals(request.getRequesterUsername())) {
            return Result.error("-1", "无权限取消该申请");
        }
        if (!"pending".equals(request.getStatus())) {
            return Result.error("-1", "仅待审批申请可取消");
        }

        request.setStatus("cancelled");
        request.setUpdateTime(LocalDateTime.now());
        boolean ok = approvalRequestService.updateById(request);
        if (ok) {
            return Result.success("取消成功");
        }
        return Result.error("-1", "取消失败");
    }

    /**
     * 管理员查看选床申请列表
     */
    @GetMapping("/bed/all")
    public Result<?> getAllBedSelection(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String search,
                                        @RequestParam(required = false) Integer centerBuildingId) {
        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "bed_selection")
                .orderByDesc("create_time");

        if (StringUtils.hasLength(status)) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasLength(search)) {
            wrapper.and(w -> w.like("requester_username", search)
                    .or().like("student_username", search)
                    .or().like("student_name", search));
        }

        Page<ApprovalRequest> page = new Page<>(pageNum, pageSize);
        page = approvalRequestService.page(page, wrapper);

        hydrateBedSelectionRecords(page.getRecords());

        if (centerBuildingId != null) {
            List<ApprovalRequest> filtered = new ArrayList<>();
            for (ApprovalRequest req : page.getRecords()) {
                Integer buildId = toInteger(req.getRequestData() == null ? null : req.getRequestData().get("center_building_id"));
                if (buildId != null && buildId.equals(centerBuildingId)) {
                    filtered.add(req);
                }
            }
            page.setRecords(filtered);
        }

        return Result.success(page);
    }

    /**
     * 管理员审批选床申请（批准即落库占床，事后并发校验申请
     */
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/bed/approve")
    public Result<?> approveBedSelection(@RequestBody Map<String, Object> params) {
        Integer id = toInteger(params.get("id"));
        String status = valueToString(params.get("status"));
        String adminReply = valueToString(params.get("adminReply"));
        String adminUsername = valueToString(params.get("adminUsername"));

        if (id == null || !StringUtils.hasLength(status)) {
            return Result.error("-1", "参数不完整");
        }

        if (!"approved".equals(status) && !"rejected".equals(status)) {
            return Result.error("-1", "无效的审批状态");
        }

        if ("rejected".equals(status)) {
            int result = approvalRequestService.reject(id, StringUtils.hasLength(adminReply) ? adminReply : "管理员已拒绝");
            return result > 0 ? Result.success("审批成功") : Result.error("-1", "审批失败");
        }

        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null || !"bed_selection".equals(request.getRequestType())) {
            return Result.error("-1", "选床申请不存在");
        }
        if ("approved".equals(request.getStatus())) {
            return Result.success("该申请已批准，无需重复处理");
        }
        if (!"pending".equals(request.getStatus())) {
            return Result.error("-1", "仅待审批申请可执行批准");
        }

        Map<String, Object> data = request.getRequestData();
        // 兼容自定义SQL未正确映射 request_data 的场景：回退到 BaseMapper 查询再取 JSON 字段
        if (data == null || data.isEmpty()) {
            ApprovalRequest fallback = approvalRequestMapper.selectById(id);
            if (fallback != null && fallback.getRequestData() != null && !fallback.getRequestData().isEmpty()) {
                data = new HashMap<>(fallback.getRequestData());
            }
        }
        if (data == null) {
            data = new HashMap<>();
        }

        String centerRoomId = valueToString(firstNonNull(data, "center_room_id", "centerRoomId"));
        Integer bedNum = parseBedNum(firstNonNull(data, "bed_num", "bedNum"));
        String bedName = normalizeBedColumn(valueToString(firstNonNull(data, "bed_name", "bedName")), bedNum);

        if (bedNum == null && StringUtils.hasLength(bedName)) {
            bedNum = bedNumFromColumn(bedName);
        }
        if (!StringUtils.hasLength(bedName) && bedNum != null) {
            bedName = normalizeBedColumn(null, bedNum);
        }

        if (!StringUtils.hasLength(centerRoomId) || bedNum == null || !StringUtils.hasLength(bedName)) {
            return Result.error("-1", "申请数据缺失，无法审核");
        }

        // 兼容历史数据：审批时补齐标准字段，避免后续流程继续读取失�?
        data.put("center_room_id", centerRoomId);
        data.put("bed_num", bedNum);
        data.put("bed_name", bedName);

        String originRoomId = null;
        Integer originBedNo = null;
        String originBedColumn = null;
        if (hasRoomBedTable()) {
            CenterRoomBed originBed = centerRoomBedMapper.selectByOccupantForUpdate(request.getStudentUsername());
            originRoomId = originBed == null ? null : originBed.getCenterRoomId();
            originBedNo = originBed == null ? null : originBed.getBedNo();
            originBedColumn = bedNameFromNo(originBedNo);
        } else {
            CenterRoom originRoom = centerRoomMapper.selectStudentBedRowForUpdate(request.getStudentUsername());
            originRoomId = originRoom == null ? null : originRoom.getCenterRoomId();
            originBedColumn = findStudentBedColumnLegacy(originRoom, request.getStudentUsername());
            originBedNo = bedNumFromColumn(originBedColumn);
        }

        CenterRoom room = centerRoomMapper.selectByIdForUpdate(centerRoomId);
        if (room == null) {
            return Result.error("-1", "目标房间不存在");
        }
        if (bedNum <= 0 || bedNum > room.getMaxCapacity()) {
            return Result.error("-1", "床位号超出房间容量范围");
        }

        if (hasRoomBedTable()) {
            CenterRoomBed targetBed = centerRoomBedMapper.selectByRoomAndBedNoForUpdate(centerRoomId, bedNum);
            if (targetBed == null) {
                return Result.error("-1", "目标床位不存在");
            }
            if (StringUtils.hasLength(targetBed.getOccupantUsername())) {
                return Result.error("-1", "床位已被占用，请刷新后重试");
            }
        } else {
            if (bedNum > 4) {
                return Result.error("-1", "当前床位表未升级，仅支持1-4号床位");
            }
            String holder = getBedValue(room, bedName);
            if (StringUtils.hasLength(holder)) {
                return Result.error("-1", "床位已被占用，请刷新后重试");
            }
        }

        Integer requestBuildId = toInteger(firstNonNull(data, "center_building_id", "centerBuildingId"));
        if (requestBuildId != null && requestBuildId != room.getCenterBuildingId()) {
            return Result.error("-1", "申请分店与目标房间分店不一致");
        }

        // 宿管分店权限校验
        if (StringUtils.hasLength(adminUsername)) {
            UserEntity admin = userEntityService.getUserInfo(adminUsername);
            if (admin != null && "admin".equals(admin.getUserType()) && admin.getCenterBuildingId() != null) {
                if (!admin.getCenterBuildingId().equals(room.getCenterBuildingId())) {
                    return Result.error("-1", "仅可审批本分店床位申请");
                }
            }
        }

        UserEntity student = userEntityService.getUserInfo(request.getStudentUsername());
        if (student == null || !"student".equals(student.getUserType())) {
            return Result.error("-1", "学生信息不存在");
        }

        int assign;
        if (hasRoomBedTable()) {
            assign = centerRoomBedMapper.assignIfEmpty(centerRoomId, bedNum, request.getStudentUsername());
        } else {
            assign = centerRoomMapper.assignBedIfEmpty(centerRoomId, bedName, request.getStudentUsername());
        }
        if (assign <= 0) {
            return Result.error("-1", "床位已被占用，请刷新后重试");
        }

        if (StringUtils.hasLength(originRoomId) && originBedNo != null) {
            boolean sameBed = centerRoomId.equals(originRoomId) && bedNum.equals(originBedNo);
            if (!sameBed) {
                int release;
                if (hasRoomBedTable()) {
                    release = centerRoomBedMapper.releaseIfOwner(originRoomId, originBedNo, request.getStudentUsername());
                } else {
                    release = centerRoomMapper.releaseBedIfOwner(originRoomId, originBedColumn, request.getStudentUsername());
                }
                if (release <= 0) {
                    throw new RuntimeException("原床位释放失败");
                }
                data.put("origin_center_room_id", originRoomId);
                data.put("origin_bed_num", originBedNo);
                data.put("origin_bed_name", normalizeBedColumn(null, originBedNo));
            }
        }

        request.setStatus("approved");
        request.setAdminReply(StringUtils.hasLength(adminReply) ? adminReply : "管理员已批准，已完成床位分配");
        request.setUpdateTime(LocalDateTime.now());

        data.put("approved_by", adminUsername);
        data.put("approved_time", LocalDateTime.now().toString());
        request.setRequestData(data);

        int update = approvalRequestMapper.updateById(request);
        if (update <= 0) {
            throw new RuntimeException("审批状态写入失败");
        }

        refreshRoomCurrentCapacity(centerRoomId);
        if (StringUtils.hasLength(originRoomId) && !centerRoomId.equals(originRoomId)) {
            refreshRoomCurrentCapacity(originRoomId);
        }

        return Result.success("审批成功，床位已分配");
    }

    private void hydrateBedSelectionRecords(List<ApprovalRequest> records) {
        Map<Integer, String> buildNameMap = buildNameMap();
        for (ApprovalRequest record : records) {
            Map<String, Object> data = record.getRequestData();
            if (data == null) {
                data = new HashMap<>();
            }
            Integer buildId = toInteger(firstNonNull(data, "center_building_id", "centerBuildingId"));
            String centerRoomId = valueToString(firstNonNull(data, "center_room_id", "centerRoomId"));
            Integer bedNum = parseBedNum(firstNonNull(data, "bed_num", "bedNum"));
            String bedName = normalizeBedColumn(valueToString(firstNonNull(data, "bed_name", "bedName")), bedNum);

            if (bedNum == null && StringUtils.hasLength(bedName)) {
                bedNum = bedNumFromColumn(bedName);
            }
            if (!StringUtils.hasLength(bedName) && bedNum != null) {
                bedName = normalizeBedColumn(null, bedNum);
            }

            if (buildId != null) {
                data.put("center_building_id", buildId);
                data.put("center_building_name", buildNameMap.get(buildId));
            }
            if (StringUtils.hasLength(centerRoomId)) {
                data.put("center_room_id", centerRoomId);
            }
            if (bedNum != null) {
                data.put("bed_num", bedNum);
            }
            if (StringUtils.hasLength(bedName)) {
                data.put("bed_name", bedName);
            }

            record.setRequestData(data);
        }
    }

    private void addAvailableBed(List<Map<String, Object>> result,
                                 CenterRoom room,
                                 UserEntity student,
                                 int bedNum,
                                 Map<Integer, String> buildNameMap) {
        if (hasRoomBedTable()) {
            CenterRoomBed bed = centerRoomBedMapper.selectByRoomAndBedNo(room.getCenterRoomId(), bedNum);
            if (bed == null || StringUtils.hasLength(bed.getOccupantUsername())) {
                return;
            }
        } else {
            if (bedNum > 4) {
                return;
            }
            String bedColumn = normalizeBedColumn(null, bedNum);
            String holder = getBedValue(room, bedColumn);
            if (StringUtils.hasLength(holder)) {
                return;
            }
        }

        Integer lockCount = countPendingBedLockLocal(room.getCenterRoomId(), bedNum, null);
        if (lockCount != null && lockCount > 0) {
            return;
        }

        String bedName = normalizeBedColumn(null, bedNum);

        Map<String, Object> item = new HashMap<>();
        item.put("centerRoomId", room.getCenterRoomId());
        item.put("centerBuildingId", room.getCenterBuildingId());
        item.put("centerBuildingName", buildNameMap.get(room.getCenterBuildingId()));
        item.put("floorNum", room.getFloorNum());
        item.put("currentCapacity", room.getCurrentCapacity());
        item.put("maxCapacity", room.getMaxCapacity());
        item.put("bedName", bedName);
        item.put("bedNum", bedNum);
        result.add(item);
    }

    /**
     * 约束�?
     * 1) 性别必须与同寝室已入住学生一�?
     * 2) 年级（此项目无年级字段，使用年龄近似）：年龄差大�?视为不满�?
     */
    private String validateRoommateConstraint(UserEntity student, CenterRoom room, String targetBed) {
        List<String> roommates = new ArrayList<>();
        if (!"first_bed".equals(targetBed) && StringUtils.hasLength(room.getFirstBed())) {
            roommates.add(room.getFirstBed());
        }
        if (!"second_bed".equals(targetBed) && StringUtils.hasLength(room.getSecondBed())) {
            roommates.add(room.getSecondBed());
        }
        if (!"third_bed".equals(targetBed) && StringUtils.hasLength(room.getThirdBed())) {
            roommates.add(room.getThirdBed());
        }
        if (!"fourth_bed".equals(targetBed) && StringUtils.hasLength(room.getFourthBed())) {
            roommates.add(room.getFourthBed());
        }

        for (String mateUsername : roommates) {
            UserEntity mate = userEntityService.getUserInfo(mateUsername);
            if (mate == null || !"student".equals(mate.getUserType())) {
                continue;
            }

            if (StringUtils.hasLength(student.getGender()) && StringUtils.hasLength(mate.getGender())) {
                if (!student.getGender().equalsIgnoreCase(mate.getGender())) {
                    return "目标房间与学生性别不匹配";
                }
            }

            if (student.getAge() != null && mate.getAge() != null) {
                int diff = Math.abs(student.getAge() - mate.getAge());
                if (diff > 2) {
                    return "目标房间与学生年级（年龄近似）不匹配";
                }
            }
        }
        return null;
    }

    private ApprovalRequest findBedSelectionByIdemKey(String requesterUsername, String studentUsername, String idemKey) {
        if (!StringUtils.hasLength(idemKey)) {
            return null;
        }
        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "bed_selection")
                .eq("requester_username", requesterUsername)
                .eq("student_username", studentUsername)
                .orderByDesc("id");
        List<ApprovalRequest> list = approvalRequestService.list(wrapper);
        for (ApprovalRequest req : list) {
            Map<String, Object> data = req.getRequestData();
            String reqIdemKey = valueToString(firstNonNull(data, "idem_key", "idemKey"));
            if (idemKey.equals(reqIdemKey)) {
                return req;
            }
        }
        return null;
    }

    private Integer countPendingBedByStudentLocal(String studentUsername, Integer excludeId) {
        if (!StringUtils.hasLength(studentUsername)) {
            return 0;
        }
        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "bed_selection")
                .eq("status", "pending")
                .eq("student_username", studentUsername);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        Long count = approvalRequestService.count(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private Integer countPendingBedLockLocal(String centerRoomId, Integer bedNum, Integer excludeId) {
        if (!StringUtils.hasLength(centerRoomId) || bedNum == null) {
            return 0;
        }
        QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("request_type", "bed_selection")
                .eq("status", "pending");
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }

        List<ApprovalRequest> list = approvalRequestService.list(wrapper);
        int count = 0;
        for (ApprovalRequest req : list) {
            Map<String, Object> data = req.getRequestData();
            String reqRoomId = valueToString(firstNonNull(data, "center_room_id", "centerRoomId"));
            Integer reqBedNum = parseBedNum(firstNonNull(data, "bed_num", "bedNum"));
            if (centerRoomId.equals(reqRoomId) && bedNum.equals(reqBedNum)) {
                count++;
            }
        }
        return count;
    }

    private Map<Integer, String> buildNameMap() {
        Map<Integer, String> map = new HashMap<>();
        List<CenterBuilding> buildList = centerBuildingService.list();
        if (buildList == null) {
            return map;
        }
        for (CenterBuilding build : buildList) {
            map.put(build.getCenterBuildingId(), build.getCenterBuildingName());
        }
        return map;
    }

    private boolean isBedEmpty(CenterRoom room, String bedName) {
        String holder = getBedValue(room, bedName);
        return !StringUtils.hasLength(holder);
    }

    private boolean hasRoomBedTable() {
        try {
            Long exists = centerRoomMapper.existsRoomBedTable();
            return exists != null && exists > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private int countOccupiedLegacy(CenterRoom room) {
        if (room == null) {
            return 0;
        }
        int count = 0;
        if (StringUtils.hasLength(room.getFirstBed())) count++;
        if (StringUtils.hasLength(room.getSecondBed())) count++;
        if (StringUtils.hasLength(room.getThirdBed())) count++;
        if (StringUtils.hasLength(room.getFourthBed())) count++;
        return count;
    }

    private String findStudentBedColumnLegacy(CenterRoom room, String username) {
        if (room == null || !StringUtils.hasLength(username)) {
            return null;
        }
        if (username.equals(room.getFirstBed())) return "first_bed";
        if (username.equals(room.getSecondBed())) return "second_bed";
        if (username.equals(room.getThirdBed())) return "third_bed";
        if (username.equals(room.getFourthBed())) return "fourth_bed";
        return null;
    }

    private String getBedValue(CenterRoom room, String bedName) {
        if ("first_bed".equals(bedName)) {
            return room.getFirstBed();
        }
        if ("second_bed".equals(bedName)) {
            return room.getSecondBed();
        }
        if ("third_bed".equals(bedName)) {
            return room.getThirdBed();
        }
        if ("fourth_bed".equals(bedName)) {
            return room.getFourthBed();
        }
        return null;
    }

    private String normalizeBedColumn(String bedName, Integer bedNum) {
        if (StringUtils.hasLength(bedName)) {
            String normalized = bedName.trim().toLowerCase();
            if ("first_bed".equals(normalized)) return "first_bed";
            if ("second_bed".equals(normalized)) return "second_bed";
            if ("third_bed".equals(normalized)) return "third_bed";
            if ("fourth_bed".equals(normalized)) return "fourth_bed";
            if (normalized.matches("^bed_([1-9]|10)$")) {
                int no = Integer.parseInt(normalized.substring(4));
                return bedNameFromNo(no);
            }
        }
        if (bedNum == null) {
            return null;
        }
        return bedNameFromNo(bedNum);
    }

    private Integer bedNumFromColumn(String bedColumn) {
        if (!StringUtils.hasLength(bedColumn)) {
            return null;
        }
        String normalized = bedColumn.trim().toLowerCase();
        if ("first_bed".equals(normalized)) return 1;
        if ("second_bed".equals(normalized)) return 2;
        if ("third_bed".equals(normalized)) return 3;
        if ("fourth_bed".equals(normalized)) return 4;
        if (normalized.matches("^bed_([1-9]|10)$")) {
            return Integer.parseInt(normalized.substring(4));
        }
        return null;
    }

    private String bedNameFromNo(Integer bedNo) {
        if (bedNo == null || bedNo < 1 || bedNo > 10) {
            return null;
        }
        if (bedNo == 1) return "first_bed";
        if (bedNo == 2) return "second_bed";
        if (bedNo == 3) return "third_bed";
        if (bedNo == 4) return "fourth_bed";
        return "bed_" + bedNo;
    }

    private String findStudentBedColumn(String studentUsername) {
        if (!StringUtils.hasLength(studentUsername)) {
            return null;
        }
        if (hasRoomBedTable()) {
            CenterRoomBed bed = centerRoomBedMapper.selectByOccupant(studentUsername);
            if (bed == null) {
                return null;
            }
            return bedNameFromNo(bed.getBedNo());
        }
        CenterRoom legacyRoom = centerRoomMapper.selectStudentBedRowLegacy(studentUsername);
        return findStudentBedColumnLegacy(legacyRoom, studentUsername);
    }

    private void refreshRoomCurrentCapacity(String centerRoomId) {
        if (!StringUtils.hasLength(centerRoomId)) {
            return;
        }
        Integer occupiedCount = null;
        if (hasRoomBedTable()) {
            Long occupied = centerRoomBedMapper.countOccupiedByRoom(centerRoomId);
            if (occupied != null) {
                occupiedCount = occupied.intValue();
            }
        }
        if (occupiedCount == null) {
            CenterRoom room = centerRoomMapper.selectById(centerRoomId);
            occupiedCount = countOccupiedLegacy(room);
        }
        UpdateWrapper<CenterRoom> uw = new UpdateWrapper<>();
        uw.eq("center_room_id", centerRoomId);
        uw.set("current_capacity", occupiedCount == null ? 0 : occupiedCount);
        centerRoomMapper.update(null, uw);
    }

    private Object firstNonNull(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!StringUtils.hasLength(key)) {
                continue;
            }
            Object value = data.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer parseBedNum(Object value) {
        Integer direct = toInteger(value);
        if (direct != null) {
            return direct;
        }

        String text = valueToString(value);
        if (!StringUtils.hasLength(text)) {
            return null;
        }

        String normalized = normalizeBedColumn(text, null);
        if (StringUtils.hasLength(normalized)) {
            return bedNumFromColumn(normalized);
        }

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            }
        }
        if (digits.length() == 0) {
            return null;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasLength(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasLength(text) ? text : null;
    }

    private LocalDateTime parseDateTimeFlexible(String dateTimeText) {
        if (!StringUtils.hasLength(dateTimeText)) {
            return null;
        }
        String normalized = dateTimeText.trim();
        try {
            return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(normalized.replace(" ", "T"));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
