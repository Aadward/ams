package com.ams.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReportResponse {
    private Long planId;
    private String planName;
    private int totalAssets;
    private int checkedAssets;
    private int normalCount;
    private int surplusCount;
    private int missingCount;
    private int pendingCount;
    private double completionRate;
    private List<InventoryRecordResponse> surplusRecords;
    private List<InventoryRecordResponse> missingRecords;
    private List<InventoryRecordResponse> normalRecords;
}
