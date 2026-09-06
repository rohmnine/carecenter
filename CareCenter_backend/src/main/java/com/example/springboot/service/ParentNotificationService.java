package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.ParentNotification;

/**
 * 家长通知Service
 */
public interface ParentNotificationService extends IService<ParentNotification> {
    
    /**
     * 创建通知
     */
    int createNotification(ParentNotification notification);
    
    /**
     * 分页查询家长的通知
     */
    Page<ParentNotification> findByParent(Integer pageNum, Integer pageSize, String parentUsername);
    
    /**
     * 标记通知为已读
     */
    int markAsRead(Integer id);
    
    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(String parentUsername);
    
    /**
     * 删除通知
     */
    int deleteNotification(Integer id);
    
    /**
     * 管理员发送未授权外出通知给家长
     */
    int sendUnauthorizedOutNotification(String studentUsername, String studentName, String parentUsername, String details);
    
    /**
     * 发送自定义通知给家长
     */
    int sendCustomNotification(String parentUsername, String studentUsername, String studentName,
                               String notificationType, String title, String content);
}
