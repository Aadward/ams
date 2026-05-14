package com.ams.controller;

import com.ams.dto.InventoryTaskResponse;
import com.ams.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-tasks")
@RequiredArgsConstructor
public class InventoryTaskController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> listTasks(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status) {
        try {
            List<InventoryTaskResponse> tasks = inventoryService.listTasks(planId, assigneeId, status);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTasks(
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            List<InventoryTaskResponse> tasks = inventoryService.getMyTasks(employeeId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/check")
    public ResponseEntity<?> checkAsset(
            @PathVariable Long id,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String remark = body != null ? body.get("remark") : null;
            return ResponseEntity.ok(inventoryService.checkAsset(id, employeeId, remark));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/uncheck")
    public ResponseEntity<?> uncheckAsset(
            @PathVariable Long id,
            @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            return ResponseEntity.ok(inventoryService.uncheckAsset(id, employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
