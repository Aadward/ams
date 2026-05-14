package com.ams.entity;

import com.ams.enums.ConsumableRecordType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumable_record")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ConsumableRecordType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(length = 200)
    private String supplier;

    @Column(length = 500)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
