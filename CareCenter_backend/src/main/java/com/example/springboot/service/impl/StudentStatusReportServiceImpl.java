package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.StudentStatusReport;
import com.example.springboot.mapper.StudentStatusReportMapper;
import com.example.springboot.service.StudentStatusReportService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 学生状况报告Service实现
 */
@Service
public class StudentStatusReportServiceImpl extends ServiceImpl<StudentStatusReportMapper, StudentStatusReport> implements StudentStatusReportService {

    @Resource
    private StudentStatusReportMapper studentStatusReportMapper;

    @Override
    public int upsertReport(StudentStatusReport report) {
        if (report == null || report.getStudentUsername() == null || report.getStudentUsername().isEmpty()) {
            return 0;
        }
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDate.now());
        }

        QueryWrapper<StudentStatusReport> qw = new QueryWrapper<>();
        qw.eq("student_username", report.getStudentUsername())
          .eq("report_date", report.getReportDate());
        StudentStatusReport exist = studentStatusReportMapper.selectOne(qw);

        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            report.setCreateTime(now);
            report.setUpdateTime(now);
            return studentStatusReportMapper.insert(report);
        } else {
            exist.setStudentName(report.getStudentName());
            exist.setParentUsername(report.getParentUsername());
            exist.setRestStatus(report.getRestStatus());
            exist.setMealStatus(report.getMealStatus());
            exist.setMoodStatus(report.getMoodStatus());
            exist.setHealthStatus(report.getHealthStatus());
            exist.setRemark(report.getRemark());
            exist.setReporterUsername(report.getReporterUsername());
            exist.setUpdateTime(now);
            return studentStatusReportMapper.updateById(exist);
        }
    }

    @Override
    public Page<StudentStatusReport> findByStudent(String studentUsername, Integer pageNum, Integer pageSize, String startDate, String endDate) {
        Page<StudentStatusReport> page = new Page<>(pageNum, pageSize);
        QueryWrapper<StudentStatusReport> qw = new QueryWrapper<>();
        qw.eq("student_username", studentUsername);
        if (startDate != null && !startDate.isEmpty()) {
            qw.ge("report_date", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le("report_date", endDate);
        }
        qw.orderByDesc("report_date").orderByDesc("id");
        return studentStatusReportMapper.selectPage(page, qw);
    }

    @Override
    public StudentStatusReport getLatestByParent(String parentUsername) {
        QueryWrapper<StudentStatusReport> qw = new QueryWrapper<>();
        qw.eq("parent_username", parentUsername)
          .orderByDesc("report_date")
          .orderByDesc("id")
          .last("limit 1");
        return studentStatusReportMapper.selectOne(qw);
    }

    @Override
    public StudentStatusReport getLatestByStudent(String studentUsername) {
        QueryWrapper<StudentStatusReport> qw = new QueryWrapper<>();
        qw.eq("student_username", studentUsername)
          .orderByDesc("report_date")
          .orderByDesc("id")
          .last("limit 1");
        return studentStatusReportMapper.selectOne(qw);
    }

    @Override
    public List<StudentStatusReport> listRecentByStudent(String studentUsername, Integer limit) {
        if (limit == null || limit <= 0) {
            return Collections.emptyList();
        }
        QueryWrapper<StudentStatusReport> qw = new QueryWrapper<>();
        qw.eq("student_username", studentUsername)
          .orderByDesc("report_date")
          .orderByDesc("id")
          .last("limit " + limit);
        return studentStatusReportMapper.selectList(qw);
    }
}
