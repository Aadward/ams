package com.ams.repository;

import com.ams.entity.ProcurementRequest;
import com.ams.enums.ProcurementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementRepository extends JpaRepository<ProcurementRequest, Long> {

    List<ProcurementRequest> findByStatus(ProcurementStatus status);

    List<ProcurementRequest> findByRequesterId(Long requesterId);

    List<ProcurementRequest> findByRequesterIdAndStatus(Long requesterId, ProcurementStatus status);

    List<ProcurementRequest> findByDepartmentId(Long departmentId);

    List<ProcurementRequest> findByDepartmentIdAndStatus(Long departmentId, ProcurementStatus status);

    List<ProcurementRequest> findByApproverId(Long approverId);
}
