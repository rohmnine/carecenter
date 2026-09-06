package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.BillingRecord;

import java.util.List;
import java.util.Map;

public interface BillingRecordService extends IService<BillingRecord> {
    
    Page<BillingRecord> findByBillType(Integer pageNum, Integer pageSize, String billType, String search, String month, String status);
    
    List<BillingRecord> getByStudentUsername(String studentUsername);
    
    BillingRecord getByStudentAndMonth(String studentUsername, String month, String billType);
    
    void generateMonthlyMealBills(String month);
    
    int markAsPaid(Integer id);
    
    List<BillingRecord> getParentChildBills(String parentUsername);
    
    Map<String, Object> getBillStatistics(String month);
}
