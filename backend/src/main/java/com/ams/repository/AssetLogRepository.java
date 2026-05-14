package com.ams.repository;

import com.ams.entity.AssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssetLogRepository extends JpaRepository<AssetLog, Long> {
    Page<AssetLog> findByAssetIdOrderByCreatedAtDesc(Long assetId, Pageable pageable);
    List<AssetLog> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') as month, COUNT(a) as count " +
           "FROM AssetLog a WHERE a.createdAt >= :since GROUP BY FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') " +
           "ORDER BY month ASC")
    List<Object[]> countByMonth(LocalDateTime since);
}
