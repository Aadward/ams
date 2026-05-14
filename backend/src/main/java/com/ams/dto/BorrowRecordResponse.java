package com.ams.dto;

import com.ams.enums.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordResponse {

    private Long id;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private Long borrowerId;
    private String borrowerName;
    private Long departmentId;
    private String departmentName;
    private Long approvalId;
    private LocalDate borrowDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private BorrowStatus status;
    private String reason;
    private String managerComment;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
