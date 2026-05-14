package com.ams.repository;

import com.ams.entity.ConsumableRecord;
import com.ams.enums.ConsumableRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsumableRecordRepository extends JpaRepository<ConsumableRecord, Long> {
    List<ConsumableRecord> findByConsumableIdOrderByCreatedAtDesc(Long consumableId);
    List<ConsumableRecord> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
        ConsumableRecordType type, LocalDateTime start, LocalDateTime end);
    List<ConsumableRecord> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime start, LocalDateTime end);
}
