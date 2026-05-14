package com.ams.repository;

import com.ams.entity.InventoryTask;
import com.ams.enums.InventoryTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTaskRepository extends JpaRepository<InventoryTask, Long> {
    List<InventoryTask> findByPlanId(Long planId);
    List<InventoryTask> findByAssigneeId(Long assigneeId);
    List<InventoryTask> findByPlanIdAndStatus(Long planId, InventoryTaskStatus status);
    List<InventoryTask> findByAssigneeIdAndStatus(Long assigneeId, InventoryTaskStatus status);
    Optional<InventoryTask> findByAssetIdAndAssigneeId(Long assetId, Long assigneeId);
    Optional<InventoryTask> findByAssetIdAndAssigneeIdAndStatus(Long assetId, Long assigneeId, InventoryTaskStatus status);
    long countByPlanId(Long planId);
    long countByPlanIdAndStatus(Long planId, InventoryTaskStatus status);
}
