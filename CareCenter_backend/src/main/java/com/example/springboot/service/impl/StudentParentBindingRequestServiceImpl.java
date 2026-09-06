package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.common.Result;
import com.example.springboot.entity.StudentParentBindingRequest;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.StudentParentBindingRequestMapper;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.StudentParentBindingRequestService;
import com.example.springboot.service.UserEntityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentParentBindingRequestServiceImpl 
        extends ServiceImpl<StudentParentBindingRequestMapper, StudentParentBindingRequest> 
        implements StudentParentBindingRequestService {

    @Resource
    private StudentParentBindingRequestMapper bindingRequestMapper;

    @Resource
    private UserEntityMapper userEntityMapper;

    @Resource
    private UserEntityService userEntityService;

    @Override
    public int createBindingRequest(String parentUsername, String studentUsername) {
        // 验证家长存在且为家长类型
        UserEntity parent = userEntityMapper.selectById(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return 0;
        }

        // 验证学生存在且为学生类型
        UserEntity student = userEntityMapper.selectById(studentUsername);
        if (student == null || !"student".equals(student.getUserType())) {
            return 0;
        }

        // 检查是否已经绑定
        List<String> boundStudents = userEntityService.getBoundStudentUsernames(parentUsername);
        if (boundStudents.contains(studentUsername)) {
            return -1; // 已绑定
        }

        // 检查是否有待处理的申请
        if (hasPendingRequest(parentUsername, studentUsername)) {
            return -2; // 已有待处理申请
        }

        // 创建申请
        StudentParentBindingRequest request = new StudentParentBindingRequest();
        request.setParentUsername(parentUsername);
        request.setStudentUsername(studentUsername);
        request.setStatus("pending");
        request.setStudentConfirmed(0);
        request.setAdminStatus(0);
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        return bindingRequestMapper.insert(request) > 0 ? 1 : 0;
    }

    @Override
    public boolean studentConfirm(Integer requestId, String studentUsername, boolean approved) {
        StudentParentBindingRequest request = bindingRequestMapper.selectById(requestId);
        if (request == null) {
            return false;
        }

        // 验证学生用户名
        if (!request.getStudentUsername().equals(studentUsername)) {
            return false;
        }

        // 检查状态
        if (!"pending".equals(request.getStatus()) && !"student_approved".equals(request.getStatus())) {
            return false;
        }

        // 更新学生确认状态
        request.setStudentConfirmed(approved ? 1 : 0);
        request.setStudentConfirmTime(LocalDateTime.now());

        if (approved) {
            request.setStatus("student_approved");
            // 如果管理员已经通过，直接执行绑定
            if (request.getAdminStatus() != null && request.getAdminStatus() == 1) {
                request.setStatus("admin_approved");
                request.setUpdateTime(LocalDateTime.now());
                bindingRequestMapper.updateById(request);
                return executeBinding(requestId);
            }
        } else {
            request.setStatus("rejected");
        }

        request.setUpdateTime(LocalDateTime.now());
        return bindingRequestMapper.updateById(request) > 0;
    }

    @Override
    public boolean adminReview(Integer requestId, String adminUsername, boolean approved, String rejectReason) {
        StudentParentBindingRequest request = bindingRequestMapper.selectById(requestId);
        if (request == null) {
            return false;
        }

        // 检查状态：学生已同意或待审核状态都可以审核
        if (!"pending".equals(request.getStatus()) && !"student_approved".equals(request.getStatus())) {
            return false;
        }

        request.setAdminUsername(adminUsername);
        request.setAdminConfirmTime(LocalDateTime.now());

        if (approved) {
            request.setAdminStatus(1);
            request.setStatus("admin_approved");
            request.setUpdateTime(LocalDateTime.now());
            bindingRequestMapper.updateById(request);

            // 如果学生也已经同意，执行绑定
            if (request.getStudentConfirmed() != null && request.getStudentConfirmed() == 1) {
                return executeBinding(requestId);
            }
            return true;
        } else {
            request.setAdminStatus(2);
            request.setStatus("rejected");
            request.setRejectReason(rejectReason);
            request.setUpdateTime(LocalDateTime.now());
            return bindingRequestMapper.updateById(request) > 0;
        }
    }

    @Override
    public List<StudentParentBindingRequest> getRequestsByParent(String parentUsername) {
        QueryWrapper<StudentParentBindingRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_username", parentUsername)
               .orderByDesc("create_time");
        return bindingRequestMapper.selectList(wrapper);
    }

    @Override
    public List<StudentParentBindingRequest> getRequestsByStudent(String studentUsername) {
        QueryWrapper<StudentParentBindingRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("student_username", studentUsername)
               .orderByDesc("create_time");
        return bindingRequestMapper.selectList(wrapper);
    }

    @Override
    public List<StudentParentBindingRequest> getPendingRequests() {
        QueryWrapper<StudentParentBindingRequest> wrapper = new QueryWrapper<>();
        wrapper.in("status", "pending", "student_approved")
               .orderByAsc("create_time");
        return bindingRequestMapper.selectList(wrapper);
    }

    @Override
    public boolean hasPendingRequest(String parentUsername, String studentUsername) {
        QueryWrapper<StudentParentBindingRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_username", parentUsername)
               .eq("student_username", studentUsername)
               .in("status", "pending", "student_approved");
        return bindingRequestMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean executeBinding(Integer requestId) {
        StudentParentBindingRequest request = bindingRequestMapper.selectById(requestId);
        if (request == null) {
            return false;
        }

        // 执行绑定：添加到家长的学生列表
        List<String> boundStudents = userEntityService.getBoundStudentUsernames(request.getParentUsername());
        if (!boundStudents.contains(request.getStudentUsername())) {
            boundStudents.add(request.getStudentUsername());
            int result = userEntityService.bindStudents(request.getParentUsername(), boundStudents);
            if (result <= 0) {
                return false;
            }
        }

        // 更新申请状态为已完成
        request.setStatus("completed");
        request.setUpdateTime(LocalDateTime.now());
        return bindingRequestMapper.updateById(request) > 0;
    }
}