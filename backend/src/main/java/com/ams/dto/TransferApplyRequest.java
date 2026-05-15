package com.ams.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferApplyRequest {
    private Long assetId;
    private Long toEmployeeId;
    private Long toDepartmentId;
    private String reason;
}
