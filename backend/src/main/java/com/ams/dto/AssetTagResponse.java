package com.ams.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetTagResponse {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String category;
    private String status;
    private String location;
    private String purchaseDate;
    private String warrantyEnd;
    private String qrCodeBase64;
    private String barcodeBase64;
}
