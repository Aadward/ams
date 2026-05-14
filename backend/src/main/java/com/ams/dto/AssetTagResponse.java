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
    private String qrCodeBase64;
}
