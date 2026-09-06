package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import com.example.springboot.mapper.ParentMealReservationMapper;
import com.example.springboot.service.MealBillService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class MealBillServiceImpl implements MealBillService {

    @Resource
    private MealBillMapper mealBillMapper;

    @Resource
    private MealBillingConfigMapper mealBillingConfigMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ParentMapper parentMapper;

    @Resource
    private LeaveRequestMapper leaveRequestMapper;

    @Resource
    private ParentMealReservationMapper parentMealReservationMapper;

    @Override
    public List<MealBillingConfig> getAllConfigs() {
        return mealBillingConfigMapper.selectList(null);
    }

    @Override
    public int updateConfig(MealBillingConfig config) {
        return mealBillingConfigMapper.updateById(config);
    }

    @Override
    public int updateStudentBoardingType(String studentUsername, String boardingType) {
        UpdateWrapper<Student> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", studentUsername)
                .set("boarding_type", boardingType);
        return studentMapper.update(null, updateWrapper);
    }

    @Override
    public Page<MealBill> findPage(Integer pageNum, Integer pageSize, String search, String month, String status) {
        Page<MealBill> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MealBill> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasLength(search)) {
            queryWrapper.and(w -> w.like("student_username", search)
                    .or().like("student_name", search));
        }
        if (StringUtils.hasLength(month)) {
            queryWrapper.eq("bill_month", month);
        }
        if (StringUtils.hasLength(status)) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        return mealBillMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<MealBill> getByStudentUsername(String studentUsername) {
        QueryWrapper<MealBill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .orderByDesc("bill_month");
        return mealBillMapper.selectList(queryWrapper);
    }

    @Override
    public MealBill getByStudentAndMonth(String studentUsername, String month) {
        QueryWrapper<MealBill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("bill_month", month);
        return mealBillMapper.selectOne(queryWrapper);
    }

    @Override
    public void generateMonthlyBills(String month) {
        // Billing rule:
        // 1) lunch + dinner charged by reservation count (defaults: 15/20 per day)
        // 2) boarding fee fixed monthly fee (default 500)
        // 3) leave refund by day (configurable, default 20/day)
        Map<String, BigDecimal> configMap = getConfigMap();
        BigDecimal lunchPrice = configMap.getOrDefault("lunch_price", new BigDecimal("15"));
        BigDecimal dinnerPrice = configMap.getOrDefault("dinner_price", new BigDecimal("20"));
        BigDecimal monthlyBaseFee = configMap.getOrDefault("boarding_fee", new BigDecimal("500"));
        BigDecimal leaveRefundPerDay = configMap.getOrDefault("leave_refund_per_day", new BigDecimal("20"));

        YearMonth yearMonth = YearMonth.parse(month);
        String startDate = yearMonth.atDay(1).toString();
        String endDate = yearMonth.atEndOfMonth().toString();

        List<Student> students = studentMapper.selectList(null);

        Map<String, Integer> leaveDaysMap = leaveRequestMapper.selectLeaveSummaryByDate(startDate, endDate)
                .stream()
                .collect(java.util.stream.Collectors.toMap(LeaveSummary::getUsername,
                        item -> item.getTotalDays() == null ? 0 : item.getTotalDays()));

        Map<String, Integer> lunchReservationMap = parentMealReservationMapper
                .selectStudentMealSummaryByDate(startDate, endDate, "lunch")
                .stream()
                .collect(java.util.stream.Collectors.toMap(MealReservationSummary::getUsername,
                        item -> item.getTotalCount() == null ? 0 : item.getTotalCount()));

        Map<String, Integer> dinnerReservationMap = parentMealReservationMapper
                .selectStudentMealSummaryByDate(startDate, endDate, "dinner")
                .stream()
                .collect(java.util.stream.Collectors.toMap(MealReservationSummary::getUsername,
                        item -> item.getTotalCount() == null ? 0 : item.getTotalCount()));

        for (Student student : students) {
            MealBill existing = getByStudentAndMonth(student.getUsername(), month);
            if (existing != null) {
                continue;
            }

            String boardingType = student.getBoardingType();
            if (!StringUtils.hasLength(boardingType)) {
                boardingType = "meal_and_rest";
            }

            String username = student.getUsername();
            int leaveDays = leaveDaysMap.getOrDefault(username, 0);
            int lunchReservations = lunchReservationMap.getOrDefault(username, 0);
            int dinnerReservations = dinnerReservationMap.getOrDefault(username, 0);

            BigDecimal lunchAmount = lunchPrice.multiply(new BigDecimal(lunchReservations));
            BigDecimal dinnerAmount = dinnerPrice.multiply(new BigDecimal(dinnerReservations));
            BigDecimal reservationMealSubtotal = lunchAmount.add(dinnerAmount);
            BigDecimal leaveRefund = leaveRefundPerDay.multiply(new BigDecimal(leaveDays));

            // UI semantics:
            // mealTotal => reservation meal subtotal (breakfast+lunch+dinner by reservation counts)
            // totalAmount => monthly base fee + reservation subtotal - leave refund
            BigDecimal finalAmount = monthlyBaseFee.add(reservationMealSubtotal).subtract(leaveRefund);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            MealBill bill = new MealBill();
            bill.setStudentUsername(student.getUsername());
            bill.setStudentName(student.getName());
            bill.setBillMonth(month);
            bill.setBoardingType(boardingType);
            bill.setLunchDays(lunchReservations);
            bill.setLunchPrice(lunchPrice);
            bill.setDinnerDays(dinnerReservations);
            bill.setDinnerPrice(dinnerPrice);
            bill.setBoardingFee(monthlyBaseFee);
            bill.setLeaveDeductDays(leaveDays);
            bill.setMealTotal(reservationMealSubtotal);
            bill.setTotalAmount(finalAmount);
            bill.setStatus("unpaid");
            bill.setCreateTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());

            mealBillMapper.insert(bill);
        }
    }

    @Override
    public int markAsPaid(Integer id) {
        MealBill bill = mealBillMapper.selectById(id);
        if (bill == null) {
            return 0;
        }
        bill.setStatus("paid");
        bill.setUpdateTime(LocalDateTime.now());
        return mealBillMapper.updateById(bill);
    }

    @Override
    public List<MealBill> getParentChildBills(String parentUsername) {
        // Find the student linked to this parent
        QueryWrapper<Parent> parentQuery = new QueryWrapper<>();
        parentQuery.eq("username", parentUsername);
        Parent parent = parentMapper.selectOne(parentQuery);
        if (parent == null || parent.getStudentUsername() == null) {
            return Collections.emptyList();
        }

        return getByStudentUsername(parent.getStudentUsername());
    }

    @Override
    public Map<String, Object> getBillDetail(Integer billId) {
        Map<String, Object> result = new HashMap<>();
        MealBill bill = mealBillMapper.selectById(billId);
        if (bill == null) {
            return result;
        }
        result.put("bill", bill);

        // Get leave records for this student in this bill month
        YearMonth yearMonth = YearMonth.parse(bill.getBillMonth());
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        QueryWrapper<LeaveRequest> leaveQuery = new QueryWrapper<>();
        leaveQuery.eq("student_username", bill.getStudentUsername())
                .eq("status", "approved")
                .le("start_time", java.sql.Date.valueOf(monthEnd))
                .ge("end_time", java.sql.Date.valueOf(monthStart));
        List<LeaveRequest> leaveRecords = leaveRequestMapper.selectList(leaveQuery);
        result.put("leaveRecords", leaveRecords);

        return result;
    }

    @Override
    public Map<String, Object> getBillStatistics(String month) {
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<MealBill> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasLength(month)) {
            queryWrapper.eq("bill_month", month);
        }
        List<MealBill> bills = mealBillMapper.selectList(queryWrapper);

        int totalBills = bills.size();
        int paidCount = 0;
        int unpaidCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (MealBill bill : bills) {
            totalAmount = totalAmount.add(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
            if ("paid".equals(bill.getStatus())) {
                paidCount++;
                paidAmount = paidAmount.add(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
            } else {
                unpaidCount++;
                unpaidAmount = unpaidAmount.add(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
            }
        }

        stats.put("totalBills", totalBills);
        stats.put("paidCount", paidCount);
        stats.put("unpaidCount", unpaidCount);
        stats.put("totalAmount", totalAmount);
        stats.put("paidAmount", paidAmount);
        stats.put("unpaidAmount", unpaidAmount);

        return stats;
    }

    private Map<String, BigDecimal> getConfigMap() {
        List<MealBillingConfig> configs = mealBillingConfigMapper.selectList(null);
        Map<String, BigDecimal> map = new HashMap<>();
        for (MealBillingConfig config : configs) {
            try {
                map.put(config.getConfigKey(), new BigDecimal(config.getConfigValue()));
            } catch (NumberFormatException e) {
                // Skip non-numeric configs
            }
        }
        return map;
    }
}
