package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.BillingRecord;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.BillingRecordService;
import com.example.springboot.service.ParentNotificationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentRecordController {
    
    @Resource
    private BillingRecordService billingRecordService;
    
    @Resource
    private UserEntityMapper userEntityMapper;
    
    @Resource
    private ParentNotificationService parentNotificationService;
    
    @GetMapping("/find")
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(required = false) String month,
                              @RequestParam(required = false) String status) {
        Page<BillingRecord> page = billingRecordService.findByBillType(pageNum, pageSize, "payment", search, month, status);
        return Result.success(page);
    }
    
    @PutMapping("/markPaid/{id}")
    public Result<?> markAsPaid(@PathVariable Integer id) {
        int result = billingRecordService.markAsPaid(id);
        if (result > 0) {
            return Result.success("标记成功");
        }
        return Result.error("-1", "标记失败");
    }
    
    @PostMapping("/sendReminder/{studentUsername}")
    public Result<?> sendReminder(@PathVariable String studentUsername) {
        QueryWrapper<UserEntity> studentQuery = new QueryWrapper<>();
        studentQuery.eq("username", studentUsername).eq("user_type", "student");
        UserEntity student = userEntityMapper.selectOne(studentQuery);
        if (student == null) {
            return Result.error("-1", "学生不存在");
        }
        
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BillingRecord unpaidRecord = billingRecordService.getByStudentAndMonth(studentUsername, currentMonth, "payment");
        
        if (unpaidRecord == null || "paid".equals(unpaidRecord.getStatus())) {
            return Result.error("-1", "没有未缴费记录");
        }
        
        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("student_username", studentUsername).eq("user_type", "parent");
        UserEntity parent = userEntityMapper.selectOne(parentQuery);
        
        if (parent == null) {
            return Result.error("-1", "未找到家长信息");
        }
        
        String content = String.format("您的孩子%s(%s)本月(%s)托管费尚未缴纳，请及时缴费。",
            student.getName(), studentUsername, currentMonth);
        
        int result = parentNotificationService.sendCustomNotification(
            parent.getUsername(),
            studentUsername,
            student.getName(),
            "payment_reminder",
                "托管费缴费提醒",
            content
        );
        
        if (result > 0) {
            return Result.success("提醒已发送");
        }
        return Result.error("-1", "发送失败");
    }
    
    @GetMapping("/student/{studentUsername}")
    public Result<?> getStudentPayment(@PathVariable String studentUsername,
                                       @RequestParam String month) {
        BillingRecord record = billingRecordService.getByStudentAndMonth(studentUsername, month, "payment");
        if (record != null) {
            return Result.success(record);
        }
        return Result.error("-1", "未找到记录");
    }
    
    @GetMapping("/unpaid")
    public Result<?> getUnpaidRecords(@RequestParam(required = false) String month) {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bill_type", "payment").eq("status", "unpaid");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("bill_month", month);
        }
        List<BillingRecord> records = billingRecordService.list(queryWrapper);
        return Result.success(records);
    }
    
    @PutMapping("/batchMarkPaid")
    public Result<?> batchMarkAsPaid(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> ids = params.get("ids");
        int count = 0;
        for (Integer id : ids) {
            count += billingRecordService.markAsPaid(id);
        }
        return Result.success("成功标记" + count + "条记录");
    }
    
    @PostMapping("/batchSendReminder")
    public Result<?> batchSendReminder(@RequestBody Map<String, List<String>> params) {
        List<String> usernames = params.get("usernames");
        int count = 0;
        for (String username : usernames) {
            try {
                Result<?> result = sendReminder(username);
                if ("0".equals(result.getCode())) {
                    count++;
                }
            } catch (Exception e) {
                // Continue with next
            }
        }
        return Result.success("成功发送" + count + "条提醒");
    }
    
    @GetMapping("/statistics")
    public Result<?> getStatistics(@RequestParam(required = false) String month) {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bill_type", "payment");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("bill_month", month);
        }
        
        Long total = billingRecordService.count(queryWrapper);
        
        QueryWrapper<BillingRecord> paidQuery = new QueryWrapper<>();
        paidQuery.eq("bill_type", "payment").eq("status", "paid");
        if (month != null && !month.isEmpty()) {
            paidQuery.eq("bill_month", month);
        }
        Long paid = billingRecordService.count(paidQuery);
        
        double rate = total > 0 ? (paid * 100.0 / total) : 0;
        
        stats.put("total", total);
        stats.put("paid", paid);
        stats.put("unpaid", total - paid);
        stats.put("rate", String.format("%.2f", rate));
        
        return Result.success(stats);
    }
    
    @GetMapping("/export")
    public void exportExcel(@RequestParam(required = false) String month, HttpServletResponse response) throws Exception {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bill_type", "payment");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("bill_month", month);
        }
        List<BillingRecord> records = billingRecordService.list(queryWrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("缴费记录");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学生账号", "学生姓名", "缴费月份", "金额", "状态", "缴费时间"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        int rowNum = 1;
        for (BillingRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getStudentUsername() != null ? record.getStudentUsername() : "");
            row.createCell(1).setCellValue(record.getStudentName() != null ? record.getStudentName() : "");
            row.createCell(2).setCellValue(record.getBillMonth() != null ? record.getBillMonth() : "");
            row.createCell(3).setCellValue(record.getTotalAmount() != null ? record.getTotalAmount().doubleValue() : 0.0);
            row.createCell(4).setCellValue(record.getStatus() != null ? record.getStatus() : "");
            row.createCell(5).setCellValue(record.getPaymentTime() != null ? record.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("缴费记录.xlsx", "UTF-8"));
        
        OutputStream out = response.getOutputStream();
        workbook.write(out);
        workbook.close();
        out.close();
    }
    
    @PostMapping("/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        int count = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            String username = getCellValue(row.getCell(0));
            String month = getCellValue(row.getCell(2));
            String status = getCellValue(row.getCell(4));
            
            BillingRecord record = billingRecordService.getByStudentAndMonth(username, month, "payment");
            
            if (record != null && "已缴费".equals(status)) {
                record.setStatus("paid");
                record.setPaymentTime(LocalDateTime.now());
                billingRecordService.updateById(record);
                count++;
            }
        }
        
        workbook.close();
        inputStream.close();
        return Result.success("成功导入" + count + "条记录");
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}
