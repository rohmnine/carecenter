package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.StudentStatusReport;

import java.util.List;

/**
 * 学生状况报告Service
 */
public interface StudentStatusReportService extends IService<StudentStatusReport> {

    /**
     * 新增或更新（按学生日期唯一）
     */
    int upsertReport(StudentStatusReport report);

    /**
     * 分页查询学生状况报告
     */
    Page<StudentStatusReport> findByStudent(String studentUsername, Integer pageNum, Integer pageSize, String startDate, String endDate);

    /**
     * 查询家长对应学生的最新状况报告
     */
    StudentStatusReport getLatestByParent(String parentUsername);

    /**
     * 查询学生最新状况报告
     */
    StudentStatusReport getLatestByStudent(String studentUsername);

    /**
     * 查询学生最近N条状况报告
     */
    List<StudentStatusReport> listRecentByStudent(String studentUsername, Integer limit);
}
