package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.WeeklyMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每周菜谱Mapper
 */
@Mapper
public interface WeeklyMenuMapper extends BaseMapper<WeeklyMenu> {
}
