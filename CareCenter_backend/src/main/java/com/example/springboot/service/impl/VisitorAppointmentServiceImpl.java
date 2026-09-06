package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.VisitorAppointment;
import com.example.springboot.mapper.VisitorAppointmentMapper;
import com.example.springboot.service.VisitorAppointmentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 家长访客预约Service实现
 */
@Service
public class VisitorAppointmentServiceImpl extends ServiceImpl<VisitorAppointmentMapper, VisitorAppointment> implements VisitorAppointmentService {

    @Resource
    private VisitorAppointmentMapper visitorAppointmentMapper;

    /**
     * 提交访客预约申请
     */
    @Override
    public int submitAppointment(VisitorAppointment appointment) {
        appointment.setStatus("pending");
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        return visitorAppointmentMapper.insert(appointment);
    }

    /**
     * 分页查询家长的预约记录
     */
    @Override
    public Page<VisitorAppointment> findByParent(Integer pageNum, Integer pageSize, String parentUsername, String status) {
        Page<VisitorAppointment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VisitorAppointment> qw = new QueryWrapper<>();
        qw.eq("parent_username", parentUsername);
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        return visitorAppointmentMapper.selectPage(page, qw);
    }

    /**
     * 分页查询所有预约（管理员）
     */
    @Override
    public Page<VisitorAppointment> findAll(Integer pageNum, Integer pageSize, String status, String search) {
        Page<VisitorAppointment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VisitorAppointment> qw = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        if (search != null && !search.isEmpty()) {
            qw.and(wrapper -> wrapper
                .like("parent_name", search)
                .or()
                .like("student_name", search));
        }
        qw.orderByDesc("create_time");
        return visitorAppointmentMapper.selectPage(page, qw);
    }

    /**
     * 管理员审批预约
     */
    @Override
    public int approveAppointment(Integer id, String status, String adminReply) {
        VisitorAppointment appointment = visitorAppointmentMapper.selectById(id);
        if (appointment == null) {
            return 0;
        }
        appointment.setStatus(status);
        appointment.setAdminReply(adminReply);
        appointment.setUpdateTime(LocalDateTime.now());
        return visitorAppointmentMapper.updateById(appointment);
    }

    /**
     * 取消预约
     */
    @Override
    public int cancelAppointment(Integer id) {
        VisitorAppointment appointment = visitorAppointmentMapper.selectById(id);
        if (appointment == null) {
            return 0;
        }
        appointment.setStatus("cancelled");
        appointment.setUpdateTime(LocalDateTime.now());
        return visitorAppointmentMapper.updateById(appointment);
    }

    /**
     * 删除预约
     */
    @Override
    public int deleteAppointment(Integer id) {
        return visitorAppointmentMapper.deleteById(id);
    }

    /**
     * 获取预约详情
     */
    @Override
    public VisitorAppointment getDetail(Integer id) {
        return visitorAppointmentMapper.selectById(id);
    }
}
