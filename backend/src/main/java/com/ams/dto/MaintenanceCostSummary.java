package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceCostSummary {
    private BigDecimal monthlyCost;
    private BigDecimal quarterlyCost;
    private BigDecimal yearlyCost;
    private Long monthlyCount;
    private Long quarterlyCount;
    private Long yearlyCount;
}
