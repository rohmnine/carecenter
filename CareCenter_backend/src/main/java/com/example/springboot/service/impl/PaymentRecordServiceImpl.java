package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Parent;
import com.example.springboot.entity.PaymentRecord;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.ParentMapper;
import com.example.springboot.mapper.PaymentRecordMapper;
import com.example.springboot.mapper.StudentMapper;
import com.example.springboot.service.ParentNotificationService;
import com.example.springboot.service.PaymentRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentRecordServiceImpl implements PaymentRecordService {
    
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private ParentMapper parentMapper;
    
    @Resource
    private ParentNotificationService parentNotificationService;
    
    @Override
    public Page<PaymentRecord> findPage(Integer pageNum, Integer pageSize, String search, String month, String status) {
        QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        if (search != null && !search.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("student_username", search)
                .or()
                .like("student_name", search));
        }
        
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("payment_month", month);
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        return paymentRecordMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }
    
    @Override
    public int markAsPaid(Integer id) {
        PaymentRecord record = paymentRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus("已缴费");
            record.setPaymentTime(LocalDateTime.now());
            return paymentRecordMapper.updateById(record);
        }
        return 0;
    }
    
    @Override
    public int sendPaymentReminder(String studentUsername) {
        Student student = studentMapper.selectById(studentUsername);
        if (student == null) {
            return 0;
        }
        
        // Check if student has unpaid records for current month
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        QueryWrapper<PaymentRecord> paymentQuery = new QueryWrapper<>();
        paymentQuery.eq("student_username", studentUsername)
                   .eq("payment_month", currentMonth)
                   .eq("status", "未缴费");
        PaymentRecord unpaidRecord = paymentRecordMapper.selectOne(paymentQuery);
        
        // Only send reminder if there's an unpaid record
        if (unpaidRecord == null) {
            return 0;
        }
        
        QueryWrapper<Parent> parentQuery = new QueryWrapper<>();
        parentQuery.eq("student_username", studentUsername);
        Parent parent = parentMapper.selectOne(parentQuery);
        
        if (parent == null) {
            return 0;
        }
        
        String content = String.format("您的孩子%s(%s)本月(%s)托管费尚未缴纳，请及时缴费",
            student.getName(), studentUsername, currentMonth);
        
        return parentNotificationService.sendCustomNotification(
            parent.getUsername(),
            studentUsername,
            student.getName(),
            "payment_reminder",
            "托管费缴费提醒",
            content
        );
    }
    
    @Override
    public void generateMonthlyPaymentRecords(BigDecimal amount) {
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Student> students = studentMapper.selectList(null);
        
        for (Student student : students) {
            QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_username", student.getUsername())
                       .eq("payment_month", currentMonth);
            
            PaymentRecord existing = paymentRecordMapper.selectOne(queryWrapper);
            if (existing == null) {
                PaymentRecord record = new PaymentRecord();
                record.setStudentUsername(student.getUsername());
                record.setStudentName(student.getName());
                record.setPaymentMonth(currentMonth);
                record.setAmount(amount);
                record.setStatus("未缴费");
                record.setCreateTime(LocalDateTime.now());
                paymentRecordMapper.insert(record);
            }
        }
    }
    
    @Override
    public PaymentRecord getByStudentAndMonth(String studentUsername, String month) {
        QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                   .eq("payment_month", month);
        return paymentRecordMapper.selectOne(queryWrapper);
    }
    
    @Override
    public List<PaymentRecord> getUnpaidRecords(String month) {
        QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "未缴费");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("payment_month", month);
        }
        return paymentRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public int batchMarkAsPaid(List<Integer> ids) {
        int count = 0;
        for (Integer id : ids) {
            count += markAsPaid(id);
        }
        return count;
    }
    
    @Override
    public int batchSendReminder(List<String> studentUsernames) {
        int count = 0;
        for (String username : studentUsernames) {
            count += sendPaymentReminder(username);
        }
        return count;
    }
    
    @Override
    public Map<String, Object> getPaymentStatistics(String month) {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("payment_month", month);
        }
        
        Long total = paymentRecordMapper.selectCount(queryWrapper);
        
        queryWrapper.eq("status", "已缴费");
        Long paid = paymentRecordMapper.selectCount(queryWrapper);
        
        double rate = total > 0 ? (paid * 100.0 / total) : 0;
        
        stats.put("total", total);
        stats.put("paid", paid);
        stats.put("unpaid", total - paid);
        stats.put("rate", String.format("%.2f", rate));
        
        return stats;
    }
    
    @Override
    public void exportToExcel(String month, HttpServletResponse response) throws Exception {
        QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("payment_month", month);
        }
        List<PaymentRecord> records = paymentRecordMapper.selectList(queryWrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("缴费记录");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学生账号", "学生姓名", "缴费月份", "金额", "状态", "缴费时间"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        int rowNum = 1;
        for (PaymentRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getStudentUsername() != null ? record.getStudentUsername() : "");
            row.createCell(1).setCellValue(record.getStudentName() != null ? record.getStudentName() : "");
            row.createCell(2).setCellValue(record.getPaymentMonth() != null ? record.getPaymentMonth() : "");
            row.createCell(3).setCellValue(record.getAmount() != null ? record.getAmount().doubleValue() : 0.0);
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
    
    @Override
    public int importFromExcel(MultipartFile file) throws Exception {
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
            
            QueryWrapper<PaymentRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_username", username).eq("payment_month", month);
            PaymentRecord record = paymentRecordMapper.selectOne(queryWrapper);
            
            if (record != null && "已缴费".equals(status)) {
                record.setStatus("已缴费");
                record.setPaymentTime(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                paymentRecordMapper.updateById(record);
                count++;
            }
        }
        
        workbook.close();
        inputStream.close();
        return count;
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
