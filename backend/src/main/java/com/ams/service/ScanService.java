package com.ams.service;

import com.ams.dto.ScanRequest;
import com.ams.dto.ScanResponse;
import com.ams.entity.Asset;
import com.ams.entity.BorrowRecord;
import com.ams.entity.Employee;
import com.ams.enums.AssetStatus;
import com.ams.enums.ApprovalType;
import com.ams.enums.BorrowStatus;
import com.ams.enums.UserRole;
import com.ams.repository.AssetRepository;
import com.ams.repository.BorrowRecordRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

    private final AssetRepository assetRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ApprovalService approvalService;
    private final BorrowService borrowService;

    @Transactional(readOnly = true)
    public ScanResponse scanAsset(String assetCode, Long employeeId) {
        log.info("Scanning asset: code={}, employeeId={}", assetCode, employeeId);

        Asset asset = assetRepository.findByAssetCodeAndDeletedFalse(assetCode)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetCode));

        List<String> actions = new ArrayList<>();
        actions.add("VIEW_DETAIL");

        String assigneeName = null;
        Long borrowRecordId = null;

        if (asset.getStatus() == AssetStatus.IN_STOCK) {
            actions.add("ASSIGN");
        } else if (asset.getStatus() == AssetStatus.IN_USE) {
            if (asset.getAssignee() != null && employeeId != null && asset.getAssignee().getId().equals(employeeId)) {
                actions.add("RETURN");
                assigneeName = asset.getAssignee().getName();
            }
        }

        // Check for active BORROWED record for this user + asset
        if (employeeId != null) {
            List<BorrowRecord> records = borrowRecordRepository.findByAssetIdAndBorrowerId(asset.getId(), employeeId);
            for (BorrowRecord r : records) {
                if (r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE) {
                    actions.add("BORROW_RETURN");
                    borrowRecordId = r.getId();
                    break;
                }
            }
        }

        return ScanResponse.builder()
                .assetId(asset.getId())
                .assetCode(asset.getAssetCode())
                .assetName(asset.getName())
                .category(asset.getCategory().name())
                .status(asset.getStatus().name())
                .assigneeName(assigneeName)
                .location(asset.getLocation())
                .availableActions(actions)
                .borrowRecordId(borrowRecordId)
                .build();
    }

    @Transactional
    public void scanAssign(String assetCode, ScanRequest request, Long employeeId) {
        log.info("Scan assign: code={}, employeeId={}", assetCode, employeeId);

        Asset asset = assetRepository.findByAssetCodeAndDeletedFalse(assetCode)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetCode));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        approvalService.createRequest(
                employeeId,
                asset.getId(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : 1L,
                ApprovalType.ASSET_ASSIGNMENT,
                request.getReason()
        );
    }

    @Transactional
    public void scanReturn(String assetCode, ScanRequest request, Long employeeId) {
        log.info("Scan return: code={}, employeeId={}", assetCode, employeeId);

        Asset asset = assetRepository.findByAssetCodeAndDeletedFalse(assetCode)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetCode));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        approvalService.createRequest(
                employeeId,
                asset.getId(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : 1L,
                ApprovalType.ASSET_RETURN,
                request.getReason()
        );
    }

    @Transactional
    public void scanBorrowReturn(String assetCode, Long employeeId) {
        log.info("Scan borrow return: code={}, employeeId={}", assetCode, employeeId);

        Asset asset = assetRepository.findByAssetCodeAndDeletedFalse(assetCode)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + assetCode));

        List<BorrowRecord> records = borrowRecordRepository.findByAssetIdAndBorrowerId(asset.getId(), employeeId);
        BorrowRecord activeRecord = null;
        for (BorrowRecord r : records) {
            if (r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE) {
                activeRecord = r;
                break;
            }
        }

        if (activeRecord == null) {
            throw new RuntimeException("No active borrow record found for this asset");
        }

        borrowService.returnAsset(activeRecord.getId(), "Scan return");
    }
}
