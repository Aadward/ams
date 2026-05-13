package com.ams.service;

import com.ams.dto.MaintenanceRecordRequest;
import com.ams.dto.MaintenanceRecordResponse;
import com.ams.entity.Asset;
import com.ams.entity.AssetLog;
import com.ams.entity.MaintenanceRecord;
import com.ams.enums.AssetAction;
import com.ams.enums.AssetStatus;
import com.ams.enums.MaintenanceType;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.AssetRepository;
import com.ams.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final AssetRepository assetRepository;
    private final AssetLogRepository assetLogRepository;

    private static final String OPERATOR = "system";

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecords(Long assetId) {
        return maintenanceRecordRepository.findByAssetIdOrderByStartDateDesc(assetId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceRecordResponse createMaintenanceRecord(Long assetId, MaintenanceRecordRequest request) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        boolean wasNotInMaintenance = asset.getStatus() != AssetStatus.MAINTENANCE;

        if (wasNotInMaintenance) {
            asset.setStatus(AssetStatus.MAINTENANCE);
            assetRepository.save(asset);
        }

        MaintenanceRecord record = MaintenanceRecord.builder()
                .asset(asset)
                .type(MaintenanceType.valueOf(request.getType()))
                .description(request.getDescription())
                .cost(request.getCost())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .vendor(request.getVendor())
                .build();
        record = maintenanceRecordRepository.save(record);

        saveLog(asset, AssetAction.MAINTENANCE, "维修类型: " + request.getType() + ", 描述: " + request.getDescription());

        return toResponse(record);
    }

    @Transactional
    public MaintenanceRecordResponse updateMaintenanceRecord(Long id, MaintenanceRecordRequest request) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修记录不存在"));

        boolean hadNoEndDate = record.getEndDate() == null;
        boolean nowHasEndDate = request.getEndDate() != null;

        if (request.getType() != null) {
            record.setType(MaintenanceType.valueOf(request.getType()));
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }
        if (request.getCost() != null) {
            record.setCost(request.getCost());
        }
        if (request.getStartDate() != null) {
            record.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            record.setEndDate(request.getEndDate());
        }
        if (request.getVendor() != null) {
            record.setVendor(request.getVendor());
        }
        record = maintenanceRecordRepository.save(record);

        if (hadNoEndDate && nowHasEndDate) {
            Asset asset = record.getAsset();
            asset.setStatus(AssetStatus.IN_STOCK);
            assetRepository.save(asset);

            saveLog(asset, AssetAction.UPDATE, "维修完成，资产恢复可用");
        }

        return toResponse(record);
    }

    private MaintenanceRecordResponse toResponse(MaintenanceRecord record) {
        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .assetId(record.getAsset().getId())
                .type(record.getType().name())
                .description(record.getDescription())
                .cost(record.getCost())
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
                .vendor(record.getVendor())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private void saveLog(Asset asset, AssetAction action, String detail) {
        AssetLog log = AssetLog.builder()
                .asset(asset)
                .action(action)
                .operator(OPERATOR)
                .detail(detail)
                .build();
        assetLogRepository.save(log);
    }
}