package com.ams.entity;

import com.ams.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(name = "claim_amount", precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "settled_amount", precision = 12, scale = 2)
    private BigDecimal settledAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Column(name = "incident_description", length = 1000)
    private String incidentDescription;

    @Column(name = "settlement_notes", length = 1000)
    private String settlementNotes;

    @Column(name = "filed_by")
    private Long filedBy;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ClaimStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
