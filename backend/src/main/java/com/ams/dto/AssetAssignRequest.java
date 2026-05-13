package com.ams.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignRequest {
    private Long assigneeId;
}
