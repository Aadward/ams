package com.ams.controller;

import com.ams.dto.DepreciationResponse;
import com.ams.dto.DepreciationSummaryResponse;
import com.ams.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depreciation")
@RequiredArgsConstructor
public class DepreciationController {
    private final AssetService assetService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssetDepreciation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(assetService.getAssetDepreciation(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ledger")
    public ResponseEntity<?> getAllDepreciations() {
        try {
            return ResponseEntity.ok(assetService.getAllAssetDepreciations());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/summary/by-category")
    public ResponseEntity<?> getSummaryByCategory() {
        try {
            return ResponseEntity.ok(assetService.getSummaryByCategory());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/summary/by-department")
    public ResponseEntity<?> getSummaryByDepartment() {
        try {
            return ResponseEntity.ok(assetService.getSummaryByDepartment());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
