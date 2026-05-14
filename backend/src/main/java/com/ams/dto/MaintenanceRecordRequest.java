package com.ams.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecordRequest {

    private Long requestorId; // nullable, 用于通知

    @NotNull(message = "维修类型不能为空")
    private String type;

    @Size(max = 1000)
    private String description;

    @DecimalMin(value = "0")
    private BigDecimal cost;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 255)
    private String vendor;
}
