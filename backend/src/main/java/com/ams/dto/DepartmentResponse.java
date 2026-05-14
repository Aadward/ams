package com.ams.dto;

import com.ams.entity.Department;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {

    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private String description;
    private List<DepartmentResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse fromEntity(Department dept) {
        return fromEntity(dept, false);
    }

    public static DepartmentResponse fromEntity(Department dept, boolean deep) {
        DepartmentResponseBuilder builder = DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt());

        if (dept.getParent() != null) {
            builder.parentId(dept.getParent().getId())
                   .parentName(dept.getParent().getName());
        }

        if (deep && dept.getChildren() != null && !dept.getChildren().isEmpty()) {
            builder.children(dept.getChildren().stream()
                    .map(child -> fromEntity(child, true))
                    .collect(Collectors.toList()));
        } else {
            builder.children(new ArrayList<>());
        }

        return builder.build();
    }
}
