package com.ams.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePolicyResponse {

    private Long id;
    private String policyNumber;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String type;
    private String typeDescription;
    private String insuranceCompany;
    private BigDecimal premium;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String policyDocument;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
