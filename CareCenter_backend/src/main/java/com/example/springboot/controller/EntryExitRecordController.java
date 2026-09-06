package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.EntryExitRecord;
import com.example.springboot.service.EntryExitRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entryExit")
public class EntryExitRecordController {

    @Resource
    private EntryExitRecordService entryExitRecordService;

    @PostMapping("/add")
    public Result<?> add(@RequestBody EntryExitRecord record) {
        record.setRecordTime(LocalDateTime.now());
        int i = entryExitRecordService.addRecord(record);
        return i == 1 ? Result.success() : Result.error("-1", "添加失败");
    }

    @PostMapping("/batchAdd")
    public Result<?> batchAdd(@RequestBody List<EntryExitRecord> records) {
        for (EntryExitRecord record : records) {
            record.setRecordTime(LocalDateTime.now());
        }
        int count = entryExitRecordService.batchAddRecords(records);
        return count > 0 ? Result.success(count) : Result.error("-1", "批量添加失败");
    }

    @GetMapping("/findByStudent")
    public Result<?> findByStudent(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam String studentUsername,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<EntryExitRecord> page = entryExitRecordService.findByStudent(pageNum, pageSize, studentUsername, startDate, endDate);
        return page != null ? Result.success(page) : Result.error("-1", "查询失败");
    }

    @GetMapping("/find")
    public Result<?> find(@RequestParam(defaultValue = "1") Integer pageNum,
                         @RequestParam(defaultValue = "10") Integer pageSize,
                         @RequestParam(defaultValue = "") String search,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<EntryExitRecord> page = entryExitRecordService.findAll(pageNum, pageSize, search, startDate, endDate);
        return page != null ? Result.success(page) : Result.error("-1", "查询失败");
    }

    @PutMapping("/appeal")
    public Result<?> appeal(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String appealReason = (String) params.get("appealReason");
        int i = entryExitRecordService.updateAppeal(id, appealReason);
        return i == 1 ? Result.success() : Result.error("-1", "申诉失败");
    }

    @PutMapping("/appealStatus")
    public Result<?> updateAppealStatus(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String appealStatus = (String) params.get("appealStatus");
        int i = entryExitRecordService.updateAppealStatus(id, appealStatus);
        return i == 1 ? Result.success() : Result.error("-1", "更新失败");
    }

    @PostMapping("/sendAlert")
    public Result<?> sendAlert(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String alertType = (String) params.get("alertType");
        int i = entryExitRecordService.sendAlert(id, alertType);
        return i == 1 ? Result.success() : Result.error("-1", "发送提醒失败");
    }

    @GetMapping("/abnormal")
    public Result<?> findAbnormal() {
        List<EntryExitRecord> records = entryExitRecordService.findAbnormalRecords();
        return Result.success(records);
    }

    @GetMapping("/todayStatus")
    public Result<?> getTodayStatus() {
        Map<String, String> statusMap = entryExitRecordService.getTodayStatus();
        return Result.success(statusMap);
    }

    @PutMapping("/updateStatus")
    public Result<?> updateStatus(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        int i = entryExitRecordService.updateStatus(id, status);
        return i >= 1 ? Result.success() : Result.error("-1", "更新失败");
    }
}
