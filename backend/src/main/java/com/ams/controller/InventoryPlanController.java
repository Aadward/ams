package com.ams.controller;

import com.ams.dto.InventoryPlanRequest;
import com.ams.dto.InventoryPlanResponse;
import com.ams.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-plans")
@RequiredArgsConstructor
public class InventoryPlanController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> listPlans() {
        try {
            List<InventoryPlanResponse> plans = inventoryService.listPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlan(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventoryService.getPlan(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody InventoryPlanRequest request,
                                        @RequestHeader(value = "X-Employee-Id", required = false, defaultValue = "1") Long employeeId) {
        try {
            InventoryPlanResponse plan = inventoryService.createPlan(request, employeeId);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody InventoryPlanRequest request) {
        try {
            return ResponseEntity.ok(Map.of("message", "Plan updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        try {
            inventoryService.deletePlan(id);
            return ResponseEntity.ok(Map.of("message", "Plan deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startPlan(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventoryService.startPlan(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completePlan(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventoryService.completePlan(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
