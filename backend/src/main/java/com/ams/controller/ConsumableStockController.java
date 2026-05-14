package com.ams.controller;

import com.ams.dto.*;
import com.ams.enums.ConsumableRecordType;
import com.ams.service.ConsumableStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
@CrossOrigin
public class ConsumableStockController {

    private final ConsumableStockService stockService;

    @PostMapping("/stock-in")
    public ResponseEntity<ConsumableRecordResponse> stockIn(
            @Valid @RequestBody StockInRequest req,
            @RequestHeader(value = "X-Employee-Id", defaultValue = "1") Long operatorId) {
        return ResponseEntity.ok(stockService.stockIn(req, operatorId));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<ConsumableRecordResponse> stockOut(
            @Valid @RequestBody StockOutRequest req,
            @RequestHeader(value = "X-Employee-Id", defaultValue = "1") Long requesterId) {
        return ResponseEntity.ok(stockService.stockOut(req, requesterId));
    }

    @GetMapping("/records")
    public ResponseEntity<List<ConsumableRecordResponse>> listRecords(
            @RequestParam(required = false) Long consumableId,
            @RequestParam(required = false) ConsumableRecordType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(stockService.listRecords(consumableId, type, start, end));
    }

    @GetMapping("/report/consumption")
    public ResponseEntity<ConsumptionReportResponse> getConsumptionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(stockService.getConsumptionReport(start, end));
    }
}
