package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DishSuggestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品建议Mapper
 */
@Mapper
public interface DishSuggestionMapper extends BaseMapper<DishSuggestion> {
}
