package com.ams.repository;

import com.ams.entity.Consumable;
import com.ams.enums.ConsumableCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConsumableRepository extends JpaRepository<Consumable, Long> {
    List<Consumable> findByDeletedFalse();
    List<Consumable> findByDeletedFalseAndCategory(ConsumableCategory category);
    Optional<Consumable> findByIdAndDeletedFalse(Long id);
}
