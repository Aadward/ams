package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponse {
    private Long id;
    private String assetCode;
    private String name;
    private String category;
    private String status;
    private String spec;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private Integer depreciationYears;
    private LocalDate warrantyEnd;
    private String supplier;
    private String location;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
