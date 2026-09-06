package com.example.springboot.scheduled;

import com.example.springboot.service.PaymentRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Component
public class PaymentScheduledTask {
    
    @Resource
    private PaymentRecordService paymentRecordService;
    
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyPaymentRecords() {
        BigDecimal defaultAmount = new BigDecimal("500.00");
        paymentRecordService.generateMonthlyPaymentRecords(defaultAmount);
    }
}
