package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetUpdateRequest {
    private String name;
    private String category;
    private String spec;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private Integer depreciationYears;
    private LocalDate warrantyEnd;
    private String supplier;
    private String location;
}
