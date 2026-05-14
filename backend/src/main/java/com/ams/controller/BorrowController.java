package com.ams.controller;

import com.ams.dto.BorrowRecordResponse;
import com.ams.entity.BorrowRecord;
import com.ams.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

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
