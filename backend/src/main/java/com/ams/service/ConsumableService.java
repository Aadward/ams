package com.ams.service;

import com.ams.dto.*;
import com.ams.entity.Consumable;
import com.ams.entity.ConsumableStock;
import com.ams.enums.ConsumableCategory;
import com.ams.repository.ConsumableRepository;
import com.ams.repository.ConsumableStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumableService {

    private final ConsumableRepository consumableRepository;
    private final ConsumableStockRepository stockRepository;

    public List<ConsumableResponse> list(ConsumableCategory category) {
        List<Consumable> list = (category == null)
            ? consumableRepository.findByDeletedFalse()
            : consumableRepository.findByDeletedFalseAndCategory(category);

        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ConsumableResponse getById(Long id) {
        Consumable c = consumableRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("易耗品不存在"));
        return toResponse(c);
    }

    @Transactional
    public ConsumableResponse create(ConsumableRequest req) {
        Consumable c = Consumable.builder()
            .name(req.getName())
            .category(req.getCategory())
            .spec(req.getSpec())
            .unit(req.getUnit())
            .threshold(req.getThreshold() != null ? req.getThreshold() : 10)
            .deleted(false)
            .build();
        c = consumableRepository.save(c);

        ConsumableStock stock = ConsumableStock.builder()
            .consumableId(c.getId())
            .currentStock(0)
            .build();
        stockRepository.save(stock);

        return toResponse(c);
    }

    @Transactional
    public ConsumableResponse update(Long id, ConsumableRequest req) {
        Consumable c = consumableRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("易耗品不存在"));
        c.setName(req.getName());
        c.setCategory(req.getCategory());
        c.setSpec(req.getSpec());
        c.setUnit(req.getUnit());
        if (req.getThreshold() != null) c.setThreshold(req.getThreshold());
        consumableRepository.save(c);
        return toResponse(c);
    }

    @Transactional
    public void delete(Long id) {
        Consumable c = consumableRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("易耗品不存在"));
        c.setDeleted(true);
        consumableRepository.save(c);
    }

    private ConsumableResponse toResponse(Consumable c) {
        Integer stock = stockRepository.findByConsumableId(c.getId())
            .map(ConsumableStock::getCurrentStock).orElse(0);
        boolean lowStock = stock < c.getThreshold();
        return ConsumableResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .category(c.getCategory())
            .categoryLabel(c.getCategory().getLabel())
            .spec(c.getSpec())
            .unit(c.getUnit())
            .threshold(c.getThreshold())
            .currentStock(stock)
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .lowStock(lowStock)
            .build();
    }
}
