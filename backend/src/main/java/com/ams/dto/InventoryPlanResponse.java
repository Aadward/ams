package com.ams.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPlanResponse {
    private Long id;
    private String name;
    private String scopeType;
    private String departmentIds;
    private String categoryIds;
    private LocalDate planDate;
    private String status;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private int totalTasks;
    private int checkedTasks;
}
