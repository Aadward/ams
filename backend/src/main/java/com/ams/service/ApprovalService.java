package com.ams.service;

import com.ams.dto.ApprovalRequestDTO;
import com.ams.entity.ApprovalRequest;
import com.ams.enums.ApprovalStatus;
import com.ams.enums.ApprovalType;
import com.ams.enums.NotificationType;
import com.ams.repository.ApprovalRequestRepository;
import com.ams.repository.AssetRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    private ApprovalRequestDTO toDTO(ApprovalRequest r) {
        String assetName = assetRepository.findById(r.getAssetId())
                .map(a -> a.getName()).orElse("未知");
        String assetCode = assetRepository.findById(r.getAssetId())
                .map(a -> a.getAssetCode()).orElse("未知");
        String requesterName = employeeRepository.findById(r.getRequesterId())
                .map(e -> e.getName()).orElse("未知");
        String deptName = departmentRepository.findById(r.getDepartmentId())
                .map(d -> d.getName()).orElse("未知");

        return ApprovalRequestDTO.builder()
                .id(r.getId())
                .requesterId(r.getRequesterId())
                .requesterName(requesterName)
                .assetId(r.getAssetId())
                .assetName(assetName)
                .assetCode(assetCode)
                .departmentId(r.getDepartmentId())
                .departmentName(deptName)
                .type(r.getType())
                .status(r.getStatus())
                .reason(r.getReason())
                .managerComment(r.getManagerComment())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }

    @Transactional
    public ApprovalRequest createRequest(Long requesterId, Long assetId, Long departmentId,
                                         ApprovalType type, String reason) {
        log.info("Creating approval request: requesterId={}, assetId={}, type={}", requesterId, assetId, type);

        ApprovalRequest request = ApprovalRequest.builder()
                .requesterId(requesterId)
                .assetId(assetId)
                .departmentId(departmentId)
                .type(type)
                .status(ApprovalStatus.PENDING)
                .reason(reason)
                .build();

        return approvalRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestDTO> getPendingRequests() {
        log.info("Fetching all pending approval requests");
        return approvalRequestRepository.findByStatus(ApprovalStatus.PENDING)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestDTO> getMyRequests(Long requesterId) {
        log.info("Fetching approval requests for requesterId={}", requesterId);
        return approvalRequestRepository.findByRequesterId(requesterId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ApprovalRequest approve(Long id, String managerComment) {
        log.info("Approving request id={}, comment={}", id, managerComment);

        ApprovalRequest request = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        request.setStatus(ApprovalStatus.APPROVED);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());
        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Notify the requester
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已通过",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已审批通过",
                NotificationType.APPROVAL_REQUIRED
        );

        return saved;
    }

    @Transactional
    public ApprovalRequest reject(Long id, String managerComment) {
        log.info("Rejecting request id={}, comment={}", id, managerComment);

        ApprovalRequest request = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(ApprovalStatus.REJECTED);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());
        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Notify the requester
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已拒绝",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已被拒绝",
                NotificationType.APPROVAL_REQUIRED
        );

        return saved;
    }
}
