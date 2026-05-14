package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecordResponse {
    private Long id;
    private Long assetId;
    private Long requestorId;
    private String type;
    private String description;
    private BigDecimal cost;
    private LocalDate startDate;
    private LocalDate endDate;
    private String vendor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
