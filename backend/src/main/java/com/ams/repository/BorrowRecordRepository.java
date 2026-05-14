package com.ams.repository;

import com.ams.entity.BorrowRecord;
import com.ams.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByStatus(BorrowStatus status);

    List<BorrowRecord> findByBorrowerId(Long borrowerId);

    List<BorrowRecord> findByAssetId(Long assetId);

    List<BorrowRecord> findByStatusAndExpectedReturnDateBefore(BorrowStatus status, LocalDate date);

    List<BorrowRecord> findByBorrowerIdAndStatus(Long borrowerId, BorrowStatus status);
}
