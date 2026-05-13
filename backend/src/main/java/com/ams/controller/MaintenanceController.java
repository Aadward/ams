package com.ams.controller;

import com.ams.dto.MaintenanceRecordRequest;
import com.ams.dto.MaintenanceRecordResponse;
import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.enums.MaintenanceType;
import com.ams.repository.AssetRepository;
import com.ams.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final AssetRepository assetRepository;

    @GetMapping("/api/assets/{assetId}/maintenance-records")
    public ResponseEntity<?> listMaintenanceRecords(@PathVariable Long assetId) {
        try {
            List<MaintenanceRecord> records = maintenanceRecordRepository.findByAssetIdOrderByStartDateDesc(assetId);
            List<MaintenanceRecordResponse> response = records.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/assets/{assetId}/maintenance-records")
    public ResponseEntity<?> createMaintenanceRecord(
            @PathVariable Long assetId,
            @RequestBody MaintenanceRecordRequest request) {
        try {
            Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                    .orElseThrow(() -> new RuntimeException("资产不存在"));

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

            return ResponseEntity.ok(toResponse(record));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/maintenance-records/{id}")
    public ResponseEntity<?> updateMaintenanceRecord(
            @PathVariable Long id,
            @RequestBody MaintenanceRecordRequest request) {
        try {
            MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("维修记录不存在"));

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

            return ResponseEntity.ok(toResponse(record));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
}