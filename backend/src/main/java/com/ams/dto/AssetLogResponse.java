package com.ams.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetLogResponse {
    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String action;
    private String operator;
    private String detail;
    private LocalDateTime createdAt;
}