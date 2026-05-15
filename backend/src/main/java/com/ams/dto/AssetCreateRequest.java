package com.ams.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetCreateRequest {

    @NotBlank(message = "资产编码不能为空")
    @Size(max = 50)
    private String assetCode;

    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200)
    private String name;

    @NotNull(message = "分类不能为空")
    private String category;

    @Size(max = 500)
    private String spec;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0", message = "价格不能为负")
    private BigDecimal purchasePrice;

    private Integer depreciationYears;

    private LocalDate warrantyEnd;

    @Size(max = 255)
    private String supplier;

    @Size(max = 255)
    private String location;

    @Size(max = 500)
    private String photoUrl;
}
