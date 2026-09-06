package com.example.springboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 就餐预约次数汇总
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MealReservationSummary {

    private String username;

    private Integer totalCount;
}
