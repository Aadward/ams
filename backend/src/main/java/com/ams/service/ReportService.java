package com.ams.service;

import com.ams.dto.AssetStatisticsResponse;
import com.ams.dto.MaintenanceCostSummary;
import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.repository.AssetRepository;
import com.ams.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AssetRepository assetRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public List<AssetStatisticsResponse> getAssetCountByCategory() {
        log.info("Generating asset count by category report");
        Map<AssetCategory, Long> counts = assetRepository.findByDeletedFalse().stream()
                .collect(Collectors.groupingBy(Asset::getCategory, Collectors.counting()));
        Map<AssetCategory, BigDecimal> values = assetRepository.findByDeletedFalse().stream()
                .collect(Collectors.groupingBy(Asset::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Asset::getPurchasePrice, BigDecimal::add)));

        return counts.entrySet().stream()
                .map(e -> AssetStatisticsResponse.builder()
                        .name(e.getKey().name())
                        .count(e.getValue())
                        .totalValue(values.getOrDefault(e.getKey(), BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
    }

    public List<AssetStatisticsResponse> getAssetCountByStatus() {
        log.info("Generating asset count by status report");
        List<Asset> assets = assetRepository.findByDeletedFalse();
        Map<AssetStatus, Long> counts = assets.stream()
                .collect(Collectors.groupingBy(Asset::getStatus, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> AssetStatisticsResponse.builder()
                        .name(e.getKey().name())
                        .count(e.getValue())
                        .totalValue(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    public List<AssetStatisticsResponse> getAssetCountByDepartment() {
        log.info("Generating asset count by department report");
        List<Asset> assets = assetRepository.findByDeletedFalse();
        Map<String, Long> counts = assets.stream()
                .collect(Collectors.groupingBy(a -> a.getAssignee() != null && a.getAssignee().getDeptName() != null
                        ? a.getAssignee().getDeptName() : "未分配", Collectors.counting()));
        Map<String, BigDecimal> values = assets.stream()
                .collect(Collectors.groupingBy(a -> a.getAssignee() != null && a.getAssignee().getDeptName() != null
                        ? a.getAssignee().getDeptName() : "未分配",
                        Collectors.reducing(BigDecimal.ZERO, Asset::getPurchasePrice, BigDecimal::add)));
        return counts.entrySet().stream()
                .map(e -> AssetStatisticsResponse.builder()
                        .name(e.getKey())
                        .count(e.getValue())
                        .totalValue(values.getOrDefault(e.getKey(), BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
    }

    public MaintenanceCostSummary getMaintenanceCostSummary() {
        log.info("Generating maintenance cost summary");
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate quarterStart = now.withDayOfYear((now.getMonthValue() - 1) / 3 * 3 + 1);
        LocalDate yearStart = now.withDayOfYear(1);

        List<MaintenanceRecord> allRecords = maintenanceRecordRepository.findAll();

        BigDecimal monthly = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(monthStart))
                .map(MaintenanceRecord::getCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal quarterly = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(quarterStart))
                .map(MaintenanceRecord::getCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal yearly = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(yearStart))
                .map(MaintenanceRecord::getCost)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long monthlyCount = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(monthStart))
                .count();
        long quarterlyCount = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(quarterStart))
                .count();
        long yearlyCount = allRecords.stream()
                .filter(r -> r.getEndDate() != null && !r.getEndDate().isBefore(yearStart))
                .count();

        return MaintenanceCostSummary.builder()
                .monthlyCost(monthly)
                .quarterlyCost(quarterly)
                .yearlyCost(yearly)
                .monthlyCount(monthlyCount)
                .quarterlyCount(quarterlyCount)
                .yearlyCount(yearlyCount)
                .build();
    }
}
