package com.ams.controller;

import com.ams.dto.InsuranceClaimRequest;
import com.ams.dto.InsuranceClaimResponse;
import com.ams.dto.InsurancePolicyRequest;
import com.ams.dto.InsurancePolicyResponse;
import com.ams.service.InsuranceClaimService;
import com.ams.service.InsuranceNotificationService;
import com.ams.service.InsurancePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InsuranceController {

    private final InsurancePolicyService insurancePolicyService;
    private final InsuranceClaimService insuranceClaimService;
    private final InsuranceNotificationService insuranceNotificationService;

    // ==================== Insurance Policy Endpoints ====================

    @GetMapping("/api/insurance-policies")
    public ResponseEntity<?> listPolicies(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<InsurancePolicyResponse> result = insurancePolicyService.listPolicies(
                    assetId, status, type, dateFrom, dateTo, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/insurance-policies/{id}")
    public ResponseEntity<?> getPolicy(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(insurancePolicyService.getPolicy(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/assets/{assetId}/insurance-policies")
    public ResponseEntity<?> getPoliciesByAsset(@PathVariable Long assetId) {
        try {
            List<InsurancePolicyResponse> policies = insurancePolicyService.getPoliciesByAsset(assetId);
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/insurance-policies")
    public ResponseEntity<?> createPolicy(@Valid @RequestBody InsurancePolicyRequest request) {
        try {
            return ResponseEntity.ok(insurancePolicyService.createPolicy(request));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/insurance-policies/{id}")
    public ResponseEntity<?> updatePolicy(
            @PathVariable Long id,
            @RequestBody InsurancePolicyRequest request) {
        try {
            return ResponseEntity.ok(insurancePolicyService.updatePolicy(id, request));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/insurance-policies/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable Long id) {
        try {
            insurancePolicyService.deletePolicy(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/insurance-policies/{id}/cancel")
    public ResponseEntity<?> cancelPolicy(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(insurancePolicyService.cancelPolicy(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Insurance Claim Endpoints ====================

    @GetMapping("/api/insurance-claims")
    public ResponseEntity<?> listClaims(
            @RequestParam(required = false) Long policyId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<InsuranceClaimResponse> result = insuranceClaimService.listClaims(policyId, status, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/insurance-claims/{id}")
    public ResponseEntity<?> getClaim(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(insuranceClaimService.getClaim(id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/insurance-policies/{policyId}/claims")
    public ResponseEntity<?> getClaimsByPolicy(@PathVariable Long policyId) {
        try {
            return ResponseEntity.ok(insuranceClaimService.getClaimsByPolicy(policyId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/insurance-claims")
    public ResponseEntity<?> createClaim(@Valid @RequestBody InsuranceClaimRequest request) {
        try {
            return ResponseEntity.ok(insuranceClaimService.createClaim(request));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/insurance-claims/{id}")
    public ResponseEntity<?> updateClaim(
            @PathVariable Long id,
            @RequestBody InsuranceClaimRequest request) {
        try {
            return ResponseEntity.ok(insuranceClaimService.updateClaim(id, request));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/insurance-claims/{id}/settle")
    public ResponseEntity<?> settleClaim(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            BigDecimal settledAmount = body.get("settledAmount") != null
                    ? new BigDecimal(body.get("settledAmount").toString()) : null;
            String settlementNotes = (String) body.get("settlementNotes");
            return ResponseEntity.ok(insuranceClaimService.settleClaim(id, settledAmount, settlementNotes));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/insurance-claims/{id}/reject")
    public ResponseEntity<?> rejectClaim(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(insuranceClaimService.rejectClaim(id, body.get("settlementNotes")));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/insurance-claims/{id}")
    public ResponseEntity<?> deleteClaim(@PathVariable Long id) {
        try {
            insuranceClaimService.deleteClaim(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Notification Endpoints ====================

    @PostMapping("/api/insurance/notifications/check")
    public ResponseEntity<?> checkExpiringInsurances(@RequestParam(defaultValue = "30") int days) {
        try {
            int count = insuranceNotificationService.sendExpiringInsuranceNotifications(days);
            return ResponseEntity.ok(Map.of("notified", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
