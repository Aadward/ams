package com.ams.controller;

import com.ams.entity.Asset;
import com.ams.repository.AssetRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-tags")
@RequiredArgsConstructor
public class AssetTagPrintController {

    private final AssetRepository assetRepository;

    @GetMapping(value = "/{id}/print", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> printAssetTag(@PathVariable Long id) {
        try {
            Asset asset = assetRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new RuntimeException("Asset not found"));

            String qrCodeBase64 = generateQRCodeBase64(String.valueOf(asset.getId()), 120, 120);
            String barcodeBase64 = generateBarcodeBase64(asset.getAssetCode(), 200, 50);

            String html = buildPrintHtml(asset, qrCodeBase64, barcodeBase64);
            return ResponseEntity.ok(html);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String generateQRCodeBase64(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String generateBarcodeBase64(String data, int width, int height) throws WriterException, IOException {
        Code128Writer writer = new Code128Writer();
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String buildPrintHtml(Asset asset, String qrBase64, String barcodeBase64) {
        String statusDisplay = asset.getStatus().name().replace("_", " ");
        String categoryDisplay = asset.getCategory() != null ? asset.getCategory().name() : "N/A";
        String assigneeDisplay = asset.getAssignee() != null ? escapeHtml(asset.getAssignee().getName()) : "Unassigned";
        String locationDisplay = asset.getLocation() != null ? escapeHtml(asset.getLocation()) : "N/A";

        return "<!DOCTYPE html>\n" +
"<html lang=\"zh-CN\">\n" +
"<head>\n" +
"  <meta charset=\"UTF-8\">\n" +
"  <title>Asset Tag - " + asset.getAssetCode() + "</title>\n" +
"  <style>\n" +
"    @page { size: 80mm 50mm; margin: 0; }\n" +
"    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
"    body { font-family: 'Microsoft YaHei', Arial, sans-serif; width: 80mm; padding: 3mm; }\n" +
"    .tag-card { border: 1.5px solid #333; border-radius: 4px; padding: 3mm; width: 73mm; }\n" +
"    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2mm; }\n" +
"    .logo { font-size: 8pt; font-weight: bold; color: #555; }\n" +
"    .asset-name { font-size: 10pt; font-weight: bold; color: #222; margin-bottom: 1mm; word-break: break-all; }\n" +
"    .asset-code { font-size: 7pt; color: #666; font-family: monospace; margin-bottom: 2mm; }\n" +
"    .codes { display: flex; align-items: center; gap: 3mm; }\n" +
"    .qr { flex-shrink: 0; }\n" +
"    .qr img { width: 18mm; height: 18mm; }\n" +
"    .barcode-section { flex: 1; text-align: center; }\n" +
"    .barcode-section img { width: 45mm; height: 10mm; }\n" +
"    .barcode-section .code-text { font-size: 6pt; font-family: monospace; color: #333; margin-top: 0.5mm; }\n" +
"    .footer { display: flex; justify-content: space-between; font-size: 6pt; color: #888; margin-top: 2mm; }\n" +
"    .status-badge { display: inline-block; font-size: 7pt; padding: 0.5mm 1.5mm; border-radius: 2px; background: #e8f0fe; color: #1a73e8; margin-bottom: 1mm; }\n" +
"  </style>\n" +
"</head>\n" +
"<body>\n" +
"  <div class=\"tag-card\">\n" +
"    <div class=\"header\">\n" +
"      <div>\n" +
"        <div class=\"logo\">AMS Asset Management</div>\n" +
"        <div class=\"asset-name\">" + escapeHtml(asset.getName()) + "</div>\n" +
"        <div class=\"asset-code\">Code: " + escapeHtml(asset.getAssetCode()) + "</div>\n" +
"        <div class=\"status-badge\">" + statusDisplay + "</div>\n" +
"      </div>\n" +
"      <div class=\"qr\">\n" +
"        <img src=\"data:image/png;base64," + qrBase64 + "\" alt=\"QR Code\" />\n" +
"      </div>\n" +
"    </div>\n" +
"    <div class=\"codes\">\n" +
"      <div class=\"barcode-section\">\n" +
"        <img src=\"data:image/png;base64," + barcodeBase64 + "\" alt=\"Barcode\" />\n" +
"        <div class=\"code-text\">" + escapeHtml(asset.getAssetCode()) + "</div>\n" +
"      </div>\n" +
"    </div>\n" +
"    <div class=\"footer\">\n" +
"      <span>Category: " + categoryDisplay + "</span>\n" +
"      <span>User: " + assigneeDisplay + "</span>\n" +
"      <span>Location: " + locationDisplay + "</span>\n" +
"    </div>\n" +
"  </div>\n" +
"</body>\n" +
"</html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;");
    }
}
