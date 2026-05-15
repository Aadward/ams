package com.ams.service;

import com.ams.dto.TransferApplyRequest;
import com.ams.dto.TransferRecordResponse;
import com.ams.entity.Asset;
import com.ams.entity.AssetTransferRecord;
import com.ams.enums.TransferStatus;
import com.ams.enums.NotificationType;
import com.ams.enums.ApprovalType;
import com.ams.repository.AssetRepository;
import com.ams.repository.AssetTransferRecordRepository;
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
public class AssetTransferService {

    private final AssetTransferRecordRepository transferRecordRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    private TransferRecordResponse toResponse(AssetTransferRecord r) {
        String assetName = assetRepository.findById(r.getAssetId())
                .map(a -> a.getName()).orElse("未知");
        String assetCode = assetRepository.findById(r.getAssetId())
                .map(a -> a.getAssetCode()).orElse("未知");
        String fromEmployeeName = employeeRepository.findById(r.getFromEmployeeId())
                .map(e -> e.getName()).orElse("未知");
        String toEmployeeName = employeeRepository.findById(r.getToEmployeeId())
                .map(e -> e.getName()).orElse("未知");
        String fromDeptName = departmentRepository.findById(r.getFromDepartmentId())
                .map(d -> d.getName()).orElse("未知");
        String toDeptName = departmentRepository.findById(r.getToDepartmentId())
                .map(d -> d.getName()).orElse("未知");

        return TransferRecordResponse.builder()
                .id(r.getId())
                .assetId(r.getAssetId())
                .assetName(assetName)
                .assetCode(assetCode)
                .fromEmployeeId(r.getFromEmployeeId())
                .fromEmployeeName(fromEmployeeName)
                .toEmployeeId(r.getToEmployeeId())
                .toEmployeeName(toEmployeeName)
                .fromDepartmentId(r.getFromDepartmentId())
                .fromDepartmentName(fromDeptName)
                .toDepartmentId(r.getToDepartmentId())
                .toDepartmentName(toDeptName)
                .approvalId(r.getApprovalId())
                .status(r.getStatus())
                .reason(r.getReason())
                .managerComment(r.getManagerComment())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }

    @Transactional
    public AssetTransferRecord applyTransfer(Long requesterId, TransferApplyRequest request) {
        log.info("Applying transfer: requesterId={}, assetId={}, toEmployeeId={}",
                requesterId, request.getAssetId(), request.getToEmployeeId());

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        Long fromEmployeeId = asset.getAssignee() != null ? asset.getAssignee().getId() : requesterId;
        Long fromDepartmentId = asset.getAssignee() != null && asset.getAssignee().getDepartment() != null
                ? asset.getAssignee().getDepartment().getId()
                : request.getToDepartmentId();

        AssetTransferRecord record = AssetTransferRecord.builder()
                .assetId(request.getAssetId())
                .fromEmployeeId(fromEmployeeId)
                .toEmployeeId(request.getToEmployeeId())
                .fromDepartmentId(fromDepartmentId)
                .toDepartmentId(request.getToDepartmentId())
                .status(TransferStatus.PENDING)
                .reason(request.getReason())
                .build();

        AssetTransferRecord saved = transferRecordRepository.save(record);

        // Notify managers about transfer request
        String assetName = asset.getName();
        String requesterName = employeeRepository.findById(requesterId).map(e -> e.getName()).orElse("未知");
        String message = requesterName + " 申请将资产「" + assetName + "」转移给「"
                + employeeRepository.findById(request.getToEmployeeId()).map(e -> e.getName()).orElse("未知") + "」，请尽快审批";

        employeeRepository.findByRole(com.ams.enums.UserRole.MANAGER).forEach(manager -> {
            notificationService.createNotification(manager.getId(), "资产转移申请", message, NotificationType.TRANSFER_REQUIRED);
        });
        employeeRepository.findByRole(com.ams.enums.UserRole.ADMIN).forEach(admin -> {
            notificationService.createNotification(admin.getId(), "资产转移申请", message, NotificationType.TRANSFER_REQUIRED);
        });

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TransferRecordResponse> getPendingTransfers() {
        return transferRecordRepository.findByStatus(TransferStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferRecordResponse> getMyTransferRequests(Long requesterId) {
        return transferRecordRepository.findByFromEmployeeId(requesterId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferRecordResponse> getTransfersByAsset(Long assetId) {
        return transferRecordRepository.findByAssetId(assetId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AssetTransferRecord approveTransfer(Long id, String managerComment) {
        log.info("Approving transfer id={}, comment={}", id, managerComment);

        AssetTransferRecord record = transferRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer record not found"));

        if (record.getStatus() != TransferStatus.PENDING) {
            throw new RuntimeException("Only pending transfers can be approved");
        }

        record.setStatus(TransferStatus.APPROVED);
        record.setManagerComment(managerComment);
        record.setResolvedAt(LocalDateTime.now());
        AssetTransferRecord saved = transferRecordRepository.save(record);

        // Update asset assignee
        Asset asset = assetRepository.findById(saved.getAssetId()).orElse(null);
        if (asset != null) {
            asset.setAssignee(employeeRepository.findById(saved.getToEmployeeId()).orElse(null));
            assetRepository.save(asset);
            log.info("Asset {} transferred to employee {}", saved.getAssetId(), saved.getToEmployeeId());
        }

        // Notify requester
        notificationService.createNotification(
                saved.getFromEmployeeId(),
                "资产转移已通过",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已转移审批通过",
                NotificationType.TRANSFER_APPROVED
        );

        return saved;
    }

    @Transactional
    public AssetTransferRecord rejectTransfer(Long id, String managerComment) {
        log.info("Rejecting transfer id={}, comment={}", id, managerComment);

        AssetTransferRecord record = transferRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer record not found"));

        if (record.getStatus() != TransferStatus.PENDING) {
            throw new RuntimeException("Only pending transfers can be rejected");
        }

        record.setStatus(TransferStatus.REJECTED);
        record.setManagerComment(managerComment);
        record.setResolvedAt(LocalDateTime.now());
        AssetTransferRecord saved = transferRecordRepository.save(record);

        // Notify requester
        notificationService.createNotification(
                saved.getFromEmployeeId(),
                "资产转移已拒绝",
                "您申请的资产「" + assetRepository.findById(saved.getAssetId()).map(a -> a.getName()).orElse("") + "」已被拒绝",
                NotificationType.TRANSFER_REJECTED
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public TransferRecordResponse getTransferById(Long id) {
        AssetTransferRecord record = transferRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer record not found"));
        return toResponse(record);
    }
}
