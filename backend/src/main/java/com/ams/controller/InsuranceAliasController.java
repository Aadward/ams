package com.ams.controller;

import com.ams.dto.InsuranceClaimRequest;
import com.ams.dto.InsurancePolicyRequest;
import com.ams.dto.InsurancePolicyResponse;
import com.ams.dto.InsuranceClaimResponse;
import com.ams.enums.ClaimStatus;
import com.ams.service.InsuranceClaimService;
import com.ams.service.InsurancePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Alias controller that provides REST endpoints matching frontend API conventions.
 * Maps frontend field names to backend DTOs and delegates to existing services.
 */
@RestController
@RequestMapping("/api/insurances")
@RequiredArgsConstructor
public class InsuranceAliasController {

    private final InsurancePolicyService insurancePolicyService;
    private final InsuranceClaimService insuranceClaimService;

    // ==================== Insurance Policy Alias Endpoints ====================

    @GetMapping
    public ResponseEntity<?> listInsurances(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<InsurancePolicyResponse> result = insurancePolicyService.listPolicies(
                    assetId, status, null, null, null, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInsurance(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(insurancePolicyService.getPolicy(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createInsurance(@RequestBody Map<String, Object> body) {
        try {
            // Map frontend field names to backend DTO
            InsurancePolicyRequest request = InsurancePolicyRequest.builder()
                    .policyNumber((String) body.get("insuranceNo"))
                    .assetId(body.get("assetId") != null ? ((Number) body.get("assetId")).longValue() : null)
                    .type((String) body.get("insuranceType"))
                    .insuranceCompany((String) body.get("insuranceCompany"))
                    .premium(body.get("premium") != null ? new BigDecimal(body.get("premium").toString()) : null)
                    .coverageAmount(body.get("coverageAmount") != null ? new BigDecimal(body.get("coverageAmount").toString()) : null)
                    .startDate(body.get("startDate") != null ? LocalDate.parse(body.get("startDate").toString()) : null)
                    .endDate(body.get("endDate") != null ? LocalDate.parse(body.get("endDate").toString()) : null)
                    .remarks((String) body.get("description"))
                    .build();
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInsurance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            InsurancePolicyRequest request = InsurancePolicyRequest.builder()
                    .type(body.get("insuranceType") != null ? (String) body.get("insuranceType") : null)
                    .insuranceCompany((String) body.get("insuranceCompany"))
                    .premium(body.get("premium") != null ? new BigDecimal(body.get("premium").toString()) : null)
                    .coverageAmount(body.get("coverageAmount") != null ? new BigDecimal(body.get("coverageAmount").toString()) : null)
                    .startDate(body.get("startDate") != null ? LocalDate.parse(body.get("startDate").toString()) : null)
                    .endDate(body.get("endDate") != null ? LocalDate.parse(body.get("endDate").toString()) : null)
                    .remarks((String) body.get("description"))
                    .policyDocument((String) body.get("policyDocument"))
                    .build();
            return ResponseEntity.ok(insurancePolicyService.updatePolicy(id, request));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInsurance(@PathVariable Long id) {
        try {
            insurancePolicyService.deletePolicy(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Claims Alias Endpoints ====================

    @PostMapping("/{id}/claims")
    public ResponseEntity<?> createInsuranceClaim(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            // Map frontend field names to backend DTO
            InsuranceClaimRequest request = InsuranceClaimRequest.builder()
                    .policyId(id)
                    .claimNumber((String) body.get("claimNo"))
                    .claimAmount(body.get("claimAmount") != null ? new BigDecimal(body.get("claimAmount").toString()) : null)
                    .incidentDate(body.get("incidentDate") != null ? LocalDate.parse(body.get("incidentDate").toString()) : LocalDate.now())
                    .incidentDescription((String) body.get("description"))
                    .build();
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

    @GetMapping("/claims")
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

    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringInsurances(@RequestParam(defaultValue = "30") int days) {
        try {
            List<Map<String, Object>> result = insuranceClaimService.getClaimsByPolicy(null).stream()
                    .map(c -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", c.getId());
                        m.put("policyId", c.getPolicyId());
                        m.put("claimNumber", c.getClaimNumber());
                        m.put("assetId", c.getAssetId());
                        m.put("assetName", c.getAssetName());
                        m.put("status", c.getStatus());
                        return m;
                    }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
