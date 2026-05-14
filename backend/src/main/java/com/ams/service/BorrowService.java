package com.ams.service;

import com.ams.dto.BorrowRecordResponse;
import com.ams.entity.Asset;
import com.ams.entity.BorrowRecord;
import com.ams.enums.AssetStatus;
import com.ams.enums.BorrowStatus;
import com.ams.enums.NotificationType;
import com.ams.repository.AssetRepository;
import com.ams.repository.BorrowRecordRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    private BorrowRecordResponse toResponse(BorrowRecord r) {
        String assetName = assetRepository.findById(r.getAssetId())
                .map(a -> a.getName()).orElse("未知");
        String assetCode = assetRepository.findById(r.getAssetId())
                .map(a -> a.getAssetCode()).orElse("未知");
        String borrowerName = employeeRepository.findById(r.getBorrowerId())
                .map(e -> e.getName()).orElse("未知");
        String deptName = departmentRepository.findById(r.getDepartmentId())
                .map(d -> d.getName()).orElse("未知");

        return BorrowRecordResponse.builder()
                .id(r.getId())
                .assetId(r.getAssetId())
                .assetName(assetName)
                .assetCode(assetCode)
                .borrowerId(r.getBorrowerId())
                .borrowerName(borrowerName)
                .departmentId(r.getDepartmentId())
                .departmentName(deptName)
                .approvalId(r.getApprovalId())
                .borrowDate(r.getBorrowDate())
                .expectedReturnDate(r.getExpectedReturnDate())
                .actualReturnDate(r.getActualReturnDate())
                .status(r.getStatus())
                .reason(r.getReason())
                .managerComment(r.getManagerComment())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }

    @Transactional
    public BorrowRecord createBorrowRecord(Long assetId, Long borrowerId, Long departmentId,
                                          Long approvalId, LocalDate borrowDate,
                                          LocalDate expectedReturnDate, String reason) {
        log.info("Creating borrow record: assetId={}, borrowerId={}, expectedReturnDate={}",
                assetId, borrowerId, expectedReturnDate);

        BorrowRecord record = BorrowRecord.builder()
                .assetId(assetId)
                .borrowerId(borrowerId)
                .departmentId(departmentId)
                .approvalId(approvalId)
                .borrowDate(borrowDate)
                .expectedReturnDate(expectedReturnDate)
                .status(BorrowStatus.BORROWED)
                .reason(reason)
                .build();

        return borrowRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getAllBorrowRecords() {
        return borrowRecordRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getBorrowRecordsByBorrower(Long borrowerId) {
        return borrowRecordRepository.findByBorrowerId(borrowerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getActiveBorrowRecords() {
        return borrowRecordRepository.findByStatus(BorrowStatus.BORROWED).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getOverdueRecords() {
        return borrowRecordRepository.findByStatusAndExpectedReturnDateBefore(
                BorrowStatus.BORROWED, LocalDate.now()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BorrowRecord returnAsset(Long id, String managerComment) {
        log.info("Returning asset for borrow record id={}", id);

        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Asset already returned");
        }

        record.setStatus(BorrowStatus.RETURNED);
        record.setActualReturnDate(LocalDate.now());
        record.setManagerComment(managerComment);
        record.setResolvedAt(java.time.LocalDateTime.now());

        // Update asset status back to IN_STOCK
        Asset asset = assetRepository.findById(record.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        asset.setStatus(AssetStatus.IN_STOCK);
        assetRepository.save(asset);

        BorrowRecord saved = borrowRecordRepository.save(record);

        // Notify the borrower
        String assetName = assetRepository.findById(record.getAssetId()).map(a -> a.getName()).orElse("");
        notificationService.createNotification(
                saved.getBorrowerId(),
                "借用已归还",
                "您借用的资产「" + assetName + "」已确认归还",
                NotificationType.ASSET_RETURNED
        );

        return saved;
    }

    @Transactional
    public void markAsOverdue(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (record.getStatus() == BorrowStatus.BORROWED) {
            record.setStatus(BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);

            String assetName = assetRepository.findById(record.getAssetId()).map(a -> a.getName()).orElse("");
            long overdueDays = ChronoUnit.DAYS.between(record.getExpectedReturnDate(), LocalDate.now());

            notificationService.createNotification(
                    record.getBorrowerId(),
                    "借用已超期",
                    String.format("您借用的资产「%s」已超期%s天，请尽快归还", assetName, overdueDays),
                    NotificationType.BORROW_OVERDUE
            );
        }
    }

    @Transactional
    public void processOverdueRecords() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByStatusAndExpectedReturnDateBefore(
                BorrowStatus.BORROWED, LocalDate.now());

        for (BorrowRecord record : overdueRecords) {
            markAsOverdue(record.getId());
        }

        log.info("Processed {} overdue borrow records", overdueRecords.size());
    }
}
