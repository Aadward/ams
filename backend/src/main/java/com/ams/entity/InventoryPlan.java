package com.ams.entity;

import com.ams.enums.InventoryPlanStatus;
import com.ams.enums.InventoryScopeType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private InventoryScopeType scopeType;

    @Column(name = "department_ids", columnDefinition = "TEXT")
    private String departmentIds;

    @Column(name = "category_ids", columnDefinition = "TEXT")
    private String categoryIds;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryPlanStatus status;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
