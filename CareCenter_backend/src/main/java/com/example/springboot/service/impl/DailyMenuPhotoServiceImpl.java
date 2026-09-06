package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.DailyMenuPhoto;
import com.example.springboot.mapper.DailyMenuPhotoMapper;
import com.example.springboot.service.DailyMenuPhotoService;
import org.springframework.stereotype.Service;

@Service
public class DailyMenuPhotoServiceImpl extends ServiceImpl<DailyMenuPhotoMapper, DailyMenuPhoto> implements DailyMenuPhotoService {
}
