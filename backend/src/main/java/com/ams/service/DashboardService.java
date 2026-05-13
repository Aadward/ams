package com.ams.service;

import com.ams.dto.AssetLogResponse;
import com.ams.dto.DashboardStatsResponse;
import com.ams.entity.AssetLog;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AssetRepository assetRepository;
    private final AssetLogRepository assetLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalAssets = assetRepository.count();
        long inStock = countByStatusAndDeletedFalse(AssetStatus.IN_STOCK);
        long inUse = countByStatusAndDeletedFalse(AssetStatus.IN_USE);
        long inMaintenance = countByStatusAndDeletedFalse(AssetStatus.MAINTENANCE);
        long retired = countByStatusAndDeletedFalse(AssetStatus.RETIRED);

        Map<String, Long> categoryStats = new HashMap<>();
        for (AssetCategory category : AssetCategory.values()) {
            categoryStats.put(category.name(), countByCategoryAndDeletedFalse(category));
        }

        return DashboardStatsResponse.builder()
                .totalAssets(totalAssets)
                .inStock(inStock)
                .inUse(inUse)
                .inMaintenance(inMaintenance)
                .retired(retired)
                .categoryStats(categoryStats)
                .build();
    }

    private long countByStatusAndDeletedFalse(AssetStatus status) {
        return assetRepository.findByDeletedFalseAndStatus(status, Pageable.unpaged()).getTotalElements();
    }

    private long countByCategoryAndDeletedFalse(AssetCategory category) {
        return assetRepository.findByDeletedFalseAndCategory(category, Pageable.unpaged()).getTotalElements();
    }

    @Transactional(readOnly = true)
    public List<AssetLogResponse> getRecentLogs() {
        List<AssetLog> logs = assetLogRepository.findTop20ByOrderByCreatedAtDesc();
        return logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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