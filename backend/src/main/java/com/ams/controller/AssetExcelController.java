package com.ams.controller;

import com.ams.service.ExcelExportService;
import com.ams.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetExcelController {

    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAssets() {
        try {
            byte[] excelBytes = excelExportService.exportAssets();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assets.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAssets(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请选择文件"));
            }
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
                return ResponseEntity.badRequest().body(Map.of("error", "只支持 .xlsx 或 .xls 文件"));
            }
            Map<String, Object> result = excelImportService.importAssets(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
