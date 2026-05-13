package com.ams.repository;

import com.ams.entity.AssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetLogRepository extends JpaRepository<AssetLog, Long> {
    Page<AssetLog> findByAssetIdOrderByCreatedAtDesc(Long assetId, Pageable pageable);
    List<AssetLog> findTop20ByOrderByCreatedAtDesc();
}
