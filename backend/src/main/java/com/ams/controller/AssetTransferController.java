package com.ams.controller;

import com.ams.dto.TransferApplyRequest;
import com.ams.dto.TransferRecordResponse;
import com.ams.entity.AssetTransferRecord;
import com.ams.service.AssetTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class AssetTransferController {

    private final AssetTransferService assetTransferService;

    @PostMapping("/apply")
    public ResponseEntity<AssetTransferRecord> applyTransfer(
            @RequestParam Long requesterId,
            @RequestBody TransferApplyRequest request) {
        AssetTransferRecord record = assetTransferService.applyTransfer(requesterId, request);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TransferRecordResponse>> getPendingTransfers() {
        return ResponseEntity.ok(assetTransferService.getPendingTransfers());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<TransferRecordResponse>> getMyTransferRequests(@RequestParam Long requesterId) {
        return ResponseEntity.ok(assetTransferService.getMyTransferRequests(requesterId));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<TransferRecordResponse>> getTransfersByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetTransferService.getTransfersByAsset(assetId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferRecordResponse> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(assetTransferService.getTransferById(id));
    }
}
