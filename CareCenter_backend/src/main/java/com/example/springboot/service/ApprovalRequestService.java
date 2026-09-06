package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.ApprovalRequest;

import java.util.List;
import java.util.Map;

public interface ApprovalRequestService extends IService<ApprovalRequest> {
    
    Page<ApprovalRequest> findByRequestType(Integer pageNum, Integer pageSize, String requestType, String status, String search);
    
    int createRequest(ApprovalRequest request);
    
    int approve(Integer id, String adminReply);
    
    int reject(Integer id, String adminReply);
    
    int cancel(Integer id);
    
    List<ApprovalRequest> getByRequester(String requesterUsername);
    
    List<ApprovalRequest> getByStudent(String studentUsername);
    
    Map<String, Object> getRequestData(Integer id);
}
