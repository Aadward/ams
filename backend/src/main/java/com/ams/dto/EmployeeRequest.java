package com.ams.dto;

import com.ams.enums.UserRole;
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

    private Long deptId;

    @Size(max = 100)
    private String deptName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    private UserRole role;
}
