package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.WeeklyMenu;
import com.example.springboot.mapper.WeeklyMenuMapper;
import com.example.springboot.service.WeeklyMenuService;
import org.springframework.stereotype.Service;

/**
 * 每周菜谱Service实现类
 */
@Service
public class WeeklyMenuServiceImpl extends ServiceImpl<WeeklyMenuMapper, WeeklyMenu> implements WeeklyMenuService {
}
