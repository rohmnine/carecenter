package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.PaymentRecord;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentRecordService {
    Page<PaymentRecord> findPage(Integer pageNum, Integer pageSize, String search, String month, String status);
    
    int markAsPaid(Integer id);
    
    int sendPaymentReminder(String studentUsername);
    
    void generateMonthlyPaymentRecords(BigDecimal amount);
    
    PaymentRecord getByStudentAndMonth(String studentUsername, String month);
    
    List<PaymentRecord> getUnpaidRecords(String month);
    
    int batchMarkAsPaid(List<Integer> ids);
    
    int batchSendReminder(List<String> studentUsernames);
    
    Map<String, Object> getPaymentStatistics(String month);
    
    void exportToExcel(String month, HttpServletResponse response) throws Exception;
    
    int importFromExcel(MultipartFile file) throws Exception;
}
