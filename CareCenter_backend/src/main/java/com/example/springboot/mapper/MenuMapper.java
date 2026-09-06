package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜谱Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}
