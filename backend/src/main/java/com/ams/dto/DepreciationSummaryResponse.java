package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DepreciationSummaryResponse {
    private String groupKey;  // category name or department name
    private String groupLabel; // display label
    private Long assetCount;
    private BigDecimal totalOriginalValue;
    private BigDecimal totalAccumulatedDepreciation;
    private BigDecimal totalNetValue;
    private BigDecimal depreciationRate; // accumulated / original (0-1)
}
