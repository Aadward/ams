package com.ams.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumptionReportResponse {
    private List<ConsumptionItem> items;
    private Integer totalQuantity;
    private Map<String, Integer> monthlyTotals;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConsumptionItem {
        private Long consumableId;
        private String consumableName;
        private String category;
        private Integer totalQuantity;
    }
}
