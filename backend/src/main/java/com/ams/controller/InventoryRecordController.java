package com.ams.controller;

import com.ams.dto.InventoryRecordResponse;
import com.ams.dto.InventoryReportResponse;
import com.ams.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-records")
@RequiredArgsConstructor
public class InventoryRecordController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> listRecords(
            @RequestParam Long planId,
            @RequestParam(required = false) String result) {
        try {
            List<InventoryRecordResponse> records = inventoryService.listRecords(planId, result);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/{planId}")
    public ResponseEntity<?> getReport(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(inventoryService.getReport(planId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/{planId}/export")
    public ResponseEntity<?> exportReport(@PathVariable Long planId) {
        try {
            byte[] excelData = inventoryService.exportToExcel(planId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "inventory_report_" + planId + ".xlsx");
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
