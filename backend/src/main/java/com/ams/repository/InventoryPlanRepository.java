package com.ams.repository;

import com.ams.entity.InventoryPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryPlanRepository extends JpaRepository<InventoryPlan, Long> {
    List<InventoryPlan> findAllByOrderByCreatedAtDesc();
}
