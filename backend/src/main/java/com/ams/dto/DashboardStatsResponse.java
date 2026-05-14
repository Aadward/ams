package com.ams.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalAssets;
    private long inStock;
    private long inUse;
    private long inMaintenance;
    private long retired;
    private Map<String, Long> categoryBreakdown;
    private List<AssetLogResponse> recentActivity;
}
