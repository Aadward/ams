package com.ams.controller;

import com.ams.dto.MaintenanceRecordRequest;
import com.ams.dto.MaintenanceRecordResponse;
import com.ams.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping("/api/assets/{assetId}/maintenance-records")
    public ResponseEntity<?> listMaintenanceRecords(@PathVariable Long assetId) {
        try {
            List<MaintenanceRecordResponse> records = maintenanceService.getMaintenanceRecords(assetId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/assets/{assetId}/maintenance-records")
    public ResponseEntity<?> createMaintenanceRecord(
            @PathVariable Long assetId,
            @RequestBody MaintenanceRecordRequest request) {
        try {
            MaintenanceRecordResponse record = maintenanceService.createMaintenanceRecord(assetId, request);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/maintenance-records/{id}")
    public ResponseEntity<?> updateMaintenanceRecord(
            @PathVariable Long id,
            @RequestBody MaintenanceRecordRequest request) {
        try {
            MaintenanceRecordResponse record = maintenanceService.updateMaintenanceRecord(id, request);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
