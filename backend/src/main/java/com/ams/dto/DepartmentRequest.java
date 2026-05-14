package com.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequest {

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100)
    private String name;

    private Long parentId;

    @Size(max = 500)
    private String description;
}
