package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.ParentNotification;
import com.example.springboot.mapper.ParentNotificationMapper;
import com.example.springboot.service.ParentNotificationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 家长通知Service实现
 */
@Service
public class ParentNotificationServiceImpl extends ServiceImpl<ParentNotificationMapper, ParentNotification> implements ParentNotificationService {

    @Resource
    private ParentNotificationMapper parentNotificationMapper;

    /**
     * 创建通知
     */
    @Override
    public int createNotification(ParentNotification notification) {
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(new Date());
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        return parentNotificationMapper.insert(notification);
    }

    /**
     * 分页查询家长的通知
     */
    @Override
    public Page<ParentNotification> findByParent(Integer pageNum, Integer pageSize, String parentUsername) {
        Page<ParentNotification> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ParentNotification> qw = new QueryWrapper<>();
        qw.eq("parent_username", parentUsername);
        qw.orderByDesc("create_time");
        return parentNotificationMapper.selectPage(page, qw);
    }

    /**
     * 标记通知为已读
     */
    @Override
    public int markAsRead(Integer id) {
        ParentNotification notification = parentNotificationMapper.selectById(id);
        if (notification == null) {
            return 0;
        }
        notification.setIsRead(1);
        return parentNotificationMapper.updateById(notification);
    }

    /**
     * 获取未读通知数量
     */
    @Override
    public Long getUnreadCount(String parentUsername) {
        QueryWrapper<ParentNotification> qw = new QueryWrapper<>();
        qw.eq("parent_username", parentUsername);
        qw.eq("is_read", 0);
        return parentNotificationMapper.selectCount(qw);
    }

    /**
     * 删除通知
     */
    @Override
    public int deleteNotification(Integer id) {
        return parentNotificationMapper.deleteById(id);
    }

    /**
     * 管理员发送未授权外出通知给家长
     */
    @Override
    public int sendUnauthorizedOutNotification(String studentUsername, String studentName, String parentUsername, String details) {
        ParentNotification notification = new ParentNotification();
        notification.setParentUsername(parentUsername);
        notification.setStudentUsername(studentUsername);
        notification.setStudentName(studentName);
        notification.setNotificationType("unauthorized_out");
        notification.setTitle("学生未授权外出通知");
        notification.setContent(String.format("您的孩子%s未经您的同意擅自外出。详情：%s", studentName, details));
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        
        return parentNotificationMapper.insert(notification);
    }
    
    /**
     * 发送自定义通知给家长
     */
    @Override
    public int sendCustomNotification(String parentUsername, String studentUsername, String studentName,
                                      String notificationType, String title, String content) {
        ParentNotification notification = new ParentNotification();
        notification.setParentUsername(parentUsername);
        notification.setStudentUsername(studentUsername);
        notification.setStudentName(studentName);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        return parentNotificationMapper.insert(notification);
    }
}
