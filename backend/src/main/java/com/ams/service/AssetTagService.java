package com.ams.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ams.dto.AssetTagResponse;
import com.ams.entity.Asset;
import com.ams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetTagService {

    private final AssetRepository assetRepository;

    private static final int QR_CODE_SIZE = 300;
    private static final int BARCODE_WIDTH = 400;
    private static final int BARCODE_HEIGHT = 80;

    @Transactional(readOnly = true)
    public AssetTagResponse generateQRCode(Long assetId) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        String qrContent = asset.getId() + ":" + asset.getAssetCode();
        String qrCodeBase64 = generateQRCodeImage(qrContent);
        String barcodeBase64 = generateBarcodeImage(asset.getAssetCode());

        return AssetTagResponse.builder()
                .assetId(asset.getId())
                .assetCode(asset.getAssetCode())
                .assetName(asset.getName())
                .category(asset.getCategory() != null ? asset.getCategory().name() : null)
                .status(asset.getStatus() != null ? asset.getStatus().name() : null)
                .location(asset.getLocation())
                .purchaseDate(asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : null)
                .warrantyEnd(asset.getWarrantyEnd() != null ? asset.getWarrantyEnd().toString() : null)
                .qrCodeBase64(qrCodeBase64)
                .barcodeBase64(barcodeBase64)
                .build();
    }

    private String generateQRCodeImage(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败");
        }
    }

    private String generateBarcodeImage(String content) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = barcodeWriter.encode(content, BarcodeFormat.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.warn("生成条形码失败 (Code128): {}, 跳过条形码", e.getMessage());
            return null;
        }
    }
}
