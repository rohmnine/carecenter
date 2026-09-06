package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.StudentStatusReport;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.service.StudentStatusReportService;
import com.example.springboot.service.UserEntityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学生状况报告接口
 */
@RestController
@RequestMapping("/studentStatus")
public class StudentStatusReportController {

    @Resource
    private StudentStatusReportService studentStatusReportService;

    @Resource
    private UserEntityService userEntityService;

    /**
     * 管理员新增/更新学生日报（同一学生同一天自动覆盖）
     */
    @PostMapping("/upsert")
    public Result<?> upsert(@RequestBody Map<String, Object> params) {
        String studentUsername = (String) params.get("studentUsername");
        String reportDateStr = (String) params.get("reportDate");
        String restStatus = (String) params.get("restStatus");
        String mealStatus = (String) params.get("mealStatus");
        String moodStatus = (String) params.get("moodStatus");
        String healthStatus = (String) params.get("healthStatus");
        String remark = (String) params.get("remark");
        String reporterUsername = (String) params.get("reporterUsername");

        if (studentUsername == null || studentUsername.isEmpty()) {
            return Result.error("-1", "学生用户名不能为空");
        }

        UserEntity student = userEntityService.getUserInfo(studentUsername);
        if (student == null || !"student".equals(student.getUserType())) {
            return Result.error("-1", "学生不存在");
        }

        UserEntity parent = userEntityService.getParentByStudentUsername(studentUsername);

        StudentStatusReport report = new StudentStatusReport();
        report.setStudentUsername(studentUsername);
        report.setStudentName(student.getName());
        report.setParentUsername(parent != null ? parent.getUsername() : null);
        report.setReportDate((reportDateStr == null || reportDateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(reportDateStr));
        report.setRestStatus(restStatus);
        report.setMealStatus(mealStatus);
        report.setMoodStatus(moodStatus);
        report.setHealthStatus(healthStatus);
        report.setRemark(remark);
        report.setReporterUsername(reporterUsername);

        int result = studentStatusReportService.upsertReport(report);
        return result > 0 ? Result.success("保存成功") : Result.error("-1", "保存失败");
    }

    /**
     * 按学生分页查询（管理员使用）
     */
    @GetMapping("/student/{studentUsername}")
    public Result<?> findByStudent(@PathVariable String studentUsername,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        Page<StudentStatusReport> page = studentStatusReportService.findByStudent(studentUsername, pageNum, pageSize, startDate, endDate);
        return Result.success(page);
    }

    /**
     * 家长查看孩子最新状态
     */
    @GetMapping("/parent/latest/{parentUsername}")
    public Result<?> getParentLatest(@PathVariable String parentUsername) {
        StudentStatusReport latest = studentStatusReportService.getLatestByParent(parentUsername);
        return Result.success(latest);
    }

    /**
     * 家长分页查看孩子状况历史
     */
    @GetMapping("/parent/list/{parentUsername}")
    public Result<?> getParentList(@PathVariable String parentUsername,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        UserEntity parent = userEntityService.getUserInfo(parentUsername);
        if (parent == null || !"parent".equals(parent.getUserType())) {
            return Result.error("-1", "家长不存在");
        }
        if (parent.getStudentUsername() == null || parent.getStudentUsername().isEmpty()) {
            return Result.success(new Page<StudentStatusReport>(pageNum, pageSize));
        }
        Page<StudentStatusReport> page = studentStatusReportService.findByStudent(parent.getStudentUsername(), pageNum, pageSize, startDate, endDate);
        return Result.success(page);
    }

    /**
     * 查询学生最近N条（用于仪表盘）
     */
    @GetMapping("/recent/{studentUsername}")
    public Result<?> recent(@PathVariable String studentUsername,
                            @RequestParam(defaultValue = "5") Integer limit) {
        List<StudentStatusReport> list = studentStatusReportService.listRecentByStudent(studentUsername, limit);
        return Result.success(list);
    }
}
