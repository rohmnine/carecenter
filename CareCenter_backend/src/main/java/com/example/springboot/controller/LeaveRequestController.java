package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.ApprovalRequestService;
import com.example.springboot.service.EntryExitRecordService;
import com.example.springboot.service.ParentNotificationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生请假申请Controller
 */
@RestController
@RequestMapping("/leaveRequest")
public class LeaveRequestController {

    @Resource
    private ApprovalRequestService approvalRequestService;

    @Resource
    private UserEntityMapper userEntityMapper;

    @Resource
    private ParentNotificationService parentNotificationService;

    @Resource
    private EntryExitRecordService entryExitRecordService;

    /**
     * 学生提交请假申请
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Map<String, Object> params) {
        String studentUsername = (String) params.get("studentUsername");
        String studentName = (String) params.get("studentName");
        String parentUsername = (String) params.get("parentUsername");
        String leaveType = (String) params.get("leaveType");
        String reason = (String) params.get("reason");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        if (studentUsername == null || reason == null || startTime == null || endTime == null) {
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

        // Get student info if not provided
        if (studentName == null || parentUsername == null) {
            QueryWrapper<UserEntity> studentQuery = new QueryWrapper<>();
            studentQuery.eq("username", studentUsername).eq("user_type", "student");
            UserEntity student = userEntityMapper.selectOne(studentQuery);
            if (student != null) {
                studentName = student.getName();
            }

            QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
            parentQuery.eq("student_username", studentUsername).eq("user_type", "parent");
            UserEntity parent = userEntityMapper.selectOne(parentQuery);
            if (parent != null) {
                parentUsername = parent.getUsername();
            }
        }

        ApprovalRequest request = new ApprovalRequest();
        request.setRequestType("leave_request");
        request.setRequesterUsername(studentUsername);
        request.setRequesterType("student");
        request.setStudentUsername(studentUsername);
        request.setStudentName(studentName);
        request.setReason(reason);
        request.setStatus("pending");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("leave_type", leaveType != null ? leaveType : "home");
        requestData.put("start_time", startTime);
        requestData.put("end_time", endTime);
        requestData.put("parent_username", parentUsername);
        requestData.put("destination", params.get("destination"));
        requestData.put("contact_phone", params.get("contactPhone"));
        requestData.put("return_cancelled", false);
        request.setRequestData(requestData);

        int result = approvalRequestService.createRequest(request);
        
        if (result > 0 && parentUsername != null) {
            // Send notification to parent
            String leaveTypeText = "home".equals(leaveType) ? "回家" : "外出";
            String content = String.format("您的孩子%s申请%s，时间：%s 至 %s，原因：%s",
                    studentName, leaveTypeText, startTime, endTime, reason);
            
            parentNotificationService.sendCustomNotification(
                    parentUsername,
                    studentUsername,
                    studentName,
                    "leave_request",
                    "学生请假申请",
                    content
            );
        }

        if (result > 0) {
            return Result.success("请假申请已提交，等待家长审批");
        } else {
            return Result.error("-1", "提交失败");
        }
    }

    /**
     * 学生查看自己的请假申请
     */
    @GetMapping("/student/{username}")
    public Result<?> getByStudent(@PathVariable String username,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "leave_request", null, username);
        
        // Filter to only show requests by this student
        List<ApprovalRequest> filtered = page.getRecords().stream()
                .filter(req -> username.equals(req.getStudentUsername()))
                .map(this::hydrateLeaveRequestFields)
                .collect(Collectors.toList());
        page.setRecords(filtered);
        
        return Result.success(page);
    }

    /**
     * 家长查看孩子的请假申请
     */
    @GetMapping("/parent/{username}")
    public Result<?> getByParent(@PathVariable String username,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        // Get parent's student
        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("username", username).eq("user_type", "parent");
        UserEntity parent = userEntityMapper.selectOne(parentQuery);
        
        if (parent == null || parent.getStudentUsername() == null) {
            return Result.success(new Page<ApprovalRequest>(pageNum, pageSize));
        }

        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "leave_request", null, parent.getStudentUsername());
        
        // Filter to only show requests for this parent's child
        List<ApprovalRequest> filtered = page.getRecords().stream()
                .filter(req -> parent.getStudentUsername().equals(req.getStudentUsername()))
                .map(this::hydrateLeaveRequestFields)
                .collect(Collectors.toList());
        page.setRecords(filtered);
        
        return Result.success(page);
    }

    /**
     * 家长审批请假申请
     */
    @PutMapping("/approve")
    public Result<?> approve(@RequestBody Map<String, Object> params) {
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
     * 查询请假申请详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Integer id) {
        ApprovalRequest request = approvalRequestService.getById(id);
        if (request != null) {
            return Result.success(hydrateLeaveRequestFields(request));
        } else {
            return Result.error("-1", "请假申请不存在");
        }
    }

    /**
     * 删除请假申请
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        boolean result = approvalRequestService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("-1", "删除失败");
        }
    }

    /**
     * 管理员查看所有请假申请
     */
    @GetMapping("/all")
    public Result<?> getAll(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(defaultValue = "") String search) {
        Page<ApprovalRequest> page = approvalRequestService.findByRequestType(pageNum, pageSize, "leave_request", null, search);
        List<ApprovalRequest> hydrated = page.getRecords().stream()
                .map(this::hydrateLeaveRequestFields)
                .collect(Collectors.toList());
        page.setRecords(hydrated);
        return Result.success(page);
    }

    /**
     * 管理员审批请假申请（当家长来不及处理时）
     */
    @PutMapping("/adminApprove")
    public Result<?> adminApprove(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        String reply = (String) params.get("parentReply");

        if (id == null || status == null) {
            return Result.error("-1", "缺少必要参数");
        }

        String adminReply = reply != null ? reply : "管理员代为审请";
        int result;
        
        if ("approved".equals(status)) {
            result = approvalRequestService.approve(id, adminReply);
        } else if ("rejected".equals(status)) {
            result = approvalRequestService.reject(id, adminReply);
        } else {
            return Result.error("-1", "无效的状态");
        }

        if (result > 0) {
            return Result.success("管理员审批成功");
        } else {
            return Result.error("-1", "审批失败");
        }
    }

    /**
     * 返校销假：结束请假并自动回填考勤状态
     */
    @PutMapping("/cancelReturn")
    public Result<?> cancelReturn(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String returnTimeText = params.get("returnTime") == null ? null : String.valueOf(params.get("returnTime"));

        if (id == null) {
            return Result.error("-1", "缺少必要参数");
        }

        ApprovalRequest request = approvalRequestService.getById(id);
        if (request == null) {
            return Result.error("-1", "请假申请不存在");
        }
        if (!"leave_request".equals(request.getRequestType())) {
            return Result.error("-1", "仅支持请假申请销假");
        }
        if (!"approved".equals(request.getStatus())) {
            return Result.error("-1", "仅已批准的请假可执行销假");
        }

        Map<String, Object> requestData = request.getRequestData();
        if (requestData == null) {
            requestData = new HashMap<>();
        }
        if (toBoolean(requestData.get("return_cancelled"))) {
            return Result.error("-1", "该请假已完成销假，无需重复操作");
        }

        String normalizedReturnTime = StringUtils.hasLength(returnTimeText) ? returnTimeText : LocalDateTime.now().toString().replace("T", " ");
        requestData.put("return_cancelled", true);
        requestData.put("return_time", normalizedReturnTime);
        request.setRequestData(requestData);
        request.setUpdateTime(LocalDateTime.now());

        boolean updated = approvalRequestService.updateById(request);
        if (!updated) {
            return Result.error("-1", "销假失败");
        }

        int backfill = entryExitRecordService.backfillAttendanceAfterLeaveReturn(
                request.getStudentUsername(),
                request.getStudentName(),
                normalizedReturnTime
        );

        ApprovalRequest hydrated = hydrateLeaveRequestFields(request);
        Map<String, Object> result = new HashMap<>();
        result.put("request", hydrated);
        result.put("attendanceBackfillCount", backfill);
        return Result.success(result);
    }

    private ApprovalRequest hydrateLeaveRequestFields(ApprovalRequest req) {
        if (req == null) {
            return null;
        }

        req.setParentReply(req.getAdminReply());

        Map<String, Object> requestData = req.getRequestData();
        if (requestData == null) {
            return req;
        }

        req.setLeaveType(valueToString(requestData.get("leave_type")));
        req.setStartTime(valueToString(requestData.get("start_time")));
        req.setEndTime(valueToString(requestData.get("end_time")));
        req.setDestination(valueToString(requestData.get("destination")));
        req.setContactPhone(valueToString(requestData.get("contact_phone")));
        req.setReturnCancelled(toBoolean(requestData.get("return_cancelled")));
        req.setReturnTime(valueToString(requestData.get("return_time")));
        return req;
    }

    private String valueToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
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
