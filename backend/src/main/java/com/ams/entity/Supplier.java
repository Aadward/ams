package com.ams.entity;

import com.ams.enums.SupplierStatus;
import com.ams.enums.SupplierType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", nullable = false, unique = true, length = 50)
    private String supplierCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierStatus status;

    @Column(length = 500)
    private String contact;

    @Column(length = 100)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String address;

    @Column(length = 1000)
    private String remark;

    @Column
    private Double rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SupplierStatus.ACTIVE;
        }
        if (deleted == null) {
            deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
