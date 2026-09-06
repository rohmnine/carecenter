package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.StudentParentBindingRequest;

import java.util.List;

public interface StudentParentBindingRequestService extends IService<StudentParentBindingRequest> {

    /**
     * 家长发起绑定申请
     * @param parentUsername 家长用户名
     * @param studentUsername 学生用户名
     * @return 申请ID，成功返回 > 0
     */
    int createBindingRequest(String parentUsername, String studentUsername);

    /**
     * 学生确认绑定申请
     * @param requestId 申请ID
     * @param studentUsername 学生用户名（验证）
     * @param approved 是否同意
     * @return 是否成功
     */
    boolean studentConfirm(Integer requestId, String studentUsername, boolean approved);

    /**
     * 管理员审核绑定申请
     * @param requestId 申请ID
     * @param adminUsername 管理员用户名
     * @param approved 是否通过
     * @param rejectReason 拒绝原因（可选）
     * @return 是否成功
     */
    boolean adminReview(Integer requestId, String adminUsername, boolean approved, String rejectReason);

    /**
     * 获取家长的绑定申请列表
     * @param parentUsername 家长用户名
     * @return 申请列表
     */
    List<StudentParentBindingRequest> getRequestsByParent(String parentUsername);

    /**
     * 获取学生的绑定申请列表
     * @param studentUsername 学生用户名
     * @return 申请列表
     */
    List<StudentParentBindingRequest> getRequestsByStudent(String studentUsername);

    /**
     * 获取所有待审核的申请（管理员用）
     * @return 待审核申请列表
     */
    List<StudentParentBindingRequest> getPendingRequests();

    /**
     * 检查是否已有有效的绑定申请
     * @param parentUsername 家长用户名
     * @param studentUsername 学生用户名
     * @return 是否有待处理的申请
     */
    boolean hasPendingRequest(String parentUsername, String studentUsername);

    /**
     * 执行绑定（当学生确认且管理员审核通过后调用）
     * @param requestId 申请ID
     * @return 是否成功
     */
    boolean executeBinding(Integer requestId);
}