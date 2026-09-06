package com.example.springboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请假天数汇总
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveSummary {

    private String username;

    private Integer totalDays;
}
