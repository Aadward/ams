package com.ams.repository;

import com.ams.entity.InventoryRecord;
import com.ams.enums.InventoryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRecordRepository extends JpaRepository<InventoryRecord, Long> {
    List<InventoryRecord> findByTaskId(Long taskId);
    List<InventoryRecord> findByPlanId(Long planId);
    List<InventoryRecord> findByPlanIdAndResult(Long planId, InventoryResult result);
    Optional<InventoryRecord> findByTaskIdAndAssetId(Long taskId, Long assetId);
    Optional<InventoryRecord> findByAssetIdAndCheckedBy(Long assetId, Long checkedBy);
}
