package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.entity.BillingRecord;
import com.example.springboot.entity.MealBillingConfig;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.mapper.ApprovalRequestMapper;
import com.example.springboot.mapper.BillingRecordMapper;
import com.example.springboot.mapper.MealBillingConfigMapper;
import com.example.springboot.mapper.UserEntityMapper;
import com.example.springboot.service.BillingRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
public class BillingRecordServiceImpl extends ServiceImpl<BillingRecordMapper, BillingRecord> implements BillingRecordService {

    @Resource
    private BillingRecordMapper billingRecordMapper;

    @Resource
    private MealBillingConfigMapper mealBillingConfigMapper;

    @Resource
    private UserEntityMapper userEntityMapper;

    @Resource
    private ApprovalRequestMapper approvalRequestMapper;

    @Override
    public Page<BillingRecord> findByBillType(Integer pageNum, Integer pageSize, String billType, String search, String month, String status) {
        Page<BillingRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("bill_type", billType);

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

        return billingRecordMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<BillingRecord> getByStudentUsername(String studentUsername) {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .orderByDesc("bill_month");
        return billingRecordMapper.selectList(queryWrapper);
    }

    @Override
    public BillingRecord getByStudentAndMonth(String studentUsername, String month, String billType) {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("bill_month", month)
                .eq("bill_type", billType);
        return billingRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public void generateMonthlyMealBills(String month) {
        Map<String, BigDecimal> configMap = getConfigMap();
        BigDecimal breakfastPrice = configMap.getOrDefault("breakfast_price", new BigDecimal("8"));
        BigDecimal lunchPrice = configMap.getOrDefault("lunch_price", new BigDecimal("15"));
        BigDecimal dinnerPrice = configMap.getOrDefault("dinner_price", new BigDecimal("20"));
        BigDecimal monthlyBaseFee = configMap.getOrDefault("boarding_fee", new BigDecimal("500"));
        BigDecimal leaveRefundPerDay = configMap.getOrDefault("leave_refund_per_day", new BigDecimal("20"));

        YearMonth yearMonth = YearMonth.parse(month);
        String startDate = yearMonth.atDay(1).toString();
        String endDate = yearMonth.atEndOfMonth().toString();

        // 1) 生成学生餐费账单
        QueryWrapper<UserEntity> studentQuery = new QueryWrapper<>();
        studentQuery.eq("user_type", "student");
        List<UserEntity> students = userEntityMapper.selectList(studentQuery);

        for (UserEntity student : students) {
            BillingRecord existing = getByStudentAndMonth(student.getUsername(), month, "meal");
            if (existing != null) {
                continue;
            }

            String boardingType = student.getBoardingType();
            if (!StringUtils.hasLength(boardingType)) {
                boardingType = "meal_and_rest";
            }

            int leaveDays = calculateLeaveDays(student.getUsername(), yearMonth);
            int breakfastReservations = countMealReservations(student.getUsername(), startDate, endDate, "breakfast");
            int lunchReservations = countMealReservations(student.getUsername(), startDate, endDate, "lunch");
            int dinnerReservations = countMealReservations(student.getUsername(), startDate, endDate, "dinner");

            BigDecimal breakfastAmount = breakfastPrice.multiply(new BigDecimal(breakfastReservations));
            BigDecimal lunchAmount = lunchPrice.multiply(new BigDecimal(lunchReservations));
            BigDecimal dinnerAmount = dinnerPrice.multiply(new BigDecimal(dinnerReservations));
            BigDecimal reservationMealSubtotal = breakfastAmount.add(lunchAmount).add(dinnerAmount);
            BigDecimal leaveRefund = leaveRefundPerDay.multiply(new BigDecimal(leaveDays));

            BigDecimal boardingFee = BigDecimal.ZERO;
            BigDecimal finalAmount = BigDecimal.ZERO;

            // 根据住宿类型计算费用
            if ("meal_and_rest".equals(boardingType)) {
                // 住校生：住宿费用+ 加餐费用 - 请假扣费
                boardingFee = monthlyBaseFee;
                finalAmount = monthlyBaseFee.add(reservationMealSubtotal).subtract(leaveRefund);
            } else if ("meal_only".equals(boardingType)) {
                // 只就餐饮宿：只有加餐费用（不住宿�?
                boardingFee = BigDecimal.ZERO;
                finalAmount = reservationMealSubtotal;
            } else if ("rest_only".equals(boardingType)) {
                // 只住宿：只有住宿费（不在校就餐）
                boardingFee = monthlyBaseFee;
                finalAmount = monthlyBaseFee.subtract(leaveRefund);
            }

            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            BillingRecord bill = new BillingRecord();
            bill.setStudentUsername(student.getUsername());
            bill.setStudentName(student.getName());
            bill.setBillMonth(month);
            bill.setBillType("meal");
            bill.setBoardingType(boardingType);
            bill.setBreakfastDays(breakfastReservations);
            bill.setBreakfastPrice(breakfastReservations > 0 ? breakfastPrice : BigDecimal.ZERO);
            bill.setLunchDays(lunchReservations);
            bill.setLunchPrice(lunchPrice);
            bill.setDinnerDays(dinnerReservations);
            bill.setDinnerPrice(dinnerPrice);
            bill.setBoardingFee(boardingFee);
            bill.setLeaveDeductDays(leaveDays);
            bill.setMealTotal(reservationMealSubtotal);
            bill.setTotalAmount(finalAmount);
            bill.setStatus("unpaid");
            bill.setCreateTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());

            billingRecordMapper.insert(bill);
        }

        // 2) 生成无孩子家长的餐费账单（按“仅午餐(走读)”模型：无午休费、无请假扣减、按预约次数计费
        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("user_type", "parent")
                .and(w -> w.isNull("student_username").or().eq("student_username", ""));
        List<UserEntity> parentsWithoutChild = userEntityMapper.selectList(parentQuery);

        for (UserEntity parent : parentsWithoutChild) {
            BillingRecord existing = getByStudentAndMonth(parent.getUsername(), month, "meal");
            if (existing != null) {
                continue;
            }

            int breakfastReservations = countParentMealReservations(parent.getUsername(), startDate, endDate, "breakfast");
            int lunchReservations = countParentMealReservations(parent.getUsername(), startDate, endDate, "lunch");
            int dinnerReservations = countParentMealReservations(parent.getUsername(), startDate, endDate, "dinner");

            // 与“仅午餐(走读)”一致：不收取午休费，不计算请假退�?
            String boardingType = "meal_only";
            int leaveDays = 0;
            BigDecimal boardingFee = BigDecimal.ZERO;
            BigDecimal reservationMealSubtotal = breakfastPrice.multiply(new BigDecimal(breakfastReservations))
                    .add(lunchPrice.multiply(new BigDecimal(lunchReservations)))
                    .add(dinnerPrice.multiply(new BigDecimal(dinnerReservations)));
            BigDecimal finalAmount = reservationMealSubtotal.max(BigDecimal.ZERO);

            BillingRecord bill = new BillingRecord();
            // 复用 student_username/student_name 字段存储账单归属主体（家长账户姓�?
            bill.setStudentUsername(parent.getUsername());
            bill.setStudentName(parent.getName());
            bill.setBillMonth(month);
            bill.setBillType("meal");
            bill.setBoardingType(boardingType);
            bill.setBreakfastDays(breakfastReservations);
            bill.setBreakfastPrice(breakfastReservations > 0 ? breakfastPrice : BigDecimal.ZERO);
            bill.setLunchDays(lunchReservations);
            bill.setLunchPrice(lunchPrice);
            bill.setDinnerDays(dinnerReservations);
            bill.setDinnerPrice(dinnerPrice);
            bill.setBoardingFee(boardingFee);
            bill.setLeaveDeductDays(leaveDays);
            bill.setMealTotal(reservationMealSubtotal);
            bill.setTotalAmount(finalAmount);
            bill.setStatus("unpaid");
            bill.setRemark("parent_self_meal_bill");
            bill.setCreateTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());

            billingRecordMapper.insert(bill);
        }
    }

    @Override
    public int markAsPaid(Integer id) {
        BillingRecord bill = billingRecordMapper.selectById(id);
        if (bill == null) {
            return 0;
        }
        bill.setStatus("paid");
        bill.setPaymentTime(LocalDateTime.now());
        bill.setUpdateTime(LocalDateTime.now());
        return billingRecordMapper.updateById(bill);
    }

    @Override
    public List<BillingRecord> getParentChildBills(String parentUsername) {
        QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
        parentQuery.eq("username", parentUsername).eq("user_type", "parent");
        UserEntity parent = userEntityMapper.selectOne(parentQuery);
        if (parent == null) {
            return Collections.emptyList();
        }

        // 有孩子家长：查询孩子账单；无孩子家长：查询本人账�?
        String billOwnerUsername = StringUtils.hasLength(parent.getStudentUsername())
                ? parent.getStudentUsername()
                : parent.getUsername();

        return getByStudentUsername(billOwnerUsername);
    }

    @Override
    public Map<String, Object> getBillStatistics(String month) {
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasLength(month)) {
            queryWrapper.eq("bill_month", month);
        }
        List<BillingRecord> bills = billingRecordMapper.selectList(queryWrapper);

        int totalBills = bills.size();
        int paidCount = 0;
        int unpaidCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (BillingRecord bill : bills) {
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

    private int calculateLeaveDays(String studentUsername, YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        QueryWrapper<ApprovalRequest> leaveQuery = new QueryWrapper<>();
        leaveQuery.eq("student_username", studentUsername)
                .eq("request_type", "leave_request")
                .eq("status", "approved");

        List<ApprovalRequest> leaveRecords = approvalRequestMapper.selectList(leaveQuery);

        int totalLeaveDays = 0;
        for (ApprovalRequest leave : leaveRecords) {
            if (leave.getRequestData() == null) {
                continue;
            }
            Object startTimeObj = leave.getRequestData().get("start_time");
            Object endTimeObj = leave.getRequestData().get("end_time");
            if (startTimeObj == null || endTimeObj == null) {
                continue;
            }

            try {
                LocalDate leaveStart = LocalDate.parse(startTimeObj.toString().substring(0, 10));
                LocalDate leaveEnd = LocalDate.parse(endTimeObj.toString().substring(0, 10));

                LocalDate effectiveStart = leaveStart.isBefore(monthStart) ? monthStart : leaveStart;
                LocalDate effectiveEnd = leaveEnd.isAfter(monthEnd) ? monthEnd : leaveEnd;

                for (LocalDate d = effectiveStart; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
                    int dayOfWeek = d.getDayOfWeek().getValue();
                    if (dayOfWeek <= 5) {
                        totalLeaveDays++;
                    }
                }
            } catch (Exception e) {
                // Skip invalid dates
            }
        }

        return totalLeaveDays;
    }

    private int countMealReservations(String studentUsername, String startDate, String endDate, String mealType) {
        QueryWrapper<ApprovalRequest> query = new QueryWrapper<>();
        query.eq("student_username", studentUsername)
                .eq("request_type", "meal_reservation")
                .in("status", "approved", "reserved");

        List<ApprovalRequest> reservations = approvalRequestMapper.selectList(query);

        int count = 0;
        for (ApprovalRequest reservation : reservations) {
            if (reservation.getRequestData() == null) {
                continue;
            }
            // Count all approved/reserved reservations (meal reservation means the parent/student reserved a meal and should pay)
            // The mealConfirmed field is optional - if set to true, it means admin confirmed actual dining
            // But for billing purposes, we count all approved reservations as they represent meals the student signed up for
            Object dateObj = reservation.getRequestData().get("reservation_date");
            Object mealTypeObj = reservation.getRequestData().get("meal_type");
            if (dateObj != null && mealTypeObj != null) {
                String date = dateObj.toString();
                if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0 && mealType.equals(mealTypeObj.toString())) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countParentMealReservations(String parentUsername, String startDate, String endDate, String mealType) {
        QueryWrapper<ApprovalRequest> query = new QueryWrapper<>();
        query.eq("requester_username", parentUsername)
                .eq("requester_type", "parent")
                .eq("request_type", "meal_reservation")
                .in("status", "approved", "reserved");

        List<ApprovalRequest> reservations = approvalRequestMapper.selectList(query);

        int count = 0;
        for (ApprovalRequest reservation : reservations) {
            if (reservation.getRequestData() == null) {
                continue;
            }
            Object dateObj = reservation.getRequestData().get("reservation_date");
            Object mealTypeObj = reservation.getRequestData().get("meal_type");
            if (dateObj != null && mealTypeObj != null) {
                String date = dateObj.toString();
                if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0 && mealType.equals(mealTypeObj.toString())) {
                    count++;
                }
            }
        }
        return count;
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
