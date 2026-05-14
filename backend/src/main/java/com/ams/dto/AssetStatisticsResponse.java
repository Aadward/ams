package com.ams.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetStatisticsResponse {
    private String name;
    private Long count;
    private BigDecimal totalValue;
}
