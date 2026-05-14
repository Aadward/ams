package com.ams.controller;

import com.ams.dto.ApprovalRequestDTO;
import com.ams.enums.ApprovalType;
import com.ams.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> request) {
        try {
            Long requesterId = Long.valueOf(request.get("requesterId").toString());
            Long assetId = Long.valueOf(request.get("assetId").toString());
            Long departmentId = Long.valueOf(request.get("departmentId").toString());
            ApprovalType type = ApprovalType.valueOf(request.get("type").toString());
            String reason = request.get("reason") != null ? request.get("reason").toString() : null;

            var result = approvalService.createRequest(requesterId, assetId, departmentId, type, reason);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyRequests(@RequestParam Long requesterId) {
        try {
            List<ApprovalRequestDTO> result = approvalService.getMyRequests(requesterId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getPendingRequests() {
        try {
            List<ApprovalRequestDTO> result = approvalService.getPendingRequests();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            var result = approvalService.approve(id, comment);
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
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            var result = approvalService.reject(id, comment);
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
