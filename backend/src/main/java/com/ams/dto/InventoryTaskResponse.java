package com.ams.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTaskResponse {
    private Long id;
    private Long planId;
    private String planName;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String category;
    private Long assigneeId;
    private String assigneeName;
    private String status;
    private LocalDateTime checkedAt;
    private LocalDateTime createdAt;
}
