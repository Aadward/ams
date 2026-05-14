package com.ams.entity;

import com.ams.enums.InventoryResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_code", nullable = false)
    private String assetCode;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryResult result;

    @Column(name = "checked_by", nullable = false)
    private Long checkedBy;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
