package com.ams.repository;

import com.ams.entity.ApprovalRequest;
import com.ams.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    List<ApprovalRequest> findByRequesterId(Long requesterId);

    List<ApprovalRequest> findByRequesterIdAndStatus(Long requesterId, ApprovalStatus status);

    List<ApprovalRequest> findByDepartmentIdAndStatus(Long departmentId, ApprovalStatus status);
}
