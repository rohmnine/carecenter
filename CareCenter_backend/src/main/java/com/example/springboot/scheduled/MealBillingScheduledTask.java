package com.example.springboot.scheduled;

import com.example.springboot.service.MealBillService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MealBillingScheduledTask {

    @Resource
    private MealBillService mealBillService;

    /**
     * Auto-generate monthly meal bills on the 1st of each month at 1:00 AM
     * Generates bills for the previous month
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyMealBills() {
        // Generate bills for the previous month
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String month = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        mealBillService.generateMonthlyBills(month);
    }
}
