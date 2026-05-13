package com.ams.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String dept;

    @Email(message = "邮箱格式不正确")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;
}
