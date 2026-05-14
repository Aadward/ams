package com.ams.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRecordResponse {
    private Long id;
    private Long taskId;
    private Long planId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long departmentId;
    private String departmentName;
    private String result;
    private Long checkedBy;
    private String checkedByName;
    private LocalDateTime checkedAt;
    private String remark;
}
