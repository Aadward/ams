package com.ams.controller;

import com.ams.dto.AssetLogResponse;
import com.ams.dto.DashboardStatsResponse;
import com.ams.entity.AssetLog;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final AssetRepository assetRepository;
    private final AssetLogRepository assetLogRepository;

    @GetMapping("/api/dashboard/stats")
    public ResponseEntity<?> getStats() {
        try {
            long totalAssets = assetRepository.count();
            long inStock = assetRepository.countByStatus(AssetStatus.IN_STOCK);
            long inUse = assetRepository.countByStatus(AssetStatus.IN_USE);
            long inMaintenance = assetRepository.countByStatus(AssetStatus.MAINTENANCE);
            long retired = assetRepository.countByStatus(AssetStatus.RETIRED);

            Map<String, Long> categoryStats = new HashMap<>();
            for (AssetCategory category : AssetCategory.values()) {
                categoryStats.put(category.name(), assetRepository.countByCategory(category));
            }

            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .totalAssets(totalAssets)
                    .inStock(inStock)
                    .inUse(inUse)
                    .inMaintenance(inMaintenance)
                    .retired(retired)
                    .categoryStats(categoryStats)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/dashboard/recent-logs")
    public ResponseEntity<?> getRecentLogs() {
        try {
            List<AssetLog> logs = assetLogRepository.findTop20ByOrderByCreatedAtDesc();
            List<AssetLogResponse> response = logs.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private AssetLogResponse toResponse(AssetLog log) {
        return AssetLogResponse.builder()
                .id(log.getId())
                .assetId(log.getAsset() != null ? log.getAsset().getId() : null)
                .assetCode(log.getAsset() != null ? log.getAsset().getAssetCode() : null)
                .assetName(log.getAsset() != null ? log.getAsset().getName() : null)
                .action(log.getAction().name())
                .operator(log.getOperator())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}