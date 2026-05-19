package com.ams.workflow;

import com.ams.entity.ApprovalRequest;
import com.ams.enums.ApprovalStatus;
import com.ams.enums.ApprovalType;
import com.ams.enums.NotificationType;
import com.ams.repository.ApprovalRequestRepository;
import com.ams.repository.AssetRepository;
import com.ams.repository.EmployeeRepository;
import com.ams.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Generic approval workflow engine.
 * Handles the core lifecycle: trigger (create + notify), approve, reject.
 * Business-specific side effects are delegated via callbacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalWorkflow {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final AssetRepository assetRepository;

    /**
     * Trigger: create an approval request and notify managers.
     * @param requesterId who is requesting
     * @param assetId asset being acted upon
     * @param departmentId department
     * @param type approval type
     * @param reason reason for request
     * @return the created ApprovalRequest
     */
    @Transactional
    public ApprovalRequest trigger(Long requesterId, Long assetId, Long departmentId,
                                    ApprovalType type, String reason) {
        log.info("Workflow trigger: requesterId={}, assetId={}, type={}", requesterId, assetId, type);

        ApprovalRequest request = ApprovalRequest.builder()
                .requesterId(requesterId)
                .assetId(assetId)
                .departmentId(departmentId)
                .type(type)
                .status(ApprovalStatus.PENDING)
                .reason(reason)
                .build();

        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Notify all MANAGER and ADMIN roles
        String assetName = assetRepository.findById(assetId).map(a -> a.getName()).orElse("未知");
        String requesterName = employeeRepository.findById(requesterId).map(e -> e.getName()).orElse("未知");
        String typeLabel = typeLabel(type);
        String title = "新的" + typeLabel + "申请";
        String message = requesterName + " 申请「" + assetName + "」" + typeLabel + "，请尽快审批";

        employeeRepository.findByRole(com.ams.enums.UserRole.MANAGER).forEach(manager -> {
            notificationService.createNotification(manager.getId(), title, message, NotificationType.APPROVAL_REQUIRED);
        });
        employeeRepository.findByRole(com.ams.enums.UserRole.ADMIN).forEach(admin -> {
            notificationService.createNotification(admin.getId(), title, message, NotificationType.APPROVAL_REQUIRED);
        });

        // For MAINTENANCE type, also notify the requester
        if (type == ApprovalType.MAINTENANCE) {
            notificationService.createNotification(
                    requesterId,
                    "维修申请已提交",
                    "您对资产「" + assetName + "」的维修申请已提交，等待审批",
                    NotificationType.REPAIR_SUBMITTED
            );
        }

        log.info("Workflow: created approval request id={}", saved.getId());
        return saved;
    }

    /**
     * Approve an approval request and execute the business callback.
     * @param approvalId approval request ID
     * @param managerComment optional comment
     * @param callback business-specific side-effect handler (e.g. create borrow record)
     * @return the updated ApprovalRequest
     */
    @Transactional
    public ApprovalRequest approve(Long approvalId, String managerComment, Consumer<ApprovalRequest> callback) {
        log.info("Workflow approve: approvalId={}", approvalId);

        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        request.setStatus(ApprovalStatus.APPROVED);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());
        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Execute business-specific callback
        if (callback != null) {
            callback.accept(saved);
        }

        // Notify requester
        String assetName = assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("");
        NotificationType notifyType = mapApprovalNotifyType(saved.getType());
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已通过",
                "您申请的资产「" + assetName + "」已审批通过",
                notifyType
        );

        log.info("Workflow: approved request id={}", saved.getId());
        return saved;
    }

    /**
     * Reject an approval request and execute the business callback.
     * @param approvalId approval request ID
     * @param managerComment rejection reason
     * @param callback business-specific side-effect handler (e.g. mark rejection state)
     * @return the updated ApprovalRequest
     */
    @Transactional
    public ApprovalRequest reject(Long approvalId, String managerComment, Consumer<ApprovalRequest> callback) {
        log.info("Workflow reject: approvalId={}", approvalId);

        ApprovalRequest request = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(ApprovalStatus.REJECTED);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());
        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Execute business-specific callback
        if (callback != null) {
            callback.accept(saved);
        }

        // Notify requester
        String assetName = assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("");
        NotificationType notifyType = mapRejectionNotifyType(saved.getType());
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已拒绝",
                "您申请的资产「" + assetName + "」已被拒绝",
                notifyType
        );

        log.info("Workflow: rejected request id={}", saved.getId());
        return saved;
    }

    private String typeLabel(ApprovalType type) {
        return switch (type) {
            case ASSET_ASSIGNMENT -> "领用";
            case ASSET_RETURN -> "归还";
            case MAINTENANCE -> "维修";
            case ASSET_BORROW -> "借用";
            case TRANSFER -> "转移";
            case PROCUREMENT -> "采购";
        };
    }

    private NotificationType mapApprovalNotifyType(ApprovalType type) {
        return switch (type) {
            case ASSET_BORROW -> NotificationType.BORROW_APPROVED;
            case TRANSFER -> NotificationType.TRANSFER_APPROVED;
            case PROCUREMENT -> NotificationType.PROCUREMENT_APPROVED;
            default -> NotificationType.APPROVAL_APPROVED;
        };
    }

    private NotificationType mapRejectionNotifyType(ApprovalType type) {
        return switch (type) {
            case ASSET_BORROW -> NotificationType.BORROW_REJECTED;
            case TRANSFER -> NotificationType.TRANSFER_REJECTED;
            case PROCUREMENT -> NotificationType.PROCUREMENT_REJECTED;
            default -> NotificationType.APPROVAL_REJECTED;
        };
    }
}
