package com.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimRequest {

    @NotBlank(message = "理赔号不能为空")
    private String claimNumber;

    @NotNull(message = "保单ID不能为空")
    private Long policyId;

    private LocalDate incidentDate;

    private BigDecimal claimAmount;

    private String incidentDescription;

    private Long filedBy;
}
