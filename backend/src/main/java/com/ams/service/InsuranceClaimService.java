package com.ams.service;

import com.ams.dto.InsuranceClaimRequest;
import com.ams.dto.InsuranceClaimResponse;
import com.ams.entity.InsuranceClaim;
import com.ams.entity.InsurancePolicy;
import com.ams.enums.ClaimStatus;
import com.ams.enums.NotificationType;
import com.ams.repository.InsuranceClaimRepository;
import com.ams.repository.InsurancePolicyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceClaimService {

    private final InsuranceClaimRepository insuranceClaimRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<InsuranceClaimResponse> listClaims(
            Long policyId, String status, Pageable pageable) {

        Specification<InsuranceClaim> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (policyId != null) {
                predicates.add(cb.equal(root.get("policy").get("id"), policyId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), ClaimStatus.valueOf(status)));
            }
            predicates.add(cb.isFalse(root.get("deleted")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return insuranceClaimRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InsuranceClaimResponse getClaim(Long id) {
        InsuranceClaim claim = insuranceClaimRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("理赔记录不存在"));
        return toResponse(claim);
    }

    @Transactional(readOnly = true)
    public List<InsuranceClaimResponse> getClaimsByPolicy(Long policyId) {
        return insuranceClaimRepository.findByPolicyIdOrderByCreatedAtDesc(policyId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public InsuranceClaimResponse createClaim(InsuranceClaimRequest request) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndDeletedFalse(request.getPolicyId())
                .orElseThrow(() -> new RuntimeException("保险单不存在"));

        InsuranceClaim claim = InsuranceClaim.builder()
                .claimNumber(request.getClaimNumber())
                .policy(policy)
                .incidentDate(request.getIncidentDate())
                .claimAmount(request.getClaimAmount())
                .incidentDescription(request.getIncidentDescription())
                .filedBy(request.getFiledBy())
                .status(ClaimStatus.PENDING)
                .build();

        claim = insuranceClaimRepository.save(claim);
        log.info("Created insurance claim {} for policy {}", claim.getClaimNumber(), policy.getPolicyNumber());

        notifyAdmins("新建理赔申请", 
                String.format("资产「%s」有新的理赔申请，理赔号：%s", 
                        policy.getAsset().getName(), claim.getClaimNumber()),
                NotificationType.SYSTEM);

        return toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse updateClaim(Long id, InsuranceClaimRequest request) {
        InsuranceClaim claim = insuranceClaimRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("理赔记录不存在"));

        if (request.getIncidentDate() != null) {
            claim.setIncidentDate(request.getIncidentDate());
        }
        if (request.getClaimAmount() != null) {
            claim.setClaimAmount(request.getClaimAmount());
        }
        if (request.getIncidentDescription() != null) {
            claim.setIncidentDescription(request.getIncidentDescription());
        }

        claim = insuranceClaimRepository.save(claim);
        return toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse settleClaim(Long id, BigDecimal settledAmount, String settlementNotes) {
        InsuranceClaim claim = insuranceClaimRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("理赔记录不存在"));

        claim.setSettledAmount(settledAmount);
        claim.setSettlementNotes(settlementNotes);
        claim.setStatus(ClaimStatus.SETTLED);

        claim = insuranceClaimRepository.save(claim);
        log.info("Settled claim {} with amount {}", claim.getClaimNumber(), settledAmount);

        notifyAdmins("理赔已处理",
                String.format("理赔号 %s 已处理，理赔金额：%s", claim.getClaimNumber(), settledAmount),
                NotificationType.SYSTEM);

        return toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse rejectClaim(Long id, String settlementNotes) {
        InsuranceClaim claim = insuranceClaimRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("理赔记录不存在"));

        claim.setSettlementNotes(settlementNotes);
        claim.setStatus(ClaimStatus.REJECTED);

        claim = insuranceClaimRepository.save(claim);
        log.info("Rejected claim {}", claim.getClaimNumber());

        notifyAdmins("理赔被拒绝",
                String.format("理赔号 %s 已被拒绝", claim.getClaimNumber()),
                NotificationType.SYSTEM);

        return toResponse(claim);
    }

    @Transactional
    public void deleteClaim(Long id) {
        InsuranceClaim claim = insuranceClaimRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("理赔记录不存在"));
        claim.setDeleted(true);
        insuranceClaimRepository.save(claim);
    }

    private InsuranceClaimResponse toResponse(InsuranceClaim claim) {
        InsurancePolicy policy = claim.getPolicy();
        return InsuranceClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .assetId(policy.getAsset().getId())
                .assetName(policy.getAsset().getName())
                .assetCode(policy.getAsset().getAssetCode())
                .incidentDate(claim.getIncidentDate())
                .claimAmount(claim.getClaimAmount())
                .settledAmount(claim.getSettledAmount())
                .status(claim.getStatus().name())
                .incidentDescription(claim.getIncidentDescription())
                .settlementNotes(claim.getSettlementNotes())
                .filedBy(claim.getFiledBy())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private void notifyAdmins(String title, String message, NotificationType type) {
        notificationService.createNotification(1L, title, message, type);
    }
}
