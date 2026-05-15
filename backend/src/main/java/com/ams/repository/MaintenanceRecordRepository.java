package com.ams.repository;

import com.ams.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long>, JpaSpecificationExecutor<MaintenanceRecord> {
    List<MaintenanceRecord> findByAssetIdOrderByStartDateDesc(Long assetId);

    List<MaintenanceRecord> findByApprovalId(Long approvalId);

    List<MaintenanceRecord> findByVendorContainingIgnoreCase(String vendor);
}
