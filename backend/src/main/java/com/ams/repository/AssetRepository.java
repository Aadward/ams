package com.ams.repository;

import com.ams.entity.Asset;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Page<Asset> findByDeletedFalse(Pageable pageable);

    Page<Asset> findByDeletedFalseAndCategory(AssetCategory category, Pageable pageable);

    Page<Asset> findByDeletedFalseAndStatus(AssetStatus status, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.deleted = false AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:keyword IS NULL OR a.name LIKE %:keyword% OR a.assetCode LIKE %:keyword%)")
    Page<Asset> searchAssets(
            @Param("category") AssetCategory category,
            @Param("status") AssetStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    Optional<Asset> findByIdAndDeletedFalse(Long id);

    Optional<Asset> findByAssetCodeAndDeletedFalse(String assetCode);

    List<Asset> findByAssigneeId(Long assigneeId);

    long countByStatus(AssetStatus status);

    long countByCategory(AssetCategory category);

    List<Asset> findByDeletedFalse();

    List<Asset> findByDeletedFalseAndWarrantyEndBetween(java.time.LocalDate start, java.time.LocalDate end);

    List<Asset> findByDeletedFalseAndSupplierContainingIgnoreCase(String supplier);
}
