package com.ams.service;

import com.ams.dto.EmployeeRequest;
import com.ams.dto.EmployeeResponse;
import com.ams.entity.AssetLog;
import com.ams.entity.Employee;
import com.ams.enums.AssetAction;
import com.ams.repository.AssetLogRepository;
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
    private final AssetLogRepository assetLogRepository;
    private final ElasticsearchLogService elasticsearchLogService;

    private static final String OPERATOR = "system";

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        return toResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .dept(request.getDept())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        employee = employeeRepository.save(employee);

        saveLog(null, AssetAction.CREATE, "创建员工: " + employee.getName());

        return toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getDept() != null) {
            employee.setDept(request.getDept());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        employee = employeeRepository.save(employee);

        saveLog(null, AssetAction.UPDATE, "更新员工: " + employee.getName());

        return toResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        employeeRepository.delete(employee);

        saveLog(null, AssetAction.DELETE, "删除员工: " + employee.getName());
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .dept(employee.getDept())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
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