package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.mapper.ApprovalRequestMapper;
import com.example.springboot.service.ApprovalRequestService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApprovalRequestServiceImpl extends ServiceImpl<ApprovalRequestMapper, ApprovalRequest> implements ApprovalRequestService {

    @Resource
    private ApprovalRequestMapper approvalRequestMapper;

    @Override
    public Page<ApprovalRequest> findByRequestType(Integer pageNum, Integer pageSize, String requestType, String status, String search) {
        Page<ApprovalRequest> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ApprovalRequest> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasLength(requestType)) {
            queryWrapper.eq("request_type", requestType);
        }

        if (StringUtils.hasLength(status)) {
            queryWrapper.eq("status", status);
        }

        if (StringUtils.hasLength(search)) {
            queryWrapper.and(w -> w.like("requester_username", search)
                    .or().like("student_username", search)
                    .or().like("student_name", search));
        }

        queryWrapper.orderByDesc("create_time");

        return approvalRequestMapper.selectPage(page, queryWrapper);
    }

    @Override
    public int createRequest(ApprovalRequest request) {
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            request.setStatus("pending");
        }
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        return approvalRequestMapper.insert(request);
    }

    @Override
    public int approve(Integer id, String adminReply) {
        ApprovalRequest request = approvalRequestMapper.selectById(id);
        if (request == null) {
            return 0;
        }
        if (!"pending".equals(request.getStatus())) {
            return -1; // Can only approve pending requests
        }
        request.setStatus("approved");
        request.setAdminReply(adminReply);
        request.setUpdateTime(LocalDateTime.now());
        return approvalRequestMapper.updateById(request);
    }

    @Override
    public int reject(Integer id, String adminReply) {
        ApprovalRequest request = approvalRequestMapper.selectById(id);
        if (request == null) {
            return 0;
        }
        if (!"pending".equals(request.getStatus())) {
            return -1; // Can only reject pending requests
        }
        request.setStatus("rejected");
        request.setAdminReply(adminReply);
        request.setUpdateTime(LocalDateTime.now());
        return approvalRequestMapper.updateById(request);
    }

    @Override
    public int cancel(Integer id) {
        ApprovalRequest request = approvalRequestMapper.selectById(id);
        if (request == null) {
            return 0;
        }
        if ("cancelled".equals(request.getStatus()) || "rejected".equals(request.getStatus())) {
            return -1; // Already cancelled or rejected
        }
        request.setStatus("cancelled");
        request.setUpdateTime(LocalDateTime.now());
        return approvalRequestMapper.updateById(request);
    }

    @Override
    public List<ApprovalRequest> getByRequester(String requesterUsername) {
        QueryWrapper<ApprovalRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("requester_username", requesterUsername)
                .orderByDesc("create_time");
        return approvalRequestMapper.selectList(queryWrapper);
    }

    @Override
    public List<ApprovalRequest> getByStudent(String studentUsername) {
        QueryWrapper<ApprovalRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .orderByDesc("create_time");
        return approvalRequestMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, Object> getRequestData(Integer id) {
        Map<String, Object> result = new HashMap<>();
        ApprovalRequest request = approvalRequestMapper.selectById(id);
        if (request == null) {
            return result;
        }
        result.put("request", request);
        result.put("requestData", request.getRequestData());
        return result;
    }
}
