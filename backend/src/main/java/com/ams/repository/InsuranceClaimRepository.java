package com.ams.repository;

import com.ams.entity.InsuranceClaim;
import com.ams.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long>, JpaSpecificationExecutor<InsuranceClaim> {

    Page<InsuranceClaim> findByPolicyId(Long policyId, Pageable pageable);

    Page<InsuranceClaim> findByStatus(ClaimStatus status, Pageable pageable);

    Optional<InsuranceClaim> findByClaimNumber(String claimNumber);

    Optional<InsuranceClaim> findByIdAndDeletedFalse(Long id);

    @Query("SELECT c FROM InsuranceClaim c WHERE c.policy.id = :policyId ORDER BY c.createdAt DESC")
    List<InsuranceClaim> findByPolicyIdOrderByCreatedAtDesc(@Param("policyId") Long policyId);

    @Query("SELECT c FROM InsuranceClaim c WHERE c.filedBy = :userId ORDER BY c.createdAt DESC")
    List<InsuranceClaim> findByFiledBy(@Param("userId") Long userId);
}
