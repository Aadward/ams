package com.ams.dto;

import com.ams.entity.Employee;
import com.ams.enums.UserRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private Long deptId;
    private String deptName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse fromEntity(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .deptId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .deptName(employee.getDeptName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .role(employee.getRole())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
