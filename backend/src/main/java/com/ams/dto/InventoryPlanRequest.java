package com.ams.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPlanRequest {
    private String name;
    private String scopeType;        // DEPARTMENT or CATEGORY
    private List<Long> departmentIds; // JSON array of department IDs
    private List<Long> categoryIds;   // JSON array of category IDs
    private LocalDate planDate;
    private List<Long> assigneeIds;   // 盘点负责人列表
    private String remark;
}
