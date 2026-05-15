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
public class InsuranceClaimResponse {

    private Long id;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private LocalDate incidentDate;
    private BigDecimal claimAmount;
    private BigDecimal settledAmount;
    private String status;
    private String incidentDescription;
    private String settlementNotes;
    private Long filedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
