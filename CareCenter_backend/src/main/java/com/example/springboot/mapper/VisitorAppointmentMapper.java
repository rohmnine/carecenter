package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.VisitorAppointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitorAppointmentMapper extends BaseMapper<VisitorAppointment> {
}
