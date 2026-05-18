package com.ams.dto;

import com.ams.enums.ProcurementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementRequestDTO {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long departmentId;
    private String departmentName;
    private String assetName;
    private String category;
    private BigDecimal budget;
    private ProcurementStatus status;
    private String reason;
    private String managerComment;
    private Long approverId;
    private String approverName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
