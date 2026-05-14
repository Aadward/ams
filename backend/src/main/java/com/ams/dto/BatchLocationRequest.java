package com.ams.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchLocationRequest {
    @NotEmpty(message = "资产ID列表不能为空")
    private List<Long> assetIds;

    @NotNull(message = "位置不能为空")
    private String location;
}
