package com.ams.controller;

import com.ams.dto.AssetStatisticsResponse;
import com.ams.dto.MaintenanceCostSummary;
import com.ams.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/assets/by-category")
    public ResponseEntity<?> getAssetCountByCategory() {
        try {
            List<AssetStatisticsResponse> result = reportService.getAssetCountByCategory();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/assets/by-status")
    public ResponseEntity<?> getAssetCountByStatus() {
        try {
            List<AssetStatisticsResponse> result = reportService.getAssetCountByStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/assets/by-department")
    public ResponseEntity<?> getAssetCountByDepartment() {
        try {
            List<AssetStatisticsResponse> result = reportService.getAssetCountByDepartment();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/maintenance/cost-summary")
    public ResponseEntity<?> getMaintenanceCostSummary() {
        try {
            MaintenanceCostSummary result = reportService.getMaintenanceCostSummary();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
