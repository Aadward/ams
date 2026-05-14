package com.ams.service;

import com.ams.dto.DepartmentRequest;
import com.ams.dto.DepartmentResponse;
import com.ams.entity.Department;
import com.ams.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listAsTree() {
        List<Department> roots = departmentRepository.findByParentIsNull();
        return roots.stream()
                .map(dept -> DepartmentResponse.fromEntity(dept, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
        return DepartmentResponse.fromEntity(dept);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department dept = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("上级部门不存在"));
            dept.setParent(parent);
        }

        dept = departmentRepository.save(dept);
        return DepartmentResponse.fromEntity(dept);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        if (request.getName() != null) {
            dept.setName(request.getName());
        }
        if (request.getDescription() != null) {
            dept.setDescription(request.getDescription());
        }
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设为上级部门");
            }
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("上级部门不存在"));
            dept.setParent(parent);
        }

        dept = departmentRepository.save(dept);
        return DepartmentResponse.fromEntity(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
            throw new RuntimeException("请先删除子部门");
        }

        departmentRepository.delete(dept);
    }
}
