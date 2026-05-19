package com.ams.service;

import com.ams.dto.ApprovalRequestDTO;
import com.ams.entity.ApprovalRequest;
import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.entity.BorrowRecord;
import com.ams.entity.AssetTransferRecord;
import com.ams.enums.*;
import com.ams.repository.*;
import com.ams.workflow.ApprovalWorkflow;
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
    private final AssetTransferRecordRepository assetTransferRecordRepository;
    private final ApprovalWorkflow approvalWorkflow;

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

    /**
     * Create an approval request via the generic workflow engine.
     */
    @Transactional
    public ApprovalRequest createRequest(Long requesterId, Long assetId, Long departmentId,
                                         ApprovalType type, String reason) {
        log.info("ApprovalService.createRequest: requesterId={}, assetId={}, type={}", requesterId, assetId, type);
        return approvalWorkflow.trigger(requesterId, assetId, departmentId, type, reason);
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

    @Transactional(readOnly = true)
    public long getPendingCount() {
        log.info("Fetching count of pending approval requests");
        return approvalRequestRepository.findByStatus(ApprovalStatus.PENDING).size();
    }

    /**
     * Approve an approval request. Delegates to workflow; business callbacks handle
     * MAINTENANCE (sync MaintenanceRecord), ASSET_BORROW (create BorrowRecord),
     * TRANSFER (sync AssetTransferRecord).
     */
    @Transactional
    public ApprovalRequest approve(Long id, String managerComment) {
        log.info("Approving request id={}, comment={}", id, managerComment);

        ApprovalRequest request = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        return approvalWorkflow.approve(id, managerComment, approvedReq -> {
            // Business-specific side effects per type
            switch (approvedReq.getType()) {
                case MAINTENANCE -> this.syncMaintenanceApproval(approvedReq);
                case ASSET_BORROW -> this.createBorrowRecordOnApproval(approvedReq);
                case TRANSFER -> this.syncTransferApproval(approvedReq);
                case PROCUREMENT -> this.syncProcurementApproval(approvedReq);
                default -> log.info("No additional business callback for type {}", approvedReq.getType());
            }
        });
    }

    /**
     * Reject an approval request. Delegates to workflow; business callbacks handle
     * MAINTENANCE and TRANSFER rejections.
     */
    @Transactional
    public ApprovalRequest reject(Long id, String managerComment) {
        log.info("Rejecting request id={}, comment={}", id, managerComment);

        ApprovalRequest request = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        return approvalWorkflow.reject(id, managerComment, rejectedReq -> {
            switch (rejectedReq.getType()) {
                case MAINTENANCE -> this.syncMaintenanceRejection(rejectedReq);
                case TRANSFER -> this.syncTransferRejection(rejectedReq);
                default -> log.info("No additional rejection callback for type {}", rejectedReq.getType());
            }
        });
    }

    // --- Business-side effect handlers ---

    private void syncMaintenanceApproval(ApprovalRequest approvedReq) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByApprovalId(approvedReq.getId());
        for (MaintenanceRecord record : records) {
            record.setStatus(MaintenanceStatus.APPROVED);
            Asset asset = record.getAsset();
            asset.setStatus(AssetStatus.MAINTENANCE);
            assetRepository.save(asset);
            maintenanceRecordRepository.save(record);
            log.info("Synced maintenance record {} to APPROVED, asset {} set to MAINTENANCE", record.getId(), asset.getId());
        }
    }

    private void createBorrowRecordOnApproval(ApprovalRequest approvedReq) {
        LocalDate expectedReturnDate = parseExpectedReturnDate(approvedReq.getReason());

        BorrowRecord borrowRecord = BorrowRecord.builder()
                .assetId(approvedReq.getAssetId())
                .borrowerId(approvedReq.getRequesterId())
                .departmentId(approvedReq.getDepartmentId())
                .approvalId(approvedReq.getId())
                .borrowDate(LocalDate.now())
                .expectedReturnDate(expectedReturnDate)
                .status(BorrowStatus.BORROWED)
                .reason(approvedReq.getReason())
                .build();

        borrowRecordRepository.save(borrowRecord);

        Asset asset = assetRepository.findById(approvedReq.getAssetId()).orElse(null);
        if (asset != null) {
            asset.setStatus(AssetStatus.IN_USE);
            asset.setAssignee(employeeRepository.findById(approvedReq.getRequesterId()).orElse(null));
            assetRepository.save(asset);
        }

        log.info("Created borrow record for asset {} borrowed by {}", approvedReq.getAssetId(), approvedReq.getRequesterId());
    }

    private void syncTransferApproval(ApprovalRequest approvedReq) {
        List<AssetTransferRecord> transferRecords = assetTransferRecordRepository.findByApprovalId(approvedReq.getId());
        for (AssetTransferRecord record : transferRecords) {
            record.setStatus(TransferStatus.APPROVED);
            record.setManagerComment(approvedReq.getManagerComment());
            record.setResolvedAt(LocalDateTime.now());
            Asset asset = assetRepository.findById(approvedReq.getAssetId()).orElse(null);
            if (asset != null) {
                asset.setAssignee(employeeRepository.findById(record.getToEmployeeId()).orElse(null));
                assetRepository.save(asset);
                log.info("Asset {} transferred to employee {} via transfer record {}",
                        asset.getId(), record.getToEmployeeId(), record.getId());
            }
            assetTransferRecordRepository.save(record);
        }
    }

    private void syncProcurementApproval(ApprovalRequest approvedReq) {
        // PROCUREMENT approval creates an asset in the system
        log.info("Procurement request {} approved, asset creation pending business logic", approvedReq.getId());
        // Asset creation for procurement is handled separately via ProcurementService
    }

    private void syncMaintenanceRejection(ApprovalRequest rejectedReq) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByApprovalId(rejectedReq.getId());
        for (MaintenanceRecord record : records) {
            record.setStatus(MaintenanceStatus.REJECTED);
            maintenanceRecordRepository.save(record);
            log.info("Synced maintenance record {} to REJECTED", record.getId());
        }
    }

    private void syncTransferRejection(ApprovalRequest rejectedReq) {
        List<AssetTransferRecord> transferRecords = assetTransferRecordRepository.findByApprovalId(rejectedReq.getId());
        for (AssetTransferRecord record : transferRecords) {
            record.setStatus(TransferStatus.REJECTED);
            record.setManagerComment(rejectedReq.getManagerComment());
            record.setResolvedAt(LocalDateTime.now());
            assetTransferRecordRepository.save(record);
            log.info("Synced transfer record {} to REJECTED", record.getId());
        }
    }

    private LocalDate parseExpectedReturnDate(String reason) {
        LocalDate expectedReturnDate = LocalDate.now().plusDays(30);
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
        return expectedReturnDate;
    }
}
