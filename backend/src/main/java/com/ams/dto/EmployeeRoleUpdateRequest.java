package com.ams.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRoleUpdateRequest {
    private String newRole;
}
