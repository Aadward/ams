package com.ams.dto;

import com.ams.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRecordResponse {

    private Long id;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private Long fromEmployeeId;
    private String fromEmployeeName;
    private Long toEmployeeId;
    private String toEmployeeName;
    private Long fromDepartmentId;
    private String fromDepartmentName;
    private Long toDepartmentId;
    private String toDepartmentName;
    private Long approvalId;
    private TransferStatus status;
    private String reason;
    private String managerComment;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
