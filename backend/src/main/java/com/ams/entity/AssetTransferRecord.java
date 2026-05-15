package com.ams.entity;

import com.ams.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_transfer_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetTransferRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "from_employee_id", nullable = false)
    private Long fromEmployeeId;

    @Column(name = "to_employee_id", nullable = false)
    private Long toEmployeeId;

    @Column(name = "from_department_id", nullable = false)
    private Long fromDepartmentId;

    @Column(name = "to_department_id", nullable = false)
    private Long toDepartmentId;

    @Column(name = "approval_id")
    private Long approvalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @Column(length = 500)
    private String reason;

    @Column(name = "manager_comment", length = 500)
    private String managerComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransferStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
