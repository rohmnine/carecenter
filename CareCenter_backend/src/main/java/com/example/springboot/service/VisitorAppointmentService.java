package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.VisitorAppointment;

import java.util.List;
import java.util.Map;

/**
 * 家长访客预约Service
 */
public interface VisitorAppointmentService extends IService<VisitorAppointment> {

    /**
     * 提交访客预约申请
     */
    int submitAppointment(VisitorAppointment appointment);

    /**
     * 分页查询家长的预约记录
     */
    Page<VisitorAppointment> findByParent(Integer pageNum, Integer pageSize, String parentUsername, String status);

    /**
     * 分页查询所有预约（管理员）
     */
    Page<VisitorAppointment> findAll(Integer pageNum, Integer pageSize, String status, String search);

    /**
     * 管理员审批预约
     */
    int approveAppointment(Integer id, String status, String adminReply);

    /**
     * 取消预约
     */
    int cancelAppointment(Integer id);

    /**
     * 删除预约
     */
    int deleteAppointment(Integer id);

    /**
     * 获取预约详情
     */
    VisitorAppointment getDetail(Integer id);
}
