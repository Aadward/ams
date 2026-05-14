package com.ams.dto;

import com.ams.enums.ConsumableCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumableRequest {
    @NotBlank(message = "名称不能为空")
    private String name;

    @NotNull(message = "分类不能为空")
    private ConsumableCategory category;

    private String spec;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @Positive
    private Integer threshold = 10;
}
