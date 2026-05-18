package com.ams.controller;

import com.ams.dto.ProcurementRequestDTO;
import com.ams.entity.ProcurementRequest;
import com.ams.enums.ProcurementStatus;
import com.ams.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/procurements")
@RequiredArgsConstructor
public class ProcurementController {

    private final ProcurementService procurementService;

    @GetMapping
    public ResponseEntity<?> getAllProcurementRequests() {
        try {
            List<ProcurementRequestDTO> result = procurementService.getAllProcurementRequests();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProcurementRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(procurementService.getProcurementRequest(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyProcurementRequests(@RequestParam Long requesterId) {
        try {
            List<ProcurementRequestDTO> result = procurementService.getProcurementRequestsByRequester(requesterId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getProcurementRequestsByDepartment(@PathVariable Long departmentId) {
        try {
            List<ProcurementRequestDTO> result = procurementService.getProcurementRequestsByDepartment(departmentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getPendingProcurementRequests() {
        try {
            List<ProcurementRequestDTO> result = procurementService.getPendingProcurementRequests();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getProcurementRequestsByStatus(@PathVariable String status) {
        try {
            ProcurementStatus procurementStatus = ProcurementStatus.valueOf(status.toUpperCase());
            List<ProcurementRequestDTO> result = procurementService.getProcurementRequestsByStatus(procurementStatus);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForProcurement(@RequestBody Map<String, Object> request) {
        try {
            Long requesterId = Long.valueOf(request.get("requesterId").toString());
            Long departmentId = Long.valueOf(request.get("departmentId").toString());
            String assetName = request.get("assetName").toString();
            String category = request.get("category") != null ? request.get("category").toString() : null;
            BigDecimal budget = request.get("budget") != null ?
                    new BigDecimal(request.get("budget").toString()) : null;
            String reason = request.get("reason") != null ? request.get("reason").toString() : null;

            ProcurementRequest result = procurementService.createProcurementRequest(
                    requesterId, departmentId, assetName, category, budget, reason);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProcurementRequest(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> request) {
        try {
            String assetName = request.get("assetName") != null ? request.get("assetName").toString() : null;
            String category = request.get("category") != null ? request.get("category").toString() : null;
            BigDecimal budget = request.get("budget") != null ?
                    new BigDecimal(request.get("budget").toString()) : null;
            String reason = request.get("reason") != null ? request.get("reason").toString() : null;

            ProcurementRequest result = procurementService.updateProcurementRequest(
                    id, assetName, category, budget, reason);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> approveProcurement(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Long approverId = Long.valueOf(request.get("approverId").toString());
            String comment = request.get("comment");

            ProcurementRequest result = procurementService.approveProcurement(id, approverId, comment);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> rejectProcurement(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Long approverId = Long.valueOf(request.get("approverId").toString());
            String comment = request.get("comment");

            ProcurementRequest result = procurementService.rejectProcurement(id, approverId, comment);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
