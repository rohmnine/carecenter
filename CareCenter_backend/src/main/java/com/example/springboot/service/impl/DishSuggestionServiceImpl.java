package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.DishSuggestion;
import com.example.springboot.mapper.DishSuggestionMapper;
import com.example.springboot.service.DishSuggestionService;
import org.springframework.stereotype.Service;

/**
 * 菜品建议Service实现类
 */
@Service
public class DishSuggestionServiceImpl extends ServiceImpl<DishSuggestionMapper, DishSuggestion> implements DishSuggestionService {
}
