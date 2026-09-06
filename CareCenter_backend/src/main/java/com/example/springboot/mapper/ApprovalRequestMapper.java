package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ApprovalRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * Approval request mapper
 */
@Mapper
public interface ApprovalRequestMapper extends BaseMapper<ApprovalRequest> {
}

