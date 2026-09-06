package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.StudentStatusReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生状况报告Mapper
 */
@Mapper
public interface StudentStatusReportMapper extends BaseMapper<StudentStatusReport> {
}
