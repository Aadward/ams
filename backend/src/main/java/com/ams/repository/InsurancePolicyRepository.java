package com.ams.repository;

import com.ams.entity.InsurancePolicy;
import com.ams.enums.InsuranceStatus;
import com.ams.enums.InsuranceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long>, JpaSpecificationExecutor<InsurancePolicy> {

    Page<InsurancePolicy> findByAssetId(Long assetId, Pageable pageable);

    Page<InsurancePolicy> findByStatus(InsuranceStatus status, Pageable pageable);

    Page<InsurancePolicy> findByType(InsuranceType type, Pageable pageable);

    Optional<InsurancePolicy> findByPolicyNumber(String policyNumber);

    Optional<InsurancePolicy> findByIdAndDeletedFalse(Long id);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.deleted = false AND p.status = :status")
    List<InsurancePolicy> findAllActive(@Param("status") InsuranceStatus status);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.deleted = false AND p.status = :status AND p.endDate BETWEEN :start AND :end")
    List<InsurancePolicy> findByExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") InsuranceStatus status);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.deleted = false AND p.asset.id = :assetId ORDER BY p.createdAt DESC")
    List<InsurancePolicy> findByAssetIdOrderByCreatedAtDesc(@Param("assetId") Long assetId);
}
