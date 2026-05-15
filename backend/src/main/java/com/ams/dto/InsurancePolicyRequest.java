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
public class InsurancePolicyRequest {

    @NotBlank(message = "保单号不能为空")
    private String policyNumber;

    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    @NotBlank(message = "保险类型不能为空")
    private String type;

    private String insuranceCompany;

    private BigDecimal premium;

    private BigDecimal coverageAmount;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String policyDocument;

    private String remarks;
}
