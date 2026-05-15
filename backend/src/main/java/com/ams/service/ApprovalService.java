package com.ams.service;

import com.ams.dto.ApprovalRequestDTO;
import com.ams.entity.ApprovalRequest;
import com.ams.enums.ApprovalStatus;
import com.ams.enums.ApprovalType;
import com.ams.enums.NotificationType;
import com.ams.enums.AssetStatus;
import com.ams.enums.MaintenanceStatus;
import com.ams.enums.MaintenanceType;
import com.ams.repository.ApprovalRequestRepository;
import com.ams.repository.AssetRepository;
import com.ams.repository.BorrowRecordRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import com.ams.repository.MaintenanceRecordRepository;
import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.entity.BorrowRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final BorrowRecordRepository borrowRecordRepository;

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

        ApprovalRequest saved = approvalRequestRepository.save(request);

        // Notify all MANAGER and ADMIN roles about the new approval request
        String assetName = assetRepository.findById(assetId).map(a -> a.getName()).orElse("未知");
        String requesterName = employeeRepository.findById(requesterId).map(e -> e.getName()).orElse("未知");
        String typeLabel = switch (type) {
            case ASSET_ASSIGNMENT -> "领用";
            case ASSET_RETURN -> "归还";
            case MAINTENANCE -> "维修";
            case ASSET_BORROW -> "借用";
            case TRANSFER -> "转移";
        };

        String title = "新的" + typeLabel + "申请";
        String message = requesterName + " 申请「" + assetName + "」" + typeLabel + "，请尽快审批";

        employeeRepository.findByRole(com.ams.enums.UserRole.MANAGER).forEach(manager -> {
            notificationService.createNotification(manager.getId(), title, message, NotificationType.APPROVAL_REQUIRED);
        });
        employeeRepository.findByRole(com.ams.enums.UserRole.ADMIN).forEach(admin -> {
            notificationService.createNotification(admin.getId(), title, message, NotificationType.APPROVAL_REQUIRED);
        });

        // For MAINTENANCE type, notify the requester that their repair request was submitted
        if (type == ApprovalType.MAINTENANCE) {
            notificationService.createNotification(
                    requesterId,
                    "维修申请已提交",
                    "您对资产「" + assetName + "」的维修申请已提交，等待审批",
                    NotificationType.REPAIR_SUBMITTED
            );
        }

        return saved;
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

        // Sync maintenance record if this is a MAINTENANCE approval
        if (saved.getType() == ApprovalType.MAINTENANCE) {
            List<MaintenanceRecord> records = maintenanceRecordRepository.findByApprovalId(saved.getId());
            for (MaintenanceRecord record : records) {
                record.setStatus(MaintenanceStatus.APPROVED);
                Asset asset = record.getAsset();
                asset.setStatus(AssetStatus.MAINTENANCE);
                assetRepository.save(asset);
                maintenanceRecordRepository.save(record);
                log.info("Synced maintenance record {} to APPROVED, asset {} set to MAINTENANCE", record.getId(), asset.getId());
            }
        }

        // Create borrow record if this is an ASSET_BORROW approval
        if (saved.getType() == ApprovalType.ASSET_BORROW) {
            // Parse expected return date from reason (format: "借用天数:X" or "归还日期:YYYY-MM-DD")
            LocalDate expectedReturnDate = LocalDate.now().plusDays(30); // default 30 days
            String reason = saved.getReason();
            if (reason != null && reason.startsWith("归还日期:")) {
                try {
                    expectedReturnDate = LocalDate.parse(reason.substring(5));
                } catch (Exception e) {
                    log.warn("Failed to parse expected return date from reason: {}", reason);
                }
            } else if (reason != null && reason.startsWith("借用天数:")) {
                try {
                    int days = Integer.parseInt(reason.substring(5));
                    expectedReturnDate = LocalDate.now().plusDays(days);
                } catch (Exception e) {
                    log.warn("Failed to parse borrow days from reason: {}", reason);
                }
            }

            BorrowRecord borrowRecord = BorrowRecord.builder()
                    .assetId(saved.getAssetId())
                    .borrowerId(saved.getRequesterId())
                    .departmentId(saved.getDepartmentId())
                    .approvalId(saved.getId())
                    .borrowDate(LocalDate.now())
                    .expectedReturnDate(expectedReturnDate)
                    .status(com.ams.enums.BorrowStatus.BORROWED)
                    .reason(reason)
                    .build();

            borrowRecordRepository.save(borrowRecord);

            // Update asset status to IN_USE
            Asset asset = assetRepository.findById(saved.getAssetId()).orElse(null);
            if (asset != null) {
                asset.setStatus(AssetStatus.IN_USE);
                asset.setAssignee(employeeRepository.findById(saved.getRequesterId()).orElse(null));
                assetRepository.save(asset);
            }

            log.info("Created borrow record for asset {} borrowed by {}", saved.getAssetId(), saved.getRequesterId());
        }

        // Notify the requester
        NotificationType notifyType = (saved.getType() == ApprovalType.ASSET_BORROW)
                ? NotificationType.BORROW_APPROVED
                : (saved.getType() == ApprovalType.TRANSFER)
                    ? NotificationType.TRANSFER_APPROVED
                    : NotificationType.APPROVAL_APPROVED;
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已通过",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已审批通过",
                notifyType
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

        // Sync maintenance record if this is a MAINTENANCE rejection
        if (saved.getType() == ApprovalType.MAINTENANCE) {
            List<MaintenanceRecord> records = maintenanceRecordRepository.findByApprovalId(saved.getId());
            for (MaintenanceRecord record : records) {
                record.setStatus(MaintenanceStatus.REJECTED);
                maintenanceRecordRepository.save(record);
                log.info("Synced maintenance record {} to REJECTED", record.getId());
            }
        }

        // Notify the requester
        NotificationType notifyType = (saved.getType() == ApprovalType.ASSET_BORROW)
                ? NotificationType.BORROW_REJECTED
                : (saved.getType() == ApprovalType.TRANSFER)
                    ? NotificationType.TRANSFER_REJECTED
                    : NotificationType.APPROVAL_REJECTED;
        notificationService.createNotification(
                saved.getRequesterId(),
                "审批已拒绝",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已被拒绝",
                notifyType
        );

        return saved;
    }
}
