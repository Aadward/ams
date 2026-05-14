package com.ams.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
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

    @Transactional(readOnly = true)
    public AssetTagResponse generateQRCode(Long assetId) {
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在"));

        String qrContent = asset.getId() + ":" + asset.getAssetCode();
        String qrCodeBase64 = generateQRCodeImage(qrContent);

        return AssetTagResponse.builder()
                .assetId(asset.getId())
                .assetCode(asset.getAssetCode())
                .assetName(asset.getName())
                .qrCodeBase64(qrCodeBase64)
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
}
