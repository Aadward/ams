package com.ams.entity;

import com.ams.enums.InventoryTaskStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryTaskStatus status;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
