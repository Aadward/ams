package com.ams.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockInRequest {
    @NotNull
    private Long consumableId;

    @NotNull @Positive
    private Integer quantity;

    private String supplier;
    private String remark;
}
