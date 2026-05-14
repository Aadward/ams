package com.ams.service;

import com.ams.dto.AssetCreateRequest;
import com.ams.entity.Asset;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final AssetRepository assetRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public Map<String, Object> importAssets(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Validate header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件为空或格式不正确");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    skipCount++;
                    continue;
                }

                try {
                    String assetCode = getCellStringValue(row.getCell(0), formatter);
                    String name = getCellStringValue(row.getCell(1), formatter);
                    String category = getCellStringValue(row.getCell(2), formatter);
                    String status = getCellStringValue(row.getCell(3), formatter);
                    String spec = getCellStringValue(row.getCell(4), formatter);
                    String purchaseDateStr = getCellStringValue(row.getCell(5), formatter);
                    String purchasePriceStr = getCellStringValue(row.getCell(6), formatter);
                    String warrantyEndStr = getCellStringValue(row.getCell(7), formatter);
                    String supplier = getCellStringValue(row.getCell(8), formatter);
                    String location = getCellStringValue(row.getCell(9), formatter);

                    if (assetCode == null || assetCode.isBlank()) {
                        errors.add("第" + (i + 1) + "行: 资产编码不能为空");
                        continue;
                    }
                    if (name == null || name.isBlank()) {
                        errors.add("第" + (i + 1) + "行: 资产名称不能为空");
                        continue;
                    }

                    AssetCreateRequest request = AssetCreateRequest.builder()
                            .assetCode(assetCode)
                            .name(name)
                            .category(category != null && !category.isBlank() ? category : "HARDWARE")
                            .spec(spec)
                            .purchaseDate(purchaseDateStr != null && !purchaseDateStr.isBlank() ? LocalDate.parse(purchaseDateStr, DATE_FORMAT) : null)
                            .purchasePrice(purchasePriceStr != null && !purchasePriceStr.isBlank() ? new BigDecimal(purchasePriceStr) : null)
                            .warrantyEnd(warrantyEndStr != null && !warrantyEndStr.isBlank() ? LocalDate.parse(warrantyEndStr, DATE_FORMAT) : null)
                            .supplier(supplier)
                            .location(location)
                            .build();

                    Asset asset = Asset.builder()
                            .assetCode(request.getAssetCode())
                            .name(request.getName())
                            .category(AssetCategory.valueOf(request.getCategory()))
                            .status(AssetStatus.IN_STOCK)
                            .spec(request.getSpec())
                            .purchaseDate(request.getPurchaseDate())
                            .purchasePrice(request.getPurchasePrice())
                            .warrantyEnd(request.getWarrantyEnd())
                            .supplier(request.getSupplier())
                            .location(request.getLocation())
                            .deleted(false)
                            .build();

                    assetRepository.save(asset);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("errors", errors);
        return result;
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }
}
