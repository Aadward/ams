package com.ams.service;

import com.ams.dto.EmployeeRequest;
import com.ams.dto.EmployeeResponse;
import com.ams.entity.AssetLog;
import com.ams.entity.Department;
import com.ams.entity.Employee;
import com.ams.enums.AssetAction;
import com.ams.enums.UserRole;
import com.ams.repository.AssetLogRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetLogRepository assetLogRepository;
    private final ElasticsearchLogService elasticsearchLogService;

    private static final String OPERATOR = "system";

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(EmployeeResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .deptName(request.getDeptName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        if (request.getDeptId() != null) {
            Department dept = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            employee.setDepartment(dept);
        }

        // Default to USER role if not specified
        employee.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);

        employee = employeeRepository.save(employee);
        saveLog(null, AssetAction.CREATE, "创建员工: " + employee.getName());
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getDeptId() != null) {
            Department dept = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            employee.setDepartment(dept);
        }
        if (request.getDeptName() != null) {
            employee.setDeptName(request.getDeptName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }

        employee = employeeRepository.save(employee);
        saveLog(null, AssetAction.UPDATE, "更新员工: " + employee.getName());
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        employeeRepository.delete(employee);
        saveLog(null, AssetAction.UNASSIGN, "删除员工: " + employee.getName());
    }

    @Transactional
    public EmployeeResponse updateRole(Long id, UserRole newRole) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        employee.setRole(newRole);
        employee = employeeRepository.save(employee);
        saveLog(null, AssetAction.UPDATE, "更新员工角色: " + employee.getName());
        return EmployeeResponse.fromEntity(employee);
    }

    private void saveLog(com.ams.entity.Asset asset, AssetAction action, String detail) {
        AssetLog log = AssetLog.builder()
                .asset(asset)
                .action(action)
                .operator(OPERATOR)
                .detail(detail)
                .build();
        assetLogRepository.save(log);
        elasticsearchLogService.saveLog(log);
    }
}
