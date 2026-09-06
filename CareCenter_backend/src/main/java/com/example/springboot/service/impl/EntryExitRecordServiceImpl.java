
package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.EntryExitRecord;
import com.example.springboot.entity.ParentNotification;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.EntryExitRecordMapper;
import com.example.springboot.mapper.ParentMapper;
import com.example.springboot.mapper.ParentNotificationMapper;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.service.EntryExitRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class EntryExitRecordServiceImpl implements EntryExitRecordService {

    @Resource
    private EntryExitRecordMapper entryExitRecordMapper;

    @Resource
    private ParentMapper parentMapper;

    @Resource
    private ParentNotificationMapper parentNotificationMapper;

    @Resource
    private StudentMapper studentMapper;

    @Override
    public int addRecord(EntryExitRecord record) {
        record.setCreateTime(LocalDateTime.now());
        int result = entryExitRecordMapper.insert(record);
        // Auto-send parent notification for abnormal records
        if (result == 1 && record.getStatus() != null && !"normal".equals(record.getStatus())) {
            autoNotifyParent(record);
        }
        return result;
    }

    @Override
    public int batchAddRecords(List<EntryExitRecord> records) {
        int count = 0;
        for (EntryExitRecord record : records) {
            record.setCreateTime(LocalDateTime.now());
            int result = entryExitRecordMapper.insert(record);
            count += result;
            // Auto-send parent notification for abnormal records
            if (result == 1 && record.getStatus() != null && !"normal".equals(record.getStatus())) {
                autoNotifyParent(record);
            }
        }
        return count;
    }

    /**
     * Auto-send notification to parent when abnormal status is recorded
     */
    private void autoNotifyParent(EntryExitRecord record) {
        try {
            record.setAlertSent(1);
            entryExitRecordMapper.updateById(record);

            QueryWrapper<com.example.springboot.entity.Parent> parentQuery = new QueryWrapper<>();
            parentQuery.eq("student_username", record.getStudentUsername());
            parentQuery.eq("user_type", "parent");
            com.example.springboot.entity.Parent parent = parentMapper.selectOne(parentQuery);

            if (parent != null) {
                String alertType = "late".equals(record.getStatus()) ? "晚归" : "未归";
                ParentNotification notification = new ParentNotification();
                notification.setParentUsername(parent.getUsername());
                notification.setStudentUsername(record.getStudentUsername());
                notification.setStudentName(record.getStudentName());
                notification.setNotificationType("entry_exit_alert");
                notification.setTitle("学生进出异常提醒");
                notification.setContent(String.format("您的孩子%s于%s出现%s情况，请关注",
                    record.getStudentName(),
                    record.getRecordTime().toString(),
                    alertType));
                notification.setIsRead(0);
                notification.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                parentNotificationMapper.insert(notification);
            }
        } catch (Exception e) {
            // Log but don't fail the main operation
            e.printStackTrace();
        }
    }

    @Override
    public Page<EntryExitRecord> findByStudent(Integer pageNum, Integer pageSize, String studentUsername, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<EntryExitRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        
        if (startDate != null && endDate != null) {
            queryWrapper.between("record_time", startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        }
        
        queryWrapper.orderByDesc("record_time");
        return entryExitRecordMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Page<EntryExitRecord> findAll(Integer pageNum, Integer pageSize, String search, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<EntryExitRecord> queryWrapper = new QueryWrapper<>();
        
        if (search != null && !search.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("student_username", search)
                .or()
                .like("student_name", search));
        }
        
        if (startDate != null && endDate != null) {
            queryWrapper.between("record_time", startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        }
        
        queryWrapper.orderByDesc("record_time");
        return entryExitRecordMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public int updateAppeal(Integer id, String appealReason) {
        EntryExitRecord record = new EntryExitRecord();
        record.setId(id);
        record.setAppealReason(appealReason);
        record.setAppealStatus("pending");
        return entryExitRecordMapper.updateById(record);
    }

    @Override
    public int updateAppealStatus(Integer id, String appealStatus) {
        EntryExitRecord record = new EntryExitRecord();
        record.setId(id);
        record.setAppealStatus(appealStatus);
        return entryExitRecordMapper.updateById(record);
    }

    @Override
    public int sendAlert(Integer id, String alertType) {
        EntryExitRecord record = entryExitRecordMapper.selectById(id);
        if (record == null) {
            return 0;
        }
        
        record.setAlertSent(1);
        entryExitRecordMapper.updateById(record);
        
        QueryWrapper<com.example.springboot.entity.Parent> parentQuery = new QueryWrapper<>();
        parentQuery.eq("student_username", record.getStudentUsername());
        parentQuery.eq("user_type", "parent");
        com.example.springboot.entity.Parent parent = parentMapper.selectOne(parentQuery);
        
        if (parent != null) {
            ParentNotification notification = new ParentNotification();
            notification.setParentUsername(parent.getUsername());
            notification.setStudentUsername(record.getStudentUsername());
            notification.setStudentName(record.getStudentName());
            notification.setNotificationType("entry_exit_alert");
            notification.setTitle("学生进出异常提醒");
            notification.setContent(String.format("您的孩子%s于%s出现%s情况，请关注",
                record.getStudentName(), 
                record.getRecordTime().toString(), 
                alertType));
            notification.setIsRead(0);
            notification.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            parentNotificationMapper.insert(notification);
        }
        
        return 1;
    }

    @Override
    public List<EntryExitRecord> findAbnormalRecords() {
        QueryWrapper<EntryExitRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", "late", "absent");
        queryWrapper.eq("alert_sent", 0);
        return entryExitRecordMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, String> getTodayStatus() {
        LocalDate today = LocalDate.now();
        QueryWrapper<EntryExitRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("record_time", today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        queryWrapper.orderByDesc("record_time");
        List<EntryExitRecord> todayRecords = entryExitRecordMapper.selectList(queryWrapper);

        Map<String, String> statusMap = new HashMap<>();
        for (EntryExitRecord record : todayRecords) {
            // Only keep the latest record for each student
            if (!statusMap.containsKey(record.getStudentUsername())) {
                statusMap.put(record.getStudentUsername(), record.getRecordType());
            }
        }
        return statusMap;
    }

    @Override
    public int updateStatus(Integer id, String status) {
        EntryExitRecord record = entryExitRecordMapper.selectById(id);
        if (record == null) {
            return 0;
        }
        record.setStatus(status);
        int result = entryExitRecordMapper.updateById(record);
        // Auto-notify parent if status changed to abnormal
        if (result == 1 && !"normal".equals(status)) {
            autoNotifyParent(record);
        }
        return result;
    }
    @Override
    public int backfillAttendanceAfterLeaveReturn(String studentUsername, String studentName, String returnTimeText) {
        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            return 0;
        }

        LocalDateTime returnTime = parseReturnTimeOrNow(returnTimeText);

        LocalDate day = returnTime.toLocalDate();
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

        QueryWrapper<EntryExitRecord> existedEntryQuery = new QueryWrapper<>();
        existedEntryQuery.eq("student_username", studentUsername)
                .eq("record_type", "entry")
                .ge("record_time", dayStart)
                .lt("record_time", dayEnd);
        Long existedCount = entryExitRecordMapper.selectCount(existedEntryQuery);
        if (existedCount != null && existedCount > 0) {
            return 0;
        }

        EntryExitRecord backfillRecord = new EntryExitRecord();
        backfillRecord.setStudentUsername(studentUsername);
        backfillRecord.setStudentName(
                (studentName == null || studentName.trim().isEmpty()) ? studentUsername : studentName.trim());
        backfillRecord.setRecordType("entry");
        backfillRecord.setRecordTime(returnTime);
        backfillRecord.setStatus("normal");
        backfillRecord.setAlertSent(0);
        backfillRecord.setCreateTime(LocalDateTime.now());

        return entryExitRecordMapper.insert(backfillRecord);
    }

    private LocalDateTime parseReturnTimeOrNow(String returnTimeText) {
        if (returnTimeText == null || returnTimeText.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        String normalized = returnTimeText.trim();
        try {
            if (normalized.contains(" ")) {
                return LocalDateTime.parse(normalized, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.parse(normalized);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

}
