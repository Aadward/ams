package com.ams.controller;

import com.ams.dto.ScanRequest;
import com.ams.dto.ScanResponse;
import com.ams.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @GetMapping("/{assetCode}")
    public ResponseEntity<?> scanAsset(
            @PathVariable String assetCode,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            ScanResponse response = scanService.scanAsset(assetCode, employeeId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{assetCode}/assign")
    public ResponseEntity<?> scanAssign(
            @PathVariable String assetCode,
            @RequestBody ScanRequest request,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            scanService.scanAssign(assetCode, request, employeeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{assetCode}/return")
    public ResponseEntity<?> scanReturn(
            @PathVariable String assetCode,
            @RequestBody ScanRequest request,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            scanService.scanReturn(assetCode, request, employeeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{assetCode}/borrow-return")
    public ResponseEntity<?> scanBorrowReturn(
            @PathVariable String assetCode,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            scanService.scanBorrowReturn(assetCode, employeeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("No active borrow record")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
