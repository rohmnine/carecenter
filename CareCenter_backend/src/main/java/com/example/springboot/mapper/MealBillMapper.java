package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.MealBill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MealBillMapper extends BaseMapper<MealBill> {
}
