package com.ams.service;

import com.ams.dto.*;
import com.ams.entity.Consumable;
import com.ams.entity.ConsumableRecord;
import com.ams.entity.ConsumableStock;
import com.ams.enums.ConsumableRecordType;
import com.ams.enums.NotificationType;
import com.ams.enums.UserRole;
import com.ams.repository.ConsumableRecordRepository;
import com.ams.repository.ConsumableRepository;
import com.ams.repository.ConsumableStockRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumableStockService {

    private final ConsumableRepository consumableRepository;
    private final ConsumableStockRepository stockRepository;
    private final ConsumableRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    @Transactional
    public ConsumableRecordResponse stockIn(StockInRequest req, Long operatorId) {
        Consumable c = consumableRepository.findByIdAndDeletedFalse(req.getConsumableId())
            .orElseThrow(() -> new RuntimeException("易耗品不存在"));

        ConsumableStock stock = stockRepository.findByConsumableId(c.getId())
            .orElseGet(() -> ConsumableStock.builder()
                .consumableId(c.getId())
                .currentStock(0)
                .build());

        stock.setCurrentStock(stock.getCurrentStock() + req.getQuantity());
        stockRepository.save(stock);

        ConsumableRecord record = ConsumableRecord.builder()
            .consumableId(c.getId())
            .type(ConsumableRecordType.IN)
            .quantity(req.getQuantity())
            .relatedUserId(operatorId)
            .supplier(req.getSupplier())
            .remark(req.getRemark())
            .build();
        record = recordRepository.save(record);

        return toRecordResponse(record, c.getName());
    }

    @Transactional
    public ConsumableRecordResponse stockOut(StockOutRequest req, Long requesterId) {
        Consumable c = consumableRepository.findByIdAndDeletedFalse(req.getConsumableId())
            .orElseThrow(() -> new RuntimeException("易耗品不存在"));

        ConsumableStock stock = stockRepository.findByConsumableId(c.getId())
            .orElseThrow(() -> new RuntimeException("库存记录不存在"));

        if (stock.getCurrentStock() < req.getQuantity()) {
            throw new RuntimeException("库存不足，当前库存：" + stock.getCurrentStock());
        }

        stock.setCurrentStock(stock.getCurrentStock() - req.getQuantity());
        stockRepository.save(stock);

        if (stock.getCurrentStock() < c.getThreshold()) {
            sendLowStockNotification(c, stock.getCurrentStock());
        }

        ConsumableRecord record = ConsumableRecord.builder()
            .consumableId(c.getId())
            .type(ConsumableRecordType.OUT)
            .quantity(req.getQuantity())
            .relatedUserId(requesterId)
            .remark(req.getRemark())
            .build();
        record = recordRepository.save(record);

        return toRecordResponse(record, c.getName());
    }

    private void sendLowStockNotification(Consumable c, Integer currentStock) {
        String title = "易耗品库存预警";
        String message = "「" + c.getName() + "」库存不足，当前库存：" + currentStock + "，阈值：" + c.getThreshold();
        employeeRepository.findByRole(UserRole.ADMIN).forEach(admin ->
            notificationService.createNotification(admin.getId(), title, message, NotificationType.SYSTEM)
        );
    }

    public List<ConsumableRecordResponse> listRecords(Long consumableId, ConsumableRecordType type,
            LocalDateTime start, LocalDateTime end) {
        List<ConsumableRecord> records;
        if (consumableId != null) {
            records = recordRepository.findByConsumableIdOrderByCreatedAtDesc(consumableId);
        } else if (type != null && start != null && end != null) {
            records = recordRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(type, start, end);
        } else if (start != null && end != null) {
            records = recordRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        } else {
            records = recordRepository.findAll();
        }
        return records.stream()
            .map(r -> {
                String name = consumableRepository.findById(r.getConsumableId())
                    .map(Consumable::getName).orElse("未知");
                return toRecordResponse(r, name);
            })
            .collect(Collectors.toList());
    }

    public ConsumptionReportResponse getConsumptionReport(LocalDateTime start, LocalDateTime end) {
        List<ConsumableRecord> records = recordRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        List<ConsumableRecord> outRecords = records.stream()
            .filter(r -> r.getType() == ConsumableRecordType.OUT)
            .collect(Collectors.toList());

        var itemMap = outRecords.stream()
            .collect(Collectors.groupingBy(ConsumableRecord::getConsumableId));
        List<ConsumptionReportResponse.ConsumptionItem> items = itemMap.entrySet().stream()
            .map(e -> {
                String name = consumableRepository.findById(e.getKey())
                    .map(Consumable::getName).orElse("未知");
                String cat = consumableRepository.findById(e.getKey())
                    .map(c -> c.getCategory().name()).orElse("UNKNOWN");
                int total = e.getValue().stream().mapToInt(ConsumableRecord::getQuantity).sum();
                return ConsumptionReportResponse.ConsumptionItem.builder()
                    .consumableId(e.getKey())
                    .consumableName(name)
                    .category(cat)
                    .totalQuantity(total)
                    .build();
            })
            .collect(Collectors.toList());

        int total = outRecords.stream().mapToInt(ConsumableRecord::getQuantity).sum();

        var monthly = outRecords.stream()
            .collect(Collectors.groupingBy(
                r -> r.getCreatedAt().getYear() + "-" + String.format("%02d", r.getCreatedAt().getMonthValue())
            ));
        var monthlyTotals = monthly.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue().stream().mapToInt(ConsumableRecord::getQuantity).sum()
            ));

        return ConsumptionReportResponse.builder()
            .items(items)
            .totalQuantity(total)
            .monthlyTotals(monthlyTotals)
            .build();
    }

    private ConsumableRecordResponse toRecordResponse(ConsumableRecord r, String consumableName) {
        String userName = r.getRelatedUserId() != null
            ? employeeRepository.findById(r.getRelatedUserId()).map(e -> e.getName()).orElse(null)
            : null;
        return ConsumableRecordResponse.builder()
            .id(r.getId())
            .consumableId(r.getConsumableId())
            .consumableName(consumableName)
            .type(r.getType())
            .typeLabel(r.getType().getLabel())
            .quantity(r.getQuantity())
            .relatedUserId(r.getRelatedUserId())
            .relatedUserName(userName)
            .supplier(r.getSupplier())
            .remark(r.getRemark())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
