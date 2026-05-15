package com.ams.repository;

import com.ams.entity.AssetTransferRecord;
import com.ams.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTransferRecordRepository extends JpaRepository<AssetTransferRecord, Long> {

    List<AssetTransferRecord> findByAssetId(Long assetId);

    List<AssetTransferRecord> findByFromEmployeeId(Long fromEmployeeId);

    List<AssetTransferRecord> findByToEmployeeId(Long toEmployeeId);

    List<AssetTransferRecord> findByStatus(TransferStatus status);

    List<AssetTransferRecord> findByFromDepartmentIdOrToDepartmentId(Long fromDepartmentId, Long toDepartmentId);
}
