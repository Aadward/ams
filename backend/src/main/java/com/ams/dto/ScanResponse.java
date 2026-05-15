package com.ams.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResponse {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String category;
    private String status;
    private String assigneeName;
    private String location;
    private List<String> availableActions;
    private Long borrowRecordId;
}
