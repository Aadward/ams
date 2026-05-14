package com.ams.repository;

import com.ams.entity.ConsumableStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConsumableStockRepository extends JpaRepository<ConsumableStock, Long> {
    Optional<ConsumableStock> findByConsumableId(Long consumableId);
}
