package com.ams.controller;

import com.ams.dto.BorrowRecordResponse;
import com.ams.dto.BorrowRequestDTO;
import com.ams.entity.BorrowRecord;
import com.ams.enums.ApprovalType;
import com.ams.service.ApprovalService;
import com.ams.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final ApprovalService approvalService;

    @GetMapping
    public ResponseEntity<?> getAllBorrowRecords() {
        try {
            List<BorrowRecordResponse> result = borrowService.getAllBorrowRecords();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBorrowRecords(@RequestParam Long borrowerId) {
        try {
            List<BorrowRecordResponse> result = borrowService.getBorrowRecordsByBorrower(borrowerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveBorrowRecords() {
        try {
            List<BorrowRecordResponse> result = borrowService.getActiveBorrowRecords();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getPendingBorrowRequests() {
        try {
            List<BorrowRequestDTO> result = approvalService.getPendingRequests().stream()
                    .filter(dto -> dto.getType() == ApprovalType.ASSET_BORROW)
                    .map(dto -> BorrowRequestDTO.builder()
                            .id(dto.getId())
                            .requesterId(dto.getRequesterId())
                            .requesterName(dto.getRequesterName())
                            .assetId(dto.getAssetId())
                            .assetName(dto.getAssetName())
                            .assetCode(dto.getAssetCode())
                            .departmentId(dto.getDepartmentId())
                            .departmentName(dto.getDepartmentName())
                            .type(dto.getType())
                            .status(dto.getStatus())
                            .reason(dto.getReason())
                            .managerComment(dto.getManagerComment())
                            .createdAt(dto.getCreatedAt())
                            .resolvedAt(dto.getResolvedAt())
                            .build())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForBorrow(@RequestBody Map<String, Object> request) {
        try {
            Long assetId = Long.valueOf(request.get("assetId").toString());
            Long borrowerId = Long.valueOf(request.get("borrowerId").toString());
            Long departmentId = Long.valueOf(request.get("departmentId").toString());
            String expectedReturnDate = request.get("expectedReturnDate") != null ?
                    request.get("expectedReturnDate").toString() : null;
            String reason = request.get("reason") != null ? request.get("reason").toString() : null;

            String fullReason = "归还日期:" + expectedReturnDate;
            if (reason != null && !reason.isBlank()) {
                fullReason += "\n" + reason;
            }

            var result = approvalService.createRequest(borrowerId, assetId, departmentId,
                    ApprovalType.ASSET_BORROW, fullReason);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getOverdueRecords() {
        try {
            List<BorrowRecordResponse> result = borrowService.getOverdueRecords();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> returnAsset(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            BorrowRecord result = borrowService.returnAsset(id, comment);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
