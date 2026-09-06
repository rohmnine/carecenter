package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.EntryExitRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EntryExitRecordService {
    
    int addRecord(EntryExitRecord record);
    
    int batchAddRecords(List<EntryExitRecord> records);
    
    Page<EntryExitRecord> findByStudent(Integer pageNum, Integer pageSize, String studentUsername, LocalDate startDate, LocalDate endDate);
    
    Page<EntryExitRecord> findAll(Integer pageNum, Integer pageSize, String search, LocalDate startDate, LocalDate endDate);
    
    int updateAppeal(Integer id, String appealReason);
    
    int updateAppealStatus(Integer id, String appealStatus);
    
    int sendAlert(Integer id, String alertType);
    
    List<EntryExitRecord> findAbnormalRecords();
    
    /**
     * Get today's latest entry/exit status for each student
     * Returns a map of studentUsername -> latest record type ("entry" or "exit")
     */
    Map<String, String> getTodayStatus();

    /**
     * Update the status of an entry/exit record
     * If status is abnormal (late/absent), auto-notify parent
     */
    int updateStatus(Integer id, String status);

    /**
     * 返校销假后自动回填考勤表
     * 在返校时间补一条 entry + normal 记录（若同日已有 entry 记录则不重复补录）
     * @return 实际补录条数
     */
    int backfillAttendanceAfterLeaveReturn(String studentUsername, String studentName, String returnTimeText);
}
