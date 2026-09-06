package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ParentNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家长通知Mapper
 */
@Mapper
public interface ParentNotificationMapper extends BaseMapper<ParentNotification> {
}
