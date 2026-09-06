package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.MealBill;
import com.example.springboot.entity.MealBillingConfig;

import java.util.List;
import java.util.Map;

public interface MealBillService {
    // Billing config
    List<MealBillingConfig> getAllConfigs();
    int updateConfig(MealBillingConfig config);

    // Student boarding type management
    int updateStudentBoardingType(String studentUsername, String boardingType);

    // Bill operations
    Page<MealBill> findPage(Integer pageNum, Integer pageSize, String search, String month, String status);
    List<MealBill> getByStudentUsername(String studentUsername);
    MealBill getByStudentAndMonth(String studentUsername, String month);
    void generateMonthlyBills(String month);
    int markAsPaid(Integer id);

    // Parent view
    List<MealBill> getParentChildBills(String parentUsername);
    Map<String, Object> getBillDetail(Integer billId);

    // Statistics
    Map<String, Object> getBillStatistics(String month);
}
