package com.ams.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRequest {
    @NotEmpty(message = "资产ID列表不能为空")
    private List<Long> assetIds;
}
