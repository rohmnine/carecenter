package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.BillingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统一账单记录表Mapper
 */
@Mapper
public interface BillingRecordMapper extends BaseMapper<BillingRecord> {
}
