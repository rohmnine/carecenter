package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统一用户表Mapper
 */
@Mapper
public interface UserEntityMapper extends BaseMapper<UserEntity> {
}
