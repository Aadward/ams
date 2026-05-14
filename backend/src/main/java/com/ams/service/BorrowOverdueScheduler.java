package com.ams.service;

import com.ams.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowOverdueScheduler {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowService borrowService;

    /**
     * Check for overdue borrow records every day at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkOverdueBorrows() {
        log.info("Running borrow overdue check...");
        borrowService.processOverdueRecords();
    }
}
