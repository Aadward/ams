package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DepreciationResponse {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String category;
    private LocalDate purchaseDate;
    private BigDecimal originalValue;    // purchasePrice
    private Integer depreciationYears;   // service life in years
    private BigDecimal annualDepreciation; // originalValue / depreciationYears
    private BigDecimal accumulatedDepreciation; // computed: (yearsUsed) * annualDepreciation
    private BigDecimal currentNetValue;  // originalValue - accumulatedDepreciation
    private Integer yearsUsed;           // floor of (today - purchaseDate) in years
    private Boolean fullyDepreciated;   // accumulated >= originalValue
}
