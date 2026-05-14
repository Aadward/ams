package com.ams.service;

import com.ams.entity.Asset;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final AssetRepository assetRepository;

    public byte[] exportAssets() throws IOException {
        List<Asset> assets = assetRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("资产列表");

            // Header row
            Row header = sheet.createRow(0);
            String[] headers = {"资产编码", "名称", "分类", "状态", "规格", "采购日期", "采购价格", "保修到期", "供应商", "位置", "领用人", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Data rows
            int rowNum = 1;
            for (Asset asset : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(asset.getAssetCode() != null ? asset.getAssetCode() : "");
                row.createCell(1).setCellValue(asset.getName() != null ? asset.getName() : "");
                row.createCell(2).setCellValue(asset.getCategory() != null ? asset.getCategory().name() : "");
                row.createCell(3).setCellValue(asset.getStatus() != null ? asset.getStatus().name() : "");
                row.createCell(4).setCellValue(asset.getSpec() != null ? asset.getSpec() : "");
                row.createCell(5).setCellValue(asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "");
                row.createCell(6).setCellValue(asset.getPurchasePrice() != null ? asset.getPurchasePrice().doubleValue() : 0.0);
                row.createCell(7).setCellValue(asset.getWarrantyEnd() != null ? asset.getWarrantyEnd().toString() : "");
                row.createCell(8).setCellValue(asset.getSupplier() != null ? asset.getSupplier() : "");
                row.createCell(9).setCellValue(asset.getLocation() != null ? asset.getLocation() : "");
                row.createCell(10).setCellValue(asset.getAssignee() != null ? asset.getAssignee().getName() : "");
                row.createCell(11).setCellValue(asset.getCreatedAt() != null ? asset.getCreatedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
