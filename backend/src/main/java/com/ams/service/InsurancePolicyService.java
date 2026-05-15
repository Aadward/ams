package com.ams.service;

import com.ams.dto.InsurancePolicyRequest;
import com.ams.dto.InsurancePolicyResponse;
import com.ams.entity.Asset;
import com.ams.entity.InsurancePolicy;
import com.ams.enums.InsuranceStatus;
import com.ams.enums.InsuranceType;
import com.ams.enums.NotificationType;
import com.ams.repository.AssetRepository;
import com.ams.repository.InsurancePolicyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsurancePolicyService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final AssetRepository assetRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<InsurancePolicyResponse> listPolicies(
            Long assetId, String status, String type,
            String dateFrom, String dateTo, Pageable pageable) {

        Specification<InsurancePolicy> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (assetId != null) {
                predicates.add(cb.equal(root.get("asset").get("id"), assetId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), InsuranceStatus.valueOf(status)));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), InsuranceType.valueOf(type)));
            }
            if (dateFrom != null && !dateFrom.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), LocalDate.parse(dateFrom)));
            }
            if (dateTo != null && !dateTo.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), LocalDate.parse(dateTo)));
            }
            predicates.add(cb.isFalse(root.get("deleted")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return insurancePolicyRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InsurancePolicyResponse getPolicy(Long id) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("保险单不存在"));
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<InsurancePolicyResponse> getPoliciesByAsset(Long assetId) {
        return insurancePolicyRepository.findByAssetIdOrderByCreatedAtDesc(assetId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public InsurancePolicyResponse createPolicy(InsurancePolicyRequest request) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(request.getAssetId())
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        InsurancePolicy policy = InsurancePolicy.builder()
                .policyNumber(request.getPolicyNumber())
                .asset(asset)
                .type(InsuranceType.valueOf(request.getType()))
                .insuranceCompany(request.getInsuranceCompany())
                .premium(request.getPremium())
                .coverageAmount(request.getCoverageAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .policyDocument(request.getPolicyDocument())
                .remarks(request.getRemarks())
                .status(InsuranceStatus.ACTIVE)
                .build();

        policy = insurancePolicyRepository.save(policy);
        log.info("Created insurance policy {} for asset {}", policy.getPolicyNumber(), asset.getAssetCode());
        return toResponse(policy);
    }

    @Transactional
    public InsurancePolicyResponse updatePolicy(Long id, InsurancePolicyRequest request) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("保险单不存在"));

        if (request.getType() != null) {
            policy.setType(InsuranceType.valueOf(request.getType()));
        }
        if (request.getInsuranceCompany() != null) {
            policy.setInsuranceCompany(request.getInsuranceCompany());
        }
        if (request.getPremium() != null) {
            policy.setPremium(request.getPremium());
        }
        if (request.getCoverageAmount() != null) {
            policy.setCoverageAmount(request.getCoverageAmount());
        }
        if (request.getStartDate() != null) {
            policy.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            policy.setEndDate(request.getEndDate());
        }
        if (request.getPolicyDocument() != null) {
            policy.setPolicyDocument(request.getPolicyDocument());
        }
        if (request.getRemarks() != null) {
            policy.setRemarks(request.getRemarks());
        }

        policy = insurancePolicyRepository.save(policy);
        return toResponse(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("保险单不存在"));
        policy.setDeleted(true);
        policy.setStatus(InsuranceStatus.CANCELLED);
        insurancePolicyRepository.save(policy);
    }

    @Transactional
    public InsurancePolicyResponse cancelPolicy(Long id) {
        InsurancePolicy policy = insurancePolicyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("保险单不存在"));
        policy.setStatus(InsuranceStatus.CANCELLED);
        policy = insurancePolicyRepository.save(policy);
        return toResponse(policy);
    }

    private InsurancePolicyResponse toResponse(InsurancePolicy policy) {
        return InsurancePolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .assetId(policy.getAsset().getId())
                .assetName(policy.getAsset().getName())
                .assetCode(policy.getAsset().getAssetCode())
                .type(policy.getType().name())
                .typeDescription(policy.getType().getDescription())
                .insuranceCompany(policy.getInsuranceCompany())
                .premium(policy.getPremium())
                .coverageAmount(policy.getCoverageAmount())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus().name())
                .policyDocument(policy.getPolicyDocument())
                .remarks(policy.getRemarks())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
