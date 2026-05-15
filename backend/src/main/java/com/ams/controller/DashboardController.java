package com.ams.controller;

import com.ams.dto.AssetLogResponse;
import com.ams.dto.DashboardStatsResponse;
import com.ams.dto.ExpiringWarrantyResponse;
import com.ams.entity.Asset;
import com.ams.entity.AssetLog;
import com.ams.entity.InsurancePolicy;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.AssetRepository;
import com.ams.service.InsuranceNotificationService;
import com.ams.service.WarrantyNotificationService;
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
    private final WarrantyNotificationService warrantyNotificationService;
    private final InsuranceNotificationService insuranceNotificationService;

    @GetMapping("/api/dashboard/stats")
    public ResponseEntity<?> getStats() {
        try {
            long totalAssets = assetRepository.count();
            long inStock = assetRepository.countByStatus(AssetStatus.IN_STOCK);
            long inUse = assetRepository.countByStatus(AssetStatus.IN_USE);
            long inMaintenance = assetRepository.countByStatus(AssetStatus.MAINTENANCE);
            long retired = assetRepository.countByStatus(AssetStatus.RETIRED);

            Map<String, Long> categoryBreakdown = new HashMap<>();
            for (AssetCategory category : AssetCategory.values()) {
                categoryBreakdown.put(category.name(), assetRepository.countByCategory(category));
            }

            // Fetch recent activity from logs
            List<AssetLog> logs = assetLogRepository.findTop20ByOrderByCreatedAtDesc();
            List<AssetLogResponse> recentActivity = logs.stream()
                    .limit(10)
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .totalAssets(totalAssets)
                    .inStock(inStock)
                    .inUse(inUse)
                    .inMaintenance(inMaintenance)
                    .retired(retired)
                    .categoryBreakdown(categoryBreakdown)
                    .recentActivity(recentActivity)
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

    @GetMapping("/api/dashboard/expiring-warranty")
    public ResponseEntity<?> getExpiringWarranty(@RequestParam(defaultValue = "30") int days) {
        try {
            List<Asset> assets = warrantyNotificationService.getExpiringWarrantyAssets(days);
            List<ExpiringWarrantyResponse> response = assets.stream()
                    .map(this::toExpiringWarrantyResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/dashboard/expiring-insurance")
    public ResponseEntity<?> getExpiringInsurance(@RequestParam(defaultValue = "30") int days) {
        try {
            List<InsurancePolicy> policies = insuranceNotificationService.getExpiringInsurancePolicies(days);
            List<Map<String, Object>> response = policies.stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("assetId", p.getAsset().getId());
                m.put("assetName", p.getAsset().getName());
                m.put("assetCode", p.getAsset().getAssetCode());
                m.put("policyNumber", p.getPolicyNumber());
                m.put("insuranceCompany", p.getInsuranceCompany());
                m.put("type", p.getType().name());
                m.put("endDate", p.getEndDate());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ExpiringWarrantyResponse toExpiringWarrantyResponse(Asset asset) {
        return ExpiringWarrantyResponse.builder()
                .id(asset.getId())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .category(asset.getCategory().name())
                .warrantyEnd(asset.getWarrantyEnd())
                .build();
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