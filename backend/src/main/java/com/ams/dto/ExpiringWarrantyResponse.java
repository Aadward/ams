package com.ams.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiringWarrantyResponse {
    private Long id;
    private String assetCode;
    private String name;
    private String category;
    private LocalDate warrantyEnd;
}
