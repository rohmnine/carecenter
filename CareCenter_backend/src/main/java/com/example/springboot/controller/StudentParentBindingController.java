package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.StudentParentBindingRequest;
import com.example.springboot.service.StudentParentBindingRequestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/binding")
public class StudentParentBindingController {

    @Resource
    private StudentParentBindingRequestService bindingRequestService;

    /**
     * 家长发起绑定申请
     */
    @PostMapping("/create")
    public Result<?> createBindingRequest(@RequestBody StudentParentBindingRequest request, HttpSession session) {
        String parentUsername = (String) session.getAttribute("username");
        if (parentUsername == null) {
            return Result.error("-1", "请先登录");
        }

        if (request.getStudentUsername() == null || request.getStudentUsername().isEmpty()) {
            return Result.error("-1", "请选择要绑定的学生");
        }

        int result = bindingRequestService.createBindingRequest(parentUsername, request.getStudentUsername());
        if (result > 0) {
            return Result.success("绑定申请已提交，请等待学生确认");
        } else if (result == -1) {
            return Result.error("-1", "该学生已被绑定");
        } else if (result == -2) {
            return Result.error("-1", "已有待处理的绑定申请，请等待处理");
        }
        return Result.error("-1", "提交失败");
    }

    /**
     * 学生确认绑定申请
     */
    @PostMapping("/student/confirm")
    public Result<?> studentConfirm(@RequestBody StudentParentBindingRequest request, HttpSession session) {
        String studentUsername = (String) session.getAttribute("username");
        if (studentUsername == null) {
            return Result.error("-1", "请先登录");
        }

        if (request.getId() == null) {
            return Result.error("-1", "申请ID不能为空");
        }

        boolean result = bindingRequestService.studentConfirm(request.getId(), studentUsername, request.getStudentConfirmed() == 1);
        if (result) {
            if (request.getStudentConfirmed() == 1) {
                return Result.success("已同意绑定申请，等待管理员审核");
            } else {
                return Result.success("已拒绝绑定申请");
            }
        }
        return Result.error("-1", "操作失败");
    }

    /**
     * 管理员审核绑定申请
     */
    @PostMapping("/admin/review")
    public Result<?> adminReview(@RequestBody StudentParentBindingRequest request, HttpSession session) {
        String adminUsername = (String) session.getAttribute("username");
        if (adminUsername == null) {
            return Result.error("-1", "请先登录");
        }

        if (request.getId() == null) {
            return Result.error("-1", "申请ID不能为空");
        }

        boolean approved = request.getAdminStatus() == 1;
        String rejectReason = request.getRejectReason();

        boolean result = bindingRequestService.adminReview(request.getId(), adminUsername, approved, rejectReason);
        if (result) {
            if (approved) {
                return Result.success("审核通过");
            } else {
                return Result.success("已拒绝该申请");
            }
        }
        return Result.error("-1", "审核操作失败");
    }

    /**
     * 获取当前用户的绑定申请列表（家长/学生）
     */
    @GetMapping("/myRequests")
    public Result<?> getMyRequests(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("userType");
        if (username == null) {
            return Result.error("-1", "请先登录");
        }

        List<StudentParentBindingRequest> requests;
        if ("parent".equals(userType)) {
            requests = bindingRequestService.getRequestsByParent(username);
        } else if ("student".equals(userType)) {
            requests = bindingRequestService.getRequestsByStudent(username);
        } else {
            return Result.error("-1", "无效的用户类型");
        }

        return Result.success(requests);
    }

    /**
     * 获取所有待审核的申请（管理员）
     */
    @GetMapping("/admin/pending")
    public Result<?> getPendingRequests(HttpSession session) {
        String adminUsername = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("userType");
        if (adminUsername == null || !"admin".equals(userType)) {
            return Result.error("-1", "权限不足");
        }

        List<StudentParentBindingRequest> requests = bindingRequestService.getPendingRequests();
        return Result.success(requests);
    }

    /**
     * 获取学生的待确认申请（学生端）
     */
    @GetMapping("/student/pending")
    public Result<?> getStudentPendingRequests(HttpSession session) {
        String studentUsername = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("userType");
        if (studentUsername == null || !"student".equals(userType)) {
            return Result.error("-1", "权限不足");
        }

        List<StudentParentBindingRequest> requests = bindingRequestService.getRequestsByStudent(studentUsername);
        // 只返回待处理状态的申请
        requests.removeIf(r -> !"pending".equals(r.getStatus()) && !"student_approved".equals(r.getStatus()));
        return Result.success(requests);
    }
}