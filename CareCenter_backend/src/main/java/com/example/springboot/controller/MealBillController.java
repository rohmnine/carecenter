package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.entity.ApprovalRequest;
import com.example.springboot.entity.BillingRecord;
import com.example.springboot.entity.MealBillingConfig;
import com.example.springboot.entity.UserEntity;
import com.example.springboot.service.ApprovalRequestService;
import com.example.springboot.service.BillingRecordService;
import com.example.springboot.mapper.MealBillingConfigMapper;
import com.example.springboot.mapper.UserEntityMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mealBill")
public class MealBillController {

    @Resource
    private BillingRecordService billingRecordService;

    @Resource
    private MealBillingConfigMapper mealBillingConfigMapper;

    @Resource
    private UserEntityMapper userEntityMapper;
    
    @Resource
    private ApprovalRequestService approvalRequestService;

    @Resource
    private com.example.springboot.service.ParentNotificationService parentNotificationService;

    // ========== Billing Config APIs ==========

    /**
     * Get all billing config items
     */
    @GetMapping("/config")
    public Result<List<MealBillingConfig>> getConfigs() {
        return Result.success(mealBillingConfigMapper.selectList(null));
    }

    /**
     * Update a billing config item
     */
    @PutMapping("/config")
    public Result<Integer> updateConfig(@RequestBody MealBillingConfig config) {
        return Result.success(mealBillingConfigMapper.updateById(config));
    }

    // ========== Student Boarding Type APIs ==========

    /**
     * Update student boarding type
     */
    @PutMapping("/studentBoardingType")
    public Result<Integer> updateStudentBoardingType(@RequestParam String studentUsername,
                                                      @RequestParam String boardingType) {
        UpdateWrapper<com.example.springboot.entity.UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", studentUsername)
                .set("boarding_type", boardingType);
        return Result.success(userEntityMapper.update(null, updateWrapper));
    }

    // ========== Bill Management APIs ==========

    /**
     * Get bills with pagination (admin)
     */
    @GetMapping("/page")
    public Result<Page<BillingRecord>> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String search,
                                                 @RequestParam(required = false) String month,
                                                 @RequestParam(required = false) String status) {
        return Result.success(billingRecordService.findByBillType(pageNum, pageSize, "meal", search, month, status));
    }

    /**
     * Get all bill IDs matching filter (for cross-page select-all)
     */
    @GetMapping("/allIds")
    public Result<List<Integer>> getAllIds(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String month,
                                            @RequestParam(required = false) String status) {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bill_type", "meal");
        queryWrapper.select("id");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("bill_month", month);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (search != null && !search.isEmpty()) {
            queryWrapper.and(w -> w.like("student_name", search).or().like("student_username", search));
        }
        List<BillingRecord> records = billingRecordService.list(queryWrapper);
        List<Integer> ids = new ArrayList<>();
        for (BillingRecord r : records) {
            ids.add(r.getId());
        }
        return Result.success(ids);
    }

    /**
     * Generate monthly bills (admin trigger)
     */
    @PostMapping("/generate")
    public Result<String> generateBills(@RequestParam String month) {
        billingRecordService.generateMonthlyMealBills(month);
        return Result.success("Bills generated successfully for " + month);
    }

    /**
     * Mark bill as paid
     */
    @PutMapping("/pay/{id}")
    public Result<Integer> markAsPaid(@PathVariable Integer id) {
        return Result.success(billingRecordService.markAsPaid(id));
    }

    /**
     * Update meal bill record (admin manual adjustment)
     */
    @PutMapping("/updateRecord")
    public Result<String> updateRecord(@RequestBody BillingRecord form) {
        if (form.getId() == null) {
            return Result.error("-1", "账单ID不能为空");
        }

        BillingRecord bill = billingRecordService.getById(form.getId());
        if (bill == null) {
            return Result.error("-1", "账单不存在");
        }
        if (!"meal".equals(bill.getBillType())) {
            return Result.error("-1", "仅支持修改餐费账单");
        }

        if (form.getBoardingType() != null) {
            bill.setBoardingType(form.getBoardingType());
        }
        if (form.getLunchDays() != null) {
            bill.setLunchDays(Math.max(form.getLunchDays(), 0));
        }
        if (form.getDinnerDays() != null) {
            bill.setDinnerDays(Math.max(form.getDinnerDays(), 0));
        }
        if (form.getLeaveDeductDays() != null) {
            bill.setLeaveDeductDays(Math.max(form.getLeaveDeductDays(), 0));
        }
        if (form.getLunchPrice() != null) {
            bill.setLunchPrice(form.getLunchPrice().max(BigDecimal.ZERO));
        }
        if (form.getDinnerPrice() != null) {
            bill.setDinnerPrice(form.getDinnerPrice().max(BigDecimal.ZERO));
        }
        if (form.getBoardingFee() != null) {
            bill.setBoardingFee(form.getBoardingFee().max(BigDecimal.ZERO));
        }
        if (form.getRemark() != null) {
            bill.setRemark(form.getRemark());
        }

        int lunchDays = bill.getLunchDays() == null ? 0 : bill.getLunchDays();
        int dinnerDays = bill.getDinnerDays() == null ? 0 : bill.getDinnerDays();
        int leaveDays = bill.getLeaveDeductDays() == null ? 0 : bill.getLeaveDeductDays();
        BigDecimal lunchPrice = bill.getLunchPrice() == null ? BigDecimal.ZERO : bill.getLunchPrice();
        BigDecimal dinnerPrice = bill.getDinnerPrice() == null ? BigDecimal.ZERO : bill.getDinnerPrice();
        BigDecimal boardingFee = bill.getBoardingFee() == null ? BigDecimal.ZERO : bill.getBoardingFee();

        BigDecimal mealTotal = lunchPrice.multiply(BigDecimal.valueOf(lunchDays))
                .add(dinnerPrice.multiply(BigDecimal.valueOf(dinnerDays)));
        bill.setMealTotal(mealTotal);

        BigDecimal leaveRefund = BigDecimal.valueOf(20L * leaveDays);
        BigDecimal autoTotal = boardingFee.add(mealTotal).subtract(leaveRefund);
        if (autoTotal.compareTo(BigDecimal.ZERO) < 0) {
            autoTotal = BigDecimal.ZERO;
        }

        if (form.getTotalAmount() != null) {
            bill.setTotalAmount(form.getTotalAmount().max(BigDecimal.ZERO));
        } else {
            bill.setTotalAmount(autoTotal);
        }

        bill.setUpdateTime(LocalDateTime.now());
        boolean ok = billingRecordService.updateById(bill);
        if (!ok) {
            return Result.error("-1", "修改失败");
        }
        return Result.success("修改成功");
    }

    /**
     * Get bill detail with leave records
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getBillDetail(@PathVariable Integer id) {
        BillingRecord bill = billingRecordService.getById(id);
        if (bill == null) {
            return Result.error("-1", "账单不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("bill", bill);
        
        // Get leave records for this month
        List<ApprovalRequest> leaveRecords = new ArrayList<>();
        if (bill.getStudentUsername() != null && bill.getBillMonth() != null) {
            QueryWrapper<ApprovalRequest> wrapper = new QueryWrapper<>();
            wrapper.eq("student_username", bill.getStudentUsername())
                   .eq("request_type", "leave_request")
                   .eq("status", "approved")
                   .likeRight("create_time", bill.getBillMonth());
            
            leaveRecords = approvalRequestService.list(wrapper);
        }
        result.put("leaveRecords", leaveRecords);
        
        return Result.success(result);
    }

    /**
     * Get bill statistics
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false) String month) {
        return Result.success(billingRecordService.getBillStatistics(month));
    }

    @PutMapping("/batchPay")
    public Result<String> batchPay(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要处理的账单");
        }

        int count = 0;
        for (Integer id : ids) {
            BillingRecord bill = billingRecordService.getById(id);
            if (bill == null || !"meal".equals(bill.getBillType())) {
                continue;
            }
            count += billingRecordService.markAsPaid(id);
        }
        return Result.success("成功标记" + count + "条记录");
    }

    @PostMapping("/batchSendReminder")
    public Result<String> batchSendReminder(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.error("-1", "请选择要提醒的账单");
        }

        int count = 0;
        for (Integer id : ids) {
            BillingRecord bill = billingRecordService.getById(id);
            if (bill == null || !"meal".equals(bill.getBillType())) {
                continue;
            }
            if ("paid".equals(bill.getStatus())) {
                continue;
            }

            String billOwnerUsername = bill.getStudentUsername();
            if (billOwnerUsername == null || billOwnerUsername.isEmpty()) {
                continue;
            }

            // 兼容两类账单
            // 1) 孩子账单：student_username=孩子账号，需要通过 student_username 找家长
            // 2) 无孩子家长自有账单：student_username=家长账号，需要直接按 username 找家长
            UserEntity parent = null;
            QueryWrapper<UserEntity> selfParentQuery = new QueryWrapper<>();
            selfParentQuery.eq("username", billOwnerUsername).eq("user_type", "parent");
            parent = userEntityMapper.selectOne(selfParentQuery);

            String notifyStudentUsername = billOwnerUsername;
            String notifyStudentName = bill.getStudentName() == null ? billOwnerUsername : bill.getStudentName();
            String content;

            if (parent != null) {
                // 无孩子家长本人账号
                content = String.format("您本人的餐费账单(%s)尚未缴纳，请及时缴费用", bill.getBillMonth());
            } else {
                // 孩子账单
                QueryWrapper<UserEntity> parentQuery = new QueryWrapper<>();
                parentQuery.eq("student_username", billOwnerUsername).eq("user_type", "parent");
                parent = userEntityMapper.selectOne(parentQuery);
                if (parent == null) {
                    continue;
                }
                content = String.format("您的孩子%s(%s)的餐费账单%s)尚未缴纳，请及时缴费",
                        notifyStudentName, notifyStudentUsername, bill.getBillMonth());
            }

            int sent = parentNotificationService.sendCustomNotification(
                    parent.getUsername(),
                    notifyStudentUsername,
                    notifyStudentName,
                    "meal_bill_reminder",
                    "餐费账单缴费提醒",
                    content
            );
            if (sent > 0) {
                count++;
            }
        }
        return Result.success("成功发送" + count + "条提醒");
    }

    @GetMapping("/export")
    public void exportExcel(@RequestParam(required = false) String month,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            HttpServletResponse response) throws Exception {
        QueryWrapper<BillingRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bill_type", "meal");
        if (month != null && !month.isEmpty()) {
            queryWrapper.eq("bill_month", month);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (search != null && !search.isEmpty()) {
            queryWrapper.and(w -> w.like("student_name", search).or().like("student_username", search));
        }

        List<BillingRecord> records = billingRecordService.list(queryWrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("餐费账单");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "账单月份", "就餐类型", "午餐次数", "晚餐次数", "请假天数", "总金额", "状态", "缴费时间"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (BillingRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getStudentUsername() != null ? record.getStudentUsername() : "");
            row.createCell(1).setCellValue(record.getStudentName() != null ? record.getStudentName() : "");
            row.createCell(2).setCellValue(record.getBillMonth() != null ? record.getBillMonth() : "");
            row.createCell(3).setCellValue(record.getBoardingType() != null ? record.getBoardingType() : "");
            row.createCell(4).setCellValue(record.getLunchDays() != null ? record.getLunchDays() : 0);
            row.createCell(5).setCellValue(record.getDinnerDays() != null ? record.getDinnerDays() : 0);
            row.createCell(6).setCellValue(record.getLeaveDeductDays() != null ? record.getLeaveDeductDays() : 0);
            row.createCell(7).setCellValue(record.getTotalAmount() != null ? record.getTotalAmount().doubleValue() : 0.0);
            row.createCell(8).setCellValue(record.getStatus() != null ? record.getStatus() : "");
            row.createCell(9).setCellValue(record.getPaymentTime() != null ? record.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("餐费账单.xlsx", "UTF-8"));

        OutputStream out = response.getOutputStream();
        workbook.write(out);
        workbook.close();
        out.close();
    }

    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        int count = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String studentUsername = getCellValue(row.getCell(0));
            String billMonth = getCellValue(row.getCell(2));
            String statusText = getCellValue(row.getCell(8));

            if (studentUsername.isEmpty() || billMonth.isEmpty()) {
                continue;
            }

            QueryWrapper<BillingRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("bill_type", "meal")
                    .eq("student_username", studentUsername)
                    .eq("bill_month", billMonth);
            BillingRecord record = billingRecordService.getOne(wrapper);

            if (record == null) {
                continue;
            }

            if ("已缴费".equals(statusText) || "paid".equalsIgnoreCase(statusText)) {
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

    // ========== Parent APIs ==========

    /**
     * Get parent's child bills
     */
    @GetMapping("/parent/{parentUsername}")
    public Result<List<BillingRecord>> getParentChildBills(@PathVariable String parentUsername) {
        return Result.success(billingRecordService.getParentChildBills(parentUsername));
    }

    /**
     * Get bills by student username
     */
    @GetMapping("/student/{studentUsername}")
    public Result<List<BillingRecord>> getByStudent(@PathVariable String studentUsername) {
        return Result.success(billingRecordService.getByStudentUsername(studentUsername));
    }
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }
}
