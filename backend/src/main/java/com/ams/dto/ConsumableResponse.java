package com.ams.dto;

import com.ams.enums.ConsumableCategory;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableResponse {
    private Long id;
    private String name;
    private ConsumableCategory category;
    private String categoryLabel;
    private String spec;
    private String unit;
    private Integer threshold;
    private Integer currentStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean lowStock;
}
